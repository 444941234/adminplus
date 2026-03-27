import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h, ref } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import MyWorkflow from './MyWorkflow.vue'

const apiMocks = vi.hoisted(() => ({
  getMyWorkflows: vi.fn(),
  getEnabledWorkflowDefinitions: vi.fn(),
  getWorkflowDraftDetail: vi.fn(),
  getWorkflowDefinition: vi.fn(),
  updateWorkflowDraft: vi.fn(),
  submitWorkflow: vi.fn(),
  deleteWorkflowDraft: vi.fn()
}))

const actionMocks = vi.hoisted(() => ({
  urgeWorkflowAction: vi.fn(),
  withdrawWorkflowAction: vi.fn(),
  cancelWorkflowAction: vi.fn()
}))

const formValuesRef = ref<Record<string, unknown>>({})
const fieldErrorsRef = ref<Record<string, string>>({})
const initForm = vi.fn()
const validateForm = vi.fn(() => true)
const buildSubmitPayload = vi.fn(() => ({
  formData: {
    reason: '补交资料',
    days: 1
  }
}))

vi.mock('@/api', () => ({
  getMyWorkflows: apiMocks.getMyWorkflows,
  getEnabledWorkflowDefinitions: apiMocks.getEnabledWorkflowDefinitions,
  getWorkflowDraftDetail: apiMocks.getWorkflowDraftDetail,
  getWorkflowDefinition: apiMocks.getWorkflowDefinition,
  updateWorkflowDraft: apiMocks.updateWorkflowDraft,
  submitWorkflow: apiMocks.submitWorkflow,
  deleteWorkflowDraft: apiMocks.deleteWorkflowDraft
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    hasPermission: vi.fn(() => true)
  })
}))

vi.mock('vue-sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
  }
}))

vi.mock('@/composables/workflow/useWorkflowActions', () => ({
  useWorkflowActions: () => ({
    actionLoading: ref(false),
    urgeWorkflowAction: actionMocks.urgeWorkflowAction,
    withdrawWorkflowAction: actionMocks.withdrawWorkflowAction,
    cancelWorkflowAction: actionMocks.cancelWorkflowAction
  })
}))

vi.mock('@/composables/workflow/useWorkflowForm', () => ({
  useWorkflowForm: () => ({
    formConfig: ref({ sections: [] }),
    formValues: formValuesRef,
    fieldErrors: fieldErrorsRef,
    initForm,
    validateForm,
    buildSubmitPayload
  })
}))

const WorkflowActionButtonsStub = defineComponent({
  name: 'WorkflowActionButtons',
  props: {
    workflow: {
      type: Object,
      required: true
    }
  },
  emits: ['edit-draft', 'submit-draft', 'delete-draft', 'urge', 'withdraw', 'cancel'],
  setup(props, { emit }) {
    return () =>
      h('div', { class: 'action-buttons-stub' }, [
        h('button', { class: `detail-${(props.workflow as { id: string }).id}` }, '详情'),
        h(
          'button',
          { class: `edit-draft-${(props.workflow as { id: string }).id}`, onClick: () => emit('edit-draft', props.workflow) },
          '编辑'
        ),
        h(
          'button',
          { class: `submit-draft-${(props.workflow as { id: string }).id}`, onClick: () => emit('submit-draft', props.workflow) },
          '继续提交'
        ),
        h(
          'button',
          { class: `urge-${(props.workflow as { id: string }).id}`, onClick: () => emit('urge', props.workflow) },
          '催办'
        )
      ])
  }
})

const WorkflowListFiltersStub = defineComponent({
  name: 'WorkflowListFilters',
  props: {
    status: {
      type: String,
      default: 'ALL'
    }
  },
  emits: ['update:status', 'refresh'],
  setup(_props, { emit }) {
    return () =>
      h('div', { class: 'workflow-list-filters-stub' }, [
        h('button', { class: 'refresh-list', onClick: () => emit('refresh') }, '刷新'),
        h('button', { class: 'set-draft-filter', onClick: () => emit('update:status', 'DRAFT') }, '草稿')
      ])
  }
})

const WorkflowStartDialogStub = defineComponent({
  name: 'WorkflowStartDialog',
  props: {
    open: Boolean,
    title: {
      type: String,
      default: ''
    }
  },
  emits: ['update:open', 'save-draft', 'submit'],
  setup(props, { emit }) {
    return () =>
      h('div', { class: 'workflow-start-dialog-stub', 'data-open': String(props.open) }, [
        h('div', { class: 'draft-title' }, props.title),
        h('button', { class: 'save-draft', onClick: () => emit('save-draft') }, '保存'),
        h('button', { class: 'submit-draft', onClick: () => emit('submit') }, '提交')
      ])
  }
})

const DialogStub = defineComponent({
  name: 'Dialog',
  props: {
    open: {
      type: Boolean,
      default: false
    }
  },
  setup(_props, { slots }) {
    return () => h('div', { class: 'dialog-stub' }, slots.default?.())
  }
})

const DialogContentStub = defineComponent({
  name: 'DialogContent',
  setup(_props, { slots }) {
    return () => h('div', { class: 'dialog-content-stub' }, slots.default?.())
  }
})

const DialogHeaderStub = defineComponent({
  name: 'DialogHeader',
  setup(_props, { slots }) {
    return () => h('div', { class: 'dialog-header-stub' }, slots.default?.())
  }
})

const DialogTitleStub = defineComponent({
  name: 'DialogTitle',
  setup(_props, { slots }) {
    return () => h('div', { class: 'dialog-title-stub' }, slots.default?.())
  }
})

const DialogFooterStub = defineComponent({
  name: 'DialogFooter',
  setup(_props, { slots }) {
    return () => h('div', { class: 'dialog-footer-stub' }, slots.default?.())
  }
})

const TextareaStub = defineComponent({
  name: 'Textarea',
  props: {
    modelValue: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h('textarea', {
        value: props.modelValue,
        onInput: (event: Event) => emit('update:modelValue', (event.target as HTMLTextAreaElement).value)
      })
  }
})

describe('MyWorkflow.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    formValuesRef.value = {}
    fieldErrorsRef.value = {}

    apiMocks.getMyWorkflows.mockResolvedValue({
      data: [
        {
          id: 'draft-001',
          definitionId: 'def-001',
          definitionName: '请假审批',
          userId: 'user-001',
          userName: '张三',
          deptId: 'dept-001',
          title: '请假审批申请',
          businessData: '{}',
          currentNodeId: '',
          currentNodeName: '',
          status: 'DRAFT',
          submitTime: null,
          finishTime: null,
          remark: '待完善',
          createTime: '2026-03-27T08:00:00Z',
          pendingApproval: false,
          canApprove: false
        },
        {
          id: 'inst-002',
          definitionId: 'def-002',
          definitionName: '费用报销',
          userId: 'user-001',
          userName: '张三',
          deptId: 'dept-001',
          title: '费用报销申请',
          businessData: '{}',
          currentNodeId: 'node-001',
          currentNodeName: '财务审批',
          status: 'PROCESSING',
          submitTime: '2026-03-27T09:00:00Z',
          finishTime: null,
          remark: '',
          createTime: '2026-03-27T09:00:00Z',
          pendingApproval: false,
          canApprove: false
        }
      ]
    })
    apiMocks.getEnabledWorkflowDefinitions.mockResolvedValue({
      data: [
        {
          id: 'def-001',
          definitionName: '请假审批',
          definitionKey: 'leave',
          category: '人事',
          description: '请假流程',
          status: 1,
          version: 1,
          formConfig: { sections: [] },
          nodeCount: 2,
          createTime: '2026-03-27T08:00:00Z',
          updateTime: '2026-03-27T08:00:00Z'
        }
      ]
    })
    apiMocks.getWorkflowDraftDetail.mockResolvedValue({
      data: {
        instance: {
          id: 'draft-001',
          definitionId: 'def-001',
          definitionName: '请假审批',
          userId: 'user-001',
          userName: '张三',
          deptId: 'dept-001',
          title: '请假审批申请',
          businessData: '{}',
          currentNodeId: '',
          currentNodeName: '',
          status: 'DRAFT',
          submitTime: null,
          finishTime: null,
          remark: '待完善',
          createTime: '2026-03-27T08:00:00Z',
          pendingApproval: false,
          canApprove: false
        },
        formConfig: { sections: [] },
        formData: {
          reason: '补交资料'
        }
      }
    })
    apiMocks.getWorkflowDefinition.mockResolvedValue({
      data: {
        id: 'def-001',
        definitionName: '请假审批',
        definitionKey: 'leave',
        category: '人事',
        description: '请假流程',
        status: 1,
        version: 1,
        formConfig: { sections: [] },
        nodeCount: 2,
        createTime: '2026-03-27T08:00:00Z',
        updateTime: '2026-03-27T08:00:00Z'
      }
    })
    apiMocks.updateWorkflowDraft.mockResolvedValue({ data: { id: 'draft-001' } })
    apiMocks.submitWorkflow.mockResolvedValue({ data: { id: 'inst-002' } })
    actionMocks.urgeWorkflowAction.mockResolvedValue({ data: { id: 'urge-001' } })
    actionMocks.withdrawWorkflowAction.mockResolvedValue(true)
    actionMocks.cancelWorkflowAction.mockResolvedValue(true)
  })

  it('loads my workflows on mount', async () => {
    const wrapper = mount(MyWorkflow, {
      global: {
        stubs: {
          WorkflowActionButtons: WorkflowActionButtonsStub,
          WorkflowListFilters: WorkflowListFiltersStub,
          WorkflowStartDialog: WorkflowStartDialogStub,
          Dialog: DialogStub,
          Textarea: TextareaStub,
          DialogContent: DialogContentStub,
          DialogHeader: DialogHeaderStub,
          DialogTitle: DialogTitleStub,
          DialogFooter: DialogFooterStub
        }
      }
    })

    await flushPromises()

    expect(apiMocks.getMyWorkflows).toHaveBeenCalledWith(undefined)
    expect(apiMocks.getEnabledWorkflowDefinitions).toHaveBeenCalled()
    expect(wrapper.text()).toContain('请假审批申请')
    expect(wrapper.text()).toContain('费用报销申请')
  })

  it('opens urge dialog and sends urge content', async () => {
    const wrapper = mount(MyWorkflow, {
      global: {
        stubs: {
          WorkflowActionButtons: WorkflowActionButtonsStub,
          WorkflowListFilters: WorkflowListFiltersStub,
          WorkflowStartDialog: WorkflowStartDialogStub,
          Dialog: DialogStub,
          Textarea: TextareaStub,
          DialogContent: DialogContentStub,
          DialogHeader: DialogHeaderStub,
          DialogTitle: DialogTitleStub,
          DialogFooter: DialogFooterStub
        }
      }
    })

    await flushPromises()
    await wrapper.find('.urge-inst-002').trigger('click')
    await wrapper.find('textarea').setValue('请尽快审批')
    await wrapper.findAll('button').find((button) => button.text() === '发送催办')!.trigger('click')
    await flushPromises()

    expect(actionMocks.urgeWorkflowAction).toHaveBeenCalledWith('inst-002', {
      content: '请尽快审批'
    })
  })

  it('loads draft detail and saves structured draft form data', async () => {
    const wrapper = mount(MyWorkflow, {
      global: {
        stubs: {
          WorkflowActionButtons: WorkflowActionButtonsStub,
          WorkflowListFilters: WorkflowListFiltersStub,
          WorkflowStartDialog: WorkflowStartDialogStub,
          Dialog: DialogStub,
          Textarea: TextareaStub,
          DialogContent: DialogContentStub,
          DialogHeader: DialogHeaderStub,
          DialogTitle: DialogTitleStub,
          DialogFooter: DialogFooterStub
        }
      }
    })

    await flushPromises()
    await wrapper.find('.edit-draft-draft-001').trigger('click')
    await flushPromises()
    await wrapper.find('.save-draft').trigger('click')
    await flushPromises()

    expect(apiMocks.getWorkflowDraftDetail).toHaveBeenCalledWith('draft-001')
    expect(apiMocks.updateWorkflowDraft).toHaveBeenCalledWith('draft-001', {
      definitionId: 'def-001',
      title: '请假审批申请',
      formData: {
        reason: '补交资料',
        days: 1
      },
      remark: '待完善'
    })
  })
})
