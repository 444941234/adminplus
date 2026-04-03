<script setup lang="ts">
import { computed } from 'vue'
import WorkflowFormSection from '@/components/workflow/WorkflowFormSection.vue'
import { parseWorkflowFormConfig } from '@/composables/workflow/useWorkflowForm'
import type { WorkflowFormConfig, WorkflowFormValues } from '@/types'

const props = defineProps<{
  config?: string | WorkflowFormConfig | null
  modelValue: WorkflowFormValues
  errors?: Record<string, string>
  readonly?: boolean
  emptyText?: string
}>()

const emit = defineEmits<{
  (_e: 'update:modelValue', _value: WorkflowFormValues): void
}>()

const normalizedConfig = computed(() => parseWorkflowFormConfig(props.config))
</script>

<template>
  <div class="space-y-4">
    <div
      v-if="normalizedConfig.sections.length === 0"
      class="rounded-lg border border-dashed border-border p-6 text-center text-sm text-muted-foreground"
    >
      {{ emptyText || '暂无表单配置' }}
    </div>

    <WorkflowFormSection
      v-for="section in normalizedConfig.sections"
      :key="section.key"
      :section="section"
      :model-value="modelValue"
      :errors="errors"
      :readonly="readonly"
      @update:model-value="(value) => emit('update:modelValue', value)"
    />
  </div>
</template>
