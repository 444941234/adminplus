<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { getMyNotifications, getUnreadCount, markAsRead, markAllAsRead, deleteNotification } from '@/api/notification'
import type { Notification } from '@/types'
import { toast } from 'vue-sonner'
import { Bell, BellRing, Check, Trash2, CheckCheck } from '@lucide/vue'
import { formatDateTime } from '@/utils/format'
import { logError } from '@/utils/logger'

const notifications = ref<Notification[]>([])
const unreadCount = ref(0)
const loading = ref(false)

// 获取通知列表
const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await getMyNotifications({ status: 0, page: 0, size: 20 })
    notifications.value = res.data?.content || []
  } finally {
    loading.value = false
  }
}

// 获取未读数量
const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (error) {
    logError('获取未读通知数量失败', error as Error, 'NotificationCenter')
  }
}

// 标记单个为已读
const handleMarkAsRead = async (id: string) => {
  try {
    await markAsRead(id)
    notifications.value = notifications.value.filter(n => n.id !== id)
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    toast.success('已标记为已读')
  } catch (error) {
    logError('标记通知已读失败', error as Error, 'NotificationCenter')
    toast.error('操作失败')
  }
}

// 全部标记为已读
const handleMarkAllAsRead = async () => {
  try {
    const res = await markAllAsRead()
    const count = res.data || 0
    notifications.value = []
    unreadCount.value = 0
    toast.success(`已标记 ${count} 条通知为已读`)
  } catch (error) {
    logError('标记全部已读失败', error as Error, 'NotificationCenter')
    toast.error('操作失败')
  }
}

// 删除通知
const handleDelete = async (id: string) => {
  try {
    await deleteNotification(id)
    notifications.value = notifications.value.filter(n => n.id !== id)
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    toast.success('已删除')
  } catch (error) {
    logError('删除通知失败', error as Error, 'NotificationCenter')
    toast.error('删除失败')
  }
}

// 获取通知类型标签
const getNotificationTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    workflow_approve: '审批通过',
    workflow_reject: '审批驳回',
    workflow_submit: '流程提交',
    workflow_cancel: '流程取消',
    workflow_rollback: '流程回退',
    workflow_cc: '抄送',
    workflow_urge: '催办'
  }
  return labels[type] || '系统通知'
}

// 获取通知类型颜色
const getNotificationTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    workflow_approve: 'bg-green-100 text-green-800',
    workflow_reject: 'bg-red-100 text-red-800',
    workflow_submit: 'bg-blue-100 text-blue-800',
    workflow_cancel: 'bg-gray-100 text-gray-800',
    workflow_rollback: 'bg-orange-100 text-orange-800',
    workflow_cc: 'bg-purple-100 text-purple-800',
    workflow_urge: 'bg-yellow-100 text-yellow-800'
  }
  return colors[type] || 'bg-gray-100 text-gray-800'
}

onMounted(() => {
  fetchNotifications()
  fetchUnreadCount()
})
</script>

<template>
  <Card>
    <CardHeader>
      <div class="flex items-center justify-between">
        <CardTitle class="flex items-center gap-2">
          <BellRing
            v-if="unreadCount > 0"
            class="h-5 w-5 text-orange-500"
          />
          <Bell
            v-else
            class="h-5 w-5"
          />
          通知中心
          <Badge
            v-if="unreadCount > 0"
            variant="destructive"
          >
            {{ unreadCount }}
          </Badge>
        </CardTitle>
        <Button
          v-if="notifications.length > 0"
          variant="outline"
          size="sm"
          @click="handleMarkAllAsRead"
        >
          <CheckCheck class="h-4 w-4 mr-1" />
          全部已读
        </Button>
      </div>
    </CardHeader>
    <CardContent>
      <div
        v-if="loading"
        class="text-center py-8 text-muted-foreground"
      >
        加载中...
      </div>

      <div
        v-else-if="notifications.length === 0"
        class="text-center py-12 text-muted-foreground"
      >
        <Bell class="h-12 w-12 mx-auto mb-4 opacity-20" />
        <p>暂无通知</p>
      </div>

      <div
        v-else
        class="space-y-3"
      >
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="flex items-start gap-3 p-4 border rounded-lg hover:bg-muted/50 transition-colors"
        >
          <div class="flex-1 space-y-1">
            <div class="flex items-center gap-2">
              <Badge :class="getNotificationTypeColor(notification.type)">
                {{ getNotificationTypeLabel(notification.type) }}
              </Badge>
              <span class="text-sm text-muted-foreground">
                {{ formatDateTime(notification.createTime) }}
              </span>
            </div>
            <h4 class="font-medium">
              {{ notification.title }}
            </h4>
            <p class="text-sm text-muted-foreground">
              {{ notification.content }}
            </p>
          </div>
          <div class="flex gap-1">
            <Button
              variant="ghost"
              size="sm"
              @click="handleMarkAsRead(notification.id)"
            >
              <Check class="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              class="text-destructive"
              @click="handleDelete(notification.id)"
            >
              <Trash2 class="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
</template>
