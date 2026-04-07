<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Badge
} from '@/components/ui'
import { ChevronDown, ChevronRight, Edit, KeyRound, Plus, Trash2, Search, Check, X } from 'lucide-vue-next'
import { ConfirmDialog, EmptyState, ListSearchBar, StatusBadge } from '@/components/common'
import { assignMenus, getMenuTree, getRoleMenus, updateRoleStatus } from '@/api'
import { getRolePagePermissionState } from '@/lib/page-permissions'
import { isValidRoleCode } from '@/lib/validators'
import type { Menu, Role } from '@/types'
import { useUserStore } from '@/stores/user'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useCRUD } from '@/composables/useCRUD'
import { useStatusToggle } from '@/composables/useStatusToggle'
import { useTreeData } from '@/composables/useTreeData'
import { createRole, deleteRole, getRoleById, getRoleList, updateRole } from '@/api'
import { useDict } from '@/composables/useDict'
import { toast } from 'vue-sonner'
import { formatDateTime } from '@/utils/format'

interface RoleFormState {
  name: string
  code: string
  description: string
  dataScope: string
  status: string
  sortOrder: number
}

// MenuOption interface removed - using tree structure instead

const userStore = useUserStore()

// 字典数据
const { options: statusOptions } = useDict('common_status')

const searchQuery = ref('')
const menus = ref<Menu[]>([])

// Assign menus dialog state (additional feature, not handled by useCRUD)
const assignDialogOpen = ref(false)
const assignRole = ref<Role | null>(null)
const selectedMenuIds = ref<string[]>([])
const menuSearchQuery = ref('')
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

// ============ 改进：使用 useTreeData 管理菜单树形结构 ============
const {
  expandedKeys,
  toggleExpand,
  expandAll,
  collapseAll,
  getDescendantKeys,
  getAllKeys
} = useTreeData<Menu>(menus)

// 改进：菜单搜索过滤功能
const filterMenus = (menuList: Menu[], keyword: string): Menu[] => {
  if (!keyword) return menuList

  const lowerKeyword = keyword.toLowerCase()
  const result: Menu[] = []

  const walk = (items: Menu[]) => {
    items.forEach((menu) => {
      const matches = menu.name.toLowerCase().includes(lowerKeyword)
      const children = menu.children ? filterMenus(menu.children, keyword) : []

      if (matches || children.length > 0) {
        result.push({
          ...menu,
          children: children.length > 0 ? children : menu.children
        })
      }
    })
  }

  walk(menuList)
  return result
}

const filteredMenus = computed(() => filterMenus(menus.value, menuSearchQuery.value.trim().toLowerCase()))

// 当搜索时自动展开所有节点
watch(menuSearchQuery, (newVal) => {
  if (newVal.trim()) {
    expandAll()
  }
})

// 改进：获取过滤后的扁平化行（用于显示）
const filteredFlattenedRows = computed(() => {
  const rows: { id: string; level: number; hasChildren: boolean; isExpanded: boolean; menu: Menu }[] = []
  const keyword = menuSearchQuery.value.trim().toLowerCase()

  const walk = (menuList: Menu[], level = 0) => {
    menuList.forEach((menu) => {
      const hasChildren = Boolean(menu.children?.length)
      const isExpanded = expandedKeys.value.has(menu.id)
      const matchesSearch = !keyword || menu.name.toLowerCase().includes(keyword) ||
        (menu.children && hasMatchingDescendant(menu.children, keyword))

      // 只显示匹配的节点或其父节点
      if (matchesSearch || hasChildren) {
        rows.push({
          id: menu.id,
          level,
          hasChildren,
          isExpanded,
          menu
        })

        if (hasChildren && isExpanded) {
          walk(menu.children!, level + 1)
        }
      }
    })
  }

  const hasMatchingDescendant = (children: Menu[], keyword: string): boolean => {
    return children.some(child =>
      child.name.toLowerCase().includes(keyword) ||
      (child.children && hasMatchingDescendant(child.children, keyword))
    )
  }

  walk(filteredMenus.value)
  return rows
})

// 改进：父子联动 - 勾选父节点时自动勾选所有子节点
const toggleMenuSelection = (menuId: string, checked: boolean) => {
  const next = new Set(selectedMenuIds.value)

  if (checked) {
    // 勾选当前节点
    next.add(menuId)

    // 找到当前节点并勾选所有后代
    const findAndCheckDescendants = (items: Menu[]): boolean => {
      for (const menu of items) {
        if (menu.id === menuId) {
          // 勾选所有子孙节点
          const descendants = getDescendantKeys(menu)
          descendants.forEach(key => next.add(key))
          return true
        }
        if (menu.children?.length && findAndCheckDescendants(menu.children)) {
          return true
        }
      }
      return false
    }
    findAndCheckDescendants(menus.value)
  } else {
    // 取消勾选当前节点和所有后代
    const findAndUncheckDescendants = (items: Menu[]): boolean => {
      for (const menu of items) {
        if (menu.id === menuId) {
          // 取消勾选所有子孙节点
          const descendants = getDescendantKeys(menu)
          descendants.forEach(key => next.delete(key))
          return true
        }
        if (menu.children?.length && findAndUncheckDescendants(menu.children)) {
          return true
        }
      }
      return false
    }
    findAndUncheckDescendants(menus.value)
  }

  selectedMenuIds.value = Array.from(next)
}

// 改进：检查节点是否应该显示为选中状态（考虑父节点状态）
const isNodeSelected = (menuId: string): boolean => {
  return selectedMenuIds.value.includes(menuId)
}

// 改进：检查节点是否应该显示为半选状态（部分子节点选中）
const isNodeIndeterminate = (menuId: string): boolean => {
  if (selectedMenuIds.value.includes(menuId)) return false

  const findChildren = (items: Menu[]): Menu[] | null => {
    for (const menu of items) {
      if (menu.id === menuId) {
        return menu.children || null
      }
      if (menu.children?.length) {
        const found = findChildren(menu.children)
        if (found) return found
      }
    }
    return null
  }

  const children = findChildren(menus.value)
  if (!children || children.length === 0) return false

  // 检查是否有任何子节点被选中
  const hasSelectedChild = (items: Menu[]): boolean => {
    return items.some(child =>
      selectedMenuIds.value.includes(child.id) ||
      (child.children?.length && hasSelectedChild(child.children))
    )
  }

  return hasSelectedChild(children)
}

// 改进：全选/取消全选功能
const isAllMenusSelected = computed(() => {
  const allKeys = getAllKeys(menus.value)
  return allKeys.length > 0 && allKeys.every(key => selectedMenuIds.value.includes(key))
})

const isSomeMenusSelected = computed(() =>
  selectedMenuIds.value.length > 0 && !isAllMenusSelected.value
)

const toggleAllMenus = (checked: boolean | string) => {
  const allKeys = getAllKeys(menus.value)
  selectedMenuIds.value = checked ? allKeys : []
}

// 改进：统计信息
const selectionStats = computed(() => {
  const allKeys = getAllKeys(menus.value)
  const selectedCount = selectedMenuIds.value.length
  const totalCount = allKeys.length

  // 统计各类型菜单
  let menuCount = 0
  let buttonCount = 0

  const countByType = (items: Menu[]) => {
    items.forEach(menu => {
      if (selectedMenuIds.value.includes(menu.id)) {
        if (menu.type === 2) {
          buttonCount++
        } else {
          menuCount++
        }
      }
      if (menu.children?.length) {
        countByType(menu.children)
      }
    })
  }
  countByType(menus.value)

  return {
    selectedCount,
    totalCount,
    menuCount,
    buttonCount
  }
})

// 改进：快捷操作
const selectOnlyMenus = () => {
  const result: string[] = []
  const walk = (items: Menu[]) => {
    items.forEach(menu => {
      if (menu.type !== 2) { // 非按钮类型
        result.push(menu.id)
      }
      if (menu.children?.length) {
        walk(menu.children)
      }
    })
  }
  walk(menus.value)
  selectedMenuIds.value = result
  toast.success(`已选中 ${result.length} 个菜单项`)
}

const selectOnlyButtons = () => {
  const result: string[] = []
  const walk = (items: Menu[]) => {
    items.forEach(menu => {
      if (menu.type === 2) { // 按钮类型
        result.push(menu.id)
      }
      if (menu.children?.length) {
        walk(menu.children)
      }
    })
  }
  walk(menus.value)
  selectedMenuIds.value = result
  toast.success(`已选中 ${result.length} 个按钮权限`)
}

const clearSelection = () => {
  selectedMenuIds.value = []
  toast.info('已清空所有选择')
}

// Fetch menus on mount
const fetchMenus = () => runAssign(
  async () => { const res = await getMenuTree(); menus.value = res.data },
  { errorMessage: '获取菜单列表失败' }
)

// Assign menus handlers
const handleOpenAssign = (role: Role) => {
  assignRole.value = role
  assignDialogOpen.value = true
  menuSearchQuery.value = ''
  runAssign(async () => {
    const [menuTreeRes, selectedRes] = await Promise.all([getMenuTree(), getRoleMenus(role.id)])
    menus.value = menuTreeRes.data
    selectedMenuIds.value = selectedRes.data
    // 默认展开所有节点
    expandAll()
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
    successMessage: `菜单权限分配成功，共分配 ${selectedMenuIds.value.length} 项权限`,
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
                创建时间
              </th>
              <th class="p-4 text-left font-medium">
                操作
              </th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-if="listLoading">
              <td
                colspan="8"
                class="p-8 text-center text-muted-foreground"
              >
                加载中...
              </td>
            </tr>
            <tr v-else-if="filteredRoles.length === 0">
              <td
                colspan="8"
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
              <td class="p-4 text-sm text-muted-foreground">
                {{ formatDateTime(role.createTime) }}
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
                  <SelectItem
                    v-for="option in statusOptions"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
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

    <!-- 改进：更人性化的菜单权限分配对话框 -->
    <Dialog
      v-if="canAssignRole"
      v-model:open="assignDialogOpen"
    >
      <DialogContent class="sm:max-w-[720px]">
        <DialogHeader>
          <DialogTitle>分配菜单权限{{ assignRole ? ` - ${assignRole.name}` : '' }}</DialogTitle>
          <DialogDescription>
            勾选菜单项为角色分配访问权限
            <Badge
              v-if="selectionStats.selectedCount > 0"
              variant="secondary"
              class="ml-2"
            >
              已选 {{ selectionStats.selectedCount }} / {{ selectionStats.totalCount }}
            </Badge>
          </DialogDescription>
        </DialogHeader>

        <div
          v-if="assignLoading"
          class="py-8 text-center text-muted-foreground"
        >
          加载中...
        </div>

        <div
          v-else
          class="space-y-4"
        >
          <!-- 改进：搜索框 -->
          <div class="relative">
            <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              v-model="menuSearchQuery"
              placeholder="搜索菜单名称..."
              class="pl-9"
            />
            <Button
              v-if="menuSearchQuery"
              variant="ghost"
              size="sm"
              class="absolute right-1 top-1/2 h-6 -translate-y-1/2"
              @click="menuSearchQuery = ''"
            >
              <X class="h-4 w-4" />
            </Button>
          </div>

          <!-- 改进：快捷操作按钮 -->
          <div class="flex flex-wrap items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              @click="expandAll"
            >
              <ChevronDown class="mr-1 h-4 w-4" />
              全部展开
            </Button>
            <Button
              variant="outline"
              size="sm"
              @click="collapseAll"
            >
              <ChevronRight class="mr-1 h-4 w-4" />
              全部收起
            </Button>
            <Button
              variant="outline"
              size="sm"
              @click="selectOnlyMenus"
            >
              仅选菜单
            </Button>
            <Button
              variant="outline"
              size="sm"
              @click="selectOnlyButtons"
            >
              仅选按钮
            </Button>
            <Button
              variant="outline"
              size="sm"
              @click="clearSelection"
            >
              <X class="mr-1 h-4 w-4" />
              清空
            </Button>
          </div>

          <!-- 改进：树形结构菜单列表 - 使用原生滚动确保兼容性 -->
          <div class="h-[400px] overflow-y-auto rounded-md border">
            <div class="p-2">
              <!-- 全选 -->
              <label class="flex cursor-pointer items-center gap-3 rounded-md border-b px-3 py-2.5 mb-2 font-medium transition-colors hover:bg-muted/50">
                <Checkbox
                  :model-value="isAllMenusSelected"
                  :indeterminate="isSomeMenusSelected"
                  @update:model-value="toggleAllMenus"
                />
                <span class="text-sm">全选所有权限</span>
              </label>

              <!-- 菜单树 -->
              <div
                v-for="row in filteredFlattenedRows"
                :key="row.id"
                class="flex items-center gap-2 rounded-md px-2 py-1.5 transition-colors hover:bg-muted/50"
                :style="{ paddingLeft: `${row.level * 20 + 8}px` }"
              >
                <!-- 展开/收起按钮 -->
                <button
                  v-if="row.hasChildren"
                  class="flex h-6 w-6 flex-shrink-0 items-center justify-center rounded text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                  :aria-label="row.isExpanded ? '收起' : '展开'"
                  :aria-expanded="row.isExpanded"
                  @click="toggleExpand(row.id)"
                >
                  <ChevronDown
                    v-if="row.isExpanded"
                    class="h-4 w-4"
                  />
                  <ChevronRight
                    v-else
                    class="h-4 w-4"
                  />
                </button>
                <span
                  v-else
                  class="w-6 flex-shrink-0"
                />

                <!-- 复选框 -->
                <Checkbox
                  :model-value="isNodeSelected(row.id)"
                  :indeterminate="isNodeIndeterminate(row.id)"
                  @update:model-value="toggleMenuSelection(row.id, Boolean($event))"
                />

                <!-- 菜单名称和类型标识 -->
                <span class="flex-1 text-sm truncate">
                  {{ row.menu.name }}
                  <Badge
                    v-if="row.menu.type === 2"
                    variant="outline"
                    class="ml-1.5 text-[10px]"
                  >
                    按钮
                  </Badge>
                  <Badge
                    v-else-if="row.menu.type === 1"
                    variant="secondary"
                    class="ml-1.5 text-[10px]"
                  >
                    菜单
                  </Badge>
                </span>
              </div>

              <!-- 空状态 -->
              <div
                v-if="filteredFlattenedRows.length === 0"
                class="py-8 text-center text-muted-foreground"
              >
                {{ menuSearchQuery ? '未找到匹配的菜单' : '暂无菜单数据' }}
              </div>
            </div>
          </div>

          <!-- 改进：统计信息 -->
          <div
            v-if="selectionStats.selectedCount > 0"
            class="flex items-center justify-between rounded-md bg-muted/50 px-3 py-2 text-sm"
          >
            <span class="text-muted-foreground">
              已选择 <span class="font-medium text-foreground">{{ selectionStats.selectedCount }}</span> 项权限
              <span v-if="selectionStats.menuCount > 0">（{{ selectionStats.menuCount }} 个菜单）</span>
              <span v-if="selectionStats.buttonCount > 0">（{{ selectionStats.buttonCount }} 个按钮）</span>
            </span>
          </div>
        </div>

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
            <Check class="mr-1 h-4 w-4" />
            保存授权 ({{ selectionStats.selectedCount }})
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
