<script setup lang="ts">
import { computed } from 'vue'
import { Badge } from '@/components/ui/badge'
import { STATUS_ACTIVE, getStatusToggleLabel } from '@/constants/status'

withDefaults(defineProps<{
  /** 1 = 正常, 0 = 禁用 */
  status: number
  /** 是否可点击切换 */
  clickable?: boolean
  /** 正常状态文案 */
  activeText?: string
  /** 活动状态文案 */
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

const ariaLabel = computed(() =>
  clickable ? `点击${getStatusToggleLabel(status)}` : undefined
)
</script>

<template>
  <Badge
    :variant="status === STATUS_ACTIVE ? 'default' : 'destructive'"
    :role="clickable ? 'button' : undefined"
    :tabindex="clickable ? 0 : undefined"
    :aria-label="ariaLabel"
    :class="clickable ? 'cursor-pointer hover:opacity-80 transition-opacity focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2' : ''"
    @click="clickable && handleClick()"
    @keydown="clickable && handleKeydown($event)"
  >
    {{ status === STATUS_ACTIVE ? activeText : inactiveText }}
  </Badge>
</template>
