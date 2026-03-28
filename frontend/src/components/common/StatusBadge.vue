<script setup lang="ts">
import { Badge } from '@/components/ui/badge'

withDefaults(defineProps<{
  /** 1 = 正常, 0 = 禁用 */
  status: number
  /** 是否可点击切换 */
  clickable?: boolean
  /** 正常状态文案 */
  activeText?: string
  /** 紻动状态文案 */
  inactiveText?: string
}>(), {
  clickable: false,
  activeText: '正常',
  inactiveText: '禁用'
})

defineEmits<{
  (e: 'toggle'): void
}>()
</script>

<template>
  <Badge
    :variant="status === 1 ? 'default' : 'destructive'"
    :class="clickable ? 'cursor-pointer hover:opacity-80 transition-opacity' : ''"
    @click="clickable && $emit('toggle')"
  >
    {{ status === 1 ? activeText : inactiveText }}
  </Badge>
</template>
