<script setup lang="ts">
import WorkflowFormField from '@/components/workflow/WorkflowFormField.vue'
import type { WorkflowFormSection as WorkflowFormSectionType, WorkflowFormValues } from '@/types'

defineProps<{
  section: WorkflowFormSectionType
  modelValue: WorkflowFormValues
  errors?: Record<string, string>
  readonly?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: WorkflowFormValues): void
}>()

const updateFieldValue = (field: string, value: unknown, modelValue: WorkflowFormValues) => {
  emit('update:modelValue', {
    ...modelValue,
    [field]: value
  })
}
</script>

<template>
  <section class="space-y-4 rounded-lg border border-border/60 p-4">
    <header class="space-y-1">
      <h3 class="text-sm font-semibold">{{ section.title }}</h3>
    </header>

    <div class="grid gap-4 md:grid-cols-2">
      <WorkflowFormField
        v-for="field in section.fields"
        :key="field.field"
        :field="field"
        :model-value="modelValue[field.field]"
        :error="errors?.[field.field]"
        :readonly="readonly"
        @update:model-value="(value) => updateFieldValue(field.field, value, modelValue)"
      />
    </div>
  </section>
</template>
