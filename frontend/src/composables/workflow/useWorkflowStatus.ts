import { useDict } from '../useDict'

// 单例模式，只初始化一次字典
let workflowStatusDict: ReturnType<typeof useDict> | null = null

const getWorkflowStatusDict = () => {
  if (!workflowStatusDict) {
    workflowStatusDict = useDict('workflow_status')
  }
  return workflowStatusDict
}

// 英文状态码到数字值的映射
const STATUS_CODE_MAP: Record<string, string> = {
  'DRAFT': '0',      // 草稿
  'RUNNING': '1',    // 运行中
  'COMPLETED': '2',  // 已完成
  'REJECTED': '3',   // 已拒绝
  'CANCELLED': '4'   // 已撤回
}

type WorkflowStatusVariant = 'default' | 'secondary' | 'destructive' | 'outline'

const TERMINAL_STATUSES = new Set(['APPROVED', 'REJECTED', 'CANCELLED', 'WITHDRAWN', 'FINISHED', 'COMPLETED', '2', '3'])

const WORKFLOW_STATUS_VARIANTS: Record<string, WorkflowStatusVariant> = {
  '0': 'outline',     // 草稿
  '1': 'default',     // 运行中
  '2': 'default',     // 已完成
  '3': 'destructive', // 已拒绝
  '4': 'outline'      // 已撤回
}

/**
 * 获取流程状态展示文案
 * @param {string | null | undefined} status - 流程状态（英文码或数字字符串）
 * @returns {string} 展示文案
 */
export const getWorkflowStatusLabel = (status?: string | null) => {
  if (!status) return '-'

  const dict = getWorkflowStatusDict()
  // 尝试将英文状态码映射到数字
  const dictValue = STATUS_CODE_MAP[status] || status
  return dict.getLabel(dictValue, status)
}

/**
 * 获取流程状态徽标样式
 * @param {string | null | undefined} status - 流程状态
 * @returns {WorkflowStatusVariant} 徽标样式
 */
export const getWorkflowStatusVariant = (status?: string | null): WorkflowStatusVariant => {
  if (!status) return 'secondary'

  const dictValue = STATUS_CODE_MAP[status] || status
  return WORKFLOW_STATUS_VARIANTS[dictValue] || 'secondary'
}

/**
 * 判断流程是否为终态
 * @param {string | null | undefined} status - 流程状态
 * @returns {boolean} 是否终态
 */
export const isWorkflowTerminalStatus = (status?: string | null) => {
  if (!status) return false
  const dictValue = STATUS_CODE_MAP[status] || status
  return TERMINAL_STATUSES.has(status) || dictValue === '2' || dictValue === '3' || dictValue === '4'
}

/**
 * 判断流程是否允许继续操作
 * @param {string | null | undefined} status - 流程状态
 * @returns {boolean} 是否允许继续操作
 */
export const canOperateWorkflow = (status?: string | null) => {
  return !isWorkflowTerminalStatus(status)
}
