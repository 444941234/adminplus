<script setup lang="ts">
import {
  Badge,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow
} from '@/components/ui'
import type { WorkflowCc } from '@/types'

defineProps<{
  records: WorkflowCc[]
}>()

const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const getCcTypeLabel = (type?: string) => {
  const map: Record<string, string> = {
    start: '发起抄送',
    approve: '审批通过',
    reject: '审批拒绝',
    rollback: '回退通知'
  }
  return map[type || ''] || type || '-'
}
</script>

<template>
  <Card v-if="records.length > 0">
    <CardHeader>
      <CardTitle>抄送记录</CardTitle>
    </CardHeader>
    <CardContent class="p-0">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>节点</TableHead>
            <TableHead>被抄送人</TableHead>
            <TableHead>类型</TableHead>
            <TableHead>内容</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>时间</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow
            v-for="cc in records"
            :key="cc.id"
          >
            <TableCell>{{ cc.nodeName || '-' }}</TableCell>
            <TableCell>{{ cc.userName || '-' }}</TableCell>
            <TableCell>{{ getCcTypeLabel(cc.ccType) }}</TableCell>
            <TableCell>{{ cc.ccContent || '-' }}</TableCell>
            <TableCell>
              <Badge :variant="cc.isRead ? 'secondary' : 'default'">
                {{ cc.isRead ? '已读' : '未读' }}
              </Badge>
            </TableCell>
            <TableCell>{{ formatDateTime(cc.createTime) }}</TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </CardContent>
  </Card>
</template>
