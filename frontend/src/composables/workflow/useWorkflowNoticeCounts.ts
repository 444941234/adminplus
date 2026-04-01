import { ref } from 'vue'
import { countMyUnreadCcRecords, countUnreadUrgeRecords, getPendingWorkflowCount } from '@/api'
import { showErrorToast } from '@/composables/useApiInterceptors'

/**
 * 工作流头部计数
 */
export const useWorkflowNoticeCounts = () => {
  const pendingCount = ref(0)
  const ccUnreadCount = ref(0)
  const urgeUnreadCount = ref(0)
  const loading = ref(false)

  const fetchCounts = async () => {
    loading.value = true
    try {
      const [pendingRes, ccRes, urgeRes] = await Promise.all([
        getPendingWorkflowCount(),
        countMyUnreadCcRecords(),
        countUnreadUrgeRecords()
      ])
      pendingCount.value = pendingRes.data
      ccUnreadCount.value = ccRes.data
      urgeUnreadCount.value = urgeRes.data
    } catch (error) {
      showErrorToast(error, '获取工作流提醒数失败')
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    pendingCount,
    ccUnreadCount,
    urgeUnreadCount,
    fetchCounts
  }
}
