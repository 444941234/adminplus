<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  Button,
  Card,
  CardContent,
  Checkbox,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  ScrollArea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui'
import { Edit, KeyRound, Plus, Trash2 } from 'lucide-vue-next'
import { ConfirmDialog, EmptyState, ListSearchBar, StatusBadge } from '@/components/common'
import { assignMenus, getMenuTree, getRoleMenus, updateRoleStatus } from '@/api'
import { getRolePagePermissionState } from '@/lib/page-permissions'
import { isValidRoleCode } from '@/lib/validators'
import type { Menu, Role } from '@/types'
import { useUserStore } from '@/stores/user'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useCRUD } from '@/composables/useCRUD'
import { useStatusToggle } from '@/composables/useStatusToggle'
import { createRole, deleteRole, getRoleById, getRoleList, updateRole } from '@/api'

interface RoleFormState {
  name: string
  code: string
  description: string
  dataScope: string
  status: string
  sortOrder: number
}

interface MenuOption {
  id: string
  label: string
  level: number
  disabled: boolean
}

const userStore = useUserStore()
const searchQuery = ref('')
const menus = ref<Menu[]>([])

// Assign menus dialog state (additional feature, not handled by useCRUD)
const assignDialogOpen = ref(false)
const assignRole = ref<Role | null>(null)
const selectedMenuIds = ref<string[]>([])
const { loading: assignLoading, run: runAssign } = useAsyncAction('操作失败')

// Status toggle
const {
  statusChangeItem: statusChangeRole,
  statusConfirmOpen,
  loading: statusLoading,
  handleStatusClick,
  handleStatusConfirm
} = useStatusToggle<Role>({
  updateStatus: (id, newStatus) => updateRoleStatus(id, newStatus),
  onSuccess: () => fetchRoles()
})

// CRUD composable
const {
  list: roles,
  loading: listLoading,
  form,
  dialogOpen,
  isEdit,
  dialogLoading,
  deleteLoading,
  deleteDialogOpen,
  fetchList: fetchRoles,
  openCreate: handleAdd,
  openEdit: handleEdit,
  handleSubmit,
  openDeleteConfirm: handleDeleteConfirm,
  handleDelete
} = useCRUD<Role, RoleFormState>({
  getList: async () => {
    const res = await getRoleList()
    return { data: res.data.records || [] }
  },
  getById: getRoleById,
  create: async (data: RoleFormState) => {
    return createRole({
      name: data.name.trim(),
      code: data.code.trim(),
      description: data.description.trim() || undefined,
      dataScope: Number(data.dataScope),
      status: Number(data.status),
      sortOrder: data.sortOrder
    })
  },
  update: async (id: string, data: RoleFormState) => {
    return updateRole(id, {
      name: data.name.trim(),
      description: data.description.trim() || undefined,
      dataScope: Number(data.dataScope),
      status: Number(data.status),
      sortOrder: data.sortOrder
    })
  },
  delete: deleteRole,
  defaultForm: {
    name: '',
    code: '',
    description: '',
    dataScope: '1',
    status: '1',
    sortOrder: 0
  },
  mapDataToForm: (role) => ({
    name: role.name,
    code: role.code,
    description: role.description || '',
    dataScope: String(role.dataScope),
    status: String(role.status ?? 1),
    sortOrder: role.sortOrder
  }),
  successMessages: {
    create: '角色创建成功',
    update: '角色更新成功',
    delete: '角色删除成功'
  },
  errorMessages: {
    list: '获取角色列表失败',
    getById: '获取角色详情失败',
    create: '保存角色失败',
    update: '保存角色失败',
    delete: '删除角色失败'
  },
  validate: (formData, isEditMode) => {
    if (!formData.name.trim()) {
      return '请输入角色名称'
    }
    if (!isEditMode && !formData.code.trim()) {
      return '请输入角色编码'
    }
    if (!isEditMode && !isValidRoleCode(formData.code.trim())) {
      return '角色编码需以字母开头，只能包含字母、数字、下划线、冒号或短横线'
    }
    return true
  }
})

// Permission checks
const permissionState = computed(() => getRolePagePermissionState(userStore.hasPermission))
const canAddRole = computed(() => permissionState.value.canAddRole)
const canEditRole = computed(() => permissionState.value.canEditRole)
const canDeleteRole = computed(() => permissionState.value.canDeleteRole)
const canAssignRole = computed(() => permissionState.value.canAssignRole)

// Search/filter
const filteredRoles = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return roles.value

  return roles.value.filter((role) =>
    [role.name, role.code, role.description ?? ''].some((value) =>
      value.toLowerCase().includes(keyword)
    )
  )
})

// Menu options for assign dialog
const menuOptions = computed<MenuOption[]>(() => {
  const options: MenuOption[] = []
  const walk = (menuList: Menu[], level = 0) => {
    menuList.forEach((menu) => {
      options.push({
        id: menu.id,
        label: menu.name,
        level,
        disabled: false
      })
      if (menu.children?.length) {
        walk(menu.children, level + 1)
      }
      if (menu.type === 1 && !menu.children?.length) {
        options[options.length - 1].disabled = false
      }
    })
  }
  walk(menus.value)
  return options
})

// Fetch menus on mount
const fetchMenus = () => runAssign(
  async () => { const res = await getMenuTree(); menus.value = res.data },
  { errorMessage: '获取菜单列表失败' }
)

// Menu selection helpers
const toggleMenuSelection = (menuId: string, checked: boolean) => {
  const next = new Set(selectedMenuIds.value)
  if (checked) {
    next.add(menuId)
  } else {
    next.delete(menuId)
  }
  selectedMenuIds.value = Array.from(next)
}

const isAllMenusSelected = computed(() =>
  menuOptions.value.length > 0 && menuOptions.value.every((item) => selectedMenuIds.value.includes(item.id))
)

const isSomeMenusSelected = computed(() =>
  menuOptions.value.some((item) => selectedMenuIds.value.includes(item.id)) && !isAllMenusSelected.value
)

const toggleAllMenus = (checked: boolean | string) => {
  selectedMenuIds.value = checked ? menuOptions.value.map((item) => item.id) : []
}

// Assign menus handlers
const handleOpenAssign = (role: Role) => {
  assignRole.value = role
  assignDialogOpen.value = true
  runAssign(async () => {
    const [menuTreeRes, selectedRes] = await Promise.all([getMenuTree(), getRoleMenus(role.id)])
    menus.value = menuTreeRes.data
    selectedMenuIds.value = selectedRes.data
  }, {
    errorMessage: '获取角色菜单权限失败',
    onError: () => { assignDialogOpen.value = false }
  })
}

const handleAssignSubmit = () => {
  if (!assignRole.value) return
  runAssign(async () => {
    await assignMenus(assignRole.value!.id, selectedMenuIds.value)
  }, {
    successMessage: '菜单权限分配成功',
    errorMessage: '分配菜单权限失败',
    onSuccess: () => { assignDialogOpen.value = false }
  })
}

onMounted(async () => {
  await Promise.all([fetchRoles(), fetchMenus()])
})
</script>

<template>
  <div class="space-y-4">
    <ListSearchBar
      v-model="searchQuery"
      placeholder="搜索角色名称、编码或描述"
    >
      <template #actions>
        <Button
          v-if="canAddRole"
          @click="handleAdd"
        >
          <Plus class="mr-2 h-4 w-4" />
          新增角色
        </Button>
      </template>
    </ListSearchBar>

    <Card>
      <CardContent class="p-0">
        <table class="w-full">
          <thead class="border-b bg-muted/50">
            <tr>
              <th class="p-4 text-left font-medium">
                角色名称
              </th>
              <th class="p-4 text-left font-medium">
                角色编码
              </th>
              <th class="p-4 text-left font-medium">
                描述
              </th>
              <th class="p-4 text-left font-medium">
                数据范围
              </th>
              <th class="p-4 text-left font-medium">
                状态
              </th>
              <th class="p-4 text-left font-medium">
                排序
              </th>
              <th class="p-4 text-left font-medium">
                操作
              </th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-if="listLoading">
              <td
                colspan="7"
                class="p-8 text-center text-muted-foreground"
              >
                加载中...
              </td>
            </tr>
            <tr v-else-if="filteredRoles.length === 0">
              <td
                colspan="7"
                class="p-0"
              >
                <EmptyState
                  type="roles"
                  :show-action="canAddRole"
                  action-text="添加角色"
                  @action="handleAdd"
                >
                  <template #action-icon>
                    <Plus class="mr-2 h-4 w-4" />
                  </template>
                </EmptyState>
              </td>
            </tr>
            <tr
              v-for="role in filteredRoles"
              :key="role.id"
              class="hover:bg-muted/30"
            >
              <td class="p-4 font-medium">
                {{ role.name }}
              </td>
              <td class="p-4">
                <code class="rounded bg-muted px-2 py-0.5 text-sm">{{ role.code }}</code>
              </td>
              <td class="p-4 text-muted-foreground">
                {{ role.description || '-' }}
              </td>
              <td class="p-4 text-muted-foreground">
                {{ role.dataScope === 1 ? '全部数据' : `范围 ${role.dataScope}` }}
              </td>
              <td class="p-4">
                <StatusBadge
                  :status="role.status"
                  :clickable="canEditRole"
                  @toggle="handleStatusClick(role)"
                />
              </td>
              <td class="p-4 text-muted-foreground">
                {{ role.sortOrder }}
              </td>
              <td class="p-4">
                <div class="flex gap-2">
                  <Button
                    v-if="canAssignRole"
                    size="sm"
                    variant="ghost"
                    @click="handleOpenAssign(role)"
                  >
                    <KeyRound class="h-4 w-4" />
                  </Button>
                  <Button
                    v-if="canEditRole"
                    size="sm"
                    variant="ghost"
                    @click="handleEdit(role.id)"
                  >
                    <Edit class="h-4 w-4" />
                  </Button>
                  <Button
                    v-if="canDeleteRole"
                    size="sm"
                    variant="ghost"
                    class="text-destructive"
                    @click="handleDeleteConfirm(role.id)"
                  >
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </CardContent>
    </Card>

    <Dialog
      v-if="canAddRole || canEditRole"
      v-model:open="dialogOpen"
    >
      <DialogContent class="sm:max-w-[520px]">
        <DialogHeader>
          <DialogTitle>{{ isEdit ? '编辑角色' : '新增角色' }}</DialogTitle>
          <DialogDescription>配置角色名称、编码和描述信息</DialogDescription>
        </DialogHeader>
        <div
          v-if="dialogLoading"
          class="py-8 text-center text-muted-foreground"
        >
          加载中...
        </div>
        <div
          v-else
          class="space-y-4 py-2"
        >
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label for="role-name">角色名称</Label>
              <Input
                id="role-name"
                v-model="form.name"
                placeholder="请输入角色名称"
              />
            </div>
            <div class="space-y-2">
              <Label for="role-code">角色编码</Label>
              <Input
                id="role-code"
                v-model="form.code"
                :disabled="isEdit"
                placeholder="例如：ROLE_MANAGER"
              />
            </div>
          </div>
          <div class="space-y-2">
            <Label for="role-description">描述</Label>
            <Input
              id="role-description"
              v-model="form.description"
              placeholder="请输入角色描述"
            />
          </div>
          <div class="grid grid-cols-3 gap-4">
            <div class="space-y-2">
              <Label for="role-dataScope">数据范围</Label>
              <Select v-model="form.dataScope">
                <SelectTrigger id="role-dataScope">
                  <SelectValue placeholder="请选择数据范围" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">
                    全部数据
                  </SelectItem>
                  <SelectItem value="2">
                    本部门及以下
                  </SelectItem>
                  <SelectItem value="3">
                    本部门
                  </SelectItem>
                  <SelectItem value="4">
                    仅本人
                  </SelectItem>
                  <SelectItem value="5">
                    自定义
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="space-y-2">
              <Label for="role-status">状态</Label>
              <Select v-model="form.status">
                <SelectTrigger id="role-status">
                  <SelectValue placeholder="请选择状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">
                    正常
                  </SelectItem>
                  <SelectItem value="0">
                    禁用
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="space-y-2">
              <Label for="role-sortOrder">排序</Label>
              <Input
                id="role-sortOrder"
                :model-value="String(form.sortOrder)"
                type="number"
                placeholder="排序值"
                @update:model-value="form.sortOrder = Number($event) || 0"
              />
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            @click="dialogOpen = false"
          >
            取消
          </Button>
          <Button
            :disabled="dialogLoading"
            @click="handleSubmit"
          >
            {{ isEdit ? '保存' : '创建' }}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <Dialog
      v-if="canAssignRole"
      v-model:open="assignDialogOpen"
    >
      <DialogContent class="sm:max-w-[680px]">
        <DialogHeader>
          <DialogTitle>分配菜单权限{{ assignRole ? ` - ${assignRole.name}` : '' }}</DialogTitle>
          <DialogDescription>勾选菜单项为角色分配访问权限</DialogDescription>
        </DialogHeader>
        <div
          v-if="assignLoading"
          class="py-8 text-center text-muted-foreground"
        >
          加载中...
        </div>
        <ScrollArea
          v-else
          class="max-h-[420px] rounded-md border"
        >
          <div class="space-y-1 p-4">
            <label class="flex cursor-pointer items-center gap-3 rounded border-b px-3 py-2 mb-2 font-medium transition-colors hover:bg-muted/50">
              <Checkbox
                :model-value="isAllMenusSelected"
                :indeterminate="isSomeMenusSelected"
                @update:model-value="toggleAllMenus"
              />
              <span class="text-sm">全选</span>
            </label>
            <label
              v-for="item in menuOptions"
              :key="item.id"
              class="flex cursor-pointer items-center gap-3 rounded px-3 py-2 transition-colors hover:bg-muted/50"
              :style="{ paddingLeft: `${item.level * 20 + 12}px` }"
            >
              <Checkbox
                :model-value="selectedMenuIds.includes(item.id)"
                @update:model-value="toggleMenuSelection(item.id, Boolean($event))"
              />
              <span class="text-sm">{{ item.label }}</span>
            </label>
          </div>
        </ScrollArea>
        <DialogFooter>
          <Button
            variant="outline"
            @click="assignDialogOpen = false"
          >
            取消
          </Button>
          <Button
            :disabled="assignLoading"
            @click="handleAssignSubmit"
          >
            保存授权
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <ConfirmDialog
      v-if="canDeleteRole"
      v-model:open="deleteDialogOpen"
      title="确认删除角色"
      description="删除后不可恢复，如果角色已绑定用户，后端可能会拒绝删除。"
      confirm-text="确认删除"
      :loading="deleteLoading"
      @confirm="handleDelete"
    />

    <!-- 状态切换确认对话框 -->
    <ConfirmDialog
      v-if="canEditRole"
      v-model:open="statusConfirmOpen"
      :title="`确认${statusChangeRole?.status === 1 ? '禁用' : '启用'}角色`"
      :description="`确定要${statusChangeRole?.status === 1 ? '禁用' : '启用'}角色「${statusChangeRole?.name}」吗？`"
      :confirm-text="statusChangeRole?.status === 1 ? '确认禁用' : '确认启用'"
      :loading="statusLoading"
      @confirm="handleStatusConfirm"
    />
  </div>
</template>