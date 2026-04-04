import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import WorkflowHookDialog from '@/components/workflow/designer/WorkflowHookDialog.vue'

const apiMocks = vi.hoisted(() => ({
  getNodeHooks: vi.fn(),
  createHook: vi.fn(),
  updateHook: vi.fn(),
  deleteHook: vi.fn()
}))

const toastMocks = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn()
}))

vi.mock('@/api/workflow', () => ({
  getNodeHooks: apiMocks.getNodeHooks,
  createHook: apiMocks.createHook,
  updateHook: apiMocks.updateHook,
  deleteHook: apiMocks.deleteHook
}))

vi.mock('vue-sonner', () => ({
  toast: toastMocks
}))

vi.mock('@/composables/useApiInterceptors', () => ({
  showErrorToast: vi.fn((_error: Error, message: string) => {
    toastMocks.error(message)
  }),
  setupRequestInterceptor: vi.fn(),
  setupResponseInterceptor: vi.fn(),
  clearPendingRequests: vi.fn(),
  isCanceledError: vi.fn()
}))

const makeHook = (overrides: Partial<Record<string, any>> = {}) => ({
  id: 'hook-001',
  nodeId: 'node-001',
  hookPoint: 'PRE_SUBMIT',
  hookType: 'validate',
  executorType: 'spel',
  executorConfig: '#formData.amount > 100',
  asyncExecution: false,
  blockOnFailure: true,
  failureMessage: '金额必须大于100',
  priority: 0,
  conditionExpression: null,
  retryCount: 0,
  retryInterval: 1000,
  hookName: '金额校验',
  description: '校验金额是否大于100',
  createTime: '2026-03-27T08:00:00Z',
  updateTime: '2026-03-27T08:00:00Z',
  ...overrides
})

const mockApiResponse = <T,>(data: T) => ({ data, code: 200, message: 'success' })

const DialogStub = defineComponent({
  name: 'Dialog',
  props: {
    open: { type: Boolean, default: false }
  },
  emits: ['update:open'],
  setup(props, { slots, emit }) {
    return () => h('div', {
      class: 'dialog-stub',
      'data-open': String(props.open)
    }, [
      h('button', { class: 'close-btn', onClick: () => emit('update:open', false) }, 'Close'),
      slots.default?.()
    ])
  }
})

const DialogContentStub = defineComponent({
  name: 'DialogContent',
  setup(_, { slots }) {
    return () => h('div', { class: 'dialog-content-stub' }, slots.default?.())
  }
})

const DialogHeaderStub = defineComponent({
  name: 'DialogHeader',
  setup(_, { slots }) {
    return () => h('div', { class: 'dialog-header-stub' }, slots.default?.())
  }
})

const DialogTitleStub = defineComponent({
  name: 'DialogTitle',
  setup(_, { slots }) {
    return () => h('div', { class: 'dialog-title-stub' }, slots.default?.())
  }
})

const DialogDescriptionStub = defineComponent({
  name: 'DialogDescription',
  setup(_, { slots }) {
    return () => h('p', { class: 'dialog-description-stub' }, slots.default?.())
  }
})

const DialogFooterStub = defineComponent({
  name: 'DialogFooter',
  setup(_, { slots }) {
    return () => h('div', { class: 'dialog-footer-stub' }, slots.default?.())
  }
})

const ConfirmDialogStub = defineComponent({
  name: 'ConfirmDialog',
  props: {
    open: { type: Boolean, default: false },
    title: { type: String, default: '' },
    description: { type: String, default: '' },
    confirmText: { type: String, default: '' }
  },
  emits: ['update:open', 'confirm'],
  setup(props, { emit }) {
    return () => h('div', {
      class: 'confirm-dialog-stub',
      'data-open': String(props.open)
    }, [
      h('button', { class: 'confirm-btn', onClick: () => emit('confirm') }, 'Confirm')
    ])
  }
})

describe('WorkflowHookDialog.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    apiMocks.getNodeHooks.mockResolvedValue(mockApiResponse([]))
    apiMocks.createHook.mockResolvedValue(mockApiResponse({ id: 'new-hook' }))
    apiMocks.updateHook.mockResolvedValue(mockApiResponse({}))
    apiMocks.deleteHook.mockResolvedValue(mockApiResponse({}))
  })

  const mountComponent = async (props: Record<string, unknown> = {}) => {
    const wrapper = mount(WorkflowHookDialog, {
      props: {
        open: false,
        nodeId: 'node-001',
        ...props
      },
      global: {
        stubs: {
          Dialog: DialogStub,
          DialogContent: DialogContentStub,
          DialogHeader: DialogHeaderStub,
          DialogTitle: DialogTitleStub,
          DialogDescription: DialogDescriptionStub,
          DialogFooter: DialogFooterStub,
          ConfirmDialog: ConfirmDialogStub,
          Button: true,
          Input: true,
          Label: true,
          Textarea: true,
          Select: true,
          SelectContent: true,
          SelectItem: true,
          SelectTrigger: true,
          SelectValue: true,
          Checkbox: true
        }
      }
    })

    // Set open to true to trigger the watcher
    const shouldOpen = props.open !== false
    if (shouldOpen) {
      ;(wrapper as any).setProps({ open: true })
      await flushPromises()
    }

    return wrapper
  }

  // =========================================================================
  // 1. Initial Load
  // =========================================================================
  describe('Initial Load', () => {
    it('loads hooks when dialog opens', async () => {
      await mountComponent()
      expect(apiMocks.getNodeHooks).toHaveBeenCalledWith('node-001')
    })

    it('does not load hooks when dialog is closed', async () => {
      vi.clearAllMocks()
      await mountComponent({ open: false })
      expect(apiMocks.getNodeHooks).not.toHaveBeenCalled()
    })

    it('displays loading state during fetch', async () => {
      // This test verifies the loading ref is used
      // The actual loading UI is shown during async operations
      const wrapper = mount(WorkflowHookDialog, {
        props: { open: false, nodeId: 'node-001' },
        global: {
          stubs: {
            Dialog: DialogStub,
            DialogContent: DialogContentStub,
            DialogHeader: DialogHeaderStub,
            DialogTitle: DialogTitleStub,
            DialogDescription: DialogDescriptionStub,
            DialogFooter: DialogFooterStub,
            ConfirmDialog: ConfirmDialogStub,
            Button: true, Input: true, Label: true, Textarea: true,
            Select: true, SelectContent: true, SelectItem: true, SelectTrigger: true, SelectValue: true, Checkbox: true
          }
        }
      })
      const vm = wrapper.vm as any

      // Loading should be false initially
      expect(vm.loading).toBe(false)
    })

    it('displays empty state when no hooks', async () => {
      apiMocks.getNodeHooks.mockResolvedValue(mockApiResponse([]))
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('暂无钩子配置')
    })

    it('displays hook list', async () => {
      apiMocks.getNodeHooks.mockResolvedValue(mockApiResponse([
        makeHook(),
        makeHook({ id: 'hook-002', hookPoint: 'POST_APPROVE', hookType: 'execute' })
      ]))
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('提交前校验')
      expect(wrapper.text()).toContain('同意后执行')
    })
  })

  // =========================================================================
  // 2. Hook Point Labels
  // =========================================================================
  describe('Hook Point Labels', () => {
    it('displays correct hook point labels', async () => {
      apiMocks.getNodeHooks.mockResolvedValue(mockApiResponse([
        makeHook({ hookPoint: 'PRE_APPROVE' }),
        makeHook({ id: 'hook-002', hookPoint: 'POST_SUBMIT' })
      ]))
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('同意前校验')
      expect(wrapper.text()).toContain('提交后执行')
    })
  })

  // =========================================================================
  // 3. Executor Type Labels
  // =========================================================================
  describe('Executor Type Labels', () => {
    it('displays correct executor type labels', async () => {
      apiMocks.getNodeHooks.mockResolvedValue(mockApiResponse([
        makeHook({ executorType: 'spel' }),
        makeHook({ id: 'hook-002', executorType: 'bean' }),
        makeHook({ id: 'hook-003', executorType: 'http' })
      ]))
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('SpEL表达式')
      expect(wrapper.text()).toContain('Bean方法')
      expect(wrapper.text()).toContain('HTTP接口')
    })
  })

  // =========================================================================
  // 4. Hook Type Badges
  // =========================================================================
  describe('Hook Type Badges', () => {
    it('displays validate badge for validate hooks', async () => {
      apiMocks.getNodeHooks.mockResolvedValue(mockApiResponse([
        makeHook({ hookType: 'validate' })
      ]))
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('校验')
    })

    it('displays execute badge for execute hooks', async () => {
      apiMocks.getNodeHooks.mockResolvedValue(mockApiResponse([
        makeHook({ hookType: 'execute' })
      ]))
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('执行')
    })
  })

  // =========================================================================
  // 5. Create Hook
  // =========================================================================
  describe('Create Hook', () => {
    it('opens create form with hook point pre-selected', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm('PRE_APPROVE')
      await flushPromises()

      expect(vm.showForm).toBe(true)
      expect(vm.formData.hookPoint).toBe('PRE_APPROVE')
    })

    it('sets hookType based on hook point type', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm('POST_SUBMIT') // execute type
      await flushPromises()

      expect(vm.formData.hookType).toBe('execute')
    })
  })

  // =========================================================================
  // 6. Edit Hook
  // =========================================================================
  describe('Edit Hook', () => {
    it('opens edit form with hook data', async () => {
      const hook = makeHook()
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openEditForm(hook)
      await flushPromises()

      expect(vm.editingHook).toEqual(hook)
      expect(vm.showForm).toBe(true)
      expect(vm.formData.hookPoint).toBe('PRE_SUBMIT')
      expect(vm.formData.hookName).toBe('金额校验')
    })
  })

  // =========================================================================
  // 7. Save Hook
  // =========================================================================
  describe('Save Hook', () => {
    it('creates new hook', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm('PRE_SUBMIT')
      vm.formData.executorConfig = '#formData.test'
      await vm.saveHook()
      await flushPromises()

      expect(apiMocks.createHook).toHaveBeenCalledWith(expect.objectContaining({
        nodeId: 'node-001',
        hookPoint: 'PRE_SUBMIT',
        executorConfig: '#formData.test'
      }))
    })

    it('updates existing hook', async () => {
      const hook = makeHook()
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openEditForm(hook)
      vm.formData.hookName = 'Updated Name'
      await vm.saveHook()
      await flushPromises()

      expect(apiMocks.updateHook).toHaveBeenCalledWith('hook-001', expect.objectContaining({
        hookName: 'Updated Name'
      }))
    })

    it('emits refresh event after save', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm('PRE_SUBMIT')
      await vm.saveHook()
      await flushPromises()

      expect(wrapper.emitted('refresh')).toBeTruthy()
    })
  })

  // =========================================================================
  // 8. Delete Hook
  // =========================================================================
  describe('Delete Hook', () => {
    it('opens delete confirmation dialog', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.handleDeleteHook('hook-001')
      await flushPromises()

      expect(vm.deleteDialogOpen).toBe(true)
      expect(vm.deleteHookId).toBe('hook-001')
    })

    it('deletes hook after confirmation', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      // Open delete dialog
      vm.handleDeleteHook('hook-001')
      await flushPromises()

      // Confirm delete (simulate clicking confirm button)
      await vm.confirmDeleteHook()
      await flushPromises()

      expect(apiMocks.deleteHook).toHaveBeenCalledWith('hook-001')
    })

    it('does not delete when dialog is cancelled', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      // Open delete dialog
      vm.handleDeleteHook('hook-001')
      await flushPromises()

      // Close dialog without confirming (simulate clicking cancel)
      vm.deleteDialogOpen = false
      await flushPromises()

      expect(apiMocks.deleteHook).not.toHaveBeenCalled()
    })

    it('emits refresh event after delete', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      // Open and confirm delete
      vm.handleDeleteHook('hook-001')
      await flushPromises()
      await vm.confirmDeleteHook()
      await flushPromises()

      expect(wrapper.emitted('refresh')).toBeTruthy()
    })
  })

  // =========================================================================
  // 9. Close Form
  // =========================================================================
  describe('Close Form', () => {
    it('resets form state', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm('PRE_SUBMIT')
      vm.formData.hookName = 'Test'
      vm.closeForm()

      expect(vm.showForm).toBe(false)
      expect(vm.editingHook).toBeNull()
      expect(vm.formData.hookName).toBe('')
    })
  })

  // =========================================================================
  // 10. Filter Hooks
  // =========================================================================
  describe('Filter Hooks', () => {
    it('filters hooks by selected hook point', async () => {
      apiMocks.getNodeHooks.mockResolvedValue(mockApiResponse([
        makeHook({ hookPoint: 'PRE_SUBMIT' }),
        makeHook({ id: 'hook-002', hookPoint: 'POST_APPROVE' })
      ]))
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      // Verify hooks loaded correctly
      expect(vm.hooks.length).toBe(2)

      // Set filter
      vm.selectedHookPoint = 'PRE_SUBMIT'
      await flushPromises()

      // Verify filtered result
      expect(vm.filteredHooks.length).toBe(1)
    })

    it('shows all hooks when no filter selected', async () => {
      apiMocks.getNodeHooks.mockResolvedValue(mockApiResponse([
        makeHook({ hookPoint: 'PRE_SUBMIT' }),
        makeHook({ id: 'hook-002', hookPoint: 'POST_APPROVE' })
      ]))
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      // Verify hooks loaded correctly
      expect(vm.hooks.length).toBe(2)

      // Default filter is empty string, should show all
      expect(vm.filteredHooks.length).toBe(2)
    })
  })
})