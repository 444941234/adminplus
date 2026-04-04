<script setup lang="ts">
import { ref, watch } from 'vue'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Search, RefreshCw } from 'lucide-vue-next'

interface Props {
  /** 搜索关键词（v-model） */
  modelValue?: string
  /** 输入框 placeholder */
  placeholder?: string
  /** 搜索按钮加载状态 */
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请输入关键词搜索',
  loading: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'search'): void
  (e: 'reset'): void
}>()

const localValue = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  localValue.value = val
})

function handleInput(value: string | number) {
  const strValue = String(value)
  localValue.value = strValue
  emit('update:modelValue', strValue)
}

function handleSearch() {
  emit('search')
}

function handleReset() {
  localValue.value = ''
  emit('update:modelValue', '')
  emit('reset')
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter') {
    handleSearch()
  }
}
</script>

<template>
  <div class="list-search-bar">
    <div class="search-filters">
      <slot name="filters">
        <Input
          :model-value="localValue"
          :placeholder="placeholder"
          clearable
          class="w-60"
          @update:model-value="handleInput"
          @keydown="handleKeydown"
        />
      </slot>

      <Button :disabled="loading" @click="handleSearch">
        <Search class="w-4 h-4 mr-1" />
        搜索
      </Button>

      <Button variant="outline" @click="handleReset">
        <RefreshCw class="w-4 h-4 mr-1" />
        重置
      </Button>
    </div>

    <div v-if="$slots.actions" class="search-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.list-search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-filters {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>