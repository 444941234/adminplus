import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import PendingApproval from '@/views/workflow/PendingApproval.vue'
import * as api from '@/api'
import { useUserStore } from '@/stores/user'

// Mock dependencies
vi.mock('@/api', () => ({
  getPendingWorkflows: vi.fn()
}))

vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn()
}))

vi.mock('@/composables/workflow/useWorkflowActions', () => ({
  useWorkflowActions: () => ({
    actionLoading: ref(false),
    approveWorkflowAction: vi.fn().mockResolvedValue(true),
    rejectWorkflowAction: vi.fn().mockResolvedValue(true)
  })
}))

vi.mock('@/lib/page-permissions', () => ({
  getWorkflowPermissionState: () => ({
    canApprovePendingActions: true
  })
}))

vi.mock('vue-sonner', () => ({
  toast: {
    warning: vi.fn(),
    success: vi.fn(),
    error: vi.fn()
  }
}))

const mockWorkflows = [
  {
    id: '1',
    title: '请假申请',
    userName: '张三',
    definitionName: '请假流程',
    currentNodeName: '部门审批',
    submitTime: '2026-04-01T10:00:00',
    createTime: '2026-04-01T09:00:00',
    status: 'running'
  },
  {
    id: '2',
    title: '报销申请',
    userName: '李四',
    definitionName: '报销流程',
    currentNodeName: '财务审批',
    submitTime: '2026-04-02T14:00:00',
    createTime: '2026-04-02T13:00:00',
    status: 'running'
  }
]

describe('PendingApproval', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    ;(useUserStore as any).mockReturnValue({
      hasPermission: vi.fn().mockReturnValue(true)
    })
  })

  const mountComponent = async (props: Record<string, unknown> = {}) => {
    ;(api.getPendingWorkflows as any).mockResolvedValue({
      data: mockWorkflows
    })

    const wrapper = mount(PendingApproval, {
      props,
      global: {
        stubs: {
          Card: {
            template: '<div><slot name="header" /><slot /></div>'
          },
          CardHeader: { template: '<div><slot /></div>' },
          CardTitle: { template: '<h3><slot /></h3>' },
          CardContent: { template: '<div><slot /></div>' },
          Table: { template: '<table><slot /></table>' },
          TableHeader: { template: '<thead><slot /></thead>' },
          TableBody: { template: '<tbody><slot /></tbody>' },
          TableRow: { template: '<tr><slot /></tr>' },
          TableHead: { template: '<th><slot /></th>' },
          TableCell: { template: '<td><slot /></td>' },
          Button: {
            template: '<button><slot /></button>',
            props: ['variant', 'disabled']
          },
          Dialog: {
            template: '<div v-if="open"><slot /></div>',
            props: ['open']
          },
          DialogContent: { template: '<div><slot /></div>' },
          DialogHeader: { template: '<div><slot /></div>' },
          DialogTitle: { template: '<h2><slot /></h2>' },
          DialogDescription: { template: '<p><slot /></p>' },
          DialogFooter: { template: '<div><slot /></div>' },
          Textarea: {
            template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue', 'placeholder']
          },
          WorkflowActionButtons: {
            template: '<div><button @click="$emit(\'approve\', workflow)">通过</button><button @click="$emit(\'reject\', workflow)">驳回</button></div>',
            props: ['workflow', 'mode', 'canApprove'],
            emits: ['approve', 'reject']
          }
        }
      }
    })

    // Wait for onMounted to complete
    await new Promise(resolve => setTimeout(resolve, 0))
    return wrapper
  }

  it('should render loading state initially', async () => {
    ;(api.getPendingWorkflows as any).mockImplementation(() => new Promise(() => {}))

    const wrapper = mount(PendingApproval, {
      global: {
        stubs: {
          Card: { template: '<div><slot name="header" /><slot /></div>' },
          CardHeader: { template: '<div><slot /></div>' },
          CardTitle: { template: '<h3><slot /></h3>' },
          CardContent: { template: '<div><slot /></div>' },
          Table: { template: '<table><slot /></table>' },
          TableHeader: { template: '<thead><slot /></thead>' },
          TableBody: { template: '<tbody><slot /></tbody>' },
          TableRow: { template: '<tr><slot /></tr>' },
          TableHead: { template: '<th><slot /></th>' },
          TableCell: { template: '<td><slot /></td>' }
        }
      }
    })

    // Wait a tick for loading to be set
    await new Promise(resolve => setTimeout(resolve, 0))
    const html = wrapper.html()
    expect(html.includes('加载中') || html.includes('待审批')).toBe(true)
  })

  it('should fetch and display pending workflows', async () => {
    const wrapper = await mountComponent()

    expect(api.getPendingWorkflows).toHaveBeenCalled()
    expect(wrapper.html()).toContain('请假申请')
    expect(wrapper.html()).toContain('报销申请')
  })

  it('should display empty state when no workflows', async () => {
    ;(api.getPendingWorkflows as any).mockResolvedValue({ data: [] })

    const wrapper = mount(PendingApproval, {
      global: {
        stubs: {
          Card: { template: '<div><slot name="header" /><slot /></div>' },
          CardHeader: { template: '<div><slot /></div>' },
          CardTitle: { template: '<h3><slot /></h3>' },
          CardContent: { template: '<div><slot /></div>' },
          Table: { template: '<table><slot /></table>' },
          TableHeader: { template: '<thead><slot /></thead>' },
          TableBody: { template: '<tbody><slot /></tbody>' },
          TableRow: { template: '<tr><slot /></tr>' },
          TableHead: { template: '<th><slot /></th>' },
          TableCell: { template: '<td><slot /></td>' }
        }
      }
    })

    await new Promise(resolve => setTimeout(resolve, 0))
    expect(wrapper.html()).toContain('当前没有待审批流程')
  })

  it('should display workflow information correctly', async () => {
    const wrapper = await mountComponent()

    expect(wrapper.html()).toContain('张三')
    expect(wrapper.html()).toContain('李四')
    expect(wrapper.html()).toContain('请假流程')
    expect(wrapper.html()).toContain('部门审批')
  })

  it('should show title', async () => {
    const wrapper = await mountComponent()

    expect(wrapper.html()).toContain('待我审批')
  })

  it('should render action buttons', async () => {
    const wrapper = await mountComponent()

    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })
})