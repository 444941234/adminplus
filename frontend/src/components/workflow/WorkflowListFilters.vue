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

defineProps<{
  status: string
}>()

const emit = defineEmits<{
  (_e: 'update:status', _value: string): void
  (_e: 'refresh'): void
}>()
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
        <SelectItem value="DRAFT">
          草稿
        </SelectItem>
        <SelectItem value="PENDING">
          审批中
        </SelectItem>
        <SelectItem value="APPROVED">
          已通过
        </SelectItem>
        <SelectItem value="REJECTED">
          已驳回
        </SelectItem>
        <SelectItem value="CANCELLED">
          已取消
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
