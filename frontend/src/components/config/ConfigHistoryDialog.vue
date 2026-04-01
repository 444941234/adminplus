<script setup lang="ts">
import { ref, watch } from 'vue'
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui'
import { RotateCcw } from 'lucide-vue-next'
import { getConfigHistory, rollbackConfig } from '@/api'
import type { Config, ConfigHistory } from '@/types'
import { useAsyncAction } from '@/composables/useAsyncAction'

interface Props {
  open: boolean
  config: Config | undefined
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const { loading: historyLoading, run: runFetchHistory } = useAsyncAction('获取历史记录失败')
const { loading: rollbackLoading, run: runRollback } = useAsyncAction('回滚失败')

const historyList = ref<ConfigHistory[]>([])
const selectedHistoryId = ref<string>()
const rollbackDialogOpen = ref(false)

// Fetch history when dialog opens
watch(
  () => props.open,
  (open) => {
    if (open && props.config?.id) {
      fetchHistory()
    }
  }
)

const fetchHistory = () =>
  runFetchHistory(async () => {
    if (!props.config?.id) return
    const res = await getConfigHistory(props.config.id)
    historyList.value = res.data || []
  })

const handleRollback = (history: ConfigHistory) => {
  selectedHistoryId.value = history.id
  rollbackDialogOpen.value = true
}

const confirmRollback = () => {
  if (!props.config?.id || !selectedHistoryId.value) return

  runRollback(async () => {
    await rollbackConfig(props.config!.id, {
      historyId: selectedHistoryId.value!,
      remark: '手动回滚'
    })
  }, {
    successMessage: '回滚成功',
    onSuccess: () => {
      rollbackDialogOpen.value = false
      emit('update:open', false)
      emit('success')
    }
  })
}

const formatValue = (value: string | null) => {
  if (!value) return '-'
  try {
    const parsed = JSON.parse(value)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return value
  }
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

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
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="sm:max-w-3xl max-h-[80vh] overflow-hidden flex flex-col">
      <DialogHeader>
        <DialogTitle>配置变更历史</DialogTitle>
        <DialogDescription>
          {{ config?.name }} ({{ config?.key }}) 的变更记录
        </DialogDescription>
      </DialogHeader>

      <div class="flex-1 overflow-y-auto py-4">
        <div v-if="historyLoading" class="flex items-center justify-center h-32">
          <div class="text-muted-foreground">加载中...</div>
        </div>
        <div v-else-if="historyList.length === 0" class="flex items-center justify-center h-32">
          <div class="text-muted-foreground">暂无历史记录</div>
        </div>
        <div v-else class="space-y-4">
          <div
            v-for="item in historyList"
            :key="item.id"
            class="rounded-lg border border-border bg-card p-4"
          >
            <div class="mb-2 flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium">{{ item.remark }}</span>
                <span v-if="item.operatorName" class="text-xs text-muted-foreground">
                  by {{ item.operatorName }}
                </span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-xs text-muted-foreground">
                  {{ formatTime(item.createTime) }}
                </span>
                <Button
                  size="sm"
                  variant="outline"
                  @click="handleRollback(item)"
                >
                  <RotateCcw class="mr-1 h-3 w-3" />
                  回滚
                </Button>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4 text-sm">
              <div>
                <div class="mb-1 text-xs font-medium text-muted-foreground">旧值</div>
                <div class="rounded bg-muted p-2">
                  <pre class="max-h-32 overflow-auto text-xs">{{ formatValue(item.oldValue) }}</pre>
                </div>
              </div>
              <div>
                <div class="mb-1 text-xs font-medium text-muted-foreground">新值</div>
                <div class="rounded bg-muted p-2">
                  <pre class="max-h-32 overflow-auto text-xs">{{ formatValue(item.newValue) }}</pre>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="emit('update:open', false)">
          关闭
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>

  <!-- Rollback Confirmation Dialog -->
  <Dialog v-model:open="rollbackDialogOpen">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>确认回滚</DialogTitle>
        <DialogDescription>
          确定要回滚到此版本吗？此操作将覆盖当前配置值。
        </DialogDescription>
      </DialogHeader>

      <DialogFooter>
        <Button variant="outline" :disabled="rollbackLoading" @click="rollbackDialogOpen = false">
          取消
        </Button>
        <Button :disabled="rollbackLoading" @click="confirmRollback">
          {{ rollbackLoading ? '回滚中...' : '确认回滚' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
