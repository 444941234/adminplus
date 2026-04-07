<script setup lang="ts">
import {
  Button,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui'
import { RefreshCw } from 'lucide-vue-next'
import { useDict } from '@/composables/useDict'

defineProps<{
  status: string
}>()

const emit = defineEmits<{
  (_e: 'update:status', _value: string): void
  (_e: 'refresh'): void
}>()

// 字典数据
const { items: workflowStatusItems } = useDict('workflow_status')

// 状态码映射（前端使用英文码）
const statusCodeMap: Record<string, string> = {
  '0': 'DRAFT',
  '1': 'RUNNING',
  '2': 'COMPLETED',
  '3': 'REJECTED',
  '4': 'CANCELLED'
}
</script>

<template>
  <div class="flex items-center gap-3">
    <Select
      :model-value="status"
      @update:model-value="(value) => emit('update:status', String(value))"
    >
      <SelectTrigger class="w-[180px]">
        <SelectValue placeholder="全部状态" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="ALL">
          全部状态
        </SelectItem>
        <SelectItem
          v-for="item in workflowStatusItems"
          :key="item.value"
          :value="statusCodeMap[item.value] || item.value"
        >
          {{ item.label }}
        </SelectItem>
      </SelectContent>
    </Select>
    <Button
      variant="outline"
      @click="emit('refresh')"
    >
      <RefreshCw class="mr-2 h-4 w-4" />
      刷新
    </Button>
  </div>
</template>
