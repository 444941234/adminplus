import { computed, ref } from 'vue'
import { getMyCcRecords, getMyUnreadCcRecords, countMyUnreadCcRecords, markCcAsRead, markCcAsReadBatch } from '@/api'
import type { WorkflowCc } from '@/types'
import { toast } from 'vue-sonner'

/**
 * 工作流抄送中心数据管理
 */
export const useWorkflowCc = () => {
  const loading = ref(false)
  const unreadCount = ref(0)
  const activeTab = ref<'all' | 'unread'>('all')
  const records = ref<WorkflowCc[]>([])

  const unreadIds = computed(() => records.value.filter((item) => !item.isRead).map((item) => item.id))

  const fetchUnreadCount = async () => {
    try {
      const res = await countMyUnreadCcRecords()
      unreadCount.value = res.data
    } catch (error) {
      const message = error instanceof Error ? error.message : '获取抄送未读数失败'
      toast.error(message)
    }
  }

  const fetchCcList = async (tab = activeTab.value) => {
    loading.value = true
    activeTab.value = tab
    try {
      const res = tab === 'unread' ? await getMyUnreadCcRecords() : await getMyCcRecords()
      records.value = res.data
    } catch (error) {
      const message = error instanceof Error ? error.message : '获取抄送记录失败'
      toast.error(message)
    } finally {
      loading.value = false
    }
  }

  const markRead = async (ccId: string) => {
    try {
      await markCcAsRead(ccId)
      toast.success('已标记为已读')
      await Promise.all([fetchCcList(), fetchUnreadCount()])
    } catch (error) {
      const message = error instanceof Error ? error.message : '标记已读失败'
      toast.error(message)
    }
  }

  const markAllRead = async () => {
    if (unreadIds.value.length === 0) {
      toast.warning('当前没有未读抄送')
      return
    }

    try {
      await markCcAsReadBatch(unreadIds.value)
      toast.success('未读抄送已全部标记')
      await Promise.all([fetchCcList(), fetchUnreadCount()])
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
    fetchCcList,
    markRead,
    markAllRead
  }
}
