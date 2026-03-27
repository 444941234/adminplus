import { ref } from 'vue'
import {
  addSignWorkflow,
  approveWorkflow,
  cancelWorkflow,
  rejectWorkflow,
  rollbackWorkflow,
  urgeWorkflow,
  withdrawWorkflow
} from '@/api'
import { toast } from 'vue-sonner'

interface ApprovalPayload {
  comment: string
  targetNodeId?: string
}

interface AddSignPayload {
  addUserId: string
  addType: 'BEFORE' | 'AFTER' | 'TRANSFER'
  reason: string
}

interface UrgePayload {
  content: string
  targetApproverId?: string
}

const getErrorMessage = (error: unknown, fallback: string) => {
  return error instanceof Error ? error.message : fallback
}

/**
 * 工作流动作封装
 * 统一处理 API 调用和提示文案，页面只负责参数校验和刷新
 */
export const useWorkflowActions = () => {
  const actionLoading = ref(false)

  const runAction = async <T>(
    executor: () => Promise<T>,
    successMessage: string,
    errorMessage: string
  ) => {
    actionLoading.value = true
    try {
      const result = await executor()
      toast.success(successMessage)
      return result
    } catch (error) {
      toast.error(getErrorMessage(error, errorMessage))
      return null
    } finally {
      actionLoading.value = false
    }
  }

  const withdrawWorkflowAction = (instanceId: string) => {
    return runAction(
      () => withdrawWorkflow(instanceId),
      '流程已撤回',
      '撤回失败'
    )
  }

  const cancelWorkflowAction = (instanceId: string) => {
    return runAction(
      () => cancelWorkflow(instanceId),
      '流程已取消',
      '取消失败'
    )
  }

  const urgeWorkflowAction = (instanceId: string, payload: UrgePayload) => {
    return runAction(
      () => urgeWorkflow(instanceId, payload),
      '催办成功',
      '催办失败'
    )
  }

  const approveWorkflowAction = (instanceId: string, payload: ApprovalPayload) => {
    return runAction(
      () => approveWorkflow(instanceId, payload),
      '审批已通过',
      '审批失败'
    )
  }

  const rejectWorkflowAction = (instanceId: string, payload: ApprovalPayload) => {
    return runAction(
      () => rejectWorkflow(instanceId, payload),
      '流程已驳回',
      '审批失败'
    )
  }

  const rollbackWorkflowAction = (instanceId: string, payload: ApprovalPayload) => {
    return runAction(
      () => rollbackWorkflow(instanceId, payload),
      '流程已回退',
      '回退失败'
    )
  }

  const addSignWorkflowAction = (instanceId: string, payload: AddSignPayload) => {
    return runAction(
      () => addSignWorkflow(instanceId, payload),
      '加签成功',
      '加签失败'
    )
  }

  return {
    actionLoading,
    withdrawWorkflowAction,
    cancelWorkflowAction,
    urgeWorkflowAction,
    approveWorkflowAction,
    rejectWorkflowAction,
    rollbackWorkflowAction,
    addSignWorkflowAction
  }
}
