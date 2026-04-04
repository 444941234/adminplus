import { ref, type Ref, reactive, type UnwrapNestedRefs } from 'vue'
import { useAsyncAction } from './useAsyncAction'
import { showErrorToast } from './useApiInterceptors'

/**
 * CRUD composable - Eliminates repetitive CRUD page patterns
 *
 * Encapsulates:
 * - List data fetching with loading state
 * - Dialog state management (open/close, create/edit mode)
 * - Form state management with reset
 * - CRUD actions with toast notifications
 *
 * Works with usePageList for paginated lists, or standalone for simple lists
 */

export interface ApiResponse<T> {
  data: T
  code?: number
  message?: string
}

export interface CRUDOptions<T, TForm> {
  /** API function to fetch list data */
  getList: (params?: Record<string, unknown>) => Promise<ApiResponse<T[]>>

  /** API function to fetch single item by ID (optional, for edit) */
  getById?: (id: string) => Promise<ApiResponse<T>>

  /** API function to create new item (optional) */
  create?: (data: TForm) => Promise<ApiResponse<T>>

  /** API function to update existing item (optional) */
  update?: (id: string, data: TForm) => Promise<ApiResponse<T | void>>

  /** API function to delete item (optional) */
  delete?: (id: string) => Promise<ApiResponse<void>>

  /** Default form values */
  defaultForm: TForm

  /** Custom form reset function (optional, overrides default behavior) */
  resetForm?: () => void

  /** Map API response data to form fields (required if getById provided) */
  mapDataToForm?: (data: T) => TForm

  /** Success messages for each action */
  successMessages?: {
    create?: string
    update?: string
    delete?: string
  }

  /** Error messages for each action */
  errorMessages?: {
    list?: string
    getById?: string
    create?: string
    update?: string
    delete?: string
  }

  /** Callback after successful action */
  onSuccess?: (action: 'create' | 'update' | 'delete', data?: T) => void

  /** Callback after fetching list (for custom data transformation) */
  onListFetched?: (data: T[]) => void

  /** Custom validation before submit (return true to proceed, false to abort) */
  validate?: (form: TForm, isEdit: boolean) => boolean | string

  /** Prepare form data for API call (optional, for data transformation) */
  prepareFormData?: (form: TForm, isEdit: boolean) => TForm
}

export interface CRUDReturn<T, TForm> {
  /** List data */
  list: Ref<T[]>

  /** List loading state */
  loading: Ref<boolean>

  /** Form state (reactive object) */
  form: UnwrapNestedRefs<TForm>

  /** Dialog open state */
  dialogOpen: Ref<boolean>

  /** Edit mode flag */
  isEdit: Ref<boolean>

  /** Current edit item ID */
  editId: Ref<string>

  /** Dialog/form loading state */
  dialogLoading: Ref<boolean>

  /** Delete operation loading state */
  deleteLoading: Ref<boolean>

  /** Delete confirmation dialog open state */
  deleteDialogOpen: Ref<boolean>

  /** Current delete item ID */
  deleteId: Ref<string>

  /** Fetch list data */
  fetchList: () => Promise<void>

  /** Open create dialog */
  openCreate: () => void

  /** Open edit dialog and fetch item data */
  openEdit: (id: string) => Promise<void>

  /** Handle form submit (create or update) */
  handleSubmit: () => Promise<void>

  /** Open delete confirmation dialog */
  openDeleteConfirm: (id: string) => void

  /** Handle delete action */
  handleDelete: () => Promise<void>

  /** Reset form to default values */
  resetForm: () => void
}

export function useCRUD<T, TForm extends object>(options: CRUDOptions<T, TForm>): CRUDReturn<T, TForm> {
  const {
    getList,
    getById,
    create,
    update,
    delete: deleteFn,
    defaultForm,
    resetForm: customResetForm,
    mapDataToForm,
    successMessages = {},
    errorMessages = {},
    onSuccess,
    onListFetched,
    validate,
    prepareFormData
  } = options

  // Data state
  const list = ref<T[]>([]) as Ref<T[]>
  const loading = ref(false)

  // Dialog state
  const dialogOpen = ref(false)
  const isEdit = ref(false)
  const editId = ref<string>('')

  // Delete confirmation state
  const deleteDialogOpen = ref(false)
  const deleteId = ref<string>('')

  // Form state - use reactive for form object
  const form = reactive<TForm>(structuredClone(defaultForm) as TForm) as UnwrapNestedRefs<TForm>

  // Async action wrappers
  const { loading: dialogLoading, run: runDialog } = useAsyncAction(errorMessages.getById || '操作失败')
  const { loading: deleteLoading, run: runDelete } = useAsyncAction(errorMessages.delete || '删除失败')

  // Reset form function
  const resetForm = () => {
    if (customResetForm) {
      customResetForm()
    } else {
      Object.assign(form as object, structuredClone(defaultForm) as object)
    }
  }

  // Fetch list
  const fetchList = async () => {
    loading.value = true
    try {
      const res = await getList()
      list.value = res.data
      onListFetched?.(res.data)
    } catch (error) {
      showErrorToast(error, errorMessages.list || '获取列表失败')
    } finally {
      loading.value = false
    }
  }

  // Open create dialog
  const openCreate = () => {
    resetForm()
    isEdit.value = false
    editId.value = ''
    dialogOpen.value = true
  }

  // Open edit dialog
  const openEdit = async (id: string) => {
    if (!getById) {
      throw new Error('getById is required for edit functionality')
    }

    isEdit.value = true
    editId.value = id
    dialogOpen.value = true

    await runDialog(async () => {
      const res = await getById(id)
      if (mapDataToForm) {
        Object.assign(form as object, mapDataToForm(res.data))
      } else {
        // If no mapDataToForm, try direct assignment (works if TForm extends T)
        Object.assign(form as object, res.data)
      }
    }, {
      errorMessage: errorMessages.getById || '获取详情失败',
      onError: () => {
        dialogOpen.value = false
      }
    })
  }

  // Handle form submit
  const handleSubmit = async () => {
    // Run validation if provided
    if (validate) {
      const result = validate(form as TForm, isEdit.value)
      if (result === false) return
      if (typeof result === 'string') {
        const { toast } = await import('vue-sonner')
        toast.warning(result)
        return
      }
    }

    // Prepare form data
    const formData = prepareFormData ? prepareFormData(form as TForm, isEdit.value) : (form as TForm)

    await runDialog(async () => {
      if (isEdit.value && editId.value && update) {
        await update(editId.value, formData)
        onSuccess?.('update')
      } else if (!isEdit.value && create) {
        const res = await create(formData)
        onSuccess?.('create', res.data)
      }
    }, {
      successMessage: isEdit.value
        ? (successMessages.update || '更新成功')
        : (successMessages.create || '创建成功'),
      errorMessage: isEdit.value
        ? (errorMessages.update || '更新失败')
        : (errorMessages.create || '创建失败'),
      onSuccess: async () => {
        dialogOpen.value = false
        await fetchList()
      }
    })
  }

  // Open delete confirmation
  const openDeleteConfirm = (id: string) => {
    deleteId.value = id
    deleteDialogOpen.value = true
  }

  // Handle delete
  const handleDelete = async () => {
    if (!deleteFn || !deleteId.value) return

    await runDelete(async () => {
      await deleteFn(deleteId.value!)
      onSuccess?.('delete')
    }, {
      successMessage: successMessages.delete || '删除成功',
      errorMessage: errorMessages.delete || '删除失败',
      onSuccess: async () => {
        await fetchList()
      }
    })

    deleteDialogOpen.value = false
  }

  return {
    list,
    loading,
    form,
    dialogOpen,
    isEdit,
    editId,
    dialogLoading,
    deleteLoading,
    deleteDialogOpen,
    deleteId,
    fetchList,
    openCreate,
    openEdit,
    handleSubmit,
    openDeleteConfirm,
    handleDelete,
    resetForm
  }
}