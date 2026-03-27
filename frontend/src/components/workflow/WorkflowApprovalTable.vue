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
import type { WorkflowApproval } from '@/types'

defineProps<{
  approvals: WorkflowApproval[]
}>()

const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle>审批记录</CardTitle>
    </CardHeader>
    <CardContent class="p-0">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>节点</TableHead>
            <TableHead>审批人</TableHead>
            <TableHead>结果</TableHead>
            <TableHead>意见</TableHead>
            <TableHead>时间</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-if="approvals.length === 0">
            <TableCell colspan="5" class="h-24 text-center text-muted-foreground">暂无审批记录</TableCell>
          </TableRow>
          <TableRow v-for="approval in approvals" :key="approval.id">
            <TableCell>{{ approval.nodeName || '-' }}</TableCell>
            <TableCell>{{ approval.approverName || '-' }}</TableCell>
            <TableCell>{{ approval.approvalStatus || '-' }}</TableCell>
            <TableCell>{{ approval.comment || '-' }}</TableCell>
            <TableCell>{{ formatDateTime(approval.approvalTime || approval.createTime) }}</TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </CardContent>
  </Card>
</template>
