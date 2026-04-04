import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import WorkflowBusinessCard from '@/components/workflow/WorkflowBusinessCard.vue'
import type { WorkflowFormConfig } from '@/types'

const workflowFormConfig: WorkflowFormConfig = {
  sections: [
    {
      key: 'basic',
      title: '基础信息',
      fields: [
        {
          field: 'reason',
          label: '申请事由',
          component: 'input'
        },
        {
          field: 'days',
          label: '请假天数',
          component: 'number'
        }
      ]
    }
  ]
}

describe('WorkflowBusinessCard.vue', () => {
  it('shows fallback text when no form config exists', () => {
    const wrapper = mount(WorkflowBusinessCard, {
      props: {
        config: null,
        formData: null
      }
    })

    expect(wrapper.text()).toContain('当前流程未配置申请表单')
  })

  it('renders readonly business form content', () => {
    const wrapper = mount(WorkflowBusinessCard, {
      props: {
        config: workflowFormConfig,
        formData: {
          reason: '外出培训',
          days: 3
        }
      }
    })

    expect(wrapper.text()).toContain('申请信息')
    expect(wrapper.text()).toContain('基础信息')
    expect(wrapper.text()).toContain('申请事由')
    expect(wrapper.text()).toContain('外出培训')
    expect(wrapper.text()).toContain('请假天数')
    expect(wrapper.text()).toContain('3')
  })
})
