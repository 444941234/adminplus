import { describe, it, expect, vi, beforeEach } from 'vitest'
import { nextTick } from 'vue'
import { useStatusToggle } from '@/composables/useStatusToggle'

interface TestItem {
  id: string
  name: string
  status: number
}

describe('useStatusToggle', () => {
  let mockUpdateStatus: ReturnType<typeof vi.fn>
  let mockOnSuccess: ReturnType<typeof vi.fn>

  beforeEach(() => {
    vi.clearAllMocks()
    mockUpdateStatus = vi.fn().mockResolvedValue({ data: null })
    mockOnSuccess = vi.fn()
  })

  const createComposable = (options: Partial<Parameters<typeof useStatusToggle<TestItem>>[0]> = {}) => {
    return useStatusToggle<TestItem>({
      updateStatus: mockUpdateStatus,
      onSuccess: mockOnSuccess,
      ...options
    })
  }

  describe('initial state', () => {
    it('should have null statusChangeItem initially', () => {
      const { statusChangeItem } = createComposable()
      expect(statusChangeItem.value).toBeNull()
    })

    it('should have statusConfirmOpen as false initially', () => {
      const { statusConfirmOpen } = createComposable()
      expect(statusConfirmOpen.value).toBe(false)
    })

    it('should have loading as false initially', () => {
      const { loading } = createComposable()
      expect(loading.value).toBe(false)
    })
  })

  describe('handleStatusClick', () => {
    it('should set statusChangeItem to the clicked item', () => {
      const { statusChangeItem, handleStatusClick } = createComposable()
      const item: TestItem = { id: '1', name: 'Test', status: 1 }

      handleStatusClick(item)

      expect(statusChangeItem.value).toEqual(item)
    })

    it('should open the confirm dialog', () => {
      const { statusConfirmOpen, handleStatusClick } = createComposable()
      const item: TestItem = { id: '1', name: 'Test', status: 1 }

      handleStatusClick(item)

      expect(statusConfirmOpen.value).toBe(true)
    })
  })

  describe('handleStatusConfirm', () => {
    it('should do nothing if statusChangeItem is null', async () => {
      const { handleStatusConfirm } = createComposable()

      await handleStatusConfirm()

      expect(mockUpdateStatus).not.toHaveBeenCalled()
    })

    it('should call updateStatus with toggled status (1 -> 0)', async () => {
      const { handleStatusClick, handleStatusConfirm } = createComposable()
      const item: TestItem = { id: '1', name: 'Test', status: 1 }

      handleStatusClick(item)
      await handleStatusConfirm()

      expect(mockUpdateStatus).toHaveBeenCalledWith('1', 0)
    })

    it('should call updateStatus with toggled status (0 -> 1)', async () => {
      const { handleStatusClick, handleStatusConfirm } = createComposable()
      const item: TestItem = { id: '2', name: 'Test', status: 0 }

      handleStatusClick(item)
      await handleStatusConfirm()

      expect(mockUpdateStatus).toHaveBeenCalledWith('2', 1)
    })

    it('should call onSuccess after successful update', async () => {
      const { handleStatusClick, handleStatusConfirm } = createComposable()
      const item: TestItem = { id: '1', name: 'Test', status: 1 }

      handleStatusClick(item)
      await handleStatusConfirm()
      await nextTick()

      expect(mockOnSuccess).toHaveBeenCalled()
    })

    it('should close the dialog after successful update', async () => {
      const { statusConfirmOpen, handleStatusClick, handleStatusConfirm } = createComposable()
      const item: TestItem = { id: '1', name: 'Test', status: 1 }

      handleStatusClick(item)
      await handleStatusConfirm()
      await nextTick()

      expect(statusConfirmOpen.value).toBe(false)
    })

    it('should clear statusChangeItem after successful update', async () => {
      const { statusChangeItem, handleStatusClick, handleStatusConfirm } = createComposable()
      const item: TestItem = { id: '1', name: 'Test', status: 1 }

      handleStatusClick(item)
      await handleStatusConfirm()
      await nextTick()

      expect(statusChangeItem.value).toBeNull()
    })

    it('should close dialog even on error', async () => {
      mockUpdateStatus.mockRejectedValueOnce(new Error('Update failed'))
      const { statusConfirmOpen, handleStatusClick, handleStatusConfirm } = createComposable()
      const item: TestItem = { id: '1', name: 'Test', status: 1 }

      handleStatusClick(item)
      await handleStatusConfirm().catch(() => {})
      await nextTick()

      expect(statusConfirmOpen.value).toBe(false)
    })
  })

  describe('custom getId and getStatus', () => {
    it('should use custom getId to get item id', async () => {
      interface CustomItem {
        key: string
        active: boolean
      }

      const mockUpdate = vi.fn().mockResolvedValue({ data: null })
      const { handleStatusClick, handleStatusConfirm } = useStatusToggle<CustomItem>({
        updateStatus: mockUpdate,
        getId: (item) => item.key,
        getStatus: (item) => item.active ? 1 : 0
      })

      const item: CustomItem = { key: 'abc', active: true }
      handleStatusClick(item)
      await handleStatusConfirm()

      expect(mockUpdate).toHaveBeenCalledWith('abc', 0)
    })

    it('should use custom getStatus to get item status', async () => {
      interface CustomItem {
        id: string
        enabled: boolean
      }

      const mockUpdate = vi.fn().mockResolvedValue({ data: null })
      const { handleStatusClick, handleStatusConfirm } = useStatusToggle<CustomItem>({
        updateStatus: mockUpdate,
        getStatus: (item) => item.enabled ? 1 : 0
      })

      const item: CustomItem = { id: '1', enabled: false }
      handleStatusClick(item)
      await handleStatusConfirm()

      expect(mockUpdate).toHaveBeenCalledWith('1', 1)
    })
  })

  describe('loading state', () => {
    it('should set loading to true during update', async () => {
      let resolvePromise: () => void
      mockUpdateStatus.mockImplementation(() => new Promise(resolve => { resolvePromise = resolve }))

      const { loading, handleStatusClick, handleStatusConfirm } = createComposable()
      const item: TestItem = { id: '1', name: 'Test', status: 1 }

      handleStatusClick(item)
      const promise = handleStatusConfirm()

      // Loading should be true while promise is pending
      expect(loading.value).toBe(true)

      resolvePromise!()
      await promise
      await nextTick()

      expect(loading.value).toBe(false)
    })

    it('should set loading to false after error', async () => {
      mockUpdateStatus.mockRejectedValueOnce(new Error('Failed'))

      const { loading, handleStatusClick, handleStatusConfirm } = createComposable()
      const item: TestItem = { id: '1', name: 'Test', status: 1 }

      handleStatusClick(item)
      await handleStatusConfirm().catch(() => {})
      await nextTick()

      expect(loading.value).toBe(false)
    })
  })
})