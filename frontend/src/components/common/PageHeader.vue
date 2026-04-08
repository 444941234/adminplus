<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Search } from '@lucide/vue'

defineProps<{
  /** 搜索关键词（v-model） */
  search?: string
  /** 输入框 placeholder */
  placeholder?: string
  /** 是否显示重置按钮 */
  showReset?: boolean
}>()

const emit = defineEmits<{
  (_e: 'update:search', _value: string): void
  (_e: 'search'): void
  (_e: 'reset'): void
}>()

const handleKeyup = (event: KeyboardEvent) => {
  if (event.key === 'Enter') emit('search')
}
</script>

<template>
  <Card>
    <CardContent class="p-4">
      <div class="flex items-center gap-4">
        <slot name="filters">
          <Input
            :model-value="search"
            :placeholder="placeholder || '搜索...'"
            clearable
            class="w-80"
            @update:model-value="emit('update:search', String($event))"
            @keyup="handleKeyup"
          />
        </slot>
        <Button @click="emit('search')">
          <Search class="mr-2 h-4 w-4" />
          搜索
        </Button>
        <Button
          v-if="showReset !== false"
          variant="outline"
          @click="emit('reset')"
        >
          重置
        </Button>
        <div class="flex-1" />
        <slot />
      </div>
    </CardContent>
  </Card>
</template>
