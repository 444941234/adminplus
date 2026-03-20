<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Badge,
  Button,
  Card,
  CardContent,
  Checkbox,
  Dialog,
  DialogContent,
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
import { Edit, KeyRound, Plus, Search, Trash2 } from 'lucide-vue-next'
import { assignMenus, createRole, deleteRole, getMenuTree, getRoleById, getRoleList, getRoleMenus, updateRole } from '@/api'
import { getRolePagePermissionState } from '@/lib/page-permissions'
import { isValidRoleCode } from '@/lib/validators'
import type { Menu, Role } from '@/types'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'

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

const loading = ref(false)
const roles = ref<Role[]>([])
const menus = ref<Menu[]>([])
const searchQuery = ref('')
const userStore = useUserStore()

const dialogOpen = ref(false)
const dialogLoading = ref(false)
const isEdit = ref(false)
const editId = ref('')

const deleteDialogOpen = ref(false)
const deleteRoleId = ref('')

const assignDialogOpen = ref(false)
const assignLoading = ref(false)
const assignRole = ref<Role | null>(null)
const selectedMenuIds = ref<string[]>([])

const form = reactive<RoleFormState>({
  name: '',
  code: '',
  description: '',
  dataScope: '1',
  status: '1',
  sortOrder: 0
})

const permissionState = computed(() => getRolePagePermissionState(userStore.hasPermission))
const canAddRole = computed(() => permissionState.value.canAddRole)
const canEditRole = computed(() => permissionState.value.canEditRole)
const canDeleteRole = computed(() => permissionState.value.canDeleteRole)
const canAssignRole = computed(() => permissionState.value.canAssignRole)

const getRoleName = (role: Role) => role.roleName ?? role.name ?? ''
const getRoleCode = (role: Role) => role.roleCode ?? role.code ?? ''
const getRoleSort = (role: Role) => role.sort ?? role.sortOrder ?? 0
const getRoleDataScope = (role: Role) => role.dataScope ?? 1

const resetForm = () => {
  Object.assign(form, {
    name: '',
    code: '',
    description: '',
    dataScope: '1',
    status: '1',
    sortOrder: 0
  })
}

const filteredRoles = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return roles.value

  return roles.value.filter((role) =>
    [getRoleName(role), getRoleCode(role), role.description ?? ''].some((value) =>
      value.toLowerCase().includes(keyword)
    )
  )
})

const menuOptions = computed<MenuOption[]>(() => {
  const options: MenuOption[] = []
  const walk = (menuList: Menu[], level = 0) => {
    menuList.forEach((menu) => {
      const type = menu.menuType ?? menu.type ?? 1
      options.push({
        id: menu.id,
        label: menu.menuName ?? menu.name ?? '',
        level,
        disabled: false
      })
      if (menu.children?.length) {
        walk(menu.children, level + 1)
      }
      if (type === 1 && !menu.children?.length) {
        options[options.length - 1].disabled = false
      }
    })
  }
  walk(menus.value)
  return options
})

const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await getRoleList()
    roles.value = res.data.records || []
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取角色列表失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

const fetchMenus = async () => {
  try {
    const res = await getMenuTree()
    menus.value = res.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取菜单列表失败'
    toast.error(message)
  }
}

const handleSearch = () => {}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  editId.value = ''
  dialogOpen.value = true
}

const handleEdit = async (id: string) => {
  isEdit.value = true
  editId.value = id
  dialogLoading.value = true
  dialogOpen.value = true
  try {
    const res = await getRoleById(id)
    const role = res.data
    Object.assign(form, {
      name: getRoleName(role),
      code: getRoleCode(role),
      description: role.description || '',
      dataScope: String(getRoleDataScope(role)),
      status: String(role.status ?? 1),
      sortOrder: getRoleSort(role)
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取角色详情失败'
    toast.error(message)
    dialogOpen.value = false
  } finally {
    dialogLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.name.trim()) {
    toast.warning('请输入角色名称')
    return
  }
  if (!isEdit.value && !form.code.trim()) {
    toast.warning('请输入角色编码')
    return
  }
  if (!isEdit.value && !isValidRoleCode(form.code.trim())) {
    toast.warning('角色编码需以字母开头，只能包含字母、数字、下划线、冒号或短横线')
    return
  }

  dialogLoading.value = true
  try {
    if (isEdit.value) {
      await updateRole(editId.value, {
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        dataScope: Number(form.dataScope),
        status: Number(form.status),
        sortOrder: Number(form.sortOrder) || 0
      })
      toast.success('角色更新成功')
    } else {
      await createRole({
        code: form.code.trim(),
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        dataScope: Number(form.dataScope),
        status: Number(form.status),
        sortOrder: Number(form.sortOrder) || 0
      })
      toast.success('角色创建成功')
    }
    dialogOpen.value = false
    await fetchRoles()
  } catch (error) {
    const message = error instanceof Error ? error.message : '保存角色失败'
    toast.error(message)
  } finally {
    dialogLoading.value = false
  }
}

const handleDeleteConfirm = (id: string) => {
  deleteRoleId.value = id
  deleteDialogOpen.value = true
}

const handleDelete = async () => {
  try {
    await deleteRole(deleteRoleId.value)
    toast.success('角色删除成功')
    await fetchRoles()
  } catch (error) {
    const message = error instanceof Error ? error.message : '删除角色失败'
    toast.error(message)
  } finally {
    deleteDialogOpen.value = false
  }
}

const toggleMenuSelection = (menuId: string, checked: boolean) => {
  const next = new Set(selectedMenuIds.value)
  if (checked) {
    next.add(menuId)
  } else {
    next.delete(menuId)
  }
  selectedMenuIds.value = Array.from(next)
}

const handleOpenAssign = async (role: Role) => {
  assignRole.value = role
  assignDialogOpen.value = true
  assignLoading.value = true
  try {
    const [menuTreeRes, selectedRes] = await Promise.all([getMenuTree(), getRoleMenus(role.id)])
    menus.value = menuTreeRes.data
    selectedMenuIds.value = selectedRes.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取角色菜单权限失败'
    toast.error(message)
    assignDialogOpen.value = false
  } finally {
    assignLoading.value = false
  }
}

const handleAssignSubmit = async () => {
  if (!assignRole.value) return
  assignLoading.value = true
  try {
    await assignMenus(assignRole.value.id, selectedMenuIds.value)
    toast.success('菜单权限分配成功')
    assignDialogOpen.value = false
  } catch (error) {
    const message = error instanceof Error ? error.message : '分配菜单权限失败'
    toast.error(message)
  } finally {
    assignLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchRoles(), fetchMenus()])
})
</script>

<template>
  <div class="space-y-4">
    <Card>
      <CardContent class="p-4">
        <div class="flex items-center gap-4">
          <Input
            v-model="searchQuery"
            placeholder="搜索角色名称、编码或描述"
            class="w-80"
            @keyup.enter="handleSearch"
          />
          <Button @click="handleSearch">
            <Search class="mr-2 h-4 w-4" />
            搜索
          </Button>
          <Button variant="outline" @click="searchQuery = ''">重置</Button>
          <div class="flex-1" />
          <Button v-if="canAddRole" @click="handleAdd">
            <Plus class="mr-2 h-4 w-4" />
            新增角色
          </Button>
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardContent class="p-0">
        <table class="w-full">
          <thead class="border-b bg-muted/50">
            <tr>
              <th class="p-4 text-left font-medium">角色名称</th>
              <th class="p-4 text-left font-medium">角色编码</th>
              <th class="p-4 text-left font-medium">描述</th>
              <th class="p-4 text-left font-medium">数据范围</th>
              <th class="p-4 text-left font-medium">状态</th>
              <th class="p-4 text-left font-medium">排序</th>
              <th class="p-4 text-left font-medium">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-if="loading">
              <td colspan="7" class="p-8 text-center text-muted-foreground">加载中...</td>
            </tr>
            <tr v-else-if="filteredRoles.length === 0">
              <td colspan="7" class="p-8 text-center text-muted-foreground">暂无数据</td>
            </tr>
            <tr v-for="role in filteredRoles" :key="role.id" class="hover:bg-muted/30">
              <td class="p-4 font-medium">{{ getRoleName(role) }}</td>
              <td class="p-4">
                <code class="rounded bg-muted px-2 py-0.5 text-sm">{{ getRoleCode(role) }}</code>
              </td>
              <td class="p-4 text-muted-foreground">{{ role.description || '-' }}</td>
              <td class="p-4 text-muted-foreground">
                {{ getRoleDataScope(role) === 1 ? '全部数据' : `范围 ${getRoleDataScope(role)}` }}
              </td>
              <td class="p-4">
                <Badge :variant="role.status === 1 ? 'default' : 'destructive'">
                  {{ role.status === 1 ? '正常' : '禁用' }}
                </Badge>
              </td>
              <td class="p-4 text-muted-foreground">{{ getRoleSort(role) }}</td>
              <td class="p-4">
                <div class="flex gap-2">
                  <Button v-if="canAssignRole" size="sm" variant="ghost" @click="handleOpenAssign(role)">
                    <KeyRound class="h-4 w-4" />
                  </Button>
                  <Button v-if="canEditRole" size="sm" variant="ghost" @click="handleEdit(role.id)">
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

    <Dialog v-if="canAddRole || canEditRole" v-model:open="dialogOpen">
      <DialogContent class="sm:max-w-[520px]">
        <DialogHeader>
          <DialogTitle>{{ isEdit ? '编辑角色' : '新增角色' }}</DialogTitle>
        </DialogHeader>
        <div v-if="dialogLoading" class="py-8 text-center text-muted-foreground">加载中...</div>
        <div v-else class="space-y-4 py-2">
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>角色名称</Label>
              <Input v-model="form.name" placeholder="请输入角色名称" />
            </div>
            <div class="space-y-2">
              <Label>角色编码</Label>
              <Input
                v-model="form.code"
                :disabled="isEdit"
                placeholder="例如：ROLE_MANAGER"
              />
            </div>
          </div>
          <div class="space-y-2">
            <Label>描述</Label>
            <Input v-model="form.description" placeholder="请输入角色描述" />
          </div>
          <div class="grid grid-cols-3 gap-4">
            <div class="space-y-2">
              <Label>数据范围</Label>
              <Select v-model="form.dataScope">
                <SelectTrigger>
                  <SelectValue placeholder="请选择数据范围" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">全部数据</SelectItem>
                  <SelectItem value="2">本部门及以下</SelectItem>
                  <SelectItem value="3">本部门</SelectItem>
                  <SelectItem value="4">仅本人</SelectItem>
                  <SelectItem value="5">自定义</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="space-y-2">
              <Label>状态</Label>
              <Select v-model="form.status">
                <SelectTrigger>
                  <SelectValue placeholder="请选择状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">正常</SelectItem>
                  <SelectItem value="0">禁用</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="space-y-2">
              <Label>排序</Label>
              <Input
                :model-value="String(form.sortOrder)"
                type="number"
                placeholder="排序值"
                @update:model-value="form.sortOrder = Number($event) || 0"
              />
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="dialogOpen = false">取消</Button>
          <Button :disabled="dialogLoading" @click="handleSubmit">{{ isEdit ? '保存' : '创建' }}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <Dialog v-if="canAssignRole" v-model:open="assignDialogOpen">
      <DialogContent class="sm:max-w-[680px]">
        <DialogHeader>
          <DialogTitle>分配菜单权限{{ assignRole ? ` - ${getRoleName(assignRole)}` : '' }}</DialogTitle>
        </DialogHeader>
        <div v-if="assignLoading" class="py-8 text-center text-muted-foreground">加载中...</div>
        <ScrollArea v-else class="max-h-[420px] rounded-md border">
          <div class="space-y-1 p-4">
            <label
              v-for="item in menuOptions"
              :key="item.id"
              class="flex cursor-pointer items-center gap-3 rounded px-3 py-2 transition-colors hover:bg-muted/50"
              :style="{ paddingLeft: `${item.level * 20 + 12}px` }"
            >
              <Checkbox
                :checked="selectedMenuIds.includes(item.id)"
                @update:checked="toggleMenuSelection(item.id, Boolean($event))"
              />
              <span class="text-sm">{{ item.label }}</span>
            </label>
          </div>
        </ScrollArea>
        <DialogFooter>
          <Button variant="outline" @click="assignDialogOpen = false">取消</Button>
          <Button :disabled="assignLoading" @click="handleAssignSubmit">保存授权</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <AlertDialog v-if="canDeleteRole" v-model:open="deleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认删除角色</AlertDialogTitle>
          <AlertDialogDescription>删除后不可恢复，如果角色已绑定用户，后端可能会拒绝删除。</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction @click="handleDelete">确认删除</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>
