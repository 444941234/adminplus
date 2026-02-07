<template>
  <div class="dict-item-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>字典项管理 - {{ dictName }}</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增字典项
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="标签">
          <el-input v-model="queryForm.keyword" placeholder="请输入标签" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="label" label="字典标签" width="150" />
        <el-table-column prop="value" label="字典值" width="150" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="warning" size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getData"
        @current-change="getData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="字典标签" prop="label">
          <el-input v-model="form.label" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="value">
          <el-input v-model="form.value" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getDictItems, createDictItem, updateDictItem, deleteDictItem, updateDictItemStatus } from '@/api/dict'
import { useRoute } from 'vue-router'

const route = useRoute()
const dictId = route.params.dictId
const dictName = route.query.dictName || '未知'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增字典项')
const isEdit = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const formRef = ref()
const form = reactive({
  id: null,
  label: '',
  value: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  label: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  value: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

const getData = async () => {
  loading.value = true
  try {
    const data = await getDictItems(dictId)
    tableData.value = data.records || data
    total.value = data.total || 0
  } catch (error) {
    console.error('获取字典项列表失败:', error)
    ElMessage.error('获取字典项列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.page = 1
  getData()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.page = 1
  getData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增字典项'
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
  Object.assign(form, {
    id: null,
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
    if (isEdit.value) {
      await updateDictItem(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createDictItem(dictId, form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    getData()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定要${action}该字典项吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateDictItemStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    getData()
  } catch (error) {
    // 取消操作
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该字典项吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDictItem(row.id)
    ElMessage.success('删除成功')
    getData()
  } catch (error) {
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

.search-form {
  margin-bottom: 20px;
}
</style>