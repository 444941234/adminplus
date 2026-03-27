<script setup lang="ts">
import {
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
import type { WorkflowAddSign } from '@/types'

defineProps<{
  records: WorkflowAddSign[]
}>()

const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const getAddSignTypeLabel = (type?: string) => {
  const map: Record<string, string> = {
    before: '前加签',
    after: '后加签',
    transfer: '转办',
    BEFORE: '前加签',
    AFTER: '后加签',
    TRANSFER: '转办'
  }
  return map[type || ''] || type || '-'
}
</script>

<template>
  <Card v-if="records.length > 0">
    <CardHeader>
      <CardTitle>加签/转办记录</CardTitle>
    </CardHeader>
    <CardContent class="p-0">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>节点</TableHead>
            <TableHead>发起人</TableHead>
            <TableHead>类型</TableHead>
            <TableHead>被加签人</TableHead>
            <TableHead>原因</TableHead>
            <TableHead>时间</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="record in records" :key="record.id">
            <TableCell>{{ record.nodeName || '-' }}</TableCell>
            <TableCell>{{ record.initiatorName || '-' }}</TableCell>
            <TableCell>{{ getAddSignTypeLabel(record.addType) }}</TableCell>
            <TableCell>{{ record.addUserName || '-' }}</TableCell>
            <TableCell>{{ record.addReason || '-' }}</TableCell>
            <TableCell>{{ formatDateTime(record.createTime) }}</TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </CardContent>
  </Card>
</template>
