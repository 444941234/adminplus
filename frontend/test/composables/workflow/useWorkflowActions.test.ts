import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { useWorkflowActions } from '@/composables/workflow/useWorkflowActions'

const apiMocks = vi.hoisted(() => ({
  withdrawWorkflow: vi.fn(),
  cancelWorkflow: vi.fn(),
  urgeWorkflow: vi.fn(),
  approveWorkflow: vi.fn(),
  rejectWorkflow: vi.fn(),
  rollbackWorkflow: vi.fn(),
  addSignWorkflow: vi.fn()
}))

const toastMocks = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn()
}))

vi.mock('@/api', () => ({
  withdrawWorkflow: apiMocks.withdrawWorkflow,
  cancelWorkflow: apiMocks.cancelWorkflow,
  urgeWorkflow: apiMocks.urgeWorkflow,
  approveWorkflow: apiMocks.approveWorkflow,
  rejectWorkflow: apiMocks.rejectWorkflow,
  rollbackWorkflow: apiMocks.rollbackWorkflow,
  addSignWorkflow: apiMocks.addSignWorkflow
}))

vi.mock('vue-sonner', () => ({
  toast: toastMocks
}))

describe('useWorkflowActions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Set up default successful responses
    apiMocks.withdrawWorkflow.mockResolvedValue({ data: {} })
    apiMocks.cancelWorkflow.mockResolvedValue({ data: {} })
    apiMocks.urgeWorkflow.mockResolvedValue({ data: {} })
    apiMocks.approveWorkflow.mockResolvedValue({ data: {} })
    apiMocks.rejectWorkflow.mockResolvedValue({ data: {} })
    apiMocks.rollbackWorkflow.mockResolvedValue({ data: {} })
    apiMocks.addSignWorkflow.mockResolvedValue({ data: {} })
  })

  // =========================================================================
  // 1. Initial State
  // =========================================================================
  describe('Initial State', () => {
    it('initializes with actionLoading set to false', () => {
      const { actionLoading } = useWorkflowActions()
      expect(actionLoading.value).toBe(false)
    })

    it('returns all workflow action functions', () => {
      const actions = useWorkflowActions()
      expect(actions.withdrawWorkflowAction).toBeInstanceOf(Function)
      expect(actions.cancelWorkflowAction).toBeInstanceOf(Function)
      expect(actions.urgeWorkflowAction).toBeInstanceOf(Function)
      expect(actions.approveWorkflowAction).toBeInstanceOf(Function)
      expect(actions.rejectWorkflowAction).toBeInstanceOf(Function)
      expect(actions.rollbackWorkflowAction).toBeInstanceOf(Function)
      expect(actions.addSignWorkflowAction).toBeInstanceOf(Function)
    })
  })

  // =========================================================================
  // 2. withdrawWorkflowAction
  // =========================================================================
  describe('withdrawWorkflowAction', () => {
    it('calls withdrawWorkflow API and shows success toast', async () => {
      const { withdrawWorkflowAction, actionLoading } = useWorkflowActions()

      const result = await withdrawWorkflowAction('instance-001')

      expect(apiMocks.withdrawWorkflow).toHaveBeenCalledWith('instance-001')
      expect(toastMocks.success).toHaveBeenCalledWith('流程已撤回')
      expect(result).toBeTruthy()
      expect(actionLoading.value).toBe(false)
    })

    it('sets actionLoading to true during execution', async () => {
      const { withdrawWorkflowAction, actionLoading } = useWorkflowActions()
      let loadingDuringExecution = false

      apiMocks.withdrawWorkflow.mockImplementation(async () => {
        loadingDuringExecution = actionLoading.value
        await Promise.resolve()
        return { data: {} }
      })

      await withdrawWorkflowAction('instance-001')

      expect(loadingDuringExecution).toBe(true)
      expect(actionLoading.value).toBe(false)
    })

    it('returns null on API failure', async () => {
      const { withdrawWorkflowAction } = useWorkflowActions()
      apiMocks.withdrawWorkflow.mockRejectedValue(new Error('API Error'))

      const result = await withdrawWorkflowAction('instance-001')

      expect(result).toBeNull()
      expect(toastMocks.success).not.toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 3. cancelWorkflowAction
  // =========================================================================
  describe('cancelWorkflowAction', () => {
    it('calls cancelWorkflow API and shows success toast', async () => {
      const { cancelWorkflowAction } = useWorkflowActions()

      const result = await cancelWorkflowAction('instance-001')

      expect(apiMocks.cancelWorkflow).toHaveBeenCalledWith('instance-001')
      expect(toastMocks.success).toHaveBeenCalledWith('流程已取消')
      expect(result).toBeTruthy()
    })

    it('returns null on API failure', async () => {
      const { cancelWorkflowAction } = useWorkflowActions()
      apiMocks.cancelWorkflow.mockRejectedValue(new Error('API Error'))

      const result = await cancelWorkflowAction('instance-001')

      expect(result).toBeNull()
    })
  })

  // =========================================================================
  // 4. urgeWorkflowAction
  // =========================================================================
  describe('urgeWorkflowAction', () => {
    it('calls urgeWorkflow API with payload and shows success toast', async () => {
      const { urgeWorkflowAction } = useWorkflowActions()
      const payload = { content: '请尽快审批', targetApproverId: 'user-001' }

      const result = await urgeWorkflowAction('instance-001', payload)

      expect(apiMocks.urgeWorkflow).toHaveBeenCalledWith('instance-001', payload)
      expect(toastMocks.success).toHaveBeenCalledWith('催办成功')
      expect(result).toBeTruthy()
    })

    it('handles payload without targetApproverId', async () => {
      const { urgeWorkflowAction } = useWorkflowActions()
      const payload = { content: '请尽快审批' }

      await urgeWorkflowAction('instance-001', payload)

      expect(apiMocks.urgeWorkflow).toHaveBeenCalledWith('instance-001', payload)
    })
  })

  // =========================================================================
  // 5. approveWorkflowAction
  // =========================================================================
  describe('approveWorkflowAction', () => {
    it('calls approveWorkflow API with payload and shows success toast', async () => {
      const { approveWorkflowAction } = useWorkflowActions()
      const payload = { comment: '同意', targetNodeId: 'node-001' }

      const result = await approveWorkflowAction('instance-001', payload)

      expect(apiMocks.approveWorkflow).toHaveBeenCalledWith('instance-001', payload)
      expect(toastMocks.success).toHaveBeenCalledWith('审批已通过')
      expect(result).toBeTruthy()
    })

    it('handles payload without targetNodeId', async () => {
      const { approveWorkflowAction } = useWorkflowActions()
      const payload = { comment: '同意' }

      await approveWorkflowAction('instance-001', payload)

      expect(apiMocks.approveWorkflow).toHaveBeenCalledWith('instance-001', payload)
    })
  })

  // =========================================================================
  // 6. rejectWorkflowAction
  // =========================================================================
  describe('rejectWorkflowAction', () => {
    it('calls rejectWorkflow API with payload and shows success toast', async () => {
      const { rejectWorkflowAction } = useWorkflowActions()
      const payload = { comment: '不符合要求', targetNodeId: 'node-001' }

      const result = await rejectWorkflowAction('instance-001', payload)

      expect(apiMocks.rejectWorkflow).toHaveBeenCalledWith('instance-001', payload)
      expect(toastMocks.success).toHaveBeenCalledWith('流程已驳回')
      expect(result).toBeTruthy()
    })
  })

  // =========================================================================
  // 7. rollbackWorkflowAction
  // =========================================================================
  describe('rollbackWorkflowAction', () => {
    it('calls rollbackWorkflow API with payload and shows success toast', async () => {
      const { rollbackWorkflowAction } = useWorkflowActions()
      const payload = { comment: '需要修改', targetNodeId: 'node-002' }

      const result = await rollbackWorkflowAction('instance-001', payload)

      expect(apiMocks.rollbackWorkflow).toHaveBeenCalledWith('instance-001', payload)
      expect(toastMocks.success).toHaveBeenCalledWith('流程已回退')
      expect(result).toBeTruthy()
    })
  })

  // =========================================================================
  // 8. addSignWorkflowAction
  // =========================================================================
  describe('addSignWorkflowAction', () => {
    it('calls addSignWorkflow API with payload and shows success toast', async () => {
      const { addSignWorkflowAction } = useWorkflowActions()
      const payload = { addUserId: 'user-002', addType: 'BEFORE', reason: '需要补充信息' }

      const result = await addSignWorkflowAction('instance-001', payload)

      expect(apiMocks.addSignWorkflow).toHaveBeenCalledWith('instance-001', payload)
      expect(toastMocks.success).toHaveBeenCalledWith('加签成功')
      expect(result).toBeTruthy()
    })

    it('handles different add types', async () => {
      const { addSignWorkflowAction } = useWorkflowActions()

      await addSignWorkflowAction('instance-001', { addUserId: 'user-002', addType: 'AFTER', reason: '审核' })
      expect(apiMocks.addSignWorkflow).toHaveBeenCalled()

      await addSignWorkflowAction('instance-002', { addUserId: 'user-003', addType: 'TRANSFER', reason: '转办' })
      expect(apiMocks.addSignWorkflow).toHaveBeenCalledTimes(2)
    })
  })

  // =========================================================================
  // 9. Loading State Management
  // =========================================================================
  describe('Loading State Management', () => {
    it('shares loading state across all actions', async () => {
      const { actionLoading, approveWorkflowAction, rejectWorkflowAction } = useWorkflowActions()

      // First action
      const promise1 = approveWorkflowAction('instance-001', { comment: 'OK' })
      expect(actionLoading.value).toBe(true)
      await promise1
      expect(actionLoading.value).toBe(false)

      // Second action
      const promise2 = rejectWorkflowAction('instance-002', { comment: 'Reject' })
      expect(actionLoading.value).toBe(true)
      await promise2
      expect(actionLoading.value).toBe(false)
    })

    it('resets loading state even on error', async () => {
      const { actionLoading, approveWorkflowAction } = useWorkflowActions()
      apiMocks.approveWorkflow.mockRejectedValue(new Error('API Error'))

      await approveWorkflowAction('instance-001', { comment: 'OK' })

      expect(actionLoading.value).toBe(false)
    })
  })

  // =========================================================================
  // 10. Integration Scenarios
  // =========================================================================
  describe('Integration Scenarios', () => {
    it('handles complete workflow approval flow', async () => {
      const { approveWorkflowAction, urgeWorkflowAction, actionLoading } = useWorkflowActions()

      // Approve workflow
      const approveResult = await approveWorkflowAction('instance-001', { comment: '同意' })
      expect(approveResult).toBeTruthy()
      expect(toastMocks.success).toHaveBeenCalledWith('审批已通过')

      // Urge pending workflow
      const urgeResult = await urgeWorkflowAction('instance-002', { content: '请审批' })
      expect(urgeResult).toBeTruthy()
      expect(toastMocks.success).toHaveBeenCalledWith('催办成功')
    })

    it('handles workflow rejection with rollback', async () => {
      const { rejectWorkflowAction, rollbackWorkflowAction } = useWorkflowActions()

      // Reject workflow
      const rejectResult = await rejectWorkflowAction('instance-001', { comment: '需要修改' })
      expect(rejectResult).toBeTruthy()
      expect(toastMocks.success).toHaveBeenCalledWith('流程已驳回')

      // Rollback to previous node
      const rollbackResult = await rollbackWorkflowAction('instance-001', { comment: '重新提交' })
      expect(rollbackResult).toBeTruthy()
      expect(toastMocks.success).toHaveBeenCalledWith('流程已回退')
    })

    it('handles workflow withdrawal and cancellation', async () => {
      const { withdrawWorkflowAction, cancelWorkflowAction } = useWorkflowActions()

      // Withdraw workflow
      const withdrawResult = await withdrawWorkflowAction('instance-001')
      expect(withdrawResult).toBeTruthy()
      expect(toastMocks.success).toHaveBeenCalledWith('流程已撤回')

      // Cancel workflow
      const cancelResult = await cancelWorkflowAction('instance-002')
      expect(cancelResult).toBeTruthy()
      expect(toastMocks.success).toHaveBeenCalledWith('流程已取消')
    })
  })
})
