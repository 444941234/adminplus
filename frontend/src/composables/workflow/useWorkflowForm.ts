import { computed, ref } from 'vue'
import type {
  WorkflowFormConfig,
  WorkflowFormField,
  WorkflowFormSection,
  WorkflowFormValues
} from '@/types'

const EMPTY_CONFIG: WorkflowFormConfig = {
  sections: []
}

const isPlainObject = (value: unknown): value is Record<string, unknown> => {
  return Object.prototype.toString.call(value) === '[object Object]'
}

const normalizeSections = (sections: unknown): WorkflowFormSection[] => {
  if (!Array.isArray(sections)) return []

  return sections
    .filter(isPlainObject)
    .map((section, index) => ({
      key: String(section.key || `section_${index + 1}`),
      title: String(section.title || `分组${index + 1}`),
      fields: Array.isArray(section.fields)
        ? section.fields.filter(isPlainObject).map((field) => ({
          field: String(field.field || ''),
          label: String(field.label || field.field || '未命名字段'),
          component: (field.component as WorkflowFormField['component']) || 'input',
          required: Boolean(field.required),
          readonly: Boolean(field.readonly),
          placeholder: field.placeholder ? String(field.placeholder) : undefined,
          defaultValue: field.defaultValue,
          options: Array.isArray(field.options)
            ? field.options
              .filter(isPlainObject)
              .map((option) => ({
                label: String(option.label || ''),
                value: typeof option.value === 'number' ? option.value : String(option.value || '')
              }))
            : undefined,
          rules: isPlainObject(field.rules)
            ? {
              min: typeof field.rules.min === 'number' ? field.rules.min : undefined,
              max: typeof field.rules.max === 'number' ? field.rules.max : undefined,
              pattern: field.rules.pattern ? String(field.rules.pattern) : undefined
            }
            : undefined,
          description: field.description ? String(field.description) : undefined
        })).filter((field) => field.field)
        : []
    }))
    .filter((section) => section.fields.length > 0)
}

export const parseWorkflowFormConfig = (config?: string | WorkflowFormConfig | null): WorkflowFormConfig => {
  if (!config) return EMPTY_CONFIG

  if (typeof config === 'string') {
    try {
      const parsed = JSON.parse(config) as unknown
      if (!isPlainObject(parsed)) return EMPTY_CONFIG
      return {
        sections: normalizeSections(parsed.sections)
      }
    } catch {
      return EMPTY_CONFIG
    }
  }

  return {
    sections: normalizeSections(config.sections)
  }
}

const parseWorkflowFormValues = (rawData?: WorkflowFormValues | string | null): WorkflowFormValues => {
  if (!rawData) return {}
  if (typeof rawData === 'string') {
    try {
      const parsed = JSON.parse(rawData) as unknown
      return isPlainObject(parsed) ? parsed : {}
    } catch {
      return {}
    }
  }
  return isPlainObject(rawData) ? { ...rawData } : {}
}

const getFieldDefaultValue = (field: WorkflowFormField) => {
  if (field.defaultValue !== undefined) return field.defaultValue
  if (field.component === 'number') return ''
  if (field.component === 'select') return ''
  if (field.component === 'date') return ''
  return ''
}

/**
 * 工作流表单状态管理
 */
export const useWorkflowForm = () => {
  const formConfig = ref<WorkflowFormConfig>(EMPTY_CONFIG)
  const formValues = ref<WorkflowFormValues>({})
  const fieldErrors = ref<Record<string, string>>({})

  const allFields = computed(() => {
    return formConfig.value.sections.flatMap((section) => section.fields)
  })

  const initForm = (config?: string | WorkflowFormConfig | null, rawData?: WorkflowFormValues | string | null) => {
    const normalizedConfig = parseWorkflowFormConfig(config)
    const normalizedValues = parseWorkflowFormValues(rawData)
    const nextValues: WorkflowFormValues = {}

    for (const field of normalizedConfig.sections.flatMap((section) => section.fields)) {
      nextValues[field.field] = normalizedValues[field.field] ?? getFieldDefaultValue(field)
    }

    formConfig.value = normalizedConfig
    formValues.value = nextValues
    fieldErrors.value = {}
  }

  const setFieldValue = (field: string, value: unknown) => {
    formValues.value = {
      ...formValues.value,
      [field]: value
    }

    if (fieldErrors.value[field]) {
      const nextErrors = { ...fieldErrors.value }
      delete nextErrors[field]
      fieldErrors.value = nextErrors
    }
  }

  const validateForm = () => {
    const nextErrors: Record<string, string> = {}

    for (const field of allFields.value) {
      const value = formValues.value[field.field]
      const normalizedValue = typeof value === 'string' ? value.trim() : value

      if (field.required && (normalizedValue === '' || normalizedValue === null || normalizedValue === undefined)) {
        nextErrors[field.field] = `请输入${field.label}`
        continue
      }

      if (typeof normalizedValue === 'string' && field.rules?.pattern) {
        const pattern = new RegExp(field.rules.pattern)
        if (normalizedValue && !pattern.test(normalizedValue)) {
          nextErrors[field.field] = `${field.label}格式不正确`
        }
      }

      if (typeof normalizedValue === 'string' && field.rules?.max !== undefined && normalizedValue.length > field.rules.max) {
        nextErrors[field.field] = `${field.label}长度不能超过${field.rules.max}`
      }
    }

    fieldErrors.value = nextErrors
    return Object.keys(nextErrors).length === 0
  }

  const buildSubmitPayload = () => {
    return {
      formData: { ...formValues.value }
    }
  }

  const resetForm = () => {
    initForm(formConfig.value)
  }

  return {
    formConfig,
    formValues,
    fieldErrors,
    allFields,
    initForm,
    setFieldValue,
    validateForm,
    buildSubmitPayload,
    resetForm
  }
}
