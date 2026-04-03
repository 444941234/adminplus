import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import WorkflowDesigner from './WorkflowDesigner.vue'

const apiMocks = vi.hoisted(() => ({
  getWorkflowDefinitions: vi.fn(),
  getWorkflowNodes: vi.fn(),
  createWorkflowDefinition: vi.fn(),
  updateWorkflowDefinition: vi.fn(),
  deleteWorkflowDefinition: vi.fn(),
  createWorkflowNode: vi.fn(),
  updateWorkflowNode: vi.fn(),
  deleteWorkflowNode: vi.fn()
}))

vi.mock('@/api', () => ({
  getWorkflowDefinitions: apiMocks.getWorkflowDefinitions,
  getWorkflowNodes: apiMocks.getWorkflowNodes,
  createWorkflowDefinition: apiMocks.createWorkflowDefinition,
  updateWorkflowDefinition: apiMocks.updateWorkflowDefinition,
  deleteWorkflowDefinition: apiMocks.deleteWorkflowDefinition,
  createWorkflowNode: apiMocks.createWorkflowNode,
  updateWorkflowNode: apiMocks.updateWorkflowNode,
  deleteWorkflowNode: apiMocks.deleteWorkflowNode
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

const WorkflowVisualizerStub = defineComponent({
  name: 'WorkflowVisualizer',
  props: {
    definitionId: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    return () => h('div', { class: 'workflow-visualizer-stub', 'data-definition-id': props.definitionId }, props.definitionId)
  }
})

const WorkflowNodePropertiesStub = defineComponent({
  name: 'WorkflowNodeProperties',
  props: {
    modelValue: {
      type: Object,
      required: true
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h('div', { class: 'workflow-node-properties-stub' }, [
        h('div', { class: 'node-name' }, String((props.modelValue as { nodeName?: string }).nodeName || '')),
        h(
          'button',
          {
            class: 'fill-node-form',
            onClick: () =>
              emit('update:modelValue', {
                ...(props.modelValue as Record<string, unknown>),
                nodeName: '财务复核',
                nodeCode: 'finance_review',
                nodeOrder: 3,
                approverType: 'role',
                approverId: 'role-002',
                isCounterSign: true,
                autoPassSameUser: false,
                description: '财务节点'
              })
          },
          'fill-node-form'
        )
      ])
  }
})

const DialogStub = defineComponent({
  name: 'Dialog',
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

const DialogDescriptionStub = defineComponent({
  name: 'DialogDescription',
  setup(_props, { slots }) {
    return () => h('p', { class: 'dialog-description-stub' }, slots.default?.())
  }
})

describe('WorkflowDesigner.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

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
    apiMocks.getWorkflowNodes.mockResolvedValue({
      data: [
        {
          id: 'node-001',
          definitionId: 'def-001',
          nodeName: '部门经理审批',
          nodeCode: 'dept_manager',
          nodeOrder: 1,
          approverType: 'user',
          approverId: 'user-001',
          isCounterSign: false,
          autoPassSameUser: false,
          description: '首审节点',
          createTime: '2026-03-27T08:10:00Z'
        }
      ]
    })
    apiMocks.createWorkflowDefinition.mockResolvedValue({ data: { id: 'def-002' } })
    apiMocks.updateWorkflowDefinition.mockResolvedValue({ data: { id: 'def-001' } })
    apiMocks.deleteWorkflowDefinition.mockResolvedValue({ data: true })
    apiMocks.createWorkflowNode.mockResolvedValue({ data: { id: 'node-002' } })
    apiMocks.updateWorkflowNode.mockResolvedValue({ data: { id: 'node-001' } })
    apiMocks.deleteWorkflowNode.mockResolvedValue({ data: true })
  })

  it('loads workflow definitions on mount', async () => {
    const wrapper = mount(WorkflowDesigner, {
      global: {
        stubs: {
          WorkflowVisualizer: WorkflowVisualizerStub,
          WorkflowNodeProperties: WorkflowNodePropertiesStub,
          Dialog: DialogStub,
          DialogContent: DialogContentStub,
          DialogHeader: DialogHeaderStub,
          DialogTitle: DialogTitleStub,
          DialogFooter: DialogFooterStub,
          DialogDescription: DialogDescriptionStub
        }
      }
    })

    await flushPromises()

    expect(apiMocks.getWorkflowDefinitions).toHaveBeenCalled()
    expect(wrapper.text()).toContain('请假审批')
    expect(wrapper.text()).toContain('leave_approval')
  })

  it('enters design mode and loads nodes for selected definition', async () => {
    const wrapper = mount(WorkflowDesigner, {
      global: {
        stubs: {
          WorkflowVisualizer: WorkflowVisualizerStub,
          WorkflowNodeProperties: WorkflowNodePropertiesStub,
          Dialog: DialogStub,
          DialogContent: DialogContentStub,
          DialogHeader: DialogHeaderStub,
          DialogTitle: DialogTitleStub,
          DialogFooter: DialogFooterStub,
          DialogDescription: DialogDescriptionStub
        }
      }
    })

    await flushPromises()
    await wrapper.findAll('button').find((button) => button.text().includes('设计'))!.trigger('click')
    await flushPromises()

    // Verify the visualizer receives the definition-id
    expect(wrapper.find('.workflow-visualizer-stub').attributes('data-definition-id')).toBe('def-001')
    // Verify view mode changed
    expect((wrapper.vm as any).viewMode).toBe('design')
    expect((wrapper.vm as any).selectedDefinition?.id).toBe('def-001')
  })

  it('creates a node with values from node properties panel', async () => {
    const wrapper = mount(WorkflowDesigner, {
      global: {
        stubs: {
          WorkflowVisualizer: WorkflowVisualizerStub,
          WorkflowNodeProperties: WorkflowNodePropertiesStub,
          Dialog: DialogStub,
          DialogContent: DialogContentStub,
          DialogHeader: DialogHeaderStub,
          DialogTitle: DialogTitleStub,
          DialogFooter: DialogFooterStub,
          DialogDescription: DialogDescriptionStub
        }
      }
    })

    await flushPromises()
    await wrapper.findAll('button').find((button) => button.text().includes('设计'))!.trigger('click')
    await flushPromises()
    await wrapper.findAll('button').find((button) => button.text().includes('添加节点'))!.trigger('click')
    await wrapper.find('.fill-node-form').trigger('click')
    await wrapper.findAll('button').find((button) => button.text() === '添加')!.trigger('click')
    await flushPromises()

    expect(apiMocks.createWorkflowNode).toHaveBeenCalledWith('def-001', {
      nodeName: '财务复核',
      nodeCode: 'finance_review',
      nodeOrder: 3,
      approverType: 'role',
      approverId: 'role-002',
      isCounterSign: true,
      autoPassSameUser: false,
      description: '财务节点'
    })
  })
})
