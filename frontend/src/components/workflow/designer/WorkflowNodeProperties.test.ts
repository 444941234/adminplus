import { describe, expect, it } from 'vitest'
import { defineComponent, h } from 'vue'
import { mount } from '@vue/test-utils'
import WorkflowNodeProperties from './WorkflowNodeProperties.vue'

const InputStub = defineComponent({
  name: 'Input',
  props: {
    modelValue: {
      type: [String, Number],
      default: ''
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h('input', {
        value: props.modelValue,
        onInput: (event: Event) => emit('update:modelValue', (event.target as HTMLInputElement).value)
      })
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

const SelectStub = defineComponent({
  name: 'Select',
  props: {
    modelValue: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue'],
  setup(props, { slots, emit }) {
    return () =>
      h('div', { class: 'select-stub', 'data-value': props.modelValue }, [
        slots.default?.(),
        h('button', { class: 'set-role', onClick: () => emit('update:modelValue', 'role') }, 'role'),
        h('button', { class: 'set-leader', onClick: () => emit('update:modelValue', 'leader') }, 'leader')
      ])
  }
})

const CheckboxStub = defineComponent({
  name: 'Checkbox',
  props: {
    checked: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:checked'],
  setup(props, { emit }) {
    return () =>
      h('input', {
        type: 'checkbox',
        checked: props.checked,
        onChange: (event: Event) => emit('update:checked', (event.target as HTMLInputElement).checked)
      })
  }
})

const WorkflowAssigneeSelectorStub = defineComponent({
  name: 'WorkflowAssigneeSelector',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    approverType: {
      type: String,
      default: 'user'
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h('div', { class: 'assignee-selector-stub', 'data-type': props.approverType }, [
        h('button', { class: 'set-assignee', onClick: () => emit('update:modelValue', 'role-002') }, 'select-assignee')
      ])
  }
})

const baseForm = {
  nodeName: '部门经理审批',
  nodeCode: 'dept_manager',
  nodeOrder: 2,
  approverType: 'user' as const,
  approverId: 'user-001',
  isCounterSign: false,
  autoPassSameUser: false,
  description: '节点说明'
}

describe('WorkflowNodeProperties.vue', () => {
  it('emits updated model when basic text fields change', async () => {
    const wrapper = mount(WorkflowNodeProperties, {
      props: {
        modelValue: baseForm
      },
      global: {
        stubs: {
          Input: InputStub,
          Textarea: TextareaStub,
          Select: SelectStub,
          SelectContent: true,
          SelectItem: true,
          SelectTrigger: true,
          SelectValue: true,
          Checkbox: CheckboxStub,
          WorkflowAssigneeSelector: WorkflowAssigneeSelectorStub
        }
      }
    })

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('财务审批')

    const events = wrapper.emitted('update:modelValue')
    expect(events).toBeTruthy()
    expect(events?.[0]?.[0]).toMatchObject({
      nodeName: '财务审批'
    })
  })

  it('emits updated approver type and assignee value', async () => {
    const wrapper = mount(WorkflowNodeProperties, {
      props: {
        modelValue: baseForm
      },
      global: {
        stubs: {
          Input: InputStub,
          Textarea: TextareaStub,
          Select: SelectStub,
          SelectContent: true,
          SelectItem: true,
          SelectTrigger: true,
          SelectValue: true,
          Checkbox: CheckboxStub,
          WorkflowAssigneeSelector: WorkflowAssigneeSelectorStub
        }
      }
    })

    await wrapper.find('.set-role').trigger('click')
    await wrapper.find('.set-assignee').trigger('click')

    const events = wrapper.emitted('update:modelValue')
    expect(events?.[0]?.[0]).toMatchObject({
      approverType: 'role'
    })
    expect(events?.[1]?.[0]).toMatchObject({
      approverId: 'role-002'
    })
  })

  it('emits updated checkbox and description values', async () => {
    const wrapper = mount(WorkflowNodeProperties, {
      props: {
        modelValue: baseForm
      },
      global: {
        stubs: {
          Input: InputStub,
          Textarea: TextareaStub,
          Select: SelectStub,
          SelectContent: true,
          SelectItem: true,
          SelectTrigger: true,
          SelectValue: true,
          Checkbox: CheckboxStub,
          WorkflowAssigneeSelector: WorkflowAssigneeSelectorStub
        }
      }
    })

    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    await checkboxes[0].setValue(true)
    await wrapper.find('textarea').setValue('新描述')

    const events = wrapper.emitted('update:modelValue')
    expect(events?.[0]?.[0]).toMatchObject({
      isCounterSign: true
    })
    expect(events?.[1]?.[0]).toMatchObject({
      description: '新描述'
    })
  })
})
