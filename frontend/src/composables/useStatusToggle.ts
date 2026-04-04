import { ref, type Ref } from 'vue'
import { useAsyncAction } from './useAsyncAction'

interface UseStatusToggleOptions<T> {
  /** 更新状态的 API 调用 */
  updateStatus: (id: string, newStatus: number) => Promise<unknown>
  /** 成功后的回调（通常是刷新列表） */
  onSuccess?: () => void
  /** 获取项目的 ID */
  getId?: (item: T) => string
  /** 获取项目的当前状态 */
  getStatus?: (item: T) => number
}

interface StatusToggleReturn<T> {
  /** 当前要更改状态的项目 */
  statusChangeItem: Ref<T | null>
  /** 确认对话框是否打开 */
  statusConfirmOpen: Ref<boolean>
  /** 状态更新是否正在进行中 */
  loading: Ref<boolean>
  /** 点击状态切换按钮时调用 */
  handleStatusClick: (item: T) => void
  /** 确认状态切换时调用 */
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
    const newStatus = currentStatus === 1 ? 0 : 1

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