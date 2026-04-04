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

const emit = defineEmits<{
  (_e: 'toggle'): void
}>()

const handleClick = () => {
  emit('toggle')
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    emit('toggle')
  }
}
</script>

<template>
  <Badge
    :variant="status === 1 ? 'default' : 'destructive'"
    :role="clickable ? 'button' : undefined"
    :tabindex="clickable ? 0 : undefined"
    :aria-label="clickable ? `点击${status === 1 ? '禁用' : '启用'}` : undefined"
    :class="clickable ? 'cursor-pointer hover:opacity-80 transition-opacity focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2' : ''"
    @click="clickable && handleClick()"
    @keydown="clickable && handleKeydown($event)"
  >
    {{ status === 1 ? activeText : inactiveText }}
  </Badge>
</template>
