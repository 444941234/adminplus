<script setup lang="ts">
import { ref } from 'vue'
import { Button, Switch } from '@/components/ui'
import { Edit, Eye, EyeOff, History, Trash2, Copy, Check } from 'lucide-vue-next'
import type { Config } from '@/types'

const copiedId = ref<string | null>(null)
const copyTimeout = ref<ReturnType<typeof setTimeout> | null>(null)

const copyValue = async (config: Config) => {
  const valueToCopy = config.value || config.defaultValue || ''
  try {
    await navigator.clipboard.writeText(valueToCopy)
    copiedId.value = config.id
    if (copyTimeout.value) clearTimeout(copyTimeout.value)
    copyTimeout.value = setTimeout(() => {
      copiedId.value = null
    }, 2000)
  } catch {
    // Fallback failed - user can manually copy
  }
}

interface Props {
  configs: Config[]
  loading?: boolean
  canEdit?: boolean
  canDelete?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
  canEdit: true,
  canDelete: true
})

const emit = defineEmits<{
  (e: 'edit', config: Config): void
  (e: 'delete', config: Config): void
  (e: 'history', config: Config): void
  (e: 'toggleStatus', config: Config): void
}>()

// Secret value visibility toggle
const visibleSecrets = ref<Set<string>>(new Set())

const toggleSecret = (configId: string) => {
  if (visibleSecrets.value.has(configId)) {
    visibleSecrets.value.delete(configId)
  } else {
    visibleSecrets.value.add(configId)
  }
}

const isSecretVisible = (configId: string) => visibleSecrets.value.has(configId)

// Effect type badge styles
const getEffectTypeClass = (effectType: string) => {
  switch (effectType) {
    case 'IMMEDIATE':
      return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
    case 'MANUAL':
      return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
    case 'RESTART':
      return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
    default:
      return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
  }
}

const getEffectTypeLabel = (effectType: string) => {
  switch (effectType) {
    case 'IMMEDIATE':
      return '立即生效'
    case 'MANUAL':
      return '手动生效'
    case 'RESTART':
      return '重启生效'
    default:
      return effectType
  }
}

// Value type label
const getValueTypeLabel = (valueType: string) => {
  const labels: Record<string, string> = {
    STRING: '字符串',
    NUMBER: '数字',
    BOOLEAN: '布尔值',
    JSON: 'JSON',
    ARRAY: '数组',
    SECRET: '密文',
    FILE: '文件'
  }
  return labels[valueType] || valueType
}

// Format config value for display
const formatValue = (config: Config) => {
  if (config.valueType === 'SECRET' && !isSecretVisible(config.id)) {
    return '****'
  }
  if (!config.value) return config.defaultValue || '-'
  if (config.valueType === 'JSON' || config.valueType === 'ARRAY') {
    try {
      const parsed = JSON.parse(config.value)
      const formatted = JSON.stringify(parsed, null, 2)
      // Truncate long JSON values
      if (formatted.length > 100) {
        return formatted.slice(0, 100) + '...'
      }
      return formatted
    } catch {
      return config.value.length > 100 ? config.value.slice(0, 100) + '...' : config.value
    }
  }
  if (config.valueType === 'BOOLEAN') {
    return config.value === 'true' ? '是' : '否'
  }
  // Truncate long string values
  return config.value.length > 50 ? config.value.slice(0, 50) + '...' : config.value
}

const getFullValue = (config: Config) => {
  if (config.valueType === 'SECRET' && !isSecretVisible(config.id)) {
    return '****'
  }
  if (!config.value) return config.defaultValue || '-'
  if (config.valueType === 'JSON' || config.valueType === 'ARRAY') {
    try {
      const parsed = JSON.parse(config.value)
      return JSON.stringify(parsed, null, 2)
    } catch {
      return config.value
    }
  }
  return config.value
}

const shouldShowCopyButton = (config: Config) => {
  return config.valueType !== 'SECRET' || isSecretVisible(config.id)
}
</script>

<template>
  <div class="overflow-x-auto">
    <table class="w-full">
      <thead class="border-b bg-muted/50">
        <tr>
          <th class="p-4 text-left font-medium">配置名称</th>
          <th class="p-4 text-left font-medium">配置键</th>
          <th class="p-4 text-left font-medium">配置值</th>
          <th class="p-4 text-left font-medium">类型</th>
          <th class="p-4 text-left font-medium">生效方式</th>
          <th class="p-4 text-left font-medium">状态</th>
          <th class="p-4 text-left font-medium">操作</th>
        </tr>
      </thead>
      <tbody class="divide-y">
        <tr v-if="loading">
          <td colspan="7" class="h-32 text-center text-muted-foreground">加载中...</td>
        </tr>
        <tr v-else-if="configs.length === 0">
          <td colspan="7" class="h-32 text-center text-muted-foreground">暂无配置项</td>
        </tr>
        <tr v-for="config in configs" :key="config.id" class="hover:bg-muted/30">
          <td class="p-4 font-medium">{{ config.name }}</td>
          <td class="p-4 font-mono text-sm text-muted-foreground">{{ config.key }}</td>
          <td class="max-w-md p-4">
            <div class="flex items-center gap-2">
              <div
                class="flex-1 truncate text-sm font-mono"
                :title="getFullValue(config)"
              >
                {{ formatValue(config) }}
              </div>
              <Button
                v-if="config.valueType === 'SECRET'"
                size="sm"
                variant="ghost"
                class="h-6 w-6 p-0"
                @click="toggleSecret(config.id)"
              >
                <Eye v-if="!isSecretVisible(config.id)" class="h-4 w-4" />
                <EyeOff v-else class="h-4 w-4" />
              </Button>
              <Button
                v-if="shouldShowCopyButton(config)"
                size="sm"
                variant="ghost"
                class="h-6 w-6 p-0"
                :title="copiedId === config.id ? '已复制' : '复制值'"
                @click="copyValue(config)"
              >
                <Check v-if="copiedId === config.id" class="h-4 w-4 text-green-600" />
                <Copy v-else class="h-4 w-4" />
              </Button>
            </div>
          </td>
          <td class="p-4 text-sm text-muted-foreground">
            {{ getValueTypeLabel(config.valueType) }}
          </td>
          <td class="p-4">
            <span
              class="rounded-full px-2.5 py-0.5 text-xs font-medium"
              :class="getEffectTypeClass(config.effectType)"
            >
              {{ getEffectTypeLabel(config.effectType) }}
            </span>
          </td>
          <td class="p-4">
            <Switch
              :model-value="config.status === 1"
              @update:model-value="emit('toggleStatus', config)"
            />
          </td>
          <td class="p-4">
            <div class="flex gap-2">
              <Button v-if="canEdit" size="sm" variant="ghost" @click="emit('edit', config)">
                <Edit class="h-4 w-4" />
              </Button>
              <Button size="sm" variant="ghost" @click="emit('history', config)">
                <History class="h-4 w-4" />
              </Button>
              <Button
                v-if="canDelete"
                size="sm"
                variant="ghost"
                class="text-destructive"
                @click="emit('delete', config)"
              >
                <Trash2 class="h-4 w-4" />
              </Button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
