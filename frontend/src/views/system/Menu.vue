<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  Badge,
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
  SelectValue
} from '@/components/ui'
import { ChevronDown, ChevronRight, Edit, Plus, Trash2 } from '@lucide/vue'
import { ConfirmDialog, EmptyState, ListSearchBar, StatusBadge } from '@/components/common'
import { batchDelete, batchUpdateStatus, createMenu, deleteMenu, getMenuById, getMenuTree, updateMenu } from '@/api'
import type { Menu } from '@/types'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useTreeData } from '@/composables/useTreeData'
import { useStatusToggle } from '@/composables/useStatusToggle'
import { useDict } from '@/composables/useDict'

interface MenuFormState {
  parentId: string
  name: string
  type: string
  path: string
  component: string
  permKey: string
  icon: string
  sortOrder: number
  visible: string
  status: string
}

interface MenuTreeRow {
  id: string
  level: number
  hasChildren: boolean
  expanded: boolean
  menu: Menu
}

const { loading, run: runList } = useAsyncAction('获取菜单列表失败')
const { loading: dialogLoading, run: runDialog } = useAsyncAction('操作失败')
const { loading: deleteLoading, run: runDelete } = useAsyncAction('删除菜单失败')

const searchQuery = ref('')
const menus = ref<Menu[]>([])
const userStore = useUserStore()

// 字典数据
const { getLabel: getMenuTypeLabel, options: menuTypeOptions } = useDict('menu_type')

// Use tree data composable for expand/collapse management
const {
  expandedKeys,
  toggleExpand,
  expandAll,
  getAllKeys,
  buildParentOptions
} = useTreeData<Menu>(menus)

// Filter menus based on search query
const matchesSearch = (menu: Menu, keyword: string) => {
  const normalized = keyword.toLowerCase()
  return [
    menu.name,
    menu.path ?? '',
    menu.component ?? '',
    menu.permKey
  ].some((value) => value.toLowerCase().includes(normalized))
}

const filterMenus = (menuList: Menu[], keyword: string): Menu[] => {
  if (!keyword) return menuList

  return menuList.reduce<Menu[]>((result, menu) => {
    const children = filterMenus(menu.children ?? [], keyword)
    if (matchesSearch(menu, keyword) || children.length > 0) {
      result.push({
        ...menu,
        children
      })
    }
    return result
  }, [])
}

const filteredMenus = computed(() => filterMenus(menus.value, searchQuery.value.trim()))

// Flattened rows with search filtering applied
const flattenedRows = computed<MenuTreeRow[]>(() => {
  const rows: MenuTreeRow[] = []
  const walk = (menuList: Menu[], level = 0) => {
    menuList.forEach((menu) => {
      const hasChildren = Boolean(menu.children?.length)
      const expanded = expandedKeys.value.has(menu.id)
      rows.push({
        id: menu.id,
        level,
        hasChildren,
        expanded,
        menu
      })
      if (hasChildren && expanded) {
        walk(menu.children ?? [], level + 1)
      }
    })
  }
  walk(filteredMenus.value)
  return rows
})

// Parent options for dialog dropdown (excludes current item when editing)
const parentOptions = computed(() => {
  const excludeKey = isEdit.value ? editId.value : undefined
  return [
    { id: '0', label: '顶级菜单' },
    ...buildParentOptions(menus.value, excludeKey)
  ]
})

const dialogOpen = ref(false)
const isEdit = ref(false)
const editId = ref('')

const deleteDialogOpen = ref(false)
const deleteMenuId = ref('')
const selectedMenuIds = ref<string[]>([])

// Status toggle
const {
  statusChangeItem: statusChangeMenu,
  statusConfirmOpen,
  handleStatusClick,
  handleStatusConfirm
} = useStatusToggle<Menu>({
  updateStatus: (id, newStatus) => batchUpdateStatus([id], newStatus),
  onSuccess: () => fetchData()
})

const form = reactive<MenuFormState>({
  parentId: '0',
  name: '',
  type: '0',
  path: '',
  component: '',
  permKey: '',
  icon: '',
  sortOrder: 0,
  visible: '1',
  status: '1'
})

const canAddMenu = computed(() => userStore.hasPermission('menu:add'))
const canEditMenu = computed(() => userStore.hasPermission('menu:edit'))
const canDeleteMenu = computed(() => userStore.hasPermission('menu:delete'))
const hasSelectedMenus = computed(() => selectedMenuIds.value.length > 0)
const allSelected = computed(
  () => flattenedRows.value.length > 0 && flattenedRows.value.every((row) => selectedMenuIds.value.includes(row.id))
)

// 根据菜单类型显示/隐藏字段
const isDirectory = computed(() => form.type === '0')
const isMenu = computed(() => form.type === '1')
const isButton = computed(() => form.type === '2')

// 监听菜单类型变化，自动清空不需要的字段
watch(() => form.type, (newType, oldType) => {
  if (!oldType) return // 首次初始化时不处理

  // 切换到按钮类型时，清空不需要的字段
  if (newType === '2') {
    form.path = ''
    form.component = ''
    form.icon = ''
    form.visible = '1'
  }
  // 切换到目录类型时，清空组件路径
  else if (newType === '0') {
    form.component = ''
  }
  // 切换到菜单类型时，保持原有数据
})

const resetForm = () => {
  Object.assign(form, {
    parentId: '0',
    name: '',
    type: '0',
    path: '',
    component: '',
    permKey: '',
    icon: '',
    sortOrder: 0,
    visible: '1',
    status: '1'
  })
}

const handleSearch = () => {
  expandAll()
}

const validateForm = () => {
  if (!form.name.trim()) {
    toast.warning('请输入菜单名称')
    return false
  }

  // 目录和菜单需要路由路径
  if (!isButton.value && !form.path.trim()) {
    toast.warning('请输入路由路径')
    return false
  }

  // 菜单类型需要组件路径
  if (isMenu.value && !form.component.trim()) {
    toast.warning('请输入组件路径')
    return false
  }

  // 按钮类型强烈建议填写权限标识
  if (isButton.value && !form.permKey.trim()) {
    toast.warning('请输入权限标识')
    return false
  }

  return true
}

const fetchData = () => runList(async () => {
  const res = await getMenuTree()
  menus.value = res.data
  expandAll()
  // Filter out selected IDs that no longer exist in the tree
  const allKeys = getAllKeys(res.data)
  selectedMenuIds.value = selectedMenuIds.value.filter((id) => allKeys.includes(id))
})

const toggleMenuSelection = (menuId: string, checked: boolean) => {
  const next = new Set(selectedMenuIds.value)
  if (checked) {
    next.add(menuId)
  } else {
    next.delete(menuId)
  }
  selectedMenuIds.value = Array.from(next)
}

const toggleSelectAll = (checked: boolean) => {
  selectedMenuIds.value = checked ? flattenedRows.value.map((row) => row.id) : []
}

const handleAdd = (parentId = '0', type = '0') => {
  resetForm()
  isEdit.value = false
  editId.value = ''
  form.parentId = parentId
  form.type = type
  dialogOpen.value = true
}

const handleEdit = (id: string) => {
  isEdit.value = true
  editId.value = id
  dialogOpen.value = true
  runDialog(async () => {
    const res = await getMenuById(id)
    const menu = res.data
    Object.assign(form, {
      parentId: menu.parentId || '0',
      name: menu.name,
      type: String(menu.type),
      path: menu.path || '',
      component: menu.component || '',
      permKey: menu.permKey,
      icon: menu.icon || '',
      sortOrder: menu.sortOrder,
      visible: String(menu.visible ?? 1),
      status: String(menu.status ?? 1)
    })
  }, {
    errorMessage: '获取菜单详情失败',
    onError: () => { dialogOpen.value = false }
  })
}

const handleSubmit = () => {
  if (!validateForm()) return

  runDialog(async () => {
    const payload = {
      parentId: form.parentId === '0' ? undefined : form.parentId,
      type: Number(form.type),
      name: form.name.trim(),
      path: form.path.trim() || undefined,
      component: form.component.trim() || undefined,
      permKey: form.permKey.trim() || undefined,
      icon: form.icon.trim() || undefined,
      sortOrder: Number(form.sortOrder) || 0,
      visible: Number(form.visible),
      status: Number(form.status)
    }

    if (isEdit.value) {
      await updateMenu(editId.value, payload)
    } else {
      await createMenu(payload)
    }
  }, {
    successMessage: isEdit.value ? '菜单更新成功' : '菜单创建成功',
    errorMessage: '保存菜单失败',
    onSuccess: () => {
      dialogOpen.value = false
      fetchData()
    }
  })
}

const handleDeleteConfirm = (id: string) => {
  deleteMenuId.value = id
  deleteDialogOpen.value = true
}

const handleBatchDeleteConfirm = () => {
  if (!selectedMenuIds.value.length) {
    toast.warning('请先选择要删除的菜单')
    return
  }
  deleteMenuId.value = ''
  deleteDialogOpen.value = true
}

const handleDelete = () => {
  runDelete(async () => {
    if (deleteMenuId.value) {
      await deleteMenu(deleteMenuId.value)
    } else {
      await batchDelete(selectedMenuIds.value)
    }
  }, {
    successMessage: deleteMenuId.value ? '菜单删除成功' : `已删除 ${selectedMenuIds.value.length} 个菜单`,
    onSuccess: () => {
      if (!deleteMenuId.value) selectedMenuIds.value = []
      fetchData()
    }
  }).finally(() => {
    deleteDialogOpen.value = false
  })
}

const handleBatchStatusChange = (status: number) => {
  if (!selectedMenuIds.value.length) {
    toast.warning('请先选择要更新状态的菜单')
    return
  }
  runList(async () => {
    await batchUpdateStatus(selectedMenuIds.value, status)
  }, {
    successMessage: `已批量${status === 1 ? '启用' : '禁用'}菜单`,
    errorMessage: '批量更新菜单状态失败',
    onSuccess: () => fetchData()
  })
}

onMounted(fetchData)
</script>

<template>
  <div class="space-y-4">
    <ListSearchBar
      v-model="searchQuery"
      placeholder="搜索菜单名称、路径或权限"
      @search="handleSearch"
      @reset="handleSearch"
    >
      <template #actions>
        <Button
          v-if="canEditMenu"
          variant="outline"
          :disabled="!hasSelectedMenus"
          @click="handleBatchStatusChange(1)"
        >
          批量启用
        </Button>
        <Button
          v-if="canEditMenu"
          variant="outline"
          :disabled="!hasSelectedMenus"
          @click="handleBatchStatusChange(0)"
        >
          批量禁用
        </Button>
        <Button
          v-if="canDeleteMenu"
          variant="outline"
          :disabled="!hasSelectedMenus"
          @click="handleBatchDeleteConfirm"
        >
          <Trash2 class="mr-2 h-4 w-4" />
          批量删除
        </Button>
        <Button
          v-if="canAddMenu"
          @click="handleAdd()"
        >
          <Plus class="mr-2 h-4 w-4" />
          新增菜单
        </Button>
      </template>
    </ListSearchBar>

    <Card>
      <CardContent class="p-0">
        <table class="w-full">
          <thead class="border-b bg-muted/50">
            <tr>
              <th class="p-4 text-left font-medium">
                <Checkbox
                  :model-value="allSelected"
                  @update:model-value="toggleSelectAll(Boolean($event))"
                />
              </th>
              <th class="p-4 text-left font-medium">
                菜单名称
              </th>
              <th class="p-4 text-left font-medium">
                类型
              </th>
              <th class="p-4 text-left font-medium">
                路由路径
              </th>
              <th class="p-4 text-left font-medium">
                组件/权限
              </th>
              <th class="p-4 text-left font-medium">
                可见
              </th>
              <th class="p-4 text-left font-medium">
                状态
              </th>
              <th class="p-4 text-left font-medium">
                排序
              </th>
              <th class="p-4 text-center font-medium">
                操作
              </th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-if="loading">
              <td
                colspan="9"
                class="p-8 text-center text-muted-foreground"
              >
                加载中...
              </td>
            </tr>
            <tr v-else-if="flattenedRows.length === 0">
              <td
                colspan="9"
                class="p-0"
              >
                <EmptyState
                  type="menus"
                  :show-action="canAddMenu"
                  action-text="添加菜单"
                  @action="handleAdd()"
                >
                  <template #action-icon>
                    <Plus class="mr-2 h-4 w-4" />
                  </template>
                </EmptyState>
              </td>
            </tr>
            <tr
              v-for="row in flattenedRows"
              :key="row.id"
              class="hover:bg-muted/30"
            >
              <td class="p-4">
                <Checkbox
                  :model-value="selectedMenuIds.includes(row.id)"
                  @update:model-value="toggleMenuSelection(row.id, Boolean($event))"
                />
              </td>
              <td class="p-4">
                <div
                  class="flex items-center gap-2"
                  :style="{ paddingLeft: `${row.level * 24}px` }"
                >
                  <button
                    v-if="row.hasChildren"
                    class="flex h-5 w-5 items-center justify-center rounded text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                    :aria-label="row.expanded ? '收起' : '展开'"
                    :aria-expanded="row.expanded"
                    @click="toggleExpand(row.id)"
                  >
                    <ChevronDown
                      v-if="row.expanded"
                      class="h-4 w-4"
                    />
                    <ChevronRight
                      v-else
                      class="h-4 w-4"
                    />
                  </button>
                  <span
                    v-else
                    class="w-5"
                  />
                  <div>
                    <p class="font-medium">
                      {{ row.menu.name }}
                    </p>
                    <p
                      v-if="row.menu.icon"
                      class="text-sm text-muted-foreground"
                    >
                      {{ row.menu.icon }}
                    </p>
                  </div>
                </div>
              </td>
              <td class="p-4">
                <Badge
                  :variant="
                    row.menu.type === 0
                      ? 'default'
                      : row.menu.type === 1
                        ? 'secondary'
                        : 'outline'
                  "
                >
                  {{ getMenuTypeLabel(row.menu.type) }}
                </Badge>
              </td>
              <td class="p-4">
                <code class="rounded bg-muted px-2 py-0.5 text-sm">{{ row.menu.path || '-' }}</code>
              </td>
              <td class="p-4 text-muted-foreground">
                {{ row.menu.component || row.menu.permKey || '-' }}
              </td>
              <td class="p-4">
                <Badge :variant="row.menu.visible === 1 ? 'secondary' : 'outline'">
                  {{ row.menu.visible === 1 ? '显示' : '隐藏' }}
                </Badge>
              </td>
              <td class="p-4">
                <StatusBadge
                  :status="row.menu.status"
                  :clickable="canEditMenu"
                  @toggle="handleStatusClick(row.menu)"
                />
              </td>
              <td class="p-4 text-muted-foreground">
                {{ row.menu.sortOrder }}
              </td>
              <td class="p-4 text-center">
                <div class="flex justify-center gap-2">
                  <Button
                    v-if="canAddMenu && row.menu.type !== 2"
                    size="sm"
                    variant="ghost"
                    @click="handleAdd(row.menu.id, row.menu.type === 0 ? '1' : '2')"
                  >
                    <Plus class="h-4 w-4" />
                  </Button>
                  <Button
                    v-if="canEditMenu"
                    size="sm"
                    variant="ghost"
                    @click="handleEdit(row.menu.id)"
                  >
                    <Edit class="h-4 w-4" />
                  </Button>
                  <Button
                    v-if="canDeleteMenu"
                    size="sm"
                    variant="ghost"
                    class="text-destructive"
                    @click="handleDeleteConfirm(row.menu.id)"
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

    <!-- 新增/编辑菜单对话框 -->
    <Dialog v-model:open="dialogOpen">
      <DialogContent class="sm:max-w-[560px]">
        <DialogHeader>
          <DialogTitle>{{ isEdit ? '编辑菜单' : '新增菜单' }}</DialogTitle>
          <DialogDescription>配置菜单名称、路径和权限标识</DialogDescription>
        </DialogHeader>
        <div
          v-if="dialogLoading"
          class="py-8 text-center text-muted-foreground"
        >
          加载中...
        </div>
        <div
          v-else-if="canAddMenu || canEditMenu"
          class="space-y-4 py-2"
        >
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>父级菜单</Label>
              <Select v-model="form.parentId">
                <SelectTrigger>
                  <SelectValue placeholder="请选择父级菜单" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem
                    v-for="item in parentOptions"
                    :key="item.id"
                    :value="item.id"
                  >
                    {{ item.label }}
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="space-y-2">
              <Label>菜单类型</Label>
              <Select v-model="form.type">
                <SelectTrigger>
                  <SelectValue placeholder="请选择菜单类型" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem
                    v-for="option in menuTypeOptions"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div class="space-y-2">
            <Label>菜单名称</Label>
            <Input
              v-model="form.name"
              placeholder="请输入菜单名称"
            />
          </div>

          <!-- 目录：显示 路由路径 + 图标 -->
          <div
            v-if="isDirectory"
            class="grid grid-cols-2 gap-4"
          >
            <div class="space-y-2">
              <Label>路由路径</Label>
              <Input
                v-model="form.path"
                placeholder="例如：/system"
              />
              <p class="text-xs text-muted-foreground">
                目录的路由路径，通常以 / 开头
              </p>
            </div>
            <div class="space-y-2">
              <Label>图标</Label>
              <Input
                v-model="form.icon"
                placeholder="例如：Users"
              />
            </div>
          </div>

          <!-- 菜单：显示 路由路径 + 组件路径 + 图标 + 可选权限标识 -->
          <div
            v-if="isMenu"
            class="grid grid-cols-2 gap-4"
          >
            <div class="space-y-2">
              <Label>路由路径</Label>
              <Input
                v-model="form.path"
                placeholder="例如：/system/user"
              />
            </div>
            <div class="space-y-2">
              <Label>组件路径</Label>
              <Input
                v-model="form.component"
                placeholder="例如：system/User.vue"
              />
            </div>
          </div>

          <div
            v-if="isMenu"
            class="grid grid-cols-2 gap-4"
          >
            <div class="space-y-2">
              <Label>图标</Label>
              <Input
                v-model="form.icon"
                placeholder="例如：Users"
              />
            </div>
            <div class="space-y-2">
              <Label>权限标识</Label>
              <Input
                v-model="form.permKey"
                placeholder="可选，页面权限标识"
              />
            </div>
          </div>

          <!-- 按钮：只显示 权限标识 -->
          <div
            v-if="isButton"
            class="space-y-2"
          >
            <Label>权限标识</Label>
            <Input
              v-model="form.permKey"
              placeholder="例如：user:add"
            />
            <p class="text-xs text-muted-foreground">
              按钮的权限标识，用于前端权限控制
            </p>
          </div>

          <!-- 通用设置：排序 + 可见性（按钮除外） + 状态 -->
          <div
            :class="isButton ? 'grid grid-cols-2 gap-4' : 'grid grid-cols-3 gap-4'"
          >
            <div class="space-y-2">
              <Label>排序</Label>
              <Input
                :model-value="String(form.sortOrder)"
                type="number"
                placeholder="排序值"
                @update:model-value="form.sortOrder = Number($event) || 0"
              />
            </div>
            <div
              v-if="!isButton"
              class="space-y-2"
            >
              <Label>可见性</Label>
              <Select v-model="form.visible">
                <SelectTrigger>
                  <SelectValue placeholder="请选择可见性" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">
                    显示
                  </SelectItem>
                  <SelectItem value="0">
                    隐藏
                  </SelectItem>
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
                  <SelectItem value="1">
                    正常
                  </SelectItem>
                  <SelectItem value="0">
                    禁用
                  </SelectItem>
                </SelectContent>
              </Select>
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

    <ConfirmDialog
      v-if="canDeleteMenu"
      v-model:open="deleteDialogOpen"
      :title="deleteMenuId ? '确认删除菜单' : '确认批量删除菜单'"
      :description="deleteMenuId ? '删除后不可恢复，如果存在子菜单或角色绑定，后端可能会拒绝删除。' : `将删除 ${selectedMenuIds.length} 个菜单，删除后不可恢复，且后端可能因层级或角色绑定拒绝部分删除。`"
      confirm-text="确认删除"
      :loading="deleteLoading"
      @confirm="handleDelete"
    />

    <!-- 状态切换确认对话框 -->
    <ConfirmDialog
      v-if="canEditMenu"
      v-model:open="statusConfirmOpen"
      :title="`确认${statusChangeMenu?.status === 1 ? '禁用' : '启用'}菜单`"
      :description="`确定要${statusChangeMenu?.status === 1 ? '禁用' : '启用'}菜单「${statusChangeMenu?.name}」吗？`"
      :confirm-text="statusChangeMenu?.status === 1 ? '确认禁用' : '确认启用'"
      :loading="loading"
      @confirm="handleStatusConfirm"
    />
  </div>
</template>
