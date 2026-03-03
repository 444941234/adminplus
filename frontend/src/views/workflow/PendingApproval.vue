<template>
  <div class="pending-approval-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h3>
              待我审批
              <el-badge v-if="pendingCount > 0" :value="pendingCount" class="badge" />
            </h3>
          </div>
          <div class="header-actions">
            <el-button @click="handleRefresh" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <div v-loading="loading" class="table-container">
        <el-empty v-if="tableData.length === 0 && !loading" description="暂无待审批流程" />

        <el-table v-else :data="tableData" stripe border>
          <el-table-column prop="title" label="流程标题" min-width="200" />
          <el-table-column prop="definitionName" label="流程类型" width="150" />
          <el-table-column prop="userName" label="发起人" width="120" />
          <el-table-column prop="currentNodeName" label="当前节点" width="120" />
          <el-table-column prop="submitTime" label="提交时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.submitTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="success" @click="handleApprove(row)">同意</el-button>
              <el-button link type="danger" @click="handleReject(row)">拒绝</el-button>
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="showApprovalDialog"
      :title="approvalTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="approvalForm" :rules="approvalRules" ref="approvalFormRef" label-width="100px">
        <el-form-item label="审批意见" prop="comment">
          <el-input
            v-model="approvalForm.comment"
            type="textarea"
            :rows="4"
            :placeholder="approvalPlaceholder"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApprovalDialog = false">取消</el-button>
        <el-button :type="approvalType === 'approve' ? 'success' : 'danger'" @click="handleConfirmApproval" :loading="approving">
          {{ approvalType === 'approve' ? '同意' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <WorkflowDetail
      v-model:visible="showDetailDialog"
      :instance-id="currentInstanceId"
      @refresh="handleRefresh"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getPendingApprovals, countPendingApprovals, approveWorkflow, rejectWorkflow } from '@/api/workflow'
import WorkflowDetail from './WorkflowDetail.vue'

const loading = ref(false)
const approving = ref(false)
const tableData = ref([])
const pendingCount = ref(0)

const showApprovalDialog = ref(false)
const showDetailDialog = ref(false)
const currentInstanceId = ref('')
const approvalType = ref('approve')

const approvalForm = reactive({
  comment: ''
})

const approvalRules = {
  comment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}

const approvalFormRef = ref(null)

const approvalTitle = ref('')
const approvalPlaceholder = ref('')

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getPendingApprovals()
    tableData.value = res.data || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载待审批数量
const loadCount = async () => {
  try {
    const res = await countPendingApprovals()
    pendingCount.value = res.data || 0
  } catch (error) {
    console.error('加载待审批数量失败', error)
  }
}

// 刷新
const handleRefresh = () => {
  loadData()
  loadCount()
}

// 查看详情
const handleView = (row) => {
  currentInstanceId.value = row.id
  showDetailDialog.value = true
}

// 同意
const handleApprove = (row) => {
  currentInstanceId.value = row.id
  approvalType.value = 'approve'
  approvalTitle.value = '同意审批'
  approvalPlaceholder.value = '请输入同意理由（可选）'
  approvalForm.comment = '同意'
  showApprovalDialog.value = true
}

// 拒绝
const handleReject = (row) => {
  currentInstanceId.value = row.id
  approvalType.value = 'reject'
  approvalTitle.value = '拒绝审批'
  approvalPlaceholder.value = '请输入拒绝原因（必填）'
  approvalForm.comment = ''
  showApprovalDialog.value = true
}

// 确认审批
const handleConfirmApproval = async () => {
  await approvalFormRef.value.validate()
  approving.value = true
  try {
    if (approvalType.value === 'approve') {
      await approveWorkflow(currentInstanceId.value, {
        comment: approvalForm.comment
      })
      ElMessage.success('已同意')
    } else {
      await rejectWorkflow(currentInstanceId.value, {
        comment: approvalForm.comment
      })
      ElMessage.success('已拒绝')
    }
    showApprovalDialog.value = false
    approvalForm.comment = ''
    handleRefresh()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    approving.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadData()
  loadCount()
})
</script>

<style scoped>
.pending-approval-page {
  padding: 20px;
  min-height: calc(100vh - 84px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 12px;
}

.badge :deep(.el-badge__content) {
  transform: translateY(-50%);
}

.table-container {
  min-height: 300px;
}
</style>
