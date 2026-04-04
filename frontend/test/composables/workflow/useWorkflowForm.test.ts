import { describe, expect, it } from 'vitest'
import { useWorkflowForm } from '@/composables/workflow/useWorkflowForm'

describe('useWorkflowForm', () => {
  it('initializes form values from config defaults and raw data', () => {
    const { formValues, initForm } = useWorkflowForm()

    initForm(
      {
        sections: [
          {
            key: 'basic',
            title: '基础信息',
            fields: [
              {
                field: 'reason',
                label: '申请事由',
                component: 'input',
                defaultValue: '默认事由'
              },
              {
                field: 'days',
                label: '请假天数',
                component: 'number'
              }
            ]
          }
        ]
      },
      {
        days: 2
      }
    )

    expect(formValues.value).toEqual({
      reason: '默认事由',
      days: 2
    })
  })

  it('validates required fields and exposes field errors', () => {
    const { fieldErrors, initForm, validateForm } = useWorkflowForm()

    initForm({
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
    })

    expect(validateForm()).toBe(false)
    expect(fieldErrors.value.reason).toBe('请输入申请事由')
  })

  it('builds submit payload from current form values', () => {
    const { buildSubmitPayload, initForm, setFieldValue } = useWorkflowForm()

    initForm({
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
    })

    setFieldValue('reason', '出差申请')

    expect(buildSubmitPayload()).toEqual({
      formData: {
        reason: '出差申请'
      }
    })
  })
})
