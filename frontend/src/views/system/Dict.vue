<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { usePageList } from '@/composables/usePageList'
import {
  Button,
  Card,
  CardContent,
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
  Textarea
} from '@/components/ui'
import { Edit, ListTree, Plus, Trash2 } from '@lucide/vue'
import { ConfirmDialog, EmptyState, ListSearchBar, Pagination, StatusBadge } from '@/components/common'
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
import type { Dict, DictItem } from '@/types'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { formatDateTime } from '@/utils/format'

const { loading, searchQuery, tableData, fetchData, goToPage, handleSearch, handleReset } = usePageList<Dict>(
  (params) => getDictList(params),
  { page: 1, size: 10 }
)
const userStore = useUserStore()

const { loading: dialogLoading, run: runDialog } = useAsyncAction('操作失败')
const { loading: deleteLoading, run: runDelete } = useAsyncAction('删除字典失败')
const { loading: itemLoading, run: runItem } = useAsyncAction('操作失败')
const { loading: itemFormLoading, run: runItemForm } = useAsyncAction('操作失败')
const { loading: deleteItemLoading, run: runDeleteItem } = useAsyncAction('删除字典项失败')

const dialogOpen = ref(false)
const isEdit = ref(false)
const editId = ref('')

const deleteDialogOpen = ref(false)
const deleteDictId = ref('')

const itemDialogOpen = ref(false)
const activeDict = ref<Dict | null>(null)
const itemTableData = ref<DictItem[]>([])

const itemFormDialogOpen = ref(false)
const isEditItem = ref(false)
const editItemId = ref('')

const deleteItemDialogOpen = ref(false)
const deleteItemId = ref('')

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

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  editId.value = ''
  dialogOpen.value = true
}

const handleEdit = (id: string) => {
  isEdit.value = true
  editId.value = id
  dialogOpen.value = true
  runDialog(async () => {
    const res = await getDictById(id)
    const dict = res.data
    Object.assign(form, {
      dictType: dict.dictType,
      dictName: dict.dictName,
      description: dict.description || '',
      status: String(dict.status ?? 1)
    })
  }, {
    errorMessage: '获取字典详情失败',
    onError: () => { dialogOpen.value = false }
  })
}

const handleSubmit = () => {
  if (!form.dictType.trim()) {
    toast.warning('请输入字典类型')
    return
  }
  if (!form.dictName.trim()) {
    toast.warning('请输入字典名称')
    return
  }

  runDialog(async () => {
    if (isEdit.value) {
      await updateDict(editId.value, {
        dictName: form.dictName.trim(),
        status: Number(form.status),
        description: form.remark.trim() || undefined
      })
      if (Number(form.status) !== (tableData.records.find((item) => item.id === editId.value)?.status ?? Number(form.status))) {
        await updateDictStatus(editId.value, Number(form.status))
      }
    } else {
      await createDict({
        dictType: form.dictType.trim(),
        dictName: form.dictName.trim(),
        description: form.remark.trim() || undefined
      })
    }
  }, {
    successMessage: isEdit.value ? '字典更新成功' : '字典创建成功',
    errorMessage: '保存字典失败',
    onSuccess: () => {
      dialogOpen.value = false
      fetchData()
    }
  })
}

const handleStatusToggle = (dict: Dict) => {
  runDialog(async () => {
    await updateDictStatus(dict.id, dict.status === 1 ? 0 : 1)
  }, {
    successMessage: '状态更新成功',
    errorMessage: '状态更新失败',
    onSuccess: () => fetchData()
  })
}

const handleDeleteConfirm = (id: string) => {
  deleteDictId.value = id
  deleteDialogOpen.value = true
}

const handleDelete = () => {
  runDelete(async () => {
    await deleteDict(deleteDictId.value)
  }, {
    successMessage: '字典删除成功',
    onSuccess: () => fetchData()
  }).finally(() => {
    deleteDialogOpen.value = false
  })
}

const fetchDictItems = (dictId: string) => {
  runItem(async () => {
    const res = await getDictItemList(dictId)
    itemTableData.value = res.data
  }, { errorMessage: '获取字典项失败' })
}

const openItemDialog = async (dict: Dict) => {
  activeDict.value = dict
  itemDialogOpen.value = true
  await fetchDictItems(dict.id)
}

const itemParentOptions = computed(() => {
  return itemTableData.value
    .filter((item) => !isEditItem.value || item.id !== editItemId.value)
    .map((item) => ({
      id: item.id,
      label: item.label
    }))
})

const itemLabelMap = computed(() => {
  return itemTableData.value.reduce<Record<string, string>>((acc, item) => {
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

const handleEditItem = (id: string) => {
  if (!activeDict.value) return
  isEditItem.value = true
  editItemId.value = id
  itemFormDialogOpen.value = true
  runItemForm(async () => {
    const res = await getDictItemById(activeDict.value!.id, id)
    const item = res.data
    Object.assign(itemForm, {
      parentId: item.parentId || '0',
      label: item.label,
      value: item.value,
      sortOrder: item.sortOrder,
      status: String(item.status ?? 1),
      remark: item.remark || ''
    })
  }, {
    errorMessage: '获取字典项详情失败',
    onError: () => { itemFormDialogOpen.value = false }
  })
}

const handleSubmitItem = () => {
  if (!activeDict.value) return
  if (!itemForm.label.trim()) {
    toast.warning('请输入字典项标签')
    return
  }
  if (!itemForm.value.trim()) {
    toast.warning('请输入字典项值')
    return
  }

  runItemForm(async () => {
    const payload = {
      parentId: itemForm.parentId === '0' ? undefined : itemForm.parentId,
      label: itemForm.label.trim(),
      value: itemForm.value.trim(),
      sortOrder: Number(itemForm.sortOrder) || 0,
      status: Number(itemForm.status),
      remark: itemForm.remark.trim() || undefined
    }
    if (isEditItem.value) {
      await updateDictItem(activeDict.value!.id, editItemId.value, payload)
    } else {
      await createDictItem(activeDict.value!.id, payload)
    }
  }, {
    successMessage: isEditItem.value ? '字典项更新成功' : '字典项创建成功',
    errorMessage: '保存字典项失败',
    onSuccess: () => {
      itemFormDialogOpen.value = false
      fetchDictItems(activeDict.value!.id)
    }
  })
}

const handleDeleteItemConfirm = (id: string) => {
  deleteItemId.value = id
  deleteItemDialogOpen.value = true
}

const handleDeleteItem = () => {
  if (!activeDict.value) return
  runDeleteItem(async () => {
    await deleteDictItem(activeDict.value!.id, deleteItemId.value)
  }, {
    successMessage: '字典项删除成功',
    onSuccess: () => fetchDictItems(activeDict.value!.id)
  }).finally(() => {
    deleteItemDialogOpen.value = false
  })
}

const handleToggleItemStatus = (item: DictItem) => {
  if (!activeDict.value) return
  runItem(async () => {
    await updateDictItemStatus(activeDict.value!.id, item.id, item.status === 1 ? 0 : 1)
  }, {
    successMessage: '字典项状态更新成功',
    errorMessage: '字典项状态更新失败',
    onSuccess: () => fetchDictItems(activeDict.value!.id)
  })
}

onMounted(fetchData)
</script>

<template>
  <div class="space-y-4">
    <ListSearchBar
      v-model="searchQuery"
      placeholder="搜索字典名称/类型"
      :loading="loading"
      @search="handleSearch"
      @reset="handleReset"
    >
      <template #actions>
        <Button
          v-if="canAddDict"
          @click="handleAdd"
        >
          <Plus class="w-4 h-4 mr-2" />
          新增字典
        </Button>
      </template>
    </ListSearchBar>

    <Card>
      <CardContent class="p-0">
        <table class="w-full">
          <thead class="bg-muted/50 border-b">
            <tr>
              <th class="text-left p-4 font-medium">
                字典名称
              </th>
              <th class="text-left p-4 font-medium">
                字典类型
              </th>
              <th class="text-left p-4 font-medium">
                备注
              </th>
              <th class="text-left p-4 font-medium">
                状态
              </th>
              <th class="text-center p-4 font-medium">
                创建时间
              </th>
              <th class="text-center p-4 font-medium">
                操作
              </th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-if="loading">
              <td
                colspan="6"
                class="p-8 text-center text-muted-foreground"
              >
                加载中...
              </td>
            </tr>
            <tr v-else-if="tableData.records.length === 0">
              <td
                colspan="6"
                class="p-0"
              >
                <EmptyState
                  type="dicts"
                  :show-action="canAddDict"
                  action-text="添加字典"
                  @action="handleAdd"
                >
                  <template #action-icon>
                    <Plus class="mr-2 h-4 w-4" />
                  </template>
                </EmptyState>
              </td>
            </tr>
            <tr
              v-for="dict in tableData.records"
              :key="dict.id"
              class="hover:bg-muted/30"
            >
              <td class="p-4 font-medium">
                {{ dict.dictName }}
              </td>
              <td class="p-4">
                <code class="bg-muted px-2 py-0.5 rounded text-sm">{{ dict.dictType }}</code>
              </td>
              <td class="p-4 text-muted-foreground">
                {{ getDictRemark(dict) || '-' }}
              </td>
              <td class="p-4">
                <StatusBadge
                  :status="dict.status"
                  :clickable="canEditDict"
                  @toggle="handleStatusToggle(dict)"
                />
              </td>
              <td class="p-4 text-muted-foreground text-sm">
                {{ formatDateTime(dict.createTime) }}
              </td>
              <td class="p-4 text-center">
                <div class="flex justify-center gap-2">
                  <Button
                    v-if="canListDictItems"
                    size="sm"
                    variant="ghost"
                    @click="openItemDialog(dict)"
                  >
                    <ListTree class="w-4 h-4" />
                  </Button>
                  <Button
                    v-if="canEditDict"
                    size="sm"
                    variant="ghost"
                    @click="handleEdit(dict.id)"
                  >
                    <Edit class="w-4 h-4" />
                  </Button>
                  <Button
                    v-if="canDeleteDict"
                    size="sm"
                    variant="ghost"
                    class="text-destructive"
                    @click="handleDeleteConfirm(dict.id)"
                  >
                    <Trash2 class="w-4 h-4" />
                  </Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <Pagination
          :current="tableData.page"
          :total="tableData.total"
          :page-size="tableData.size"
          @change="goToPage"
        />
      </CardContent>
    </Card>

    <Dialog
      v-if="canAddDict || canEditDict"
      v-model:open="dialogOpen"
    >
      <DialogContent class="sm:max-w-[520px]">
        <DialogHeader>
          <DialogTitle>{{ isEdit ? '编辑字典' : '新增字典' }}</DialogTitle>
          <DialogDescription>配置字典类型和名称</DialogDescription>
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
          <div class="space-y-2">
            <Label>字典类型</Label>
            <Input
              v-model="form.dictType"
              :disabled="isEdit"
              placeholder="例如：user_status"
            />
          </div>
          <div class="space-y-2">
            <Label>字典名称</Label>
            <Input
              v-model="form.dictName"
              placeholder="请输入字典名称"
            />
          </div>
          <div class="space-y-2">
            <Label>备注</Label>
            <Textarea
              v-model="form.remark"
              placeholder="请输入备注"
            />
          </div>
          <div
            v-if="isEdit"
            class="space-y-2"
          >
            <Label>状态</Label>
            <div class="flex gap-2">
              <Button
                :variant="form.status === '1' ? 'default' : 'outline'"
                @click="form.status = '1'"
              >
                正常
              </Button>
              <Button
                :variant="form.status === '0' ? 'destructive' : 'outline'"
                @click="form.status = '0'"
              >
                禁用
              </Button>
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
      v-if="canDeleteDict"
      v-model:open="deleteDialogOpen"
      title="确认删除字典"
      description="删除字典后将无法恢复，如果存在关联字典项，后端可能会拒绝删除。"
      :loading="deleteLoading"
      @confirm="handleDelete"
    />

    <Dialog
      v-if="canListDictItems"
      v-model:open="itemDialogOpen"
    >
      <DialogContent class="sm:max-w-4xl">
        <DialogHeader>
          <DialogTitle>字典项管理{{ activeDict ? ` - ${activeDict.dictName}` : '' }}</DialogTitle>
          <DialogDescription>管理字典项列表</DialogDescription>
        </DialogHeader>
        <div class="space-y-4">
          <div class="flex justify-end">
            <Button
              v-if="canAddDictItem"
              @click="openAddItemDialog"
            >
              <Plus class="w-4 h-4 mr-2" />
              新增字典项
            </Button>
          </div>

          <table class="w-full">
            <thead class="bg-muted/50 border-b">
              <tr>
                <th class="text-left p-4 font-medium">
                  标签
                </th>
                <th class="text-left p-4 font-medium">
                  值
                </th>
                <th class="text-left p-4 font-medium">
                  父级
                </th>
                <th class="text-left p-4 font-medium">
                  排序
                </th>
                <th class="text-left p-4 font-medium">
                  备注
                </th>
                <th class="text-left p-4 font-medium">
                  状态
                </th>
                <th class="text-left p-4 font-medium">
                  操作
                </th>
              </tr>
            </thead>
            <tbody class="divide-y">
              <tr v-if="itemLoading">
                <td
                  colspan="7"
                  class="p-8 text-center text-muted-foreground"
                >
                  加载中...
                </td>
              </tr>
              <tr v-else-if="itemTableData.length === 0">
                <td
                  colspan="7"
                  class="p-8 text-center text-muted-foreground"
                >
                  暂无字典项
                </td>
              </tr>
              <tr
                v-for="item in itemTableData"
                :key="item.id"
                class="hover:bg-muted/30"
              >
                <td class="p-4 font-medium">
                  {{ item.label }}
                </td>
                <td class="p-4">
                  <code class="bg-muted px-2 py-0.5 rounded text-sm">{{ item.value }}</code>
                </td>
                <td class="p-4 text-muted-foreground">
                  {{ getItemParentId(item) === '0' ? '顶级字典项' : itemLabelMap[item.parentId ?? '0'] || '-' }}
                </td>
                <td class="p-4">
                  {{ item.sortOrder }}
                </td>
                <td class="p-4 text-muted-foreground">
                  {{ item.remark || '-' }}
                </td>
                <td class="p-4">
                  <StatusBadge
                    :status="item.status"
                    :clickable="canEditDictItem"
                    @toggle="handleToggleItemStatus(item)"
                  />
                </td>
                <td class="p-4">
                  <div class="flex gap-2">
                    <Button
                      v-if="canAddDictItem"
                      size="sm"
                      variant="ghost"
                      @click="openAddChildItemDialog(item)"
                    >
                      <Plus class="w-4 h-4" />
                    </Button>
                    <Button
                      v-if="canEditDictItem"
                      size="sm"
                      variant="ghost"
                      @click="handleEditItem(item.id)"
                    >
                      <Edit class="w-4 h-4" />
                    </Button>
                    <Button
                      v-if="canDeleteDictItem"
                      size="sm"
                      variant="ghost"
                      class="text-destructive"
                      @click="handleDeleteItemConfirm(item.id)"
                    >
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

    <Dialog
      v-if="canAddDictItem || canEditDictItem"
      v-model:open="itemFormDialogOpen"
    >
      <DialogContent class="sm:max-w-[520px]">
        <DialogHeader>
          <DialogTitle>{{ isEditItem ? '编辑字典项' : '新增字典项' }}</DialogTitle>
          <DialogDescription>配置字典项的标签、值和排序</DialogDescription>
        </DialogHeader>
        <div
          v-if="itemFormLoading"
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
              <Label>父级字典项</Label>
              <Select v-model="itemForm.parentId">
                <SelectTrigger>
                  <SelectValue placeholder="选择父级字典项" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="0">
                    顶级字典项
                  </SelectItem>
                  <SelectItem
                    v-for="item in itemParentOptions"
                    :key="item.id"
                    :value="item.id"
                  >
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
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>标签</Label>
              <Input
                v-model="itemForm.label"
                placeholder="请输入字典项标签"
              />
            </div>
            <div class="space-y-2">
              <Label>值</Label>
              <Input
                v-model="itemForm.value"
                placeholder="请输入字典项值"
              />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>排序</Label>
              <Input
                v-model="itemForm.sortOrder"
                type="number"
                placeholder="排序值"
              />
            </div>
            <div class="space-y-2">
              <Label>备注</Label>
              <Input
                v-model="itemForm.remark"
                placeholder="请输入备注"
              />
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            @click="itemFormDialogOpen = false"
          >
            取消
          </Button>
          <Button
            :disabled="itemFormLoading"
            @click="handleSubmitItem"
          >
            {{ isEditItem ? '保存' : '创建' }}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <ConfirmDialog
      v-if="canDeleteDictItem"
      v-model:open="deleteItemDialogOpen"
      title="确认删除字典项"
      description="删除后不可恢复，如果存在子项，后端可能会拒绝删除。"
      :loading="deleteItemLoading"
      @confirm="handleDeleteItem"
    />
  </div>
</template>
