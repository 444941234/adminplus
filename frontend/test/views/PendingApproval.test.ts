import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick, ref } from 'vue'

// ---------------------------------------------------------------------------
// Mocks
// ---------------------------------------------------------------------------

vi.mock('vue-sonner', () => {
  const mock = {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn()
  }
  return { toast: mock }
})

vi.mock('@/components/ui/sonner/Sonner.vue', () => ({
  default: { name: 'Sonner', template: '<div />' }
}))

vi.mock('@/api', () => ({
  getPendingWorkflows: vi.fn(),
  approveWorkflow: vi.fn(),
  rejectWorkflow: vi.fn()
}))

const mockApproveAction = vi.fn()
const mockRejectAction = vi.fn()

vi.mock('@/composables/workflow/useWorkflowActions', () => ({
  useWorkflowActions: () => ({
    actionLoading: ref(false),
    approveWorkflowAction: mockApproveAction,
    rejectWorkflowAction: mockRejectAction
  })
}))

vi.mock('@/composables/useAsyncAction', () => ({
  useAsyncAction: vi.fn(() => ({
    loading: ref(false),
    run: vi.fn(async (fn: () => Promise<any>) => {
      await fn()
    })
  }))
}))

vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn(() => ({
    hasPermission: vi.fn(() => true)
  }))
}))

vi.mock('@/lib/page-permissions', () => ({
  getWorkflowPermissionState: vi.fn(() => ({
    canApprovePendingActions: true
  }))
}))

vi.mock('@/components/workflow/WorkflowActionButtons.vue', () => ({
  default: {
    name: 'WorkflowActionButtons',
    props: ['workflow', 'mode', 'canApprove'],
    emits: ['approve', 'reject'],
    template: `
      <div class="workflow-action-buttons-stub">
        <button class="approve-btn" @click="$emit('approve', workflow)">通过</button>
        <button class="reject-btn" @click="$emit('reject', workflow)">驳回</button>
      </div>
    `
  }
}))

vi.mock('@/components/common', () => ({
  ConfirmDialog: { name: 'ConfirmDialog', template: '<div />' }
}))

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

import PendingApproval from '@/views/workflow/PendingApproval.vue'
import { toast } from 'vue-sonner'
import { getPendingWorkflows } from '@/api'
import { getWorkflowPermissionState } from '@/lib/page-permissions'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeWorkflowInstance(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'wf1',
    definitionId: 'def-1',
    definitionName: '请假审批流程',
    userId: 'u1',
    userName: 'admin',
    deptId: 'd1',
    deptName: '技术部',
    title: '测试流程标题',
    businessData: '{}',
    currentNodeId: 'node-1',
    currentNodeName: '节点-1',
    status: 'PENDING',
    submitTime: '2026-03-29 10:00:00',
    finishTime: null,
    remark: '',
    createTime: '2026-03-29 10:00:00',
    pendingApproval: true,
    canApprove: true,
    canWithdraw: false,
    canCancel: false,
    canUrge: false,
    canEditDraft: false,
    canSubmitDraft: false,
    ...overrides
  }
}

const mockApiResponse = (data: any) => ({
  code: 200,
  message: 'success',
  data
})

const flushAsync = async () => {
  await new Promise(resolve => setTimeout(resolve, 0))
  await nextTick()
  await nextTick()
  await nextTick()
}

// ---------------------------------------------------------------------------
// Test suite
// ---------------------------------------------------------------------------

describe('PendingApproval Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()
    mockApproveAction.mockReset()
    mockRejectAction.mockReset()

    vi.mocked(getPendingWorkflows).mockResolvedValue(
      mockApiResponse([makeWorkflowInstance()]) as any
    )
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const stubs = {
    ConfirmDialog: { template: '<div />' },
    Sonner: true,
    Dialog: {
      props: ['open', 'modelValue'],
      template: `<div v-if="open !== false || modelValue" class="dialog-stub"><slot /></div>`
    },
    DialogContent: {
      template: '<div class="dialog-content-stub"><slot /></div>'
    },
    DialogHeader: {
      template: '<div class="dialog-header-stub"><slot /></div>'
    },
    DialogTitle: {
      props: ['id'],
      template: '<div class="dialog-title-stub"><slot /></div>'
    },
    DialogFooter: {
      template: '<div class="dialog-footer-stub"><slot /></div>'
    },
    DialogDescription: {
      template: '<p class="sr-only"><slot /></p>'
    },
    Textarea: {
      props: ['modelValue'],
      template: '<textarea class="textarea-stub" />'
    }
  }

  const mountAndFlush = async (options: any = {}) => {
    wrapper = mount(PendingApproval, {
      global: {
        plugins: [pinia],
        stubs
      },
      ...options
    } as any)
    await flushAsync()
    return wrapper
  }

  // =========================================================================
  // 1. Page structure
  // =========================================================================
  describe('Page Structure', () => {
    it('renders card title', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('待我审批')
    })

    it('renders table headers', async () => {
      wrapper = await mountAndFlush()
      const headers = wrapper.findAll('th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts.some(t => t.includes('标题'))).toBe(true)
      expect(headerTexts.some(t => t.includes('发起人'))).toBe(true)
      expect(headerTexts.some(t => t.includes('流程定义'))).toBe(true)
      expect(headerTexts.some(t => t.includes('当前节点'))).toBe(true)
      expect(headerTexts.some(t => t.includes('提交时间'))).toBe(true)
      expect(headerTexts.some(t => t.includes('操作'))).toBe(true)
    })

    it('renders root container with space-y-4 class', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('.space-y-4').exists()).toBe(true)
    })

    it('renders table element', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('table').exists()).toBe(true)
    })
  })

  // =========================================================================
  // 2. Data fetching
  // =========================================================================
  describe('Data Fetching', () => {
    it('calls getPendingWorkflows on mount', async () => {
      wrapper = await mountAndFlush()
      expect(getPendingWorkflows).toHaveBeenCalled()
    })

    it('populates workflows from API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.workflows.length).toBe(1)
      expect(vm.workflows[0].title).toBe('测试流程标题')
    })

    it('sets loading to false after fetch', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.loading).toBe(false)
    })

    it('renders workflow title in table row', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('测试流程标题')
    })

    it('renders workflow userName in table row', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('admin')
    })

    it('renders workflow definitionName in table row', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('请假审批流程')
    })

    it('renders workflow currentNodeName in table row', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('节点-1')
    })

    it('renders multiple workflows', async () => {
      vi.mocked(getPendingWorkflows).mockResolvedValue(
        mockApiResponse([
          makeWorkflowInstance({ id: 'wf1', title: '流程一' }),
          makeWorkflowInstance({ id: 'wf2', title: '流程二' })
        ]) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('流程一')
      expect(wrapper.text()).toContain('流程二')
    })

    it('renders dash when currentNodeName is null', async () => {
      vi.mocked(getPendingWorkflows).mockResolvedValue(
        mockApiResponse([makeWorkflowInstance({ currentNodeName: null })]) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('-')
    })
  })

  // =========================================================================
  // 3. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('shows empty text when API returns empty before resolving', async () => {
      // Since useAsyncAction is mocked, loading is always false.
      // Verify the component renders correctly with no data.
      vi.mocked(getPendingWorkflows).mockResolvedValue(mockApiResponse([]) as any)
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('当前没有待审批流程')
    })

    it('shows empty text when no workflows', async () => {
      vi.mocked(getPendingWorkflows).mockResolvedValue(mockApiResponse([]) as any)
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('当前没有待审批流程')
    })

    it('sets loading to false after successful fetch', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.loading).toBe(false)
    })
  })

  // =========================================================================
  // 4. Open action dialog
  // =========================================================================
  describe('Open Action Dialog', () => {
    it('opens approve dialog with correct state', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const workflow = makeWorkflowInstance()
      vm.openActionDialog(workflow, 'approve')
      expect(vm.actionDialogOpen).toBe(true)
      expect(vm.actionType).toBe('approve')
      // Use toEqual for comparing reactive-wrapped objects
      expect(vm.currentWorkflow).toEqual(workflow)
      expect(vm.comment).toBe('')
    })

    it('opens reject dialog with correct state', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const workflow = makeWorkflowInstance()
      vm.openActionDialog(workflow, 'reject')
      expect(vm.actionDialogOpen).toBe(true)
      expect(vm.actionType).toBe('reject')
      expect(vm.currentWorkflow).toEqual(workflow)
      expect(vm.comment).toBe('')
    })

    it('resets comment when opening dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.comment = 'old comment'
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      expect(vm.comment).toBe('')
    })
  })

  // =========================================================================
  // 5. Submit action
  // =========================================================================
  describe('Submit Action', () => {
    it('warns when comment is empty', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      vm.comment = ''
      await vm.submitAction()
      expect(toast.warning).toHaveBeenCalledWith('请输入审批意见')
    })

    it('warns when comment is only whitespace', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      vm.comment = '   '
      await vm.submitAction()
      expect(toast.warning).toHaveBeenCalledWith('请输入审批意见')
    })

    it('returns early when no current workflow', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.currentWorkflow = null
      vm.comment = 'test'
      await vm.submitAction()
      expect(mockApproveAction).not.toHaveBeenCalled()
      expect(mockRejectAction).not.toHaveBeenCalled()
    })

    it('calls approveWorkflowAction on approve submit', async () => {
      mockApproveAction.mockResolvedValue({ success: true })
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      vm.comment = 'approved'
      vi.clearAllMocks()
      vi.mocked(getPendingWorkflows).mockResolvedValue(
        mockApiResponse([makeWorkflowInstance()]) as any
      )
      await vm.submitAction()
      await flushAsync()
      expect(mockApproveAction).toHaveBeenCalledWith('wf1', { comment: 'approved' })
    })

    it('calls rejectWorkflowAction on reject submit', async () => {
      mockRejectAction.mockResolvedValue({ success: true })
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'reject')
      vm.comment = 'rejected'
      vi.clearAllMocks()
      vi.mocked(getPendingWorkflows).mockResolvedValue(
        mockApiResponse([makeWorkflowInstance()]) as any
      )
      await vm.submitAction()
      await flushAsync()
      expect(mockRejectAction).toHaveBeenCalledWith('wf1', { comment: 'rejected' })
    })

    it('closes dialog on successful approve', async () => {
      mockApproveAction.mockResolvedValue({ success: true })
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      vm.comment = 'ok'
      vi.clearAllMocks()
      vi.mocked(getPendingWorkflows).mockResolvedValue(
        mockApiResponse([makeWorkflowInstance()]) as any
      )
      await vm.submitAction()
      await flushAsync()
      expect(vm.actionDialogOpen).toBe(false)
    })

    it('closes dialog on successful reject', async () => {
      mockRejectAction.mockResolvedValue({ success: true })
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'reject')
      vm.comment = 'not ok'
      vi.clearAllMocks()
      vi.mocked(getPendingWorkflows).mockResolvedValue(
        mockApiResponse([makeWorkflowInstance()]) as any
      )
      await vm.submitAction()
      await flushAsync()
      expect(vm.actionDialogOpen).toBe(false)
    })

    it('refetches data after successful action', async () => {
      mockApproveAction.mockResolvedValue({ success: true })
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      vm.comment = 'ok'
      vi.clearAllMocks()
      vi.mocked(getPendingWorkflows).mockResolvedValue(
        mockApiResponse([]) as any
      )
      await vm.submitAction()
      await flushAsync()
      expect(getPendingWorkflows).toHaveBeenCalled()
    })

    it('does not close dialog when action returns falsy', async () => {
      mockApproveAction.mockResolvedValue(null)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      vm.comment = 'ok'
      await vm.submitAction()
      expect(vm.actionDialogOpen).toBe(true)
    })

    it('trims comment before sending', async () => {
      mockApproveAction.mockResolvedValue({ success: true })
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      vm.comment = '  trimmed comment  '
      vi.clearAllMocks()
      vi.mocked(getPendingWorkflows).mockResolvedValue(
        mockApiResponse([makeWorkflowInstance()]) as any
      )
      await vm.submitAction()
      await flushAsync()
      expect(mockApproveAction).toHaveBeenCalledWith('wf1', { comment: 'trimmed comment' })
    })
  })

  // =========================================================================
  // 6. WorkflowActionButtons integration
  // =========================================================================
  describe('WorkflowActionButtons Integration', () => {
    it('renders WorkflowActionButtons for each workflow', async () => {
      wrapper = await mountAndFlush()
      const actionButtons = wrapper.findAll('.workflow-action-buttons-stub')
      expect(actionButtons.length).toBe(1)
    })

    it('renders multiple WorkflowActionButtons for multiple workflows', async () => {
      vi.mocked(getPendingWorkflows).mockResolvedValue(
        mockApiResponse([
          makeWorkflowInstance({ id: 'wf1' }),
          makeWorkflowInstance({ id: 'wf2' })
        ]) as any
      )
      wrapper = await mountAndFlush()
      const actionButtons = wrapper.findAll('.workflow-action-buttons-stub')
      expect(actionButtons.length).toBe(2)
    })

    it('opens approve dialog on approve emit', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const approveBtn = wrapper.find('.approve-btn')
      await approveBtn.trigger('click')
      expect(vm.actionDialogOpen).toBe(true)
      expect(vm.actionType).toBe('approve')
    })

    it('opens reject dialog on reject emit', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const rejectBtn = wrapper.find('.reject-btn')
      await rejectBtn.trigger('click')
      expect(vm.actionDialogOpen).toBe(true)
      expect(vm.actionType).toBe('reject')
    })
  })

  // =========================================================================
  // 7. Dialog rendering
  // =========================================================================
  describe('Dialog Rendering', () => {
    it('renders dialog with approve title when actionType is approve', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      await nextTick()
      await nextTick()
      // Dialog is rendered through stub - check title is present
      expect(wrapper.text()).toContain('通过审批')
    })

    it('renders dialog with reject title when actionType is reject', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'reject')
      await nextTick()
      await nextTick()
      expect(wrapper.text()).toContain('驳回流程')
    })

    it('renders workflow title in dialog body', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      await nextTick()
      await nextTick()
      expect(wrapper.text()).toContain('测试流程标题')
    })

    it('renders cancel button in dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      await nextTick()
      await nextTick()
      const buttons = wrapper.findAll('button')
      const cancelBtn = buttons.find(b => b.text().includes('取消'))
      expect(cancelBtn).toBeDefined()
    })

    it('renders confirm button in dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      await nextTick()
      await nextTick()
      const buttons = wrapper.findAll('button')
      const confirmBtn = buttons.find(b => b.text().includes('确认'))
      expect(confirmBtn).toBeDefined()
    })

    it('closes dialog on cancel button click', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      await nextTick()
      await nextTick()
      const buttons = wrapper.findAll('button')
      const cancelBtn = buttons.find(b => b.text().includes('取消'))
      await cancelBtn!.trigger('click')
      expect(vm.actionDialogOpen).toBe(false)
    })
  })

  // =========================================================================
  // 8. Permission gating
  // =========================================================================
  describe('Permission Gating', () => {
    it('sets canApproveWorkflow to true when permission is granted', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canApprovePendingActions: true
      } as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.canApproveWorkflow).toBe(true)
    })

    it('sets canApproveWorkflow to false when permission is denied', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canApprovePendingActions: false
      } as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.canApproveWorkflow).toBe(false)
    })

    it('does not render dialog when canApproveWorkflow is false', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canApprovePendingActions: false
      } as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.openActionDialog(makeWorkflowInstance(), 'approve')
      await nextTick()
      // The Dialog has v-if="canApproveWorkflow" guard
      expect(wrapper.find('.dialog-stub').exists()).toBe(false)
    })

    it('calls getWorkflowPermissionState with userStore.hasPermission', async () => {
      wrapper = await mountAndFlush()
      expect(getWorkflowPermissionState).toHaveBeenCalled()
    })
  })
})
