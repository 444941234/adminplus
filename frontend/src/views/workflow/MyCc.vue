<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Tabs,
  TabsList,
  TabsTrigger
} from '@/components/ui'
import { useWorkflowCc } from '@/composables/workflow/useWorkflowCc'
import { useUserStore } from '@/stores/user'
import { getWorkflowPermissionState } from '@/lib/page-permissions'

const {
  loading,
  unreadCount,
  activeTab,
  records,
  fetchUnreadCount,
  fetchCcList,
  markRead,
  markAllRead
} = useWorkflowCc()
const userStore = useUserStore()
const permissionState = computed(() => getWorkflowPermissionState(userStore.hasPermission))

const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const getCcTypeLabel = (type?: string) => {
  const map: Record<string, string> = {
    start: '发起抄送',
    approve: '审批通过',
    reject: '审批驳回',
    rollback: '回退通知'
  }
  return map[type || ''] || type || '-'
}

onMounted(async () => {
  if (!permissionState.value.canViewCc) return
  await Promise.all([fetchCcList(), fetchUnreadCount()])
})
</script>

<template>
  <div class="space-y-4">
    <Card>
      <CardHeader class="flex flex-row items-center justify-between space-y-0">
        <div class="flex items-center gap-3">
          <CardTitle>抄送我的</CardTitle>
          <Badge variant="default">未读 {{ unreadCount }}</Badge>
        </div>
        <div class="flex items-center gap-2">
          <Button v-if="permissionState.canViewCc" variant="outline" size="sm" @click="fetchCcList()">刷新</Button>
          <Button v-if="permissionState.canMarkCcRead" variant="secondary" size="sm" @click="markAllRead">全部已读</Button>
        </div>
      </CardHeader>
      <CardContent class="space-y-4">
        <div
          v-if="!permissionState.canViewCc"
          class="rounded-md border border-dashed border-border bg-muted/20 px-4 py-10 text-center text-sm text-muted-foreground"
        >
          当前没有查看抄送记录权限
        </div>
        <template v-else>
        <Tabs :default-value="activeTab" @update:model-value="(value) => fetchCcList(String(value) as 'all' | 'unread')">
          <TabsList>
            <TabsTrigger value="all">全部</TabsTrigger>
            <TabsTrigger value="unread">未读</TabsTrigger>
          </TabsList>
        </Tabs>

        <div class="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>节点</TableHead>
                <TableHead>被抄送人</TableHead>
                <TableHead>类型</TableHead>
                <TableHead>内容</TableHead>
                <TableHead>状态</TableHead>
                <TableHead>时间</TableHead>
                <TableHead class="text-right">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-if="loading">
                <TableCell colspan="7" class="h-24 text-center text-muted-foreground">加载中...</TableCell>
              </TableRow>
              <TableRow v-else-if="records.length === 0">
                <TableCell colspan="7" class="h-24 text-center text-muted-foreground">暂无抄送记录</TableCell>
              </TableRow>
              <TableRow v-for="record in records" :key="record.id">
                <TableCell>{{ record.nodeName || '-' }}</TableCell>
                <TableCell>{{ record.userName || '-' }}</TableCell>
                <TableCell>{{ getCcTypeLabel(record.ccType) }}</TableCell>
                <TableCell>{{ record.ccContent || '-' }}</TableCell>
                <TableCell>
                  <Badge :variant="record.isRead ? 'secondary' : 'default'">
                    {{ record.isRead ? '已读' : '未读' }}
                  </Badge>
                </TableCell>
                <TableCell>{{ formatDateTime(record.createTime) }}</TableCell>
                <TableCell class="text-right">
                  <div class="flex justify-end gap-2">
                    <RouterLink :to="`/workflow/detail/${record.instanceId}`">
                      <Button size="sm" variant="outline">详情</Button>
                    </RouterLink>
                    <Button
                      v-if="!record.isRead && permissionState.canMarkCcRead"
                      size="sm"
                      @click="markRead(record.id)"
                    >
                      标记已读
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
        </template>
      </CardContent>
    </Card>
  </div>
</template>
