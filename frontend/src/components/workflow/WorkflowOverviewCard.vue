<script setup lang="ts">
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle
} from '@/components/ui'
import WorkflowStatusBadge from '@/components/workflow/WorkflowStatusBadge.vue'
import type { WorkflowInstance } from '@/types'

defineProps<{
  instance: WorkflowInstance | null
  loading?: boolean
}>()

const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle>流程概览</CardTitle>
    </CardHeader>
    <CardContent
      v-if="instance"
      class="grid gap-4 md:grid-cols-2 xl:grid-cols-4"
    >
      <div>
        <div class="text-sm text-muted-foreground">
          标题
        </div>
        <div class="mt-1 font-medium">
          {{ instance.title }}
        </div>
      </div>
      <div>
        <div class="text-sm text-muted-foreground">
          流程定义
        </div>
        <div class="mt-1 font-medium">
          {{ instance.definitionName }}
        </div>
      </div>
      <div>
        <div class="text-sm text-muted-foreground">
          状态
        </div>
        <div class="mt-1">
          <WorkflowStatusBadge :status="instance.status" />
        </div>
      </div>
      <div>
        <div class="text-sm text-muted-foreground">
          当前节点
        </div>
        <div class="mt-1 font-medium">
          {{ instance.currentNodeName || '-' }}
        </div>
      </div>
      <div>
        <div class="text-sm text-muted-foreground">
          发起人
        </div>
        <div class="mt-1">
          {{ instance.userName || '-' }}
        </div>
      </div>
      <div>
        <div class="text-sm text-muted-foreground">
          提交时间
        </div>
        <div class="mt-1">
          {{ formatDateTime(instance.submitTime || instance.createTime) }}
        </div>
      </div>
      <div class="md:col-span-2 xl:col-span-2">
        <div class="text-sm text-muted-foreground">
          备注
        </div>
        <div class="mt-1 whitespace-pre-wrap">
          {{ instance.remark || '无备注' }}
        </div>
      </div>
    </CardContent>
    <CardContent
      v-else
      class="py-12 text-center text-muted-foreground"
    >
      {{ loading ? '加载中...' : '未获取到流程详情' }}
    </CardContent>
  </Card>
</template>
