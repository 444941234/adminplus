import { computed, ref } from 'vue'
import {
  countUnreadUrgeRecords,
  getReceivedUrgeRecords,
  getSentUrgeRecords,
  getUnreadUrgeRecords,
  markUrgeAsRead,
  markUrgeAsReadBatch
} from '@/api'
import type { WorkflowUrge } from '@/types'
import { toast } from 'vue-sonner'

/**
 * 工作流催办中心数据管理
 */
export const useWorkflowUrge = () => {
  const loading = ref(false)
  const unreadCount = ref(0)
  const activeTab = ref<'received' | 'sent' | 'unread'>('received')
  const records = ref<WorkflowUrge[]>([])

  const unreadIds = computed(() => records.value.filter((item) => !item.isRead).map((item) => item.id))

  const fetchUnreadCount = async () => {
    try {
      const res = await countUnreadUrgeRecords()
      unreadCount.value = res.data
    } catch (error) {
      const message = error instanceof Error ? error.message : '获取催办未读数失败'
      toast.error(message)
    }
  }

  const fetchUrgeList = async (tab = activeTab.value) => {
    loading.value = true
    activeTab.value = tab
    try {
      let res
      if (tab === 'sent') {
        res = await getSentUrgeRecords()
      } else if (tab === 'unread') {
        res = await getUnreadUrgeRecords()
      } else {
        res = await getReceivedUrgeRecords()
      }
      records.value = res.data
    } catch (error) {
      const message = error instanceof Error ? error.message : '获取催办记录失败'
      toast.error(message)
    } finally {
      loading.value = false
    }
  }

  const markRead = async (urgeId: string) => {
    try {
      await markUrgeAsRead(urgeId)
      toast.success('已标记为已读')
      await Promise.all([fetchUrgeList(), fetchUnreadCount()])
    } catch (error) {
      const message = error instanceof Error ? error.message : '标记已读失败'
      toast.error(message)
    }
  }

  const markAllRead = async () => {
    if (unreadIds.value.length === 0) {
      toast.warning('当前没有未读催办')
      return
    }

    try {
      await markUrgeAsReadBatch(unreadIds.value)
      toast.success('未读催办已全部标记')
      await Promise.all([fetchUrgeList(), fetchUnreadCount()])
    } catch (error) {
      const message = error instanceof Error ? error.message : '批量标记已读失败'
      toast.error(message)
    }
  }

  return {
    loading,
    unreadCount,
    activeTab,
    records,
    fetchUnreadCount,
    fetchUrgeList,
    markRead,
    markAllRead
  }
}
