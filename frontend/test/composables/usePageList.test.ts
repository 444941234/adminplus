import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { usePageList } from '@/composables/usePageList'

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

interface TestRecord {
  id: string
  name: string
}

const makePageResult = (overrides: Partial<Record<string, any>> = {}) => ({
  records: [
    { id: '001', name: 'Record 1' },
    { id: '002', name: 'Record 2' }
  ],
  total: 10,
  page: 1,
  size: 10,
  ...overrides
})

describe('usePageList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // =========================================================================
  // 1. Initial State
  // =========================================================================
  describe('Initial State', () => {
    it('initializes with default page and size', () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { tableData, loading, searchQuery } = usePageList<TestRecord>(fetchFn)

      expect(tableData.page).toBe(1)
      expect(tableData.size).toBe(10)
      expect(tableData.records).toEqual([])
      expect(tableData.total).toBe(0)
      expect(loading.value).toBe(false)
      expect(searchQuery.value).toBe('')
    })

    it('initializes with custom page and size from options', () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { tableData } = usePageList<TestRecord>(fetchFn, { page: 2, size: 20 })

      expect(tableData.page).toBe(2)
      expect(tableData.size).toBe(20)
    })
  })

  // =========================================================================
  // 2. fetchData
  // =========================================================================
  describe('fetchData', () => {
    it('fetches data with default params', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { fetchData, loading, tableData } = usePageList<TestRecord>(fetchFn)

      await fetchData()

      expect(fetchFn).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: undefined
      })
      expect(tableData.records).toHaveLength(2)
      expect(tableData.total).toBe(10)
      expect(loading.value).toBe(false)
    })

    it('sets loading to true during fetch', async () => {
      const fetchFn = vi.fn().mockImplementation(() => {
        return new Promise((resolve) => {
          setTimeout(() => resolve({ data: makePageResult() }), 10)
        })
      })
      const { fetchData, loading } = usePageList<TestRecord>(fetchFn)

      const promise = fetchData()
      expect(loading.value).toBe(true)

      await promise
      expect(loading.value).toBe(false)
    })

    it('includes searchQuery in params when not empty', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { fetchData, searchQuery } = usePageList<TestRecord>(fetchFn)

      searchQuery.value = 'test search'
      await fetchData()

      expect(fetchFn).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: 'test search'
      })
    })

    it('trims searchQuery before sending', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { fetchData, searchQuery } = usePageList<TestRecord>(fetchFn)

      searchQuery.value = '  trimmed  '
      await fetchData()

      expect(fetchFn).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: 'trimmed'
      })
    })

    it('omits keyword when searchQuery is empty after trim', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { fetchData, searchQuery } = usePageList<TestRecord>(fetchFn)

      searchQuery.value = '   '
      await fetchData()

      expect(fetchFn).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: undefined
      })
    })

    it('merges extraParams into request', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { fetchData } = usePageList<TestRecord>(fetchFn)

      await fetchData({ status: 'active', category: 'A' })

      expect(fetchFn).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: undefined,
        status: 'active',
        category: 'A'
      })
    })

    it('calls getParams from options on each fetch', async () => {
      const getParams = vi.fn(() => ({ filter: 'value' }))
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { fetchData } = usePageList<TestRecord>(fetchFn, { getParams })

      await fetchData()

      expect(getParams).toHaveBeenCalled()
      expect(fetchFn).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: undefined,
        filter: 'value'
      })
    })

    it('shows error toast on fetch failure', async () => {
      const fetchFn = vi.fn().mockRejectedValue(new Error('API Error'))
      const { fetchData } = usePageList<TestRecord>(fetchFn)

      await fetchData()

      expect(toastMocks.error).toHaveBeenCalledWith('获取列表失败')
    })

    it('resets loading to false after error', async () => {
      const fetchFn = vi.fn().mockRejectedValue(new Error('API Error'))
      const { fetchData, loading } = usePageList<TestRecord>(fetchFn)

      await fetchData()

      expect(loading.value).toBe(false)
    })
  })

  // =========================================================================
  // 3. goToPage
  // =========================================================================
  describe('goToPage', () => {
    it('navigates to valid page and fetches data', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult({ total: 30, page: 2 }) })
      const { goToPage, tableData } = usePageList<TestRecord>(fetchFn, { size: 10 })

      // Set total first so totalPages = 3
      tableData.total = 30
      await goToPage(2)

      expect(tableData.page).toBe(2)
      expect(fetchFn).toHaveBeenCalled()
    })

    it('does not navigate to page less than 1', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { goToPage, tableData } = usePageList<TestRecord>(fetchFn)

      tableData.total = 10
      await goToPage(0)

      expect(tableData.page).toBe(1)
      expect(fetchFn).not.toHaveBeenCalled()
    })

    it('does not navigate to page greater than total pages', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult({ total: 20 }) })
      const { goToPage, tableData } = usePageList<TestRecord>(fetchFn, { size: 10 })

      await goToPage(3) // total pages = 2

      expect(tableData.page).toBe(1)
      expect(fetchFn).not.toHaveBeenCalled()
    })

    it('does not navigate to same page', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult({ page: 2 }) })
      const { goToPage, tableData } = usePageList<TestRecord>(fetchFn)

      tableData.page = 2
      await goToPage(2)

      expect(fetchFn).not.toHaveBeenCalled()
    })

    it('calculates total pages correctly', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult({ total: 25 }) })
      const { goToPage, tableData } = usePageList<TestRecord>(fetchFn, { size: 10 })

      // 25 items / 10 per page = 3 pages
      tableData.total = 25
      await goToPage(3)

      expect(fetchFn).toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 4. handleSearch
  // =========================================================================
  describe('handleSearch', () => {
    it('resets to page 1 and fetches data', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { handleSearch, tableData, searchQuery } = usePageList<TestRecord>(fetchFn)

      tableData.page = 5
      searchQuery.value = 'test query'

      await handleSearch()

      expect(tableData.page).toBe(1)
      expect(fetchFn).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: 'test query'
      })
    })

    it('trims whitespace from search query', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { handleSearch, searchQuery } = usePageList<TestRecord>(fetchFn)

      searchQuery.value = '  query  '
      await handleSearch()

      expect(fetchFn).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: 'query'
      })
    })
  })

  // =========================================================================
  // 5. handleReset
  // =========================================================================
  describe('handleReset', () => {
    it('clears searchQuery, resets page, and fetches data', async () => {
      const fetchFn = vi.fn().mockResolvedValue({ data: makePageResult() })
      const { handleReset, tableData, searchQuery } = usePageList<TestRecord>(fetchFn)

      tableData.page = 5
      searchQuery.value = 'test query'

      await handleReset()

      expect(searchQuery.value).toBe('')
      expect(tableData.page).toBe(1)
      expect(fetchFn).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: undefined
      })
    })
  })

  // =========================================================================
  // 6. Integration Scenarios
  // =========================================================================
  describe('Integration Scenarios', () => {
    it('handles typical search workflow', async () => {
      const fetchFn = vi.fn().mockImplementation((params: any) => {
        const page = params.page || 1
        return Promise.resolve({ data: makePageResult({ page }) })
      })
      const { searchQuery, handleSearch, goToPage, fetchData, tableData } = usePageList<TestRecord>(fetchFn)

      // Initial load
      await fetchData()
      expect(fetchFn).toHaveBeenCalledTimes(1)

      // Search
      searchQuery.value = 'keyword'
      await handleSearch()
      expect(tableData.page).toBe(1)
      expect(fetchFn).toHaveBeenCalledTimes(2)

      // Navigate to page 2
      tableData.total = 25
      await goToPage(2)
      expect(tableData.page).toBe(2)
      expect(fetchFn).toHaveBeenCalledTimes(3)
    })

    it('maintains page size during navigation', async () => {
      const fetchFn = vi.fn().mockResolvedValue((params: any) => ({
        data: makePageResult({ page: params.page, size: params.size })
      }))
      const { goToPage, fetchData, tableData } = usePageList<TestRecord>(fetchFn, { size: 20 })

      await fetchData()
      expect(tableData.size).toBe(20)

      await goToPage(2)
      expect(tableData.size).toBe(20)
    })
  })
})
