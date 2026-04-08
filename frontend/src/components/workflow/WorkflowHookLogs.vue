<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { getInstanceHookLogs, type WorkflowHookLog } from '@/api/workflow'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { ChevronDown, ChevronRight } from '@lucide/vue'
import { formatDateTime } from '@/utils/format'

const props = defineProps<{
  instanceId: string
}>()

const logs = ref<WorkflowHookLog[]>([])
const loading = ref(false)
const expandedLogId = ref<string | null>(null)

const groupedLogs = computed(() => {
  const groups: Record<string, WorkflowHookLog[]> = {}
  logs.value.forEach(log => {
    if (!groups[log.hookPoint]) {
      groups[log.hookPoint] = []
    }
    groups[log.hookPoint].push(log)
  })
  return groups
})

const hookPointLabel: Record<string, string> = {
  'PRE_SUBMIT': '提交前校验',
  'POST_SUBMIT': '提交后执行',
  'PRE_APPROVE': '同意前校验',
  'POST_APPROVE': '同意后执行',
  'PRE_REJECT': '拒绝前校验',
  'POST_REJECT': '拒绝后执行',
  'PRE_ROLLBACK': '退回前校验',
  'POST_ROLLBACK': '退回后执行',
  'PRE_CANCEL': '取消前校验',
  'POST_CANCEL': '取消后执行',
  'PRE_WITHDRAW': '撤回前校验',
  'POST_WITHDRAW': '撤回后执行',
  'PRE_ADD_SIGN': '加签前校验',
  'POST_ADD_SIGN': '加签后执行'
}

const loadLogs = async () => {
  if (!props.instanceId) return
  loading.value = true
  try {
    const response = await getInstanceHookLogs(props.instanceId)
    logs.value = response.data || []
  } finally {
    loading.value = false
  }
}

const toggleExpand = (logId: string) => {
  if (expandedLogId.value === logId) {
    expandedLogId.value = null
  } else {
    expandedLogId.value = logId
  }
}

const getExecutorTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'spel': 'SpEL表达式',
    'bean': 'Bean方法',
    'http': 'HTTP接口'
  }
  return labels[type] || type
}

const formatExecutionTime = (ms?: number) => {
  if (!ms) return '-'
  return ms < 1000 ? `${ms}ms` : `${(ms / 1000).toFixed(2)}s`
}

watch(() => props.instanceId, () => {
  loadLogs()
}, { immediate: true })
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle class="text-lg">
        钩子执行日志
      </CardTitle>
    </CardHeader>
    <CardContent>
      <div
        v-if="loading"
        class="flex justify-center py-8"
      >
        <div class="text-muted-foreground">
          加载中...
        </div>
      </div>

      <div
        v-else-if="Object.keys(groupedLogs).length === 0"
        class="text-center py-8 text-muted-foreground"
      >
        暂无钩子执行日志
      </div>

      <div
        v-else
        class="space-y-4"
      >
        <div
          v-for="(logGroup, hookPoint) in groupedLogs"
          :key="hookPoint"
          class="space-y-2"
        >
          <div class="text-sm font-medium text-muted-foreground">
            {{ hookPointLabel[hookPoint] || hookPoint }}
          </div>
          <div
            v-for="log in logGroup"
            :key="log.id"
            class="border rounded-lg overflow-hidden"
            :class="{ 'border-muted-foreground/50': !log.success }"
          >
            <div
              class="flex items-center justify-between p-3 cursor-pointer hover:bg-muted/50"
              role="button"
              tabindex="0"
              :aria-expanded="expandedLogId === log.id"
              :aria-label="expandedLogId === log.id ? '收起详情' : '展开详情'"
              @click="toggleExpand(log.id)"
              @keydown.enter="toggleExpand(log.id)"
              @keydown.space.prevent="toggleExpand(log.id)"
            >
              <div class="flex items-center gap-2">
                <component
                  :is="expandedLogId === log.id ? ChevronDown : ChevronRight"
                  class="h-4 w-4"
                />
                <Badge :variant="log.success ? 'default' : 'destructive'">
                  {{ log.success ? '成功' : '失败' }}
                </Badge>
                <span class="text-sm">{{ getExecutorTypeLabel(log.executorType) }}</span>
                <span
                  v-if="log.async"
                  class="text-xs px-2 py-0.5 rounded bg-purple-100 text-purple-800"
                >异步</span>
              </div>
              <div class="flex items-center gap-4 text-sm text-muted-foreground">
                <span>{{ formatExecutionTime(log.executionTime) }}</span>
                <span v-if="log.retryAttempts && log.retryAttempts > 0">重试{{ log.retryAttempts }}次</span>
                <span class="text-xs">{{ formatDateTime(log.createTime) }}</span>
              </div>
            </div>

            <div
              v-if="expandedLogId === log.id"
              class="border-t p-3 space-y-2 text-sm bg-muted/30"
            >
              <div class="grid grid-cols-2 gap-2">
                <div>
                  <span class="text-muted-foreground">结果码:</span>
                  <span class="ml-2 font-mono">{{ log.resultCode }}</span>
                </div>
                <div>
                  <span class="text-muted-foreground">来源:</span>
                  <span class="ml-2">{{ log.hookSource === 'node_field' ? '节点字段' : '钩子表' }}</span>
                </div>
              </div>

              <div
                v-if="log.resultMessage"
                class="p-2 bg-background rounded"
              >
                <span class="text-muted-foreground">消息:</span>
                <span class="ml-2">{{ log.resultMessage }}</span>
              </div>

              <div
                v-if="log.executorConfig"
                class="p-2 bg-background rounded"
              >
                <div class="text-muted-foreground mb-1">
                  执行配置:
                </div>
                <pre class="text-xs overflow-x-auto">{{ log.executorConfig }}</pre>
              </div>

              <div class="grid grid-cols-2 gap-2 text-xs">
                <div v-if="log.operatorId">
                  <span class="text-muted-foreground">操作人:</span>
                  <span class="ml-2">{{ log.operatorName }} ({{ log.operatorId }})</span>
                </div>
                <div v-if="log.nodeId">
                  <span class="text-muted-foreground">节点ID:</span>
                  <span class="ml-2 font-mono">{{ log.nodeId }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
</template>
