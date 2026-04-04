import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import WorkflowFormRenderer from '@/components/workflow/WorkflowFormRenderer.vue'
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
          component: 'input',
          required: true
        }
      ]
    }
  ]
}

describe('WorkflowFormRenderer.vue', () => {
  it('shows empty text when config is empty', () => {
    const wrapper = mount(WorkflowFormRenderer, {
      props: {
        config: null,
        modelValue: {},
        emptyText: '暂无申请表单'
      }
    })

    expect(wrapper.text()).toContain('暂无申请表单')
  })

  it('renders configured section title and field label', () => {
    const wrapper = mount(WorkflowFormRenderer, {
      props: {
        config: workflowFormConfig,
        modelValue: {
          reason: '年假申请'
        }
      }
    })

    expect(wrapper.text()).toContain('基础信息')
    expect(wrapper.text()).toContain('申请事由')
  })

  it('emits updated model value when field value changes', async () => {
    const wrapper = mount(WorkflowFormRenderer, {
      props: {
        config: workflowFormConfig,
        modelValue: {
          reason: ''
        }
      }
    })

    const input = wrapper.find('input')
    await input.setValue('调休申请')

    const events = wrapper.emitted('update:modelValue')
    expect(events).toBeTruthy()
    expect(events?.[0]?.[0]).toEqual({
      reason: '调休申请'
    })
  })
})
