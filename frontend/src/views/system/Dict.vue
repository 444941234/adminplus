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
  Dialog,
  DialogContent,
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
  Textarea
} from '@/components/ui'
import { ChevronLeft, ChevronRight, Edit, ListTree, Plus, Search, Trash2 } from 'lucide-vue-next'
import {
  createDict,
  createDictItem,
  deleteDict,
  deleteDictItem,
  getDictById,
  getDictItemById,
  getDictItemList,
  getDictList,
  updateDict,
  updateDictItem,
  updateDictItemStatus,
  updateDictStatus
} from '@/api'
import type { Dict, DictItem, PageResult } from '@/types'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'

const loading = ref(false)
const searchQuery = ref('')
const tableData = ref<PageResult<Dict>>({ records: [], total: 0, page: 1, size: 10 })
const userStore = useUserStore()

const dialogOpen = ref(false)
const dialogLoading = ref(false)
const isEdit = ref(false)
const editId = ref('')

const deleteDialogOpen = ref(false)
const deleteDictId = ref('')
const deleteLoading = ref(false)

const itemDialogOpen = ref(false)
const itemLoading = ref(false)
const activeDict = ref<Dict | null>(null)
const itemTableData = ref<{ records: DictItem[]; total: number }>({ records: [], total: 0 })

const itemFormDialogOpen = ref(false)
const itemFormLoading = ref(false)
const isEditItem = ref(false)
const editItemId = ref('')

const deleteItemDialogOpen = ref(false)
const deleteItemId = ref('')
const deleteItemLoading = ref(false)

const form = reactive({
  dictType: '',
  dictName: '',
  remark: '',
  status: '1'
})

const itemForm = reactive({
  parentId: '0',
  label: '',
  value: '',
  sortOrder: 0,
  status: '1',
  remark: ''
})

const canAddDict = computed(() => userStore.hasPermission('dict:add'))
const canEditDict = computed(() => userStore.hasPermission('dict:edit'))
const canDeleteDict = computed(() => userStore.hasPermission('dict:delete'))
const canListDictItems = computed(() => userStore.hasPermission('dictitem:list'))
const canAddDictItem = computed(() => userStore.hasPermission('dictitem:add'))
const canEditDictItem = computed(() => userStore.hasPermission('dictitem:edit'))
const canDeleteDictItem = computed(() => userStore.hasPermission('dictitem:delete'))

const totalPages = computed(() => Math.ceil(tableData.value.total / tableData.value.size) || 1)

const visiblePages = computed(() => {
  const current = tableData.value.page
  const total = totalPages.value
  const pages: Array<number | string> = []
  if (total <= 7) {
    for (let i = 1; i <= total; i += 1) pages.push(i)
    return pages
  }
  pages.push(1)
  if (current > 3) pages.push('...')
  const start = Math.max(2, current - 1)
  const end = Math.min(total - 1, current + 1)
  for (let i = start; i <= end; i += 1) {
    pages.push(i)
  }
  if (current < total - 2) pages.push('...')
  pages.push(total)
  return pages
})

const getDictRemark = (dict: Dict) => dict.description ?? ''
const getItemParentId = (item: DictItem) => item.parentId ?? '0'

const resetForm = () => {
  Object.assign(form, {
    dictType: '',
    dictName: '',
    remark: '',
    status: '1'
  })
}

const resetItemForm = () => {
  Object.assign(itemForm, {
    parentId: '0',
    label: '',
    value: '',
    sortOrder: 0,
    status: '1',
    remark: ''
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDictList({
      page: tableData.value.page,
      size: tableData.value.size,
      keyword: searchQuery.value.trim() || undefined
    })
    tableData.value = res.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取字典列表失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  tableData.value.page = 1
  fetchData()
}

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value && page !== tableData.value.page) {
    tableData.value.page = page
    fetchData()
  }
}

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
    const res = await getDictById(id)
    const dict = res.data
    Object.assign(form, {
      dictType: dict.dictType,
      dictName: dict.dictName,
      description: dict.description || '',
      status: String(dict.status ?? 1)
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取字典详情失败'
    toast.error(message)
    dialogOpen.value = false
  } finally {
    dialogLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.dictType.trim()) {
    toast.warning('请输入字典类型')
    return
  }
  if (!form.dictName.trim()) {
    toast.warning('请输入字典名称')
    return
  }

  dialogLoading.value = true
  try {
    if (isEdit.value) {
      await updateDict(editId.value, {
        dictName: form.dictName.trim(),
        status: Number(form.status),
        description: form.remark.trim() || undefined
      })
      if (Number(form.status) !== (tableData.value.records.find((item) => item.id === editId.value)?.status ?? Number(form.status))) {
        await updateDictStatus(editId.value, Number(form.status))
      }
      toast.success('字典更新成功')
    } else {
      await createDict({
        dictType: form.dictType.trim(),
        dictName: form.dictName.trim(),
        description: form.remark.trim() || undefined
      })
      toast.success('字典创建成功')
    }
    dialogOpen.value = false
    fetchData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '保存字典失败'
    toast.error(message)
  } finally {
    dialogLoading.value = false
  }
}

const handleStatusToggle = async (dict: Dict) => {
  try {
    await updateDictStatus(dict.id, dict.status === 1 ? 0 : 1)
    toast.success('状态更新成功')
    fetchData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '状态更新失败'
    toast.error(message)
  }
}

const handleDeleteConfirm = (id: string) => {
  deleteDictId.value = id
  deleteDialogOpen.value = true
}

const handleDelete = async () => {
  deleteLoading.value = true
  try {
    await deleteDict(deleteDictId.value)
    toast.success('字典删除成功')
    fetchData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '删除字典失败'
    toast.error(message)
  } finally {
    deleteLoading.value = false
    deleteDialogOpen.value = false
  }
}

const fetchDictItems = async (dictId: string) => {
  itemLoading.value = true
  try {
    const res = await getDictItemList(dictId)
    itemTableData.value = res.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取字典项失败'
    toast.error(message)
  } finally {
    itemLoading.value = false
  }
}

const openItemDialog = async (dict: Dict) => {
  activeDict.value = dict
  itemDialogOpen.value = true
  await fetchDictItems(dict.id)
}

const itemParentOptions = computed(() => {
  return itemTableData.value.records
    .filter((item) => !isEditItem.value || item.id !== editItemId.value)
    .map((item) => ({
      id: item.id,
      label: item.label
    }))
})

const itemLabelMap = computed(() => {
  return itemTableData.value.records.reduce<Record<string, string>>((acc, item) => {
    acc[item.id] = item.label
    return acc
  }, {})
})

const openAddItemDialog = () => {
  resetItemForm()
  isEditItem.value = false
  editItemId.value = ''
  itemFormDialogOpen.value = true
}

const openAddChildItemDialog = (parentItem: DictItem) => {
  resetItemForm()
  itemForm.parentId = parentItem.id
  isEditItem.value = false
  editItemId.value = ''
  itemFormDialogOpen.value = true
}

const handleEditItem = async (id: string) => {
  if (!activeDict.value) return
  isEditItem.value = true
  editItemId.value = id
  itemFormLoading.value = true
  itemFormDialogOpen.value = true
  try {
    const res = await getDictItemById(activeDict.value.id, id)
    const item = res.data
    Object.assign(itemForm, {
      parentId: item.parentId || '0',
      label: item.label,
      value: item.value,
      sortOrder: item.sortOrder,
      status: String(item.status ?? 1),
      remark: item.remark || ''
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取字典项详情失败'
    toast.error(message)
    itemFormDialogOpen.value = false
  } finally {
    itemFormLoading.value = false
  }
}

const handleSubmitItem = async () => {
  if (!activeDict.value) return
  if (!itemForm.label.trim()) {
    toast.warning('请输入字典项标签')
    return
  }
  if (!itemForm.value.trim()) {
    toast.warning('请输入字典项值')
    return
  }

  itemFormLoading.value = true
  try {
    const payload = {
      parentId: itemForm.parentId === '0' ? undefined : itemForm.parentId,
      label: itemForm.label.trim(),
      value: itemForm.value.trim(),
      sortOrder: Number(itemForm.sortOrder) || 0,
      status: Number(itemForm.status),
      remark: itemForm.remark.trim() || undefined
    }
    if (isEditItem.value) {
      await updateDictItem(activeDict.value.id, editItemId.value, payload)
      toast.success('字典项更新成功')
    } else {
      await createDictItem(activeDict.value.id, payload)
      toast.success('字典项创建成功')
    }
    itemFormDialogOpen.value = false
    fetchDictItems(activeDict.value.id)
  } catch (error) {
    const message = error instanceof Error ? error.message : '保存字典项失败'
    toast.error(message)
  } finally {
    itemFormLoading.value = false
  }
}

const handleDeleteItemConfirm = (id: string) => {
  deleteItemId.value = id
  deleteItemDialogOpen.value = true
}

const handleDeleteItem = async () => {
  if (!activeDict.value) return
  deleteItemLoading.value = true
  try {
    await deleteDictItem(activeDict.value.id, deleteItemId.value)
    toast.success('字典项删除成功')
    fetchDictItems(activeDict.value.id)
  } catch (error) {
    const message = error instanceof Error ? error.message : '删除字典项失败'
    toast.error(message)
  } finally {
    deleteItemLoading.value = false
    deleteItemDialogOpen.value = false
  }
}

const handleToggleItemStatus = async (item: DictItem) => {
  if (!activeDict.value) return
  try {
    await updateDictItemStatus(activeDict.value.id, item.id, item.status === 1 ? 0 : 1)
    toast.success('字典项状态更新成功')
    fetchDictItems(activeDict.value.id)
  } catch (error) {
    const message = error instanceof Error ? error.message : '字典项状态更新失败'
    toast.error(message)
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="space-y-4">
    <Card>
      <CardContent class="p-4">
        <div class="flex gap-4 items-center">
          <Input v-model="searchQuery" placeholder="搜索字典名称/类型" class="w-72" @keyup.enter="handleSearch" />
          <Button @click="handleSearch">
            <Search class="w-4 h-4 mr-2" />
            搜索
          </Button>
          <Button variant="outline" @click="searchQuery = ''; handleSearch()">重置</Button>
          <div class="flex-1" />
          <Button v-if="canAddDict" @click="handleAdd">
            <Plus class="w-4 h-4 mr-2" />
            新增字典
          </Button>
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardContent class="p-0">
        <table class="w-full">
          <thead class="bg-muted/50 border-b">
            <tr>
              <th class="text-left p-4 font-medium">ID</th>
              <th class="text-left p-4 font-medium">字典名称</th>
              <th class="text-left p-4 font-medium">字典类型</th>
              <th class="text-left p-4 font-medium">备注</th>
              <th class="text-left p-4 font-medium">状态</th>
              <th class="text-left p-4 font-medium">创建时间</th>
              <th class="text-left p-4 font-medium">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-if="loading">
              <td colspan="7" class="p-8 text-center text-muted-foreground">加载中...</td>
            </tr>
            <tr v-else-if="tableData.records.length === 0">
              <td colspan="7" class="p-8 text-center text-muted-foreground">暂无数据</td>
            </tr>
            <tr v-for="dict in tableData.records" :key="dict.id" class="hover:bg-muted/30">
              <td class="p-4 text-muted-foreground">{{ dict.id }}</td>
              <td class="p-4 font-medium">{{ dict.dictName }}</td>
              <td class="p-4"><code class="bg-muted px-2 py-0.5 rounded text-sm">{{ dict.dictType }}</code></td>
              <td class="p-4 text-muted-foreground">{{ getDictRemark(dict) || '-' }}</td>
              <td class="p-4">
                <Badge
                  :variant="dict.status === 1 ? 'default' : 'destructive'"
                  :class="canEditDict ? 'cursor-pointer hover:opacity-80 transition-opacity' : ''"
                  @click="canEditDict && handleStatusToggle(dict)"
                >
                  {{ dict.status === 1 ? '正常' : '禁用' }}
                </Badge>
              </td>
              <td class="p-4 text-muted-foreground">{{ dict.createTime }}</td>
              <td class="p-4">
                <div class="flex gap-2">
                  <Button v-if="canListDictItems" size="sm" variant="ghost" @click="openItemDialog(dict)">
                    <ListTree class="w-4 h-4" />
                  </Button>
                  <Button v-if="canEditDict" size="sm" variant="ghost" @click="handleEdit(dict.id)">
                    <Edit class="w-4 h-4" />
                  </Button>
                  <Button v-if="canDeleteDict" size="sm" variant="ghost" class="text-destructive" @click="handleDeleteConfirm(dict.id)">
                    <Trash2 class="w-4 h-4" />
                  </Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="flex items-center justify-between px-4 py-4 border-t">
          <p class="text-sm text-muted-foreground">
            共 <span class="font-medium">{{ tableData.total }}</span> 条记录，
            第 <span class="font-medium">{{ tableData.page }}</span> / <span class="font-medium">{{ totalPages }}</span> 页
          </p>
          <div class="flex items-center gap-1">
            <Button variant="outline" size="icon" :disabled="tableData.page === 1" @click="goToPage(tableData.page - 1)">
              <ChevronLeft class="w-4 h-4" />
            </Button>
            <template v-for="(page, index) in visiblePages" :key="index">
              <span v-if="page === '...'" class="px-2 text-muted-foreground">...</span>
              <Button
                v-else
                :variant="page === tableData.page ? 'default' : 'outline'"
                size="icon"
                @click="goToPage(page as number)"
              >
                {{ page }}
              </Button>
            </template>
            <Button
              variant="outline"
              size="icon"
              :disabled="tableData.page >= totalPages"
              @click="goToPage(tableData.page + 1)"
            >
              <ChevronRight class="w-4 h-4" />
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>

    <Dialog v-if="canAddDict || canEditDict" v-model:open="dialogOpen">
      <DialogContent class="sm:max-w-[520px]">
        <DialogHeader>
          <DialogTitle>{{ isEdit ? '编辑字典' : '新增字典' }}</DialogTitle>
        </DialogHeader>
        <div v-if="dialogLoading" class="py-8 text-center text-muted-foreground">加载中...</div>
        <div v-else class="space-y-4 py-2">
          <div class="space-y-2">
            <Label>字典类型</Label>
            <Input v-model="form.dictType" :disabled="isEdit" placeholder="例如：user_status" />
          </div>
          <div class="space-y-2">
            <Label>字典名称</Label>
            <Input v-model="form.dictName" placeholder="请输入字典名称" />
          </div>
          <div class="space-y-2">
            <Label>备注</Label>
            <Textarea v-model="form.remark" placeholder="请输入备注" />
          </div>
          <div v-if="isEdit" class="space-y-2">
            <Label>状态</Label>
            <div class="flex gap-2">
              <Button :variant="form.status === '1' ? 'default' : 'outline'" @click="form.status = '1'">正常</Button>
              <Button :variant="form.status === '0' ? 'destructive' : 'outline'" @click="form.status = '0'">禁用</Button>
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="dialogOpen = false">取消</Button>
          <Button :disabled="dialogLoading" @click="handleSubmit">{{ isEdit ? '保存' : '创建' }}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <AlertDialog v-if="canDeleteDict" v-model:open="deleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认删除字典</AlertDialogTitle>
          <AlertDialogDescription>删除字典后将无法恢复，如果存在关联字典项，后端可能会拒绝删除。</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction :disabled="deleteLoading" @click="handleDelete">确认删除</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>

    <Dialog v-if="canListDictItems" v-model:open="itemDialogOpen">
      <DialogContent class="sm:max-w-4xl">
        <DialogHeader>
          <DialogTitle>字典项管理{{ activeDict ? ` - ${activeDict.dictName}` : '' }}</DialogTitle>
        </DialogHeader>
        <div class="space-y-4">
          <div class="flex justify-end">
            <Button v-if="canAddDictItem" @click="openAddItemDialog">
              <Plus class="w-4 h-4 mr-2" />
              新增字典项
            </Button>
          </div>

          <table class="w-full">
            <thead class="bg-muted/50 border-b">
              <tr>
                <th class="text-left p-4 font-medium">标签</th>
                <th class="text-left p-4 font-medium">值</th>
                <th class="text-left p-4 font-medium">父级</th>
                <th class="text-left p-4 font-medium">排序</th>
                <th class="text-left p-4 font-medium">备注</th>
                <th class="text-left p-4 font-medium">状态</th>
                <th class="text-left p-4 font-medium">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y">
              <tr v-if="itemLoading">
                <td colspan="7" class="p-8 text-center text-muted-foreground">加载中...</td>
              </tr>
              <tr v-else-if="itemTableData.records.length === 0">
                <td colspan="7" class="p-8 text-center text-muted-foreground">暂无字典项</td>
              </tr>
              <tr v-for="item in itemTableData.records" :key="item.id" class="hover:bg-muted/30">
                <td class="p-4 font-medium">{{ item.label }}</td>
                <td class="p-4"><code class="bg-muted px-2 py-0.5 rounded text-sm">{{ item.value }}</code></td>
                <td class="p-4 text-muted-foreground">
                  {{ getItemParentId(item) === '0' ? '顶级字典项' : itemLabelMap[item.parentId ?? '0'] || '-' }}
                </td>
                <td class="p-4">{{ item.sortOrder }}</td>
                <td class="p-4 text-muted-foreground">{{ item.remark || '-' }}</td>
                <td class="p-4">
                  <Badge
                    :variant="item.status === 1 ? 'default' : 'destructive'"
                    :class="canEditDictItem ? 'cursor-pointer hover:opacity-80 transition-opacity' : ''"
                    @click="canEditDictItem && handleToggleItemStatus(item)"
                  >
                    {{ item.status === 1 ? '正常' : '禁用' }}
                  </Badge>
                </td>
                <td class="p-4">
                  <div class="flex gap-2">
                    <Button v-if="canAddDictItem" size="sm" variant="ghost" @click="openAddChildItemDialog(item)">
                      <Plus class="w-4 h-4" />
                    </Button>
                    <Button v-if="canEditDictItem" size="sm" variant="ghost" @click="handleEditItem(item.id)">
                      <Edit class="w-4 h-4" />
                    </Button>
                    <Button v-if="canDeleteDictItem" size="sm" variant="ghost" class="text-destructive" @click="handleDeleteItemConfirm(item.id)">
                      <Trash2 class="w-4 h-4" />
                    </Button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </DialogContent>
    </Dialog>

    <Dialog v-if="canAddDictItem || canEditDictItem" v-model:open="itemFormDialogOpen">
      <DialogContent class="sm:max-w-[520px]">
        <DialogHeader>
          <DialogTitle>{{ isEditItem ? '编辑字典项' : '新增字典项' }}</DialogTitle>
        </DialogHeader>
        <div v-if="itemFormLoading" class="py-8 text-center text-muted-foreground">加载中...</div>
        <div v-else class="space-y-4 py-2">
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>父级字典项</Label>
              <Select v-model="itemForm.parentId">
                <SelectTrigger>
                  <SelectValue placeholder="选择父级字典项" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="0">顶级字典项</SelectItem>
                  <SelectItem v-for="item in itemParentOptions" :key="item.id" :value="item.id">
                    {{ item.label }}
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="space-y-2">
              <Label>状态</Label>
              <Select v-model="itemForm.status">
                <SelectTrigger>
                  <SelectValue placeholder="选择状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">正常</SelectItem>
                  <SelectItem value="0">禁用</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>标签</Label>
              <Input v-model="itemForm.label" placeholder="请输入字典项标签" />
            </div>
            <div class="space-y-2">
              <Label>值</Label>
              <Input v-model="itemForm.value" placeholder="请输入字典项值" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>排序</Label>
              <Input v-model="itemForm.sortOrder" type="number" placeholder="排序值" />
            </div>
            <div class="space-y-2">
              <Label>备注</Label>
              <Input v-model="itemForm.remark" placeholder="请输入备注" />
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="itemFormDialogOpen = false">取消</Button>
          <Button :disabled="itemFormLoading" @click="handleSubmitItem">{{ isEditItem ? '保存' : '创建' }}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <AlertDialog v-if="canDeleteDictItem" v-model:open="deleteItemDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认删除字典项</AlertDialogTitle>
          <AlertDialogDescription>删除后不可恢复，如果存在子项，后端可能会拒绝删除。</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction :disabled="deleteItemLoading" @click="handleDeleteItem">确认删除</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>
