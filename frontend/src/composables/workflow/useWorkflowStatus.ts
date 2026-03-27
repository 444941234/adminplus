type WorkflowStatusVariant = 'default' | 'secondary' | 'destructive' | 'outline'

const WORKFLOW_STATUS_LABELS: Record<string, string> = {
  DRAFT: '草稿',
  PENDING: '审批中',
  PROCESSING: '进行中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  CANCELLED: '已取消',
  WITHDRAWN: '已撤回',
  FINISHED: '已完成',
  COMPLETED: '已完成'
}

const TERMINAL_STATUSES = new Set(['APPROVED', 'REJECTED', 'CANCELLED', 'WITHDRAWN', 'FINISHED', 'COMPLETED'])

const WORKFLOW_STATUS_VARIANTS: Record<string, WorkflowStatusVariant> = {
  DRAFT: 'outline',
  PENDING: 'secondary',
  PROCESSING: 'default',
  APPROVED: 'default',
  REJECTED: 'destructive',
  CANCELLED: 'outline',
  WITHDRAWN: 'outline',
  FINISHED: 'default',
  COMPLETED: 'default'
}

/**
 * 获取流程状态展示文案
 * @param {string | null | undefined} status - 流程状态
 * @returns {string} 展示文案
 */
export const getWorkflowStatusLabel = (status?: string | null) => {
  return WORKFLOW_STATUS_LABELS[status || ''] || status || '-'
}

/**
 * 获取流程状态徽标样式
 * @param {string | null | undefined} status - 流程状态
 * @returns {WorkflowStatusVariant} 徽标样式
 */
export const getWorkflowStatusVariant = (status?: string | null): WorkflowStatusVariant => {
  return WORKFLOW_STATUS_VARIANTS[status || ''] || 'secondary'
}

/**
 * 判断流程是否为终态
 * @param {string | null | undefined} status - 流程状态
 * @returns {boolean} 是否终态
 */
export const isWorkflowTerminalStatus = (status?: string | null) => {
  return TERMINAL_STATUSES.has(status || '')
}

/**
 * 判断流程是否允许继续操作
 * @param {string | null | undefined} status - 流程状态
 * @returns {boolean} 是否允许继续操作
 */
export const canOperateWorkflow = (status?: string | null) => {
  return !isWorkflowTerminalStatus(status)
}
