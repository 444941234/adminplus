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
  getStatistics: vi.fn(),
  getOnlineUsers: vi.fn(),
  getSystemInfo: vi.fn()
}))

vi.mock('lucide-vue-next', () => ({
  Activity: { name: 'Activity', template: '<svg />' },
  Eye: { name: 'Eye', template: '<svg />' },
  TrendingUp: { name: 'TrendingUp', template: '<svg />' },
  UserPlus: { name: 'UserPlus', template: '<svg />' }
}))

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

import Statistics from '@/views/analysis/Statistics.vue'
import { toast } from 'vue-sonner'
import { getStatistics, getOnlineUsers, getSystemInfo } from '@/api'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeStatisticsData(overrides: Partial<Record<string, any>> = {}) {
  return {
    totalUsers: 100,
    todayVisits: 50,
    activeUsers: 30,
    todayNewUsers: 5,
    userGrowthData: {
      labels: ['2026-03-25', '2026-03-26', '2026-03-27'],
      values: [10, 20, 30]
    },
    visitTrendData: {
      labels: ['2026-03-25', '2026-03-26', '2026-03-27'],
      values: [100, 200, 150]
    },
    ...overrides
  }
}

function makeOnlineUser(overrides: Partial<Record<string, any>> = {}) {
  return {
    userId: 'u1',
    username: 'admin',
    loginTime: '2026-03-29 10:00:00',
    ip: '127.0.0.1',
    browser: 'Chrome 120',
    os: 'Windows 11',
    ...overrides
  }
}

function makeSystemInfo(overrides: Partial<Record<string, any>> = {}) {
  return {
    osName: 'Linux',
    osVersion: '5.15.0',
    javaVersion: '21',
    jvmMemory: '512MB',
    cpuUsage: 25.5,
    memoryUsage: 50,
    diskUsage: 60,
    ...overrides
  }
}

const mockApiResponse = (data: any) => ({
  code: 200,
  message: 'success',
  data
})

const flushAsync = async () => {
  await new Promise(resolve => setTimeout(resolve, 0))
  await nextTick()
  await nextTick()
  await nextTick()
}

// ---------------------------------------------------------------------------
// Test suite
// ---------------------------------------------------------------------------

describe('Statistics Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()

    vi.mocked(getStatistics).mockResolvedValue(
      mockApiResponse(makeStatisticsData()) as any
    )
    vi.mocked(getOnlineUsers).mockResolvedValue(
      mockApiResponse([makeOnlineUser()]) as any
    )
    vi.mocked(getSystemInfo).mockResolvedValue(
      mockApiResponse(makeSystemInfo()) as any
    )
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountAndFlush = async (options: any = {}) => {
    wrapper = mount(Statistics, {
      global: {
        plugins: [pinia],
        stubs: {
          Sonner: true
        },
        ...options.global
      },
      ...options
    } as any)
    await flushAsync()
    return wrapper
  }

  // =========================================================================
  // 1. Page structure
  // =========================================================================
  describe('Page Structure', () => {
    it('renders root container', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('.space-y-6').exists()).toBe(true)
    })

    it('renders four summary cards', async () => {
      wrapper = await mountAndFlush()
      const cards = wrapper.findAll('.space-y-6 > .grid:first-child .rounded-xl')
      // There are 4 cards in the summary grid
      const vm = wrapper.vm as any
      expect(vm.summaryCards.length).toBe(4)
    })

    it('renders user growth card title', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('用户增长趋势')
    })

    it('renders visit trend card title', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('访问量趋势')
    })

    it('renders online users card title', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('在线用户')
    })

    it('renders system info card title', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('系统信息')
    })
  })

  // =========================================================================
  // 2. Data fetching
  // =========================================================================
  describe('Data Fetching', () => {
    it('calls all three APIs on mount', async () => {
      wrapper = await mountAndFlush()
      expect(getStatistics).toHaveBeenCalled()
      expect(getOnlineUsers).toHaveBeenCalled()
      expect(getSystemInfo).toHaveBeenCalled()
    })

    it('populates statistics from API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.statistics.totalUsers).toBe(100)
      expect(vm.statistics.todayVisits).toBe(50)
      expect(vm.statistics.activeUsers).toBe(30)
      expect(vm.statistics.todayNewUsers).toBe(5)
    })

    it('populates onlineUsers from API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.onlineUsers.length).toBe(1)
      expect(vm.onlineUsers[0].username).toBe('admin')
    })

    it('populates systemInfo from API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.systemInfo.osName).toBe('Linux')
      expect(vm.systemInfo.cpuUsage).toBe(25.5)
    })
  })

  // =========================================================================
  // 3. Summary cards
  // =========================================================================
  describe('Summary Cards', () => {
    it('shows totalUsers value', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('100')
    })

    it('shows todayVisits value', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('50')
    })

    it('shows activeUsers value', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('30')
    })

    it('shows todayNewUsers value', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('5')
    })

    it('shows ... while loading', async () => {
      vi.mocked(getStatistics).mockReturnValue(new Promise(() => {}))
      vi.mocked(getOnlineUsers).mockReturnValue(new Promise(() => {}))
      vi.mocked(getSystemInfo).mockReturnValue(new Promise(() => {}))

      wrapper = mount(Statistics, {
        global: { plugins: [pinia], stubs: { Sonner: true } }
      } as any)
      await nextTick()

      expect(wrapper.text()).toContain('...')
    })

    it('shows label for each card', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('用户总数')
      expect(wrapper.text()).toContain('今日访问')
      expect(wrapper.text()).toContain('活跃用户')
      expect(wrapper.text()).toContain('今日新增')
    })

    it('shows 0 when statistics value is null', async () => {
      vi.mocked(getStatistics).mockResolvedValue(
        mockApiResponse(makeStatisticsData({ totalUsers: null, todayVisits: null })) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.summaryCards[0].value).toBe(0)
      expect(vm.summaryCards[1].value).toBe(0)
    })
  })

  // =========================================================================
  // 4. User growth chart
  // =========================================================================
  describe('User Growth Chart', () => {
    it('renders growth labels', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('2026-03-25')
      expect(wrapper.text()).toContain('2026-03-26')
      expect(wrapper.text()).toContain('2026-03-27')
    })

    it('renders growth values', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('10')
      expect(wrapper.text()).toContain('20')
      expect(wrapper.text()).toContain('30')
    })

    it('shows empty text when no growth data', async () => {
      vi.mocked(getStatistics).mockResolvedValue(
        mockApiResponse(makeStatisticsData({ userGrowthData: { labels: [], values: [] } })) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('暂无趋势数据')
    })

    it('shows empty text when growth data is null', async () => {
      vi.mocked(getStatistics).mockResolvedValue(
        mockApiResponse(makeStatisticsData({ userGrowthData: null })) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('暂无趋势数据')
    })
  })

  // =========================================================================
  // 5. Visit trend chart
  // =========================================================================
  describe('Visit Trend Chart', () => {
    it('renders visit labels', async () => {
      wrapper = await mountAndFlush()
      // Visit labels are same as growth labels
      expect(wrapper.text()).toContain('2026-03-25')
    })

    it('renders visit values', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('100')
      expect(wrapper.text()).toContain('200')
      expect(wrapper.text()).toContain('150')
    })

    it('shows empty text when no visit data', async () => {
      vi.mocked(getStatistics).mockResolvedValue(
        mockApiResponse(makeStatisticsData({ visitTrendData: { labels: [], values: [] } })) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('暂无访问数据')
    })

    it('shows empty text when visit data is null', async () => {
      vi.mocked(getStatistics).mockResolvedValue(
        mockApiResponse(makeStatisticsData({ visitTrendData: null })) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('暂无访问数据')
    })
  })

  // =========================================================================
  // 6. Online users table
  // =========================================================================
  describe('Online Users Table', () => {
    it('renders table headers', async () => {
      wrapper = await mountAndFlush()
      const headers = wrapper.findAll('th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts.some(t => t.includes('用户名'))).toBe(true)
      expect(headerTexts.some(t => t.includes('IP'))).toBe(true)
      expect(headerTexts.some(t => t.includes('浏览器'))).toBe(true)
      expect(headerTexts.some(t => t.includes('操作系统'))).toBe(true)
      expect(headerTexts.some(t => t.includes('登录时间'))).toBe(true)
    })

    it('renders user details', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('admin')
      expect(wrapper.text()).toContain('127.0.0.1')
    })

    it('shows dash for missing ip', async () => {
      vi.mocked(getOnlineUsers).mockResolvedValue(
        mockApiResponse([makeOnlineUser({ ip: undefined })]) as any
      )
      wrapper = await mountAndFlush()
      // The template renders {{ user.ip || '-' }}
      expect(wrapper.text()).toContain('-')
    })

    it('shows dash for missing browser', async () => {
      vi.mocked(getOnlineUsers).mockResolvedValue(
        mockApiResponse([makeOnlineUser({ browser: undefined })]) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('-')
    })

    it('shows dash for missing os', async () => {
      vi.mocked(getOnlineUsers).mockResolvedValue(
        mockApiResponse([makeOnlineUser({ os: undefined })]) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('-')
    })

    it('renders multiple online users', async () => {
      vi.mocked(getOnlineUsers).mockResolvedValue(
        mockApiResponse([
          makeOnlineUser({ userId: 'u1', username: 'admin' }),
          makeOnlineUser({ userId: 'u2', username: 'guest' })
        ]) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('admin')
      expect(wrapper.text()).toContain('guest')
    })

    it('shows empty text when no online users', async () => {
      vi.mocked(getOnlineUsers).mockResolvedValue(
        mockApiResponse([]) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('暂无在线用户')
    })
  })

  // =========================================================================
  // 7. System info
  // =========================================================================
  describe('System Info', () => {
    it('renders OS info', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('Linux')
      expect(wrapper.text()).toContain('5.15.0')
    })

    it('renders Java version', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('21')
    })

    it('renders JVM memory', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('512MB')
    })

    it('renders CPU usage with percent', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('25.5%')
    })

    it('renders memory usage with percent', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('50%')
    })

    it('renders disk usage with percent', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('60%')
    })

    it('shows empty text when systemInfo is null', async () => {
      vi.mocked(getSystemInfo).mockResolvedValue(
        mockApiResponse(null) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('暂无系统信息')
    })

    it('renders label texts', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('系统')
      expect(wrapper.text()).toContain('Java')
      expect(wrapper.text()).toContain('JVM 内存')
      expect(wrapper.text()).toContain('CPU 使用率')
      expect(wrapper.text()).toContain('内存使用率')
      expect(wrapper.text()).toContain('磁盘使用率')
    })
  })

  // =========================================================================
  // 8. toNumbers helper
  // =========================================================================
  describe('toNumbers Helper', () => {
    it('converts string values to numbers', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.toNumbers(['10', '20', '30'])).toEqual([10, 20, 30])
    })

    it('handles mixed number/string values', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.toNumbers([10, '20', 30])).toEqual([10, 20, 30])
    })

    it('returns 0 for non-numeric strings', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.toNumbers(['abc', '10'])).toEqual([0, 10])
    })

    it('returns empty array for undefined input', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.toNumbers(undefined)).toEqual([])
    })
  })

  // =========================================================================
  // 9. getMaxValue helper
  // =========================================================================
  describe('getMaxValue Helper', () => {
    it('returns max value from chart data', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.getMaxValue({ labels: ['a'], values: [10, 20, 30] })).toBe(30)
    })

    it('returns at least 1 when all values are 0', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.getMaxValue({ labels: ['a'], values: [0, 0] })).toBe(1)
    })

    it('returns 1 for null chart', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.getMaxValue(null)).toBe(1)
    })

    it('returns 1 for undefined chart', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.getMaxValue(undefined)).toBe(1)
    })
  })

  // =========================================================================
  // 10. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('shows loading text in table while fetching', async () => {
      vi.mocked(getStatistics).mockReturnValue(new Promise(() => {}))
      vi.mocked(getOnlineUsers).mockReturnValue(new Promise(() => {}))
      vi.mocked(getSystemInfo).mockReturnValue(new Promise(() => {}))

      wrapper = mount(Statistics, {
        global: { plugins: [pinia], stubs: { Sonner: true } }
      } as any)
      await nextTick()

      expect(wrapper.text()).toContain('加载中...')
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
    it('handles API error gracefully', async () => {
      vi.mocked(getStatistics).mockRejectedValue(new Error('统计获取失败'))
      vi.mocked(getOnlineUsers).mockRejectedValue(new Error('用户获取失败'))
      vi.mocked(getSystemInfo).mockRejectedValue(new Error('系统获取失败'))
      wrapper = await mountAndFlush()
      expect(toast.error).toHaveBeenCalled()
    })
  })
})
