import { reactive, ref } from 'vue'
import { toast } from 'vue-sonner'

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface UsePageListOptions {
  page?: number
  size?: number
  /** Reactive params getter — called on every fetch (search, reset, page change) */
  getParams?: () => Record<string, unknown>
}

/**
 * 分页列表数据管理 composable
 *
 * 封装 loading / tableData / fetchData / 分页跳转 / 错误处理
 */
export function usePageList<T>(
  fetchFn: (params: Record<string, unknown>) => Promise<{ data: PageResult<T> }>,
  options: UsePageListOptions = {}
) {
  const loading = ref(false)
  const searchQuery = ref('')

  const tableData = reactive<PageResult<T>>({
    records: [] as unknown as T[],
    total: 0,
    page: options.page ?? 1,
    size: options.size ?? 10
  })

  const fetchData = async (extraParams?: Record<string, unknown>) => {
    loading.value = true
    try {
      const res = await fetchFn({
        page: tableData.page,
        size: tableData.size,
        keyword: searchQuery.value.trim() || undefined,
        ...options.getParams?.(),
        ...extraParams
      })
      Object.assign(tableData, {
        records: res.data.records,
        total: res.data.total,
        page: res.data.page,
        size: res.data.size
      })
    } catch (error) {
      const message = error instanceof Error ? error.message : '获取列表失败'
      toast.error(message)
    } finally {
      loading.value = false
    }
  }

  const goToPage = (page: number) => {
    const totalPages = Math.ceil(tableData.total / tableData.size) || 1
    if (page >= 1 && page <= totalPages && page !== tableData.page) {
      tableData.page = page
      fetchData()
    }
  }

  const handleSearch = () => {
    tableData.page = 1
    fetchData()
  }

  const handleReset = () => {
    searchQuery.value = ''
    tableData.page = 1
    fetchData()
  }

  return {
    loading,
    searchQuery,
    tableData,
    fetchData,
    goToPage,
    handleSearch,
    handleReset
  }
}
