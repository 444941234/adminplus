<template>
  <div class="workflow-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h3>我的工作流</h3>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="showStartDialog = true">
              <el-icon><Plus /></el-icon>
              发起流程
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索表单 -->
      <div class="search-form">
        <el-form :inline="true">
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="全部" value="" />
              <el-option label="草稿" value="draft" />
              <el-option label="进行中" value="running" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
              <el-option label="已取消" value="cancelled" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
      >
        <el-table-column prop="title" label="流程标题" min-width="200" />
        <el-table-column prop="definitionName" label="流程类型" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else-if="row.status === 'running'" type="warning">进行中</el-tag>
            <el-tag v-else-if="row.status === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="row.status === 'rejected'" type="danger">已拒绝</el-tag>
            <el-tag v-else-if="row.status === 'cancelled'" type="info">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentNodeName" label="当前节点" width="120">
          <template #default="{ row }">
            {{ row.currentNodeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 'draft'" link type="primary" @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="row.status === 'draft' || row.status === 'rejected'" link type="warning" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'draft' || row.status === 'running'" link type="danger" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 发起流程对话框 -->
    <el-dialog
      v-model="showStartDialog"
      title="发起流程"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="startForm" :rules="startRules" ref="startFormRef" label-width="100px">
        <el-form-item label="流程类型" prop="definitionId">
          <el-select v-model="startForm.definitionId" placeholder="请选择流程类型" @change="handleDefinitionChange">
            <el-option
              v-for="def in definitions"
              :key="def.id"
              :label="def.definitionName"
              :value="def.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="流程标题" prop="title">
          <el-input v-model="startForm.title" placeholder="请输入流程标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="startForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStartDialog = false">取消</el-button>
        <el-button type="primary" @click="handleStart" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <WorkflowDetail
      v-model:visible="showDetailDialog"
      :instance-id="currentInstanceId"
      @refresh="handleQuery"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getMyWorkflows, startWorkflow, submitWorkflow, cancelWorkflow } from '@/api/workflow'
import { listEnabledWorkflowDefinitions } from '@/api/workflow'
import WorkflowDetail from './WorkflowDetail.vue'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const definitions = ref([])

const queryParams = reactive({
  status: '',
  page: 1,
  size: 10
})

const showStartDialog = ref(false)
const showDetailDialog = ref(false)
const currentInstanceId = ref('')

const startForm = reactive({
  definitionId: '',
  title: '',
  remark: ''
})

const startRules = {
  definitionId: [{ required: true, message: '请选择流程类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入流程标题', trigger: 'blur' }]
}

const startFormRef = ref(null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const status = queryParams.status || undefined
    const res = await getMyWorkflows(status)
    tableData.value = res.data || []
    total.value = tableData.value.length
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载流程定义
const loadDefinitions = async () => {
  try {
    const res = await listEnabledWorkflowDefinitions()
    definitions.value = res.data || []
  } catch (error) {
    ElMessage.error('加载流程类型失败')
  }
}

// 查询
const handleQuery = () => {
  queryParams.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryParams.status = ''
  queryParams.page = 1
  loadData()
}

// 查看详情
const handleView = (row) => {
  currentInstanceId.value = row.id
  showDetailDialog.value = true
}

// 提交流程
const handleSubmit = async (row) => {
  try {
    await ElMessageBox.confirm('确认提交此流程？', '提示', { type: 'warning' })
    await submitWorkflow(row.id)
    ElMessage.success('提交成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
    }
  }
}

// 编辑草稿
const handleEdit = (row) => {
  ElMessage.info('编辑功能待实现')
}

// 取消流程
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确认取消此流程？取消后无法恢复。', '提示', { type: 'warning' })
    await cancelWorkflow(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

// 流程定义变化
const handleDefinitionChange = (definitionId) => {
  const def = definitions.value.find(d => d.id === definitionId)
  if (def && !startForm.title) {
    startForm.title = def.definitionName
  }
}

// 发起流程
const handleStart = async () => {
  await startFormRef.value.validate()
  submitting.value = true
  try {
    await startWorkflow({
      definitionId: startForm.definitionId,
      title: startForm.title,
      remark: startForm.remark
    })
    ElMessage.success('发起成功')
    showStartDialog.value = false
    Object.assign(startForm, {
      definitionId: '',
      title: '',
      remark: ''
    })
    loadData()
  } catch (error) {
    ElMessage.error('发起失败')
  } finally {
    submitting.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadData()
  loadDefinitions()
})
</script>
