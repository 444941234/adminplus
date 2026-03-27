<script setup lang="ts">
import { computed } from 'vue'
import { Badge } from '@/components/ui'
import { getWorkflowStatusLabel, getWorkflowStatusVariant } from '@/composables/workflow/useWorkflowStatus'

const props = defineProps<{
  status?: string | null
}>()

const label = computed(() => getWorkflowStatusLabel(props.status))
const variant = computed(() => getWorkflowStatusVariant(props.status))
</script>

<template>
  <Badge :variant="variant">
    {{ label }}
  </Badge>
</template>
