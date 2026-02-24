<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  getDictList,
  createDict,
  updateDict,
  deleteDict,
  updateDictStatus
} from '@/api/dict'
import {
  getDictItemTree,
  createDictItem,
  updateDictItem,
  deleteDictItem,
  updateDictItemStatus
} from '@/api/dict'
import { debounce } from '@/utils/debounce'
import { useConfirm } from '@/composables/useConfirm'

// ============ 字典管理 ============
const dictList = ref([])
const dictLoading = ref(false)
const selectedDictId = ref(null)
const selectedDict = ref(null)

// 字典查询参数
const dictQueryParams = reactive({
  page: 1,
  size: 100, // 左侧列表一次加载更多
  keyword: ''
})

const dictTotal = ref(0)

// 字典表单
const dictDialogVisible = ref(false)
const dictDialogTitle = ref('')
const dictFormData = reactive({
  id: null,
  dictType: '',
  dictName: '',
  status: 1,
  remark: ''
})

const dictFormRef = ref(null)
const dictRules = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }]
}

// 确认操作
const confirmDictDelete = useConfirm({
  message: '确定要删除该字典吗？删除后字典项也会被删除！',
  type: 'warning'
})

const confirmDictStatus = useConfirm({
  message: '确定要执行此操作吗？',
  type: 'warning'
})

// 获取字典列表
const getDictListData = async () => {
  dictLoading.value = true
  try {
    const data = await getDictList(dictQueryParams)
    dictList.value = data.records || []
    dictTotal.value = data.total || 0

    // 如果没有选中的字典，默认选中第一个
    if (!selectedDictId.value && dictList.value.length > 0) {
      selectDict(dictList.value[0])
    }
  } catch {
    ElMessage.error('获取字典列表失败')
  } finally {
    dictLoading.value = false
  }
}

// 搜索（使用防抖）
const searchDebounced = debounce(() => {
  dictQueryParams.page = 1
  getDictListData()
}, 300)

const handleDictSearch = () => {
  searchDebounced()
}

// 选择字典
const selectDict = (dict) => {
  selectedDictId.value = dict.id
  selectedDict.value = dict
  // 加载字典项
  getDictItemData()
}

// 新增字典
const handleDictAdd = () => {
  dictDialogTitle.value = '新增字典'
  Object.assign(dictFormData, {
    id: null,
    dictType: '',
    dictName: '',
    status: 1,
    remark: ''
  })
  dictDialogVisible.value = true
}

// 编辑字典
const handleDictEdit = (row) => {
  dictDialogTitle.value = '编辑字典'
  Object.assign(dictFormData, {
    id: row.id,
    dictType: row.dictType,
    dictName: row.dictName,
    status: row.status,
    remark: row.remark
  })
  dictDialogVisible.value = true
}

// 提交字典
const handleDictSubmit = async () => {
  try {
    await dictFormRef.value.validate()
    if (dictFormData.id) {
      await updateDict(dictFormData.id, {
        dictName: dictFormData.dictName,
        status: dictFormData.status,
        remark: dictFormData.remark
      })
      ElMessage.success('更新成功')
    } else {
      await createDict(dictFormData)
      ElMessage.success('创建成功')
    }
    dictDialogVisible.value = false
    getDictListData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  }
}

// 删除字典
const handleDictDelete = async (row) => {
  try {
    await confirmDictDelete()
    await deleteDict(row.id)
    ElMessage.success('删除成功')
    if (selectedDictId.value === row.id) {
      selectedDictId.value = null
      selectedDict.value = null
      dictItemList.value = []
    }
    getDictListData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDictStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  try {
    await confirmDictStatus(`确定要${action}该字典吗？`)
    await updateDictStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    getDictListData()
  } catch {
    // 取消操作
  }
}

// ============ 字典项管理 ============
const dictItemList = ref([])
const itemLoading = ref(false)
const itemDialogVisible = ref(false)
const itemDialogTitle = ref('新增字典项')
const isItemEdit = ref(false)
const submitLoading = ref(false)

const itemQueryForm = reactive({
  keyword: ''
})

const itemFormRef = ref()
const itemForm = reactive({
  id: null,
  parentId: null,
  label: '',
  value: '',
  sortOrder: 0,
  status: 1
})

const itemRules = {
  label: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  value: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

// 确认操作
const confirmItemDelete = useConfirm({
  message: '确定要删除该字典项吗？',
  type: 'warning'
})

const confirmItemStatus = useConfirm({
  message: '确定要执行此操作吗？',
  type: 'warning'
})

// 用于选择的父节点树
const parentTreeData = computed(() => {
  return buildParentTreeData(dictItemList.value, isItemEdit.value ? itemForm.id : null)
})

const buildParentTreeData = (items, excludeId) => {
  return items
    .filter(item => item.id !== excludeId)
    .map(item => ({
      id: item.id,
      label: item.label,
      children: item.children ? buildParentTreeData(item.children, excludeId) : undefined
    }))
}

// 获取字典项树
const getDictItemData = async () => {
  if (!selectedDictId.value) {
    dictItemList.value = []
    return
  }

  itemLoading.value = true
  try {
    dictItemList.value = await getDictItemTree(selectedDictId.value)
  } catch {
    ElMessage.error('获取字典项树失败')
  } finally {
    itemLoading.value = false
  }
}

const handleItemSearch = () => {
  // 树形结构暂不支持搜索过滤
  ElMessage.info('树形结构暂不支持搜索，请使用列表视图')
}

const handleItemReset = () => {
  itemQueryForm.keyword = ''
  getDictItemData()
}

const handleItemAdd = () => {
  if (!selectedDictId.value) {
    ElMessage.warning('请先选择字典')
    return
  }
  isItemEdit.value = false
  itemDialogTitle.value = '新增字典项'
  resetItemForm()
  itemDialogVisible.value = true
}

const handleItemAddChild = (row) => {
  isItemEdit.value = false
  itemDialogTitle.value = '新增子字典项'
  resetItemForm()
  itemForm.parentId = row.id
  itemDialogVisible.value = true
}

const handleItemEdit = (row) => {
  isItemEdit.value = true
  itemDialogTitle.value = '编辑字典项'
  Object.assign(itemForm, row)
  itemDialogVisible.value = true
}

const handleItemDialogClose = () => {
  itemFormRef.value?.resetFields()
  resetItemForm()
}

const resetItemForm = () => {
  Object.assign(itemForm, {
    id: null,
    parentId: null,
    label: '',
    value: '',
    sortOrder: 0,
    status: 1
  })
}

const handleItemSubmit = async () => {
  await itemFormRef.value.validate()

  submitLoading.value = true
  try {
    const submitData = { ...itemForm }
    if (!isItemEdit.value) {
      submitData.dictId = selectedDictId.value
    }
    delete submitData.id
    delete submitData.children

    if (isItemEdit.value) {
      await updateDictItem(selectedDictId.value, itemForm.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createDictItem(submitData)
      ElMessage.success('创建成功')
    }
    itemDialogVisible.value = false
    getDictItemData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleItemStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  try {
    await confirmItemStatus(`确定要${action}该字典项吗？`)
    await updateDictItemStatus(selectedDictId.value, row.id, newStatus)
    ElMessage.success(`${action}成功`)
    getDictItemData()
  } catch {
    // 取消操作
  }
}

const handleItemDelete = async (row) => {
  try {
    await confirmItemDelete()
    await deleteDictItem(selectedDictId.value, row.id)
    ElMessage.success('删除成功')
    getDictItemData()
  } catch {
    // 取消操作
  }
}

// 页面加载
onMounted(() => {
  getDictListData()
})
</script>

<template>
  <div class="dict-container">
    <el-row :gutter="20">
      <!-- 左侧：字典列表 (30%) -->
      <el-col
        :xs="24"
        :sm="24"
        :md="8"
        :lg="6"
        :xl="6"
      >
        <el-card class="dict-list-card">
          <template #header>
            <div class="card-header">
              <span>字典列表</span>
              <el-button
                type="primary"
                size="small"
                @click="handleDictAdd"
              >
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
            </div>
          </template>

          <!-- 搜索栏 -->
          <el-form
            :inline="false"
            :model="dictQueryParams"
            class="search-form"
          >
            <el-input
              v-model="dictQueryParams.keyword"
              placeholder="搜索字典类型/名称"
              clearable
              @input="handleDictSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form>

          <!-- 字典列表 -->
          <div class="dict-list-wrapper">
            <div
              v-for="dict in dictList"
              :key="dict.id"
              :class="['dict-item', { active: selectedDictId === dict.id }]"
              @click="selectDict(dict)"
            >
              <div class="dict-item-header">
                <span class="dict-type">{{ dict.dictType }}</span>
                <el-tag
                  :type="dict.status === 1 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ dict.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </div>
              <div class="dict-item-name">
                {{ dict.dictName }}
              </div>
              <div class="dict-item-actions">
                <el-button
                  type="primary"
                  size="small"
                  text
                  @click.stop="handleDictEdit(dict)"
                >
                  编辑
                </el-button>
                <el-button
                  type="warning"
                  size="small"
                  text
                  @click.stop="handleDictStatus(dict)"
                >
                  {{ dict.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click.stop="handleDictDelete(dict)"
                >
                  删除
                </el-button>
              </div>
            </div>

            <el-empty
              v-if="!dictLoading && dictList.length === 0"
              description="暂无字典"
            />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：字典项管理 (70%) -->
      <el-col
        :xs="24"
        :sm="24"
        :md="16"
        :lg="18"
        :xl="18"
      >
        <el-card class="dict-item-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span
                  v-if="selectedDict"
                  class="header-title"
                >
                  字典项管理 - {{ selectedDict.dictName }}
                  <el-tag
                    size="small"
                    style="margin-left: 8px"
                  >
                    {{ selectedDict.dictType }}
                  </el-tag>
                </span>
                <span
                  v-else
                  class="header-title"
                >字典项管理</span>
              </div>
              <el-button
                type="primary"
                :disabled="!selectedDictId"
                @click="handleItemAdd"
              >
                <el-icon><Plus /></el-icon>
                新增字典项
              </el-button>
            </div>
          </template>

          <div
            v-if="!selectedDictId"
            class="empty-state"
          >
            <el-empty description="请从左侧选择一个字典" />
          </div>

          <div v-else>
            <!-- 搜索表单 -->
            <el-form
              :inline="true"
              :model="itemQueryForm"
              class="search-form"
            >
              <el-form-item label="标签">
                <el-input
                  v-model="itemQueryForm.keyword"
                  placeholder="请输入标签"
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  @click="handleItemSearch"
                >
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="handleItemReset">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-form>

            <!-- 数据表格 - 树形结构 -->
            <el-table
              v-loading="itemLoading"
              :data="dictItemList"
              border
              row-key="id"
              :tree-props="{ children: 'children' }"
              default-expand-all
            >
              <el-table-column
                prop="id"
                label="ID"
                width="80"
              />
              <el-table-column
                prop="label"
                label="字典标签"
                width="200"
              />
              <el-table-column
                prop="value"
                label="字典值"
                width="150"
              />
              <el-table-column
                prop="sortOrder"
                label="排序"
                width="80"
              />
              <el-table-column
                label="状态"
                width="100"
              >
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                label="操作"
                width="280"
                fixed="right"
              >
                <template #default="{ row }">
                  <el-button
                    type="primary"
                    size="small"
                    @click="handleItemAddChild(row)"
                  >
                    新增子项
                  </el-button>
                  <el-button
                    type="warning"
                    size="small"
                    @click="handleItemEdit(row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="info"
                    size="small"
                    @click="handleItemStatus(row)"
                  >
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="handleItemDelete(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 字典新增/编辑对话框 -->
    <el-dialog
      v-model="dictDialogVisible"
      :title="dictDialogTitle"
      width="500px"
    >
      <el-form
        ref="dictFormRef"
        :model="dictFormData"
        :rules="dictRules"
        label-width="100px"
      >
        <el-form-item
          label="字典类型"
          prop="dictType"
        >
          <el-input
            v-model="dictFormData.dictType"
            placeholder="请输入字典类型"
            :disabled="!!dictFormData.id"
          />
        </el-form-item>
        <el-form-item
          label="字典名称"
          prop="dictName"
        >
          <el-input
            v-model="dictFormData.dictName"
            placeholder="请输入字典名称"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dictFormData.status">
            <el-radio :label="1">
              正常
            </el-radio>
            <el-radio :label="0">
              禁用
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="dictFormData.remark"
            type="textarea"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="handleDictSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 字典项新增/编辑对话框 -->
    <el-dialog
      v-model="itemDialogVisible"
      :title="itemDialogTitle"
      width="500px"
      @close="handleItemDialogClose"
    >
      <el-form
        ref="itemFormRef"
        :model="itemForm"
        :rules="itemRules"
        label-width="100px"
      >
        <el-form-item
          label="父节点"
          prop="parentId"
        >
          <el-tree-select
            v-model="itemForm.parentId"
            :data="parentTreeData"
            :props="{ label: 'label', value: 'id' }"
            placeholder="请选择父节点（不选则为顶级节点）"
            clearable
            check-strictly
            :render-after-expand="false"
          />
        </el-form-item>
        <el-form-item
          label="字典标签"
          prop="label"
        >
          <el-input
            v-model="itemForm.label"
            placeholder="请输入字典标签"
          />
        </el-form-item>
        <el-form-item
          label="字典值"
          prop="value"
        >
          <el-input
            v-model="itemForm.value"
            placeholder="请输入字典值"
          />
        </el-form-item>
        <el-form-item
          label="排序"
          prop="sortOrder"
        >
          <el-input-number
            v-model="itemForm.sortOrder"
            :min="0"
          />
        </el-form-item>
        <el-form-item
          label="状态"
          prop="status"
        >
          <el-radio-group v-model="itemForm.status">
            <el-radio :value="1">
              正常
            </el-radio>
            <el-radio :value="0">
              禁用
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleItemSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dict-container {
  padding: 20px;
  height: calc(100vh - 120px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
}

/* 左侧字典列表卡片 */
.dict-list-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.dict-list-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 16px;
}

.search-form {
  margin-bottom: 12px;
}

.dict-list-wrapper {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

/* 字典列表项 */
.dict-item {
  padding: 12px;
  margin-bottom: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.dict-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.dict-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.dict-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.dict-type {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.dict-item-name {
  color: #606266;
  font-size: 13px;
  margin-bottom: 8px;
}

.dict-item-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

/* 右侧字典项卡片 */
.dict-item-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.dict-item-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 滚动条样式 */
.dict-list-wrapper::-webkit-scrollbar {
  width: 6px;
}

.dict-list-wrapper::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.dict-list-wrapper::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .dict-container {
    height: auto;
  }

  .dict-list-card,
  .dict-item-card {
    height: auto;
    min-height: 500px;
  }

  .dict-list-wrapper {
    max-height: 400px;
  }
}
</style>