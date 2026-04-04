<script setup lang="ts">
import { ref, computed, reactive, watch } from 'vue'
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Checkbox
} from '@/components/ui'
import { batchUpdateConfigs } from '@/api'
import type { Config } from '@/types'
import { useAsyncAction } from '@/composables/useAsyncAction'

interface Props {
  open: boolean
  configs: Config[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (_e: 'update:open', _value: boolean): void
  (_e: 'success'): void
}>()

const { loading, run } = useAsyncAction('批量更新失败')

// Selected configs for batch edit
const selectedConfigIds = ref<Set<string>>(new Set())

// Form state - map of configId to new value
const formValues = reactive<Record<string, string>>({})

// Computed
const selectedConfigs = computed(() => {
  return props.configs.filter(c => selectedConfigIds.value.has(c.id))
})

const isAllSelected = computed(() => {
  return props.configs.length > 0 && selectedConfigIds.value.size === props.configs.length
})

// Watch for dialog open/close
watch(
  () => props.open,
  (open) => {
    if (open) {
      // Reset state
      selectedConfigIds.value = new Set()
      Object.keys(formValues).forEach(key => delete formValues[key])
    }
  }
)

// Toggle all selection
const toggleAll = (checked: boolean) => {
  if (checked) {
    selectedConfigIds.value = new Set(props.configs.map(c => c.id))
  } else {
    selectedConfigIds.value.clear()
  }
}

// Submit handler
const handleSubmit = () => {
  if (selectedConfigIds.value.size === 0) {
    throw new Error('请至少选择一个配置项')
  }

  const items = Array.from(selectedConfigIds.value)
    .filter(id => formValues[id] !== undefined && formValues[id] !== '')
    .map(id => ({
      id,
      value: formValues[id]
    }))

  if (items.length === 0) {
    throw new Error('请至少修改一个配置值')
  }

  run(async () => {
    await batchUpdateConfigs(items)
  }, {
    successMessage: `成功更新 ${items.length} 个配置项`,
    onSuccess: () => {
      emit('success')
      emit('update:open', false)
    }
  })
}

const handleCancel = () => {
  emit('update:open', false)
}

// Get input type based on value type
const getInputType = (config: Config) => {
  switch (config.valueType) {
    case 'SECRET':
      return 'password'
    case 'NUMBER':
      return 'number'
    default:
      return 'text'
  }
}
</script>

<template>
  <Dialog
    :open="open"
    @update:open="emit('update:open', $event)"
  >
    <DialogContent class="sm:max-w-3xl max-h-[85vh] overflow-hidden flex flex-col">
      <DialogHeader>
        <DialogTitle>批量编辑配置</DialogTitle>
        <DialogDescription>
          选择多个配置项并批量修改其值
        </DialogDescription>
      </DialogHeader>

      <div class="flex-1 space-y-4 overflow-y-auto py-4">
        <!-- Selection Controls -->
        <div class="flex items-center gap-4 border-b border-border pb-4">
          <label class="flex items-center gap-2">
            <Checkbox
              :checked="isAllSelected"
              @update:checked="toggleAll"
            />
            <span class="text-sm font-medium">全选 ({{ selectedConfigs.length }}/{{ configs.length }})</span>
          </label>
        </div>

        <!-- Config List -->
        <div class="space-y-3">
          <div
            v-for="config in configs"
            :key="config.id"
            :class="[
              'rounded-lg border p-4 transition-colors',
              selectedConfigIds.has(config.id) ? 'border-primary bg-primary/5' : 'border-border'
            ]"
          >
            <div class="mb-3 flex items-start gap-3">
              <Checkbox
                :checked="selectedConfigIds.has(config.id)"
                class="mt-1"
                @update:checked="(checked: boolean) => {
                  if (checked) {
                    selectedConfigIds.add(config.id)
                  } else {
                    selectedConfigIds.delete(config.id)
                    delete formValues[config.id]
                  }
                }"
              />
              <div class="flex-1">
                <div class="flex items-center gap-2">
                  <span class="font-medium">{{ config.name }}</span>
                  <span class="text-xs text-muted-foreground">({{ config.key }})</span>
                </div>
                <p class="mt-1 text-xs text-muted-foreground">
                  {{ config.description || '无描述' }}
                </p>
                <div class="mt-2 flex items-center gap-2 text-xs">
                  <span class="rounded bg-muted px-2 py-0.5">{{ config.valueType }}</span>
                  <span class="rounded bg-muted px-2 py-0.5">{{ config.effectType === 'IMMEDIATE' ? '立即生效' : config.effectType === 'MANUAL' ? '手动生效' : '重启生效' }}</span>
                </div>
              </div>
            </div>

            <!-- Value Input -->
            <div
              v-if="selectedConfigIds.has(config.id)"
              class="ml-6"
            >
              <div class="space-y-1">
                <Label
                  :for="`value-${config.id}`"
                  class="text-xs"
                >配置值</Label>
                <div class="flex items-center gap-2">
                  <Input
                    :id="`value-${config.id}`"
                    v-model="formValues[config.id]"
                    :type="getInputType(config)"
                    :placeholder="config.value || config.defaultValue || '请输入新值'"
                    :disabled="loading"
                    class="flex-1"
                  />
                  <div class="text-xs text-muted-foreground w-24 truncate">
                    当前: {{ config.value || '-' }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <DialogFooter>
        <Button
          variant="outline"
          :disabled="loading"
          @click="handleCancel"
        >
          取消
        </Button>
        <Button
          :disabled="loading || selectedConfigIds.size === 0"
          @click="handleSubmit"
        >
          {{ loading ? '更新中...' : `更新 ${selectedConfigIds.size} 个配置` }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
