import { describe, expect, it } from 'vitest'
import { mount, RouterLinkStub } from '@vue/test-utils'
import WorkflowActionButtons from '@/components/workflow/WorkflowActionButtons.vue'
import type { WorkflowInstance } from '@/types'

const baseWorkflow: WorkflowInstance = {
  id: 'inst-001',
  definitionId: 'def-001',
  definitionName: '请假审批',
  userId: 'user-001',
  userName: '张三',
  deptId: 'dept-001',
  deptName: '技术部',
  title: '请假申请',
  businessData: '{}',
  currentNodeId: 'node-001',
  currentNodeName: '部门经理审批',
  status: 'PROCESSING',
  submitTime: '2026-03-27T08:00:00Z',
  finishTime: null,
  remark: '',
  createTime: '2026-03-27T08:00:00Z',
  pendingApproval: false,
  canApprove: false,
  canWithdraw: false,
  canCancel: false,
  canUrge: false,
  canEditDraft: false,
  canSubmitDraft: false
}

describe('WorkflowActionButtons.vue', () => {
  it('renders draft actions for draft workflow in my mode', () => {
    const wrapper = mount(WorkflowActionButtons, {
      props: {
        workflow: {
          ...baseWorkflow,
          status: 'DRAFT'
        },
        mode: 'my',
        canManage: true,
        canDraft: true
      },
      global: {
        stubs: {
          RouterLink: RouterLinkStub
        }
      }
    })

    expect(wrapper.text()).toContain('详情')
    expect(wrapper.text()).toContain('编辑')
    expect(wrapper.text()).toContain('继续提交')
    expect(wrapper.text()).toContain('删除')
  })

  it('renders processing actions for processing workflow in my mode', () => {
    const wrapper = mount(WorkflowActionButtons, {
      props: {
        workflow: baseWorkflow,
        mode: 'my',
        canManage: true,
        canUrge: true,
        canWithdraw: true,
        canCancel: true
      },
      global: {
        stubs: {
          RouterLink: RouterLinkStub
        }
      }
    })

    expect(wrapper.text()).toContain('详情')
    expect(wrapper.text()).toContain('催办')
    expect(wrapper.text()).toContain('撤回')
    expect(wrapper.text()).toContain('取消')
    expect(wrapper.text()).not.toContain('编辑')
  })

  it('renders approval actions for pending mode', () => {
    const wrapper = mount(WorkflowActionButtons, {
      props: {
        workflow: baseWorkflow,
        mode: 'pending',
        canApprove: true
      },
      global: {
        stubs: {
          RouterLink: RouterLinkStub
        }
      }
    })

    expect(wrapper.text()).toContain('详情')
    expect(wrapper.text()).toContain('通过')
    expect(wrapper.text()).toContain('驳回')
  })
})
