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

/**
 * 工作流动作封装
 * 统一处理 API 调用和提示文案，页面只负责参数校验和刷新
 * 注意：错误提示已在响应拦截器中统一处理，此处不再重复显示
 */
export const useWorkflowActions = () => {
  const actionLoading = ref(false)

  const runAction = async <T>(
    executor: () => Promise<T>,
    successMessage: string
  ) => {
    actionLoading.value = true
    try {
      const result = await executor()
      toast.success(successMessage)
      return result
    } catch {
      // 错误提示已在响应拦截器 (useApiInterceptors) 中统一处理
      return null
    } finally {
      actionLoading.value = false
    }
  }

  const withdrawWorkflowAction = (instanceId: string) => {
    return runAction(
      () => withdrawWorkflow(instanceId),
      '流程已撤回'
    )
  }

  const cancelWorkflowAction = (instanceId: string) => {
    return runAction(
      () => cancelWorkflow(instanceId),
      '流程已取消'
    )
  }

  const urgeWorkflowAction = (instanceId: string, payload: UrgePayload) => {
    return runAction(
      () => urgeWorkflow(instanceId, payload),
      '催办成功'
    )
  }

  const approveWorkflowAction = (instanceId: string, payload: ApprovalPayload) => {
    return runAction(
      () => approveWorkflow(instanceId, payload),
      '审批已通过'
    )
  }

  const rejectWorkflowAction = (instanceId: string, payload: ApprovalPayload) => {
    return runAction(
      () => rejectWorkflow(instanceId, payload),
      '流程已驳回'
    )
  }

  const rollbackWorkflowAction = (instanceId: string, payload: ApprovalPayload) => {
    return runAction(
      () => rollbackWorkflow(instanceId, payload),
      '流程已回退'
    )
  }

  const addSignWorkflowAction = (instanceId: string, payload: AddSignPayload) => {
    return runAction(
      () => addSignWorkflow(instanceId, payload),
      '加签成功'
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
