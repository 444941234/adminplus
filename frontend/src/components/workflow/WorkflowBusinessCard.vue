<script setup lang="ts">
import { computed } from 'vue'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui'
import WorkflowFormRenderer from '@/components/workflow/WorkflowFormRenderer.vue'
import { parseWorkflowFormConfig } from '@/composables/workflow/useWorkflowForm'
import type { WorkflowFormConfig, WorkflowFormValues } from '@/types'

const props = defineProps<{
  config?: string | WorkflowFormConfig | null
  formData?: WorkflowFormValues | null
}>()

const normalizedConfig = computed(() => parseWorkflowFormConfig(props.config))
const normalizedFormData = computed(() => props.formData ?? {})
const hasBusinessContent = computed(() => normalizedConfig.value.sections.length > 0)
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle>申请信息</CardTitle>
    </CardHeader>
    <CardContent>
      <WorkflowFormRenderer
        v-if="hasBusinessContent"
        :config="normalizedConfig"
        :model-value="normalizedFormData"
        readonly
      />
      <div
        v-else
        class="rounded-lg border border-dashed border-border p-6 text-center text-sm text-muted-foreground"
      >
        当前流程未配置申请表单
      </div>
    </CardContent>
  </Card>
</template>
