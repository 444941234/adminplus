import { ref, type Ref } from 'vue'
import { useAsyncAction } from './useAsyncAction'
import { STATUS_ACTIVE, STATUS_INACTIVE } from '@/constants/status'

interface UseStatusToggleOptions<T> {
  updateStatus: (id: string, newStatus: number) => Promise<unknown>
  onSuccess?: () => void
  getId?: (item: T) => string
  getStatus?: (item: T) => number
}

interface StatusToggleReturn<T> {
  statusChangeItem: Ref<T | null>
  statusConfirmOpen: Ref<boolean>
  loading: Ref<boolean>
  handleStatusClick: (item: T) => void
  handleStatusConfirm: () => Promise<void>
}

/**
 * 统一的状态切换逻辑 composable
 *
 * 用于 User, Role, Menu, Dept 等需要状态切换确认的页面
 *
 * @example
 * ```ts
 * const { statusChangeItem, statusConfirmOpen, handleStatusClick, handleStatusConfirm } = useStatusToggle<User>({
 *   updateStatus: (id, newStatus) => updateUserStatus(id, newStatus),
 *   onSuccess: () => fetchUsers()
 * })
 * ```
 */
export function useStatusToggle<T>(options: UseStatusToggleOptions<T>): StatusToggleReturn<T> {
  const {
    updateStatus,
    onSuccess,
    getId = (item: T) => (item as any).id,
    getStatus = (item: T) => (item as any).status
  } = options

  const statusChangeItem = ref<T | null>(null) as Ref<T | null>
  const statusConfirmOpen = ref(false)

  const { loading, run } = useAsyncAction('更新状态失败')

  const handleStatusClick = (item: T) => {
    statusChangeItem.value = item
    statusConfirmOpen.value = true
  }

  const handleStatusConfirm = async () => {
    if (!statusChangeItem.value) return

    const item = statusChangeItem.value
    const id = getId(item)
    const currentStatus = getStatus(item)
    const newStatus = currentStatus === STATUS_ACTIVE ? STATUS_INACTIVE : STATUS_ACTIVE

    await run(
      async () => {
        await updateStatus(id, newStatus)
      },
      {
        successMessage: '状态更新成功',
        errorMessage: '更新状态失败',
        onSuccess
      }
    )

    statusConfirmOpen.value = false
    statusChangeItem.value = null
  }

  return {
    statusChangeItem,
    statusConfirmOpen,
    loading,
    handleStatusClick,
    handleStatusConfirm
  }
}
