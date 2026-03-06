<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    title="流程详情"
    width="800px"
    :close-on-click-modal="false"
  >
    <div v-loading="loading" class="detail-container">
      <el-empty v-if="!detail && !loading" description="流程不存在" />

      <div v-else-if="detail">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="流程标题">{{ detail.instance.title }}</el-descriptions-item>
          <el-descriptions-item label="流程类型">{{ detail.instance.definitionName }}</el-descriptions-item>
          <el-descriptions-item label="发起人">{{ detail.instance.userName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="detail.instance.status === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else-if="detail.instance.status === 'running'" type="warning">进行中</el-tag>
            <el-tag v-else-if="detail.instance.status === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="detail.instance.status === 'rejected'" type="danger">已拒绝</el-tag>
            <el-tag v-else-if="detail.instance.status === 'cancelled'" type="info">已取消</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前节点">
            {{ detail.instance.currentNodeName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDate(detail.instance.submitTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="完成时间" v-if="detail.instance.finishTime">
            {{ formatDate(detail.instance.finishTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2" v-if="detail.instance.remark">
            {{ detail.instance.remark }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 审批进度 -->
        <div class="approval-progress">
          <h4>审批进度</h4>
          <el-steps :active="getCurrentStep()" align-center finish-status="success">
            <el-step
              v-for="node in detail.nodes"
              :key="node.id"
              :title="node.nodeName"
              :description="getNodeStatus(node.id)"
            />
          </el-steps>
        </div>

        <!-- 审批记录 -->
        <div class="approval-history">
          <h4>审批记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="approval in detail.approvals"
              :key="approval.id"
              :type="getTimelineType(approval.approvalStatus)"
              :icon="getTimelineIcon(approval.approvalStatus)"
            >
              <div class="timeline-item">
                <div class="timeline-header">
                  <span class="node-name">{{ approval.nodeName }}</span>
                  <el-tag
                    :type="getStatusType(approval.approvalStatus)"
                    size="small"
                  >
                    {{ getStatusText(approval.approvalStatus) }}
                  </el-tag>
                </div>
                <div class="timeline-content">
                  <span class="approver">{{ approval.approverName }}</span>
                  <span class="time">{{ formatDate(approval.approvalTime || approval.createTime) }}</span>
                </div>
                <div v-if="approval.comment" class="timeline-comment">
                  {{ approval.comment }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>

    <template #footer v-if="detail && detail.canApprove">
      <el-button @click="$emit('update:visible', false)">关闭</el-button>
      <el-button type="success" @click="handleApprove">同意</el-button>
      <el-button type="danger" @click="handleReject">拒绝</el-button>
    </template>
    <template #footer v-else>
      <el-button @click="$emit('update:visible', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from '@/utils/elementCompat'
import { getWorkflowDetail, approveWorkflow, rejectWorkflow } from '@/api/workflow'

const props = defineProps({
  visible: Boolean,
  instanceId: String
})

const emit = defineEmits(['update:visible', 'refresh'])

const loading = ref(false)
const detail = ref(null)

// 加载详情
const loadDetail = async () => {
  if (!props.instanceId) return

  loading.value = true
  try {
    const res = await getWorkflowDetail(props.instanceId)
    detail.value = res.data
  } catch (error) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

// 获取当前步骤
const getCurrentStep = () => {
  if (!detail.value) return 0

  const instance = detail.value.instance
  if (instance.status === 'approved') return detail.value.nodes.length
  if (instance.status === 'rejected' || instance.status === 'cancelled') return -1

  // 找到当前节点的索引
  const currentIndex = detail.value.nodes.findIndex(
    n => n.id === instance.currentNodeId
  )
  return currentIndex >= 0 ? currentIndex : 0
}

// 获取节点状态
const getNodeStatus = (nodeId) => {
  if (!detail.value) return ''

  const instance = detail.value.instance
  if (instance.status === 'draft') return '草稿'
  if (instance.status === 'approved') return '已完成'
  if (instance.status === 'rejected') return '已拒绝'
  if (instance.status === 'cancelled') return '已取消'

  // 检查节点是否已审批
  const approved = detail.value.approvals.some(
    a => a.nodeId === nodeId && a.approvalStatus === 'approved'
  )
  if (approved) return '已通过'

  // 检查节点是否被拒绝
  const rejected = detail.value.approvals.some(
    a => a.nodeId === nodeId && a.approvalStatus === 'rejected'
  )
  if (rejected) return '已拒绝'

  // 当前节点
  if (nodeId === instance.currentNodeId) return '待审批'

  return '等待中'
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    transferred: 'info',
    delegated: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    pending: '待审批',
    approved: '已同意',
    rejected: '已拒绝',
    transferred: '已转审',
    delegated: '已代理'
  }
  return texts[status] || status
}

// 获取时间线类型
const getTimelineType = (status) => {
  const types = {
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'primary'
}

// 获取时间线图标
const getTimelineIcon = (status) => {
  return undefined
}

// 同意
const handleApprove = async () => {
  try {
    await approveWorkflow(props.instanceId, { comment: '同意' })
    ElMessage.success('已同意')
    emit('refresh')
    emit('update:visible', false)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 拒绝
const handleReject = async () => {
  try {
    await rejectWorkflow(props.instanceId, { comment: '拒绝' })
    ElMessage.success('已拒绝')
    emit('refresh')
    emit('update:visible', false)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 监听对话框显示状态
watch(() => props.visible, (val) => {
  if (val) {
    loadDetail()
  }
})

// 监听实例ID变化
watch(() => props.instanceId, () => {
  if (props.visible) {
    loadDetail()
  }
})
</script>
