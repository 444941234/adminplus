import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import { useCRUD } from '@/composables/useCRUD'

const toastMocks = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn()
}))

vi.mock('vue-sonner', () => ({
  toast: toastMocks
}))

vi.mock('@/composables/useApiInterceptors', () => ({
  showErrorToast: vi.fn((error: Error, message: string) => {
    toastMocks.error(message)
  })
}))

vi.mock('@/composables/useAsyncAction', () => ({
  useAsyncAction: vi.fn((_errorMessage: string) => ({
    loading: { value: false },
    run: vi.fn(async (fn: () => Promise<any>, options?: { successMessage?: string; errorMessage?: string; onSuccess?: () => void; onError?: () => void }) => {
      try {
        const result = await fn()
        if (options?.successMessage) toastMocks.success(options.successMessage)
        if (options?.onSuccess) options.onSuccess()
        return result
      } catch (error) {
        toastMocks.error(options?.errorMessage || _errorMessage)
        if (options?.onError) options.onError()
        return undefined
      }
    })
  }))
}))

interface TestItem {
  id: string
  name: string
  status: number
}

interface TestForm {
  name: string
  status: number
}

const makeItem = (overrides: Partial<TestItem> = {}): TestItem => ({
  id: 'item-001',
  name: 'Test Item',
  status: 1,
  ...overrides
})

const mockApiResponse = <T>(data: T) => ({ data, code: 200, message: 'success' })

describe('useCRUD', () => {
  let getList: ReturnType<typeof vi.fn>
  let getById: ReturnType<typeof vi.fn>
  let create: ReturnType<typeof vi.fn>
  let update: ReturnType<typeof vi.fn>
  let deleteFn: ReturnType<typeof vi.fn>

  const defaultForm: TestForm = {
    name: '',
    status: 1
  }

  beforeEach(() => {
    vi.clearAllMocks()

    getList = vi.fn().mockResolvedValue(mockApiResponse([makeItem()]))
    getById = vi.fn().mockResolvedValue(mockApiResponse(makeItem()))
    create = vi.fn().mockResolvedValue(mockApiResponse(makeItem({ id: 'new-id' })))
    update = vi.fn().mockResolvedValue(mockApiResponse(undefined))
    deleteFn = vi.fn().mockResolvedValue(mockApiResponse(undefined))
  })

  const createCRUD = (options: Partial<Parameters<typeof useCRUD<TestItem, TestForm>>[0]> = {}) => {
    return useCRUD<TestItem, TestForm>({
      getList,
      getById,
      create,
      update,
      delete: deleteFn,
      defaultForm,
      ...options
    })
  }

  // =========================================================================
  // 1. Initial State
  // =========================================================================
  describe('Initial State', () => {
    it('initializes with empty list', () => {
      const { list } = createCRUD()
      expect(list.value).toEqual([])
    })

    it('initializes with default form values', () => {
      const { form } = createCRUD()
      expect(form.name).toBe('')
      expect(form.status).toBe(1)
    })

    it('initializes with closed dialogs', () => {
      const { dialogOpen, deleteDialogOpen } = createCRUD()
      expect(dialogOpen.value).toBe(false)
      expect(deleteDialogOpen.value).toBe(false)
    })

    it('initializes with false edit mode', () => {
      const { isEdit, editId } = createCRUD()
      expect(isEdit.value).toBe(false)
      expect(editId.value).toBe('')
    })
  })

  // =========================================================================
  // 2. fetchList
  // =========================================================================
  describe('fetchList', () => {
    it('calls getList API', async () => {
      const { fetchList } = createCRUD()
      await fetchList()
      expect(getList).toHaveBeenCalled()
    })

    it('populates list from API response', async () => {
      const items = [makeItem(), makeItem({ id: 'item-002', name: 'Item 2' })]
      getList.mockResolvedValue(mockApiResponse(items))

      const { fetchList, list } = createCRUD()
      await fetchList()

      expect(list.value).toEqual(items)
    })

    it('sets loading state during fetch', async () => {
      const { fetchList, loading } = createCRUD()

      const promise = fetchList()
      // Loading should be true during the async call
      expect(loading.value).toBe(true)

      await promise
      expect(loading.value).toBe(false)
    })

    it('calls onListFetched callback', async () => {
      const items = [makeItem()]
      getList.mockResolvedValue(mockApiResponse(items))

      const onListFetched = vi.fn()
      const { fetchList } = createCRUD({ onListFetched })
      await fetchList()

      expect(onListFetched).toHaveBeenCalledWith(items)
    })

    it('shows error toast on failure', async () => {
      getList.mockRejectedValue(new Error('Network error'))

      const { fetchList } = createCRUD({
        errorMessages: { list: '获取列表失败' }
      })
      await fetchList()

      expect(toastMocks.error).toHaveBeenCalledWith('获取列表失败')
    })
  })

  // =========================================================================
  // 3. openCreate
  // =========================================================================
  describe('openCreate', () => {
    it('resets form to default values', () => {
      const { form, openCreate } = createCRUD()

      // Modify form
      form.name = 'Modified'
      form.status = 0

      openCreate()

      expect(form.name).toBe('')
      expect(form.status).toBe(1)
    })

    it('sets isEdit to false', () => {
      const { isEdit, openCreate } = createCRUD()
      openCreate()
      expect(isEdit.value).toBe(false)
    })

    it('clears editId', () => {
      const { editId, openCreate } = createCRUD()
      openCreate()
      expect(editId.value).toBe('')
    })

    it('opens dialog', () => {
      const { dialogOpen, openCreate } = createCRUD()
      openCreate()
      expect(dialogOpen.value).toBe(true)
    })
  })

  // =========================================================================
  // 4. openEdit
  // =========================================================================
  describe('openEdit', () => {
    it('throws error when getById is not provided', async () => {
      const { openEdit } = createCRUD({ getById: undefined })

      await expect(openEdit('item-001')).rejects.toThrow('getById is required for edit functionality')
    })

    it('sets isEdit to true', async () => {
      const { isEdit, openEdit } = createCRUD()
      await openEdit('item-001')
      expect(isEdit.value).toBe(true)
    })

    it('sets editId', async () => {
      const { editId, openEdit } = createCRUD()
      await openEdit('item-001')
      expect(editId.value).toBe('item-001')
    })

    it('opens dialog', async () => {
      const { dialogOpen, openEdit } = createCRUD()
      await openEdit('item-001')
      expect(dialogOpen.value).toBe(true)
    })

    it('calls getById with correct id', async () => {
      const { openEdit } = createCRUD()
      await openEdit('item-001')
      expect(getById).toHaveBeenCalledWith('item-001')
    })

    it('populates form with mapDataToForm', async () => {
      getById.mockResolvedValue(mockApiResponse(makeItem({ name: 'Fetched Item' })))

      const { form, openEdit } = createCRUD({
        mapDataToForm: (data) => ({
          name: data.name,
          status: data.status
        })
      })

      await openEdit('item-001')
      expect(form.name).toBe('Fetched Item')
    })

    it('closes dialog on fetch error', async () => {
      getById.mockRejectedValue(new Error('Not found'))

      const { dialogOpen, openEdit } = createCRUD()
      await openEdit('item-001')

      expect(dialogOpen.value).toBe(false)
    })
  })

  // =========================================================================
  // 5. handleSubmit
  // =========================================================================
  describe('handleSubmit', () => {
    it('calls create when not in edit mode', async () => {
      const { openCreate, handleSubmit } = createCRUD()

      openCreate()
      await handleSubmit()

      expect(create).toHaveBeenCalled()
      expect(update).not.toHaveBeenCalled()
    })

    it('calls update when in edit mode', async () => {
      const { openEdit, handleSubmit, form } = createCRUD()

      await openEdit('item-001')
      form.name = 'Updated Name'
      await handleSubmit()

      expect(update).toHaveBeenCalledWith('item-001', expect.objectContaining({ name: 'Updated Name' }))
      expect(create).not.toHaveBeenCalled()
    })

    it('shows success toast after create', async () => {
      const { openCreate, handleSubmit } = createCRUD({
        successMessages: { create: '创建成功' }
      })

      openCreate()
      await handleSubmit()

      expect(toastMocks.success).toHaveBeenCalledWith('创建成功')
    })

    it('shows success toast after update', async () => {
      const { openEdit, handleSubmit } = createCRUD({
        successMessages: { update: '更新成功' }
      })

      await openEdit('item-001')
      await handleSubmit()

      expect(toastMocks.success).toHaveBeenCalledWith('更新成功')
    })

    it('calls onSuccess callback', async () => {
      const onSuccess = vi.fn()
      const { openCreate, handleSubmit } = createCRUD({ onSuccess })

      openCreate()
      await handleSubmit()

      expect(onSuccess).toHaveBeenCalledWith('create', expect.any(Object))
    })

    it('closes dialog after success', async () => {
      const { openCreate, handleSubmit, dialogOpen } = createCRUD()

      openCreate()
      await handleSubmit()

      expect(dialogOpen.value).toBe(false)
    })

    it('refreshes list after success', async () => {
      getList.mockClear() // Clear any previous calls

      const { openCreate, handleSubmit } = createCRUD()

      openCreate()
      await handleSubmit()
      await nextTick()

      // getList is called once in fetchList after successful submit
      expect(getList).toHaveBeenCalled()
    })

    it('validates form before submit (returns false)', async () => {
      const validate = vi.fn().mockReturnValue(false)
      const { openCreate, handleSubmit } = createCRUD({ validate })

      openCreate()
      await handleSubmit()

      expect(create).not.toHaveBeenCalled()
    })

    it('validates form before submit (returns warning string)', async () => {
      const validate = vi.fn().mockReturnValue('Name is required')
      const { openCreate, handleSubmit } = createCRUD({ validate })

      openCreate()
      await handleSubmit()

      expect(toastMocks.warning).toHaveBeenCalledWith('Name is required')
      expect(create).not.toHaveBeenCalled()
    })

    it('transforms form data with prepareFormData', async () => {
      const prepareFormData = vi.fn((form, isEdit) => ({
        ...form,
        name: form.name.toUpperCase()
      }))

      const { openCreate, handleSubmit, form } = createCRUD({ prepareFormData })

      openCreate()
      form.name = 'test'
      await handleSubmit()

      expect(prepareFormData).toHaveBeenCalled()
      expect(create).toHaveBeenCalledWith(expect.objectContaining({ name: 'TEST' }))
    })
  })

  // =========================================================================
  // 6. openDeleteConfirm
  // =========================================================================
  describe('openDeleteConfirm', () => {
    it('sets deleteId', () => {
      const { deleteId, openDeleteConfirm } = createCRUD()
      openDeleteConfirm('item-001')
      expect(deleteId.value).toBe('item-001')
    })

    it('opens delete dialog', () => {
      const { deleteDialogOpen, openDeleteConfirm } = createCRUD()
      openDeleteConfirm('item-001')
      expect(deleteDialogOpen.value).toBe(true)
    })
  })

  // =========================================================================
  // 7. handleDelete
  // =========================================================================
  describe('handleDelete', () => {
    it('calls delete API', async () => {
      const { openDeleteConfirm, handleDelete } = createCRUD()

      openDeleteConfirm('item-001')
      await handleDelete()

      expect(deleteFn).toHaveBeenCalledWith('item-001')
    })

    it('shows success toast', async () => {
      const { openDeleteConfirm, handleDelete } = createCRUD({
        successMessages: { delete: '删除成功' }
      })

      openDeleteConfirm('item-001')
      await handleDelete()

      expect(toastMocks.success).toHaveBeenCalledWith('删除成功')
    })

    it('calls onSuccess callback', async () => {
      const onSuccess = vi.fn()
      const { openDeleteConfirm, handleDelete } = createCRUD({ onSuccess })

      openDeleteConfirm('item-001')
      await handleDelete()

      expect(onSuccess).toHaveBeenCalledWith('delete')
    })

    it('closes dialog after success', async () => {
      const { openDeleteConfirm, handleDelete, deleteDialogOpen } = createCRUD()

      openDeleteConfirm('item-001')
      await handleDelete()

      expect(deleteDialogOpen.value).toBe(false)
    })

    it('refreshes list after success', async () => {
      const { openDeleteConfirm, handleDelete } = createCRUD()

      openDeleteConfirm('item-001')
      await handleDelete()
      await nextTick()

      expect(getList).toHaveBeenCalled()
    })

    it('does nothing if deleteId is empty', async () => {
      const { handleDelete } = createCRUD()

      await handleDelete()

      expect(deleteFn).not.toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 8. resetForm
  // =========================================================================
  describe('resetForm', () => {
    it('resets form to default values', () => {
      const { form, resetForm } = createCRUD()

      form.name = 'Modified'
      form.status = 0

      resetForm()

      expect(form.name).toBe('')
      expect(form.status).toBe(1)
    })

    it('calls custom resetForm if provided', () => {
      const customResetForm = vi.fn()
      const { resetForm } = createCRUD({ resetForm: customResetForm })

      resetForm()

      expect(customResetForm).toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 9. Edge Cases
  // =========================================================================
  describe('Edge Cases', () => {
    it('works without optional API functions', async () => {
      const { openCreate, handleSubmit, openDeleteConfirm, handleDelete } = createCRUD({
        create: undefined,
        update: undefined,
        delete: undefined
      })

      // Should not throw
      openCreate()
      await handleSubmit()
      await openDeleteConfirm('item-001')
      await handleDelete()
    })

    it('handles undefined getById gracefully', async () => {
      const { openEdit } = createCRUD({ getById: undefined })

      await expect(openEdit('item-001')).rejects.toThrow()
    })
  })
})