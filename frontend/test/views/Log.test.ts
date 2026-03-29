import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'

// ---------------------------------------------------------------------------
// Mocks
// ---------------------------------------------------------------------------

vi.mock('vue-sonner', () => {
  const mock = {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn()
  }
  return { toast: mock }
})

vi.mock('@/components/ui/sonner/Sonner.vue', () => ({
  default: { name: 'Sonner', template: '<div />' }
}))

vi.mock('@/api', () => ({
  getLogList: vi.fn(),
  getLogById: vi.fn(),
  getLogStatistics: vi.fn(),
  deleteLog: vi.fn(),
  deleteLogsBatch: vi.fn(),
  deleteLogsByCondition: vi.fn(),
  cleanupExpiredLogs: vi.fn(),
  exportLogExcel: vi.fn(() => '/api/export/excel'),
  exportLogCsv: vi.fn(() => '/api/export/csv')
}))

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

import Log from '@/views/system/Log.vue'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'
import {
  getLogList,
  getLogById,
  getLogStatistics,
  deleteLog,
  deleteLogsBatch,
  deleteLogsByCondition,
  cleanupExpiredLogs
} from '@/api'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeLog(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'log1',
    logType: 1,
    username: 'admin',
    module: '用户管理',
    operationType: 2,
    description: '新增用户 test',
    requestMethod: 'POST',
    method: 'POST',
    requestUrl: '/api/v1/sys/users',
    requestParams: '{"username":"test"}',
    params: '{"username":"test"}',
    responseStatus: 200,
    ip: '127.0.0.1',
    location: '本地',
    status: 1,
    duration: 120,
    costTime: 120,
    errorMsg: '',
    createTime: '2026-03-20 10:00:00',
    ...overrides
  }
}

const mockPageResult = (records: any[] = [], total = 0) => ({
  code: 200,
  message: 'success',
  data: { records, total, page: 1, size: 10 }
})

const mockApiResponse = (data: any) => ({
  code: 200,
  message: 'success',
  data
})

const mockStats = {
  totalCount: 100,
  loginCount: 30,
  operationCount: 60,
  systemCount: 10,
  todayCount: 5,
  successCount: 90,
  failureCount: 10
}

const flushAsync = async () => {
  await new Promise(resolve => setTimeout(resolve, 0))
  await nextTick()
  await nextTick()
  await nextTick()
}

// ---------------------------------------------------------------------------
// Test suite
// ---------------------------------------------------------------------------

describe('Log Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper
  let userStore: ReturnType<typeof useUserStore>

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)

    userStore = useUserStore()
    userStore.hasPermission = vi.fn(() => true)

    vi.clearAllMocks()

    vi.mocked(getLogList).mockResolvedValue(mockPageResult([makeLog()]) as any)
    vi.mocked(getLogStatistics).mockResolvedValue(mockApiResponse(mockStats) as any)
    vi.mocked(getLogById).mockResolvedValue(mockApiResponse(makeLog()) as any)
    vi.mocked(deleteLog).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(deleteLogsBatch).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(deleteLogsByCondition).mockResolvedValue(mockApiResponse(5) as any)
    vi.mocked(cleanupExpiredLogs).mockResolvedValue(mockApiResponse(3) as any)
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountAndFlush = async (options = {}) => {
    wrapper = mount(Log, {
      global: {
        plugins: [pinia],
        stubs: {
          ConfirmDialog: true,
          Pagination: true,
          StatusBadge: true
        }
      },
      ...options
    })
    await flushAsync()
    return wrapper
  }

  // =========================================================================
  // 1. Page structure
  // =========================================================================
  describe('Page Structure', () => {
    it('renders root container', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('.space-y-4').exists()).toBe(true)
    })

    it('renders statistics cards', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('日志总数')
      expect(wrapper.text()).toContain('今日日志')
      expect(wrapper.text()).toContain('成功 / 失败')
    })

    it('renders filter controls with search and reset buttons', async () => {
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const buttonTexts = buttons.map(b => b.text())
      expect(buttonTexts.some(t => t.includes('搜索'))).toBe(true)
      expect(buttonTexts.some(t => t.includes('重置'))).toBe(true)
    })

    it('renders table with column headers', async () => {
      wrapper = await mountAndFlush()
      const headers = wrapper.findAll('thead th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts).toContain('类型')
      expect(headerTexts).toContain('操作人')
      expect(headerTexts).toContain('状态')
      expect(headerTexts).toContain('操作')
    })

    it('renders Pagination stub', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.findComponent({ name: 'Pagination' }).exists()).toBe(true)
    })
  })

  // =========================================================================
  // 2. Data fetching on mount
  // =========================================================================
  describe('Data Fetching on Mount', () => {
    it('calls getLogList and getLogStatistics on mount', async () => {
      wrapper = await mountAndFlush()
      expect(getLogList).toHaveBeenCalled()
      expect(getLogStatistics).toHaveBeenCalled()
    })

    it('populates tableData with log records', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.tableData.records.length).toBe(1)
      expect(vm.tableData.records[0].username).toBe('admin')
    })

    it('populates statistics from API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.statistics.totalCount).toBe(100)
      expect(vm.statistics.loginCount).toBe(30)
      expect(vm.statistics.todayCount).toBe(5)
    })

    it('skips fetch when canQueryLog is false', async () => {
      userStore.hasPermission = vi.fn((perm: string) => perm !== 'log:query')
      wrapper = await mountAndFlush()
      expect(getLogList).not.toHaveBeenCalled()
      expect(getLogStatistics).not.toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 3. Statistics rendering
  // =========================================================================
  describe('Statistics Display', () => {
    it('shows statistics numbers', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('100')  // totalCount
      expect(wrapper.text()).toContain('5')    // todayCount
    })

    it('shows loading dash when statisticsLoading', async () => {
      vi.mocked(getLogList).mockReturnValue(new Promise(() => {}))
      vi.mocked(getLogStatistics).mockReturnValue(new Promise(() => {}))

      wrapper = mount(Log, {
        global: {
          plugins: [pinia],
          stubs: { ConfirmDialog: true, Pagination: true, StatusBadge: true }
        }
      })
      await nextTick()

      expect(wrapper.text()).toContain('-')
    })
  })

  // =========================================================================
  // 4. Search and filter
  // =========================================================================
  describe('Search and Filter', () => {
    it('handleSearch resets page and fetches data', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([]) as any)

      vm.tableData.page = 3
      await vm.handleSearch()
      await flushAsync()

      expect(vm.tableData.page).toBe(1)
      expect(getLogList).toHaveBeenCalled()
    })

    it('handleReset resets all filters and fetches', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([]) as any)

      vm.filters.username = 'admin'
      vm.filters.logType = '1'
      vm.filters.status = '0'
      await nextTick()

      await vm.handleReset()
      await flushAsync()

      expect(vm.filters.username).toBe('')
      expect(vm.filters.logType).toBe('all')
      expect(vm.filters.status).toBe('all')
      expect(getLogList).toHaveBeenCalled()
    })

    it('preserves valid selections after refresh', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedLogIds = ['log1']
      await nextTick()

      vi.clearAllMocks()
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([makeLog()]) as any)

      await vm.fetchDataWithSelection()
      await flushAsync()

      expect(vm.selectedLogIds).toContain('log1')
    })

    it('removes stale selections after refresh', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedLogIds = ['log1', 'log_deleted']
      await nextTick()

      vi.clearAllMocks()
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([makeLog()]) as any)

      await vm.fetchDataWithSelection()
      await flushAsync()

      expect(vm.selectedLogIds).toContain('log1')
      expect(vm.selectedLogIds).not.toContain('log_deleted')
    })
  })

  // =========================================================================
  // 5. View detail
  // =========================================================================
  describe('View Detail', () => {
    it('handleView opens dialog and fetches log', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.handleView('log1')
      await flushAsync()

      expect(vm.detailDialogOpen).toBe(true)
      expect(getLogById).toHaveBeenCalledWith('log1')
    })

    it('populates currentLog from API', async () => {
      vi.mocked(getLogById).mockResolvedValue(
        mockApiResponse(makeLog({ username: 'testuser', description: '测试操作' })) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.handleView('log1')
      await flushAsync()

      expect(vm.currentLog.username).toBe('testuser')
      expect(vm.currentLog.description).toBe('测试操作')
    })

    it('closes dialog on fetch error', async () => {
      vi.mocked(getLogById).mockRejectedValueOnce(new Error('Not found'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.handleView('bad-id')
      await flushAsync()

      expect(vm.detailDialogOpen).toBe(false)
    })
  })

  // =========================================================================
  // 6. Delete log
  // =========================================================================
  describe('Delete Log', () => {
    it('handleDeleteConfirm sets id and opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleDeleteConfirm('log-del')
      expect(vm.deleteLogId).toBe('log-del')
      expect(vm.deleteDialogOpen).toBe(true)
    })

    it('single delete calls deleteLog API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(getLogStatistics).mockResolvedValue(mockApiResponse(mockStats) as any)
      vi.mocked(deleteLog).mockResolvedValue(mockApiResponse(null) as any)

      vm.deleteLogId = 'log-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(deleteLog).toHaveBeenCalledWith('log-del')
      expect(vm.deleteDialogOpen).toBe(false)
      expect(toast.success).toHaveBeenCalledWith('日志删除成功')
    })

    it('batch delete calls deleteLogsBatch API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(getLogStatistics).mockResolvedValue(mockApiResponse(mockStats) as any)
      vi.mocked(deleteLogsBatch).mockResolvedValue(mockApiResponse(null) as any)

      vm.deleteLogId = ''
      vm.selectedLogIds = ['log1', 'log2']
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(deleteLogsBatch).toHaveBeenCalledWith(['log1', 'log2'])
      expect(toast.success).toHaveBeenCalledWith('已删除 2 条日志')
    })

    it('clears selectedLogIds after batch delete', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(getLogStatistics).mockResolvedValue(mockApiResponse(mockStats) as any)
      vi.mocked(deleteLogsBatch).mockResolvedValue(mockApiResponse(null) as any)

      vm.deleteLogId = ''
      vm.selectedLogIds = ['log1', 'log2']
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(vm.selectedLogIds).toEqual([])
    })

    it('closes dialog even on error', async () => {
      vi.mocked(deleteLog).mockRejectedValueOnce(new Error('删除失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.deleteLogId = 'log-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('删除失败')
      expect(vm.deleteDialogOpen).toBe(false)
    })

    it('handleBatchDeleteConfirm warns when nothing selected', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedLogIds = []
      vm.handleBatchDeleteConfirm()

      expect(toast.warning).toHaveBeenCalledWith('请先选择要删除的日志')
    })
  })

  // =========================================================================
  // 7. Cleanup
  // =========================================================================
  describe('Cleanup', () => {
    it('handleCleanupConfirm sets mode and opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleCleanupConfirm('condition')
      expect(vm.cleanupMode).toBe('condition')
      expect(vm.cleanupDialogOpen).toBe(true)

      vm.cleanupDialogOpen = false
      vm.handleCleanupConfirm('expired')
      expect(vm.cleanupMode).toBe('expired')
      expect(vm.cleanupDialogOpen).toBe(true)
    })

    it('condition cleanup calls deleteLogsByCondition', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(getLogStatistics).mockResolvedValue(mockApiResponse(mockStats) as any)
      vi.mocked(deleteLogsByCondition).mockResolvedValue(mockApiResponse(5) as any)

      vm.cleanupMode = 'condition'
      vm.cleanupDialogOpen = true

      await vm.handleCleanup()
      await flushAsync()

      expect(deleteLogsByCondition).toHaveBeenCalled()
      expect(toast.success).toHaveBeenCalledWith('已按条件清理 5 条日志')
      expect(vm.cleanupDialogOpen).toBe(false)
    })

    it('expired cleanup calls cleanupExpiredLogs', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(getLogStatistics).mockResolvedValue(mockApiResponse(mockStats) as any)
      vi.mocked(cleanupExpiredLogs).mockResolvedValue(mockApiResponse(3) as any)

      vm.cleanupMode = 'expired'
      vm.cleanupDialogOpen = true

      await vm.handleCleanup()
      await flushAsync()

      expect(cleanupExpiredLogs).toHaveBeenCalled()
      expect(toast.success).toHaveBeenCalledWith('已清理 3 条过期日志')
      expect(vm.cleanupDialogOpen).toBe(false)
    })

    it('clears selections after cleanup', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(getLogStatistics).mockResolvedValue(mockApiResponse(mockStats) as any)
      vi.mocked(deleteLogsByCondition).mockResolvedValue(mockApiResponse(2) as any)

      vm.cleanupMode = 'condition'
      vm.cleanupDialogOpen = true
      vm.selectedLogIds = ['log1']

      await vm.handleCleanup()
      await flushAsync()

      expect(vm.selectedLogIds).toEqual([])
    })

    it('closes dialog on error', async () => {
      vi.mocked(deleteLogsByCondition).mockRejectedValueOnce(new Error('清理失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.cleanupMode = 'condition'
      vm.cleanupDialogOpen = true

      await vm.handleCleanup()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('清理失败')
      expect(vm.cleanupDialogOpen).toBe(false)
    })
  })

  // =========================================================================
  // 8. Selection
  // =========================================================================
  describe('Selection', () => {
    it('toggleLogSelection adds and removes', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.toggleLogSelection('log1', true)
      expect(vm.selectedLogIds).toContain('log1')

      vm.toggleLogSelection('log1', false)
      expect(vm.selectedLogIds).not.toContain('log1')
    })

    it('toggleSelectAll selects all records', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.toggleSelectAll(true)
      expect(vm.selectedLogIds).toEqual(['log1'])
    })

    it('toggleSelectAll deselects all', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedLogIds = ['log1']
      vm.toggleSelectAll(false)
      expect(vm.selectedLogIds).toEqual([])
    })

    it('hasSelectedLogs computed', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.hasSelectedLogs).toBe(false)
      vm.selectedLogIds = ['log1']
      await nextTick()
      expect(vm.hasSelectedLogs).toBe(true)
    })

    it('allSelected computed', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedLogIds = ['log1']
      await nextTick()
      expect(vm.allSelected).toBe(true)

      vm.selectedLogIds = []
      await nextTick()
      expect(vm.allSelected).toBe(false)
    })
  })

  // =========================================================================
  // 9. Permissions
  // =========================================================================
  describe('Permissions', () => {
    it('computes permission flags correctly', async () => {
      userStore.hasPermission = vi.fn((perm: string) => {
        return ['log:query', 'log:delete'].includes(perm)
      })
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.canQueryLog).toBe(true)
      expect(vm.canDeleteLog).toBe(true)
      expect(vm.canExportLog).toBe(false)
    })

    it('all false when no permissions', async () => {
      userStore.hasPermission = vi.fn(() => false)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.canQueryLog).toBe(false)
      expect(vm.canDeleteLog).toBe(false)
      expect(vm.canExportLog).toBe(false)
    })
  })

  // =========================================================================
  // 10. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('shows loading text while loading', async () => {
      vi.mocked(getLogList).mockReturnValue(new Promise(() => {}))
      vi.mocked(getLogStatistics).mockReturnValue(new Promise(() => {}))

      wrapper = mount(Log, {
        global: {
          plugins: [pinia],
          stubs: { ConfirmDialog: true, Pagination: true, StatusBadge: true }
        }
      })
      await nextTick()

      expect(wrapper.text()).toContain('加载中...')
    })

    it('shows empty text when no records', async () => {
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([], 0) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('暂无日志数据')
    })

    it('sets loading to false after fetch', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.loading).toBe(false)
    })
  })

  // =========================================================================
  // 11. Error handling
  // =========================================================================
  describe('Error Handling', () => {
    it('handles getLogList error gracefully', async () => {
      vi.mocked(getLogList).mockRejectedValue(new Error('获取日志失败'))
      wrapper = await mountAndFlush()

      expect(toast.error).toHaveBeenCalledWith('获取日志失败')
    })

    it('handles getLogStatistics error gracefully', async () => {
      vi.mocked(getLogStatistics).mockRejectedValue(new Error('统计失败'))
      wrapper = await mountAndFlush()

      expect(toast.error).toHaveBeenCalledWith('统计失败')
    })
  })

  // =========================================================================
  // 12. Table content
  // =========================================================================
  describe('Table Content', () => {
    it('renders log data in table', async () => {
      vi.mocked(getLogList).mockResolvedValue(
        mockPageResult([makeLog({ username: 'admin', module: '用户管理', description: '新增用户' })]) as any
      )
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('admin')
      expect(wrapper.text()).toContain('用户管理')
      expect(wrapper.text()).toContain('新增用户')
    })

    it('renders multiple log rows', async () => {
      vi.mocked(getLogList).mockResolvedValue(
        mockPageResult([makeLog({ id: 'l1' }), makeLog({ id: 'l2' })], 2) as any
      )
      wrapper = await mountAndFlush()

      const rows = wrapper.findAll('tbody tr')
      expect(rows.length).toBe(2)
    })
  })

  // =========================================================================
  // 13. Label helpers
  // =========================================================================
  describe('Label Helpers', () => {
    it('getLogTypeLabel returns correct labels', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.getLogTypeLabel(1)).toBe('操作日志')
      expect(vm.getLogTypeLabel(2)).toBe('登录日志')
      expect(vm.getLogTypeLabel(3)).toBe('系统日志')
      expect(vm.getLogTypeLabel(99)).toBe('未知')
    })

    it('getOperationLabel returns correct labels', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.getOperationLabel(1)).toBe('查询')
      expect(vm.getOperationLabel(2)).toBe('新增')
      expect(vm.getOperationLabel(3)).toBe('修改')
      expect(vm.getOperationLabel(4)).toBe('删除')
      expect(vm.getOperationLabel(5)).toBe('导出')
      expect(vm.getOperationLabel(6)).toBe('导入')
      expect(vm.getOperationLabel(7)).toBe('其他')
      expect(vm.getOperationLabel(99)).toBe('其他')
    })

    it('getRequestMethod falls back to method', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.getRequestMethod(makeLog({ requestMethod: 'GET' }))).toBe('GET')
      expect(vm.getRequestMethod(makeLog({ requestMethod: undefined, method: 'POST' }))).toBe('POST')
      expect(vm.getRequestMethod(makeLog({ requestMethod: undefined, method: undefined }))).toBe('-')
    })

    it('getRequestParams falls back to params', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.getRequestParams(makeLog({ requestParams: '{"a":1}' }))).toBe('{"a":1}')
      expect(vm.getRequestParams(makeLog({ requestParams: undefined, params: '{"b":2}' }))).toBe('{"b":2}')
    })

    it('getDuration falls back to costTime', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.getDuration(makeLog({ duration: 100 }))).toBe(100)
      expect(vm.getDuration(makeLog({ duration: undefined, costTime: 200 }))).toBe(200)
    })
  })

  // =========================================================================
  // 14. Pagination
  // =========================================================================
  describe('Pagination', () => {
    it('passes correct props to Pagination', async () => {
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([makeLog()], 42) as any)
      wrapper = await mountAndFlush()
      const pagination = wrapper.findComponent({ name: 'Pagination' })

      expect(pagination.props('current')).toBe(1)
      expect(pagination.props('total')).toBe(42)
      expect(pagination.props('pageSize')).toBe(10)
    })

    it('calls goToPage on Pagination change event', async () => {
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([makeLog()], 25) as any)
      wrapper = await mountAndFlush()
      const pagination = wrapper.findComponent({ name: 'Pagination' })

      vi.clearAllMocks()
      vi.mocked(getLogList).mockResolvedValue(mockPageResult([]) as any)

      pagination.vm.$emit('change', 3)
      await nextTick()
      await nextTick()
      await nextTick()

      expect(getLogList).toHaveBeenCalledWith(expect.objectContaining({ page: 3 }))
    })
  })

  // =========================================================================
  // 15. Export
  // =========================================================================
  describe('Export', () => {
    it('handleExport calls exportLogExcel', async () => {
      const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleExport('excel')

      expect(openSpy).toHaveBeenCalled()
      openSpy.mockRestore()
    })

    it('handleExport calls exportLogCsv', async () => {
      const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleExport('csv')

      expect(openSpy).toHaveBeenCalled()
      openSpy.mockRestore()
    })
  })
})
