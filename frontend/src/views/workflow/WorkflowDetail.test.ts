import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import WorkflowDetailView from './WorkflowDetail.vue'
import type { WorkflowDetail as WorkflowDetailData } from '@/types'

const apiMocks = vi.hoisted(() => ({
  getWorkflowDetail: vi.fn(),
  getRollbackableNodes: vi.fn(),
  getInstanceUrgeRecords: vi.fn()
}))

const actionMocks = vi.hoisted(() => ({
  approveWorkflowAction: vi.fn(),
  rejectWorkflowAction: vi.fn(),
  rollbackWorkflowAction: vi.fn(),
  addSignWorkflowAction: vi.fn()
}))

const toastMocks = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: 'inst-001' } })
}))

vi.mock('@/api', () => ({
  getWorkflowDetail: apiMocks.getWorkflowDetail,
  getRollbackableNodes: apiMocks.getRollbackableNodes,
  getInstanceUrgeRecords: apiMocks.getInstanceUrgeRecords
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    hasPermission: vi.fn(() => true)
  })
}))

vi.mock('vue-sonner', () => ({
  toast: toastMocks
}))

vi.mock('@/lib/page-permissions', () => ({
  getWorkflowPermissionState: () => ({
    canApproveDetail: true,
    canRejectDetail: true,
    canRollbackDetail: true,
    canAddSignDetail: true
  })
}))

vi.mock('@/composables/workflow/useWorkflowActions', () => ({
  useWorkflowActions: () => ({
    actionLoading: { value: false },
    approveWorkflowAction: actionMocks.approveWorkflowAction,
    rejectWorkflowAction: actionMocks.rejectWorkflowAction,
    rollbackWorkflowAction: actionMocks.rollbackWorkflowAction,
    addSignWorkflowAction: actionMocks.addSignWorkflowAction
  })
}))

const WorkflowOverviewCardStub = defineComponent({
  name: 'WorkflowOverviewCard',
  props: {
    instance: {
      type: Object,
      default: null
    }
  },
  setup(props) {
    return () => h('div', { class: 'overview-card-stub' }, props.instance ? String((props.instance as { title?: string }).title || '') : '')
  }
})

const WorkflowBusinessCardStub = defineComponent({
  name: 'WorkflowBusinessCard',
  props: {
    config: {
      type: [Object, String],
      default: null
    },
    formData: {
      type: Object,
      default: null
    }
  },
  setup(props) {
    return () => h('div', { class: 'business-card-stub' }, JSON.stringify(props.formData || {}))
  }
})

const WorkflowVisualizerStub = defineComponent({
  name: 'WorkflowVisualizer',
  props: {
    instanceId: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    return () => h('div', { class: 'visualizer-stub', 'data-instance-id': props.instanceId }, props.instanceId)
  }
})

const WorkflowTimelineTabsStub = defineComponent({
  name: 'WorkflowTimelineTabs',
  props: {
    approvals: {
      type: Array,
      default: () => []
    },
    ccRecords: {
      type: Array,
      default: () => []
    },
    urgeRecords: {
      type: Array,
      default: () => []
    },
    addSignRecords: {
      type: Array,
      default: () => []
    }
  },
  setup(props) {
    return () => h('div', { class: 'timeline-tabs-stub' }, [
      h('span', { class: 'approvals-count' }, String(props.approvals.length)),
      h('span', { class: 'cc-count' }, String(props.ccRecords.length)),
      h('span', { class: 'urge-count' }, String(props.urgeRecords.length)),
      h('span', { class: 'add-sign-count' }, String(props.addSignRecords.length))
    ])
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
    return () => h('textarea', {
      value: props.modelValue,
      onInput: (event: Event) => emit('update:modelValue', (event.target as HTMLTextAreaElement).value)
    })
  }
})

const workflowDetail: WorkflowDetailData = {
  instance: {
    id: 'inst-001',
    definitionId: 'def-001',
    definitionName: '请假审批',
    userId: 'user-001',
    userName: '张三',
    deptId: 'dept-001',
    deptName: '技术部',
    title: '年假申请',
    businessData: '{}',
    currentNodeId: 'node-002',
    currentNodeName: '部门经理审批',
    status: 'PROCESSING',
    submitTime: '2026-03-27T08:00:00Z',
    finishTime: null,
    remark: '请尽快处理',
    createTime: '2026-03-27T08:00:00Z',
    pendingApproval: true,
    canApprove: true,
    canWithdraw: false,
    canCancel: false,
    canUrge: false,
    canEditDraft: false,
    canSubmitDraft: false
  },
  approvals: [
    {
      id: 'approval-001',
      instanceId: 'inst-001',
      nodeId: 'node-001',
      nodeName: '发起',
      approverId: 'user-001',
      approverName: '张三',
      approvalStatus: 'APPROVED',
      comment: '已提交',
      attachments: null,
      approvalTime: '2026-03-27T08:00:00Z',
      createTime: '2026-03-27T08:00:00Z'
    }
  ],
  nodes: [
    {
      id: 'node-001',
      definitionId: 'def-001',
      nodeName: '发起',
      nodeCode: 'start',
      nodeOrder: 1,
      approverType: 'user',
      approverId: 'user-001',
      isCounterSign: false,
      autoPassSameUser: false,
      description: '发起节点',
      createTime: '2026-03-27T08:00:00Z'
    },
    {
      id: 'node-002',
      definitionId: 'def-001',
      nodeName: '部门经理审批',
      nodeCode: 'manager',
      nodeOrder: 2,
      approverType: 'role',
      approverId: 'role-001',
      isCounterSign: false,
      autoPassSameUser: false,
      description: '审批节点',
      createTime: '2026-03-27T08:10:00Z'
    }
  ],
  currentNode: {
    id: 'node-002',
    definitionId: 'def-001',
    nodeName: '部门经理审批',
    nodeCode: 'manager',
    nodeOrder: 2,
    approverType: 'role',
    approverId: 'role-001',
    isCounterSign: false,
    autoPassSameUser: false,
    description: '审批节点',
    createTime: '2026-03-27T08:10:00Z'
  },
  canApprove: true,
  formConfig: {
    sections: [
      {
        key: 'basic',
        title: '基础信息',
        fields: [
          {
            field: 'reason',
            label: '申请事由',
            component: 'input'
          }
        ]
      }
    ]
  },
  formData: {
    reason: '年假申请'
  },
  ccRecords: [
    {
      id: 'cc-001',
      instanceId: 'inst-001',
      nodeId: 'node-002',
      nodeName: '部门经理审批',
      userId: 'user-002',
      userName: '李四',
      ccType: 'approve',
      ccContent: '审批通过抄送',
      isRead: false,
      readTime: null,
      createTime: '2026-03-27T09:00:00Z'
    }
  ],
  addSignRecords: [
    {
      id: 'add-sign-001',
      instanceId: 'inst-001',
      nodeId: 'node-002',
      nodeName: '部门经理审批',
      initiatorId: 'user-010',
      initiatorName: '审批主管',
      addUserId: 'user-011',
      addUserName: '会签人',
      addType: 'BEFORE',
      addReason: '补充审核',
      createTime: '2026-03-27T09:20:00Z'
    }
  ]
}

describe('WorkflowDetail.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    apiMocks.getWorkflowDetail.mockResolvedValue({ data: workflowDetail })
    apiMocks.getInstanceUrgeRecords.mockResolvedValue({
      data: [
        {
          id: 'urge-001',
          instanceId: 'inst-001',
          nodeId: 'node-002',
          nodeName: '部门经理审批',
          urgeUserId: 'user-001',
          urgeUserName: '张三',
          urgeTargetId: 'user-002',
          urgeTargetName: '李四',
          urgeContent: '请尽快审批',
          isRead: false,
          readTime: null,
          createTime: '2026-03-27T09:10:00Z'
        }
      ]
    })
    actionMocks.approveWorkflowAction.mockResolvedValue({ data: { id: 'inst-001' } })
    actionMocks.rejectWorkflowAction.mockResolvedValue({ data: { id: 'inst-001' } })
    actionMocks.rollbackWorkflowAction.mockResolvedValue({ data: { id: 'inst-001' } })
    actionMocks.addSignWorkflowAction.mockResolvedValue({ data: { id: 'add-sign-001' } })
  })

  it('loads detail data and passes instanceId to workflow visualizer', async () => {
    const wrapper = mount(WorkflowDetailView, {
      global: {
        stubs: {
          WorkflowOverviewCard: WorkflowOverviewCardStub,
          WorkflowBusinessCard: WorkflowBusinessCardStub,
          WorkflowVisualizer: WorkflowVisualizerStub,
          WorkflowTimelineTabs: WorkflowTimelineTabsStub,
          Textarea: TextareaStub
        }
      }
    })

    await flushPromises()

    expect(apiMocks.getWorkflowDetail).toHaveBeenCalledWith('inst-001')
    expect(wrapper.find('.overview-card-stub').text()).toContain('年假申请')
    expect(wrapper.find('.business-card-stub').text()).toContain('年假申请')
    expect(wrapper.find('.visualizer-stub').attributes('data-instance-id')).toBe('inst-001')
  })

  it('passes aggregated record counts to timeline tabs', async () => {
    const wrapper = mount(WorkflowDetailView, {
      global: {
        stubs: {
          WorkflowOverviewCard: WorkflowOverviewCardStub,
          WorkflowBusinessCard: WorkflowBusinessCardStub,
          WorkflowVisualizer: WorkflowVisualizerStub,
          WorkflowTimelineTabs: WorkflowTimelineTabsStub,
          Textarea: TextareaStub
        }
      }
    })

    await flushPromises()

    expect(wrapper.find('.approvals-count').text()).toBe('1')
    expect(wrapper.find('.cc-count').text()).toBe('1')
    expect(wrapper.find('.urge-count').text()).toBe('1')
    expect(wrapper.find('.add-sign-count').text()).toBe('1')
  })

  it('uses empty arrays when detail has no ccRecords/addSignRecords', async () => {
    const detailWithoutRecords = {
      ...workflowDetail,
      ccRecords: undefined,
      addSignRecords: undefined
    }
    apiMocks.getWorkflowDetail.mockResolvedValue({ data: detailWithoutRecords })

    const wrapper = mount(WorkflowDetailView, {
      global: {
        stubs: {
          WorkflowOverviewCard: WorkflowOverviewCardStub,
          WorkflowBusinessCard: WorkflowBusinessCardStub,
          WorkflowVisualizer: WorkflowVisualizerStub,
          WorkflowTimelineTabs: WorkflowTimelineTabsStub,
          Textarea: TextareaStub
        }
      }
    })

    await flushPromises()

    expect(wrapper.find('.cc-count').text()).toBe('0')
    expect(wrapper.find('.add-sign-count').text()).toBe('0')
  })

  it('renders approval action area when detail is approvable', async () => {
    const wrapper = mount(WorkflowDetailView, {
      global: {
        stubs: {
          WorkflowOverviewCard: WorkflowOverviewCardStub,
          WorkflowBusinessCard: WorkflowBusinessCardStub,
          WorkflowVisualizer: WorkflowVisualizerStub,
          WorkflowTimelineTabs: WorkflowTimelineTabsStub,
          Textarea: TextareaStub,
          Dialog: true,
          DialogContent: true,
          DialogHeader: true,
          DialogTitle: true,
          DialogFooter: true,
          Select: true,
          SelectContent: true,
          SelectItem: true,
          SelectTrigger: true,
          SelectValue: true
        }
      }
    })

    await flushPromises()

    const buttonTexts = wrapper.findAll('button').map((button) => button.text())
    expect(buttonTexts).toContain('通过')
    expect(buttonTexts).toContain('驳回')
    expect(buttonTexts).toContain('回退')
    expect(buttonTexts).toContain('加签/转办')
  })
})
