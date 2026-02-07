<template>
  <div class="dict-item-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button
              type="default"
              :icon="ArrowLeft"
              @click="handleBack"
            >
              返回
            </el-button>
            <span class="header-title">字典项管理 - {{ dictName }}</span>
          </div>
          <el-button
            type="primary"
            @click="handleAdd"
          >
            <el-icon><Plus /></el-icon>
            新增字典项
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form
        :inline="true"
        :model="queryForm"
        class="search-form"
      >
        <el-form-item label="标签">
          <el-input
            v-model="queryForm.keyword"
            placeholder="请输入标签"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="handleSearch"
          >
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 - 树形结构 -->
      <el-table
        v-loading="loading"
        :data="tableData"
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
              @click="handleAddChild(row)"
            >
              新增子项
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="父节点"
          prop="parentId"
        >
          <el-tree-select
            v-model="form.parentId"
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
            v-model="form.label"
            placeholder="请输入字典标签"
          />
        </el-form-item>
        <el-form-item
          label="字典值"
          prop="value"
        >
          <el-input
            v-model="form.value"
            placeholder="请输入字典值"
          />
        </el-form-item>
        <el-form-item
          label="排序"
          prop="sortOrder"
        >
          <el-input-number
            v-model="form.sortOrder"
            :min="0"
          />
        </el-form-item>
        <el-form-item
          label="状态"
          prop="status"
        >
          <el-radio-group v-model="form.status">
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
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, Refresh, ArrowLeft } from '@element-plus/icons-vue'
import { getDictItemTree, createDictItem, updateDictItem, deleteDictItem, updateDictItemStatus } from '@/api/dict'
import { useRoute, useRouter } from 'vue-router'
import { useConfirm } from '@/composables/useConfirm'

const route = useRoute()
const router = useRouter()
const dictId = route.params.dictId
const dictName = route.query.dictName || '未知'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增字典项')
const isEdit = ref(false)
const tableData = ref([])

const queryForm = reactive({
  keyword: ''
})

const formRef = ref()
const form = reactive({
  id: null,
  parentId: null,
  label: '',
  value: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  label: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  value: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

// 确认操作
const confirmDelete = useConfirm({
  message: '确定要删除该字典项吗？',
  type: 'warning'
})

const confirmStatus = useConfirm({
  message: '确定要执行此操作吗？',
  type: 'warning'
})

// 用于选择的父节点树（不包含当前编辑的节点及其子节点）
const parentTreeData = computed(() => {
  return buildParentTreeData(tableData.value, isEdit.value ? form.id : null)
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

const getData = async () => {
  loading.value = true
  try {
    tableData.value = await getDictItemTree(dictId)
  } catch {
    ElMessage.error('获取字典项树失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 树形结构暂不支持搜索过滤，可以提示用户
  ElMessage.info('树形结构暂不支持搜索，请使用列表视图')
}

const handleReset = () => {
  queryForm.keyword = ''
  getData()
}

const handleBack = () => {
  router.back()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增字典项'
  dialogVisible.value = true
}

const handleAddChild = (row) => {
  isEdit.value = false
  dialogTitle.value = '新增子字典项'
  resetForm()
  form.parentId = row.id
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑字典项'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    parentId: null,
    label: '',
    value: '',
    sortOrder: 0,
    status: 1
  })
}

const handleSubmit = async () => {
  await formRef.value.validate()

  submitLoading.value = true
  try {
    const submitData = { ...form }
    if (!isEdit.value) {
      submitData.dictId = dictId
    }
    delete submitData.id
    delete submitData.children

    if (isEdit.value) {
      await updateDictItem(dictId, form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createDictItem(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    getData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  try {
    await confirmStatus(`确定要${action}该字典项吗？`)
    await updateDictItemStatus(dictId, row.id, newStatus)
    ElMessage.success(`${action}成功`)
    getData()
  } catch {
    // 取消操作
  }
}

const handleDelete = async (row) => {
  try {
    await confirmDelete()
    await deleteDictItem(dictId, row.id)
    ElMessage.success('删除成功')
    getData()
  } catch {
    // 取消操作
  }
}

onMounted(() => {
  getData()
})
</script>

<style scoped>
.dict-item-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
}

.search-form {
  margin-bottom: 20px;
}
</style>