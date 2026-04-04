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
import type { WorkflowUrge } from '@/types'

defineProps<{
  records: WorkflowUrge[]
}>()

const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}
</script>

<template>
  <Card v-if="records.length > 0">
    <CardHeader>
      <CardTitle>催办记录</CardTitle>
    </CardHeader>
    <CardContent class="p-0">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>节点</TableHead>
            <TableHead>催办人</TableHead>
            <TableHead>被催办人</TableHead>
            <TableHead>内容</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>时间</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow
            v-for="record in records"
            :key="record.id"
          >
            <TableCell>{{ record.nodeName || '-' }}</TableCell>
            <TableCell>{{ record.urgeUserName || '-' }}</TableCell>
            <TableCell>{{ record.urgeTargetName || '-' }}</TableCell>
            <TableCell>{{ record.urgeContent || '-' }}</TableCell>
            <TableCell>
              <Badge :variant="record.isRead ? 'secondary' : 'default'">
                {{ record.isRead ? '已读' : '未读' }}
              </Badge>
            </TableCell>
            <TableCell>{{ formatDateTime(record.createTime) }}</TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </CardContent>
  </Card>
</template>
