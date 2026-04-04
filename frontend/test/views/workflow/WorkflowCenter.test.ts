import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { defineComponent, h, ref } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import WorkflowCenter from '@/views/workflow/WorkflowCenter.vue'

const apiMocks = vi.hoisted(() => ({
  getWorkflowDefinitions: vi.fn(),
  getEnabledWorkflowDefinitions: vi.fn(),
  getWorkflowDefinition: vi.fn(),
  createWorkflowDraft: vi.fn(),
  startWorkflow: vi.fn()
}))

const formValuesRef = ref<Record<string, unknown>>({})
const fieldErrorsRef = ref<Record<string, string>>({})
const initForm = vi.fn()
const validateForm = vi.fn(() => true)
const buildSubmitPayload = vi.fn(() => ({
  formData: {
    reason: '年假申请',
    days: 2
  }
}))

vi.mock('@/api', () => ({
  getWorkflowDefinitions: apiMocks.getWorkflowDefinitions,
  getEnabledWorkflowDefinitions: apiMocks.getEnabledWorkflowDefinitions,
  getWorkflowDefinition: apiMocks.getWorkflowDefinition,
  createWorkflowDraft: apiMocks.createWorkflowDraft,
  startWorkflow: apiMocks.startWorkflow
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

const WorkflowStartDialogStub = defineComponent({
  name: 'WorkflowStartDialog',
  props: {
    open: Boolean,
    title: {
      type: String,
      default: ''
    },
    definitionId: {
      type: String,
      default: ''
    }
  },
  emits: [
    'update:open',
    'update:definitionId',
    'update:title',
    'update:remark',
    'update:formValues',
    'save-draft',
    'submit'
  ],
  setup(props, { emit }) {
    return () =>
      h('div', { class: 'workflow-start-dialog-stub', 'data-open': String(props.open) }, [
        h('div', { class: 'dialog-title' }, props.title),
        h('button', { class: 'save-draft', onClick: () => emit('save-draft') }, 'save'),
        h('button', { class: 'submit-workflow', onClick: () => emit('submit') }, 'submit')
      ])
  }
})

describe('WorkflowCenter.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    formValuesRef.value = {}
    fieldErrorsRef.value = {}

    apiMocks.getWorkflowDefinitions.mockResolvedValue({
      data: [
        {
          id: 'def-001',
          definitionName: '请假审批',
          definitionKey: 'leave_approval',
          category: '人事',
          description: '请假流程',
          status: 1,
          version: 1,
          formConfig: '',
          nodeCount: 2,
          createTime: '2026-03-27T08:00:00Z',
          updateTime: '2026-03-27T08:00:00Z'
        }
      ]
    })
    apiMocks.getEnabledWorkflowDefinitions.mockResolvedValue({
      data: [
        {
          id: 'def-001'
        }
      ]
    })
    apiMocks.getWorkflowDefinition.mockResolvedValue({
      data: {
        id: 'def-001',
        definitionName: '请假审批',
        definitionKey: 'leave_approval',
        category: '人事',
        description: '请假流程',
        status: 1,
        version: 1,
        formConfig: {
          sections: [
            {
              key: 'basic',
              title: '基础信息',
              fields: [
                {
                  field: 'reason',
                  label: '申请事由',
                  component: 'input',
                  required: true
                }
              ]
            }
          ]
        },
        nodeCount: 2,
        createTime: '2026-03-27T08:00:00Z',
        updateTime: '2026-03-27T08:00:00Z'
      }
    })
    apiMocks.createWorkflowDraft.mockResolvedValue({ data: { id: 'draft-001' } })
    apiMocks.startWorkflow.mockResolvedValue({ data: { id: 'inst-001' } })
  })

  it('loads definitions and opens start dialog with selected definition', async () => {
    const wrapper = mount(WorkflowCenter, {
      global: {
        stubs: {
          WorkflowStartDialog: WorkflowStartDialogStub,
          Play: true
        }
      }
    })

    await flushPromises()
    expect(apiMocks.getWorkflowDefinitions).toHaveBeenCalled()
    expect(apiMocks.getEnabledWorkflowDefinitions).toHaveBeenCalled()

    await wrapper.find('button').trigger('click')
    await flushPromises()

    const dialog = wrapper.findComponent(WorkflowStartDialogStub)
    expect(dialog.exists()).toBe(true)
    expect(dialog.attributes('data-open')).toBe('true')
  })

  it('submits structured formData when saving draft', async () => {
    const wrapper = mount(WorkflowCenter, {
      global: {
        stubs: {
          WorkflowStartDialog: WorkflowStartDialogStub,
          Play: true
        }
      }
    })

    await flushPromises()
    const startButton = wrapper.findAll('button').find((button) => button.text().includes('立即发起'))
    expect(startButton).toBeTruthy()
    await startButton!.trigger('click')
    await flushPromises()

    await wrapper.find('.save-draft').trigger('click')
    await flushPromises()

    expect(apiMocks.createWorkflowDraft).toHaveBeenCalledWith({
      definitionId: 'def-001',
      title: '请假审批申请',
      formData: {
        reason: '年假申请',
        days: 2
      },
      remark: undefined
    })
  })

  it('submits structured formData when starting workflow', async () => {
    const wrapper = mount(WorkflowCenter, {
      global: {
        stubs: {
          WorkflowStartDialog: WorkflowStartDialogStub,
          Play: true
        }
      }
    })

    await flushPromises()
    const startButton = wrapper.findAll('button').find((button) => button.text().includes('立即发起'))
    expect(startButton).toBeTruthy()
    await startButton!.trigger('click')
    await flushPromises()

    await wrapper.find('.submit-workflow').trigger('click')
    await flushPromises()

    expect(apiMocks.startWorkflow).toHaveBeenCalledWith({
      definitionId: 'def-001',
      title: '请假审批申请',
      formData: {
        reason: '年假申请',
        days: 2
      },
      remark: undefined
    })
  })
})
