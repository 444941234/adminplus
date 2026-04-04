import { get, post, put, del } from '@/utils/request'
import type { Notification, NotificationSendReq, SpringPage } from '@/types'

/**
 * 获取当前用户的通知列表
 */
export function getMyNotifications(params?: {
  status?: number
  page?: number
  size?: number
}) {
  return get<SpringPage<Notification>>('/notifications', params)
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount() {
  return get<number>('/notifications/unread-count')
}

/**
 * 标记通知为已读
 */
export function markAsRead(id: string) {
  return put(`/notifications/${id}/read`)
}

/**
 * 标记所有通知为已读
 */
export function markAllAsRead() {
  return put<number>('/notifications/read-all')
}

/**
 * 删除通知
 */
export function deleteNotification(id: string) {
  return del(`/notifications/${id}`)
}

/**
 * 发送通知（需要权限）
 */
export function sendNotification(data: NotificationSendReq) {
  return post<Notification>('/notifications', data)
}

/**
 * 批量发送通知（需要权限）
 */
export function sendBatchNotification(recipientIds: string[], data: NotificationSendReq) {
  // 将 recipientIds 放到请求体中
  return post('/notifications/batch', {
    ...data,
    recipientIds
  })
}
