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
  getOnlineUsers: vi.fn(),
  getSystemInfo: vi.fn()
}))

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

import Config from '@/views/system/Config.vue'
import { toast } from 'vue-sonner'
import { getOnlineUsers, getSystemInfo } from '@/api'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeSystemInfo(overrides: Partial<Record<string, any>> = {}) {
  return {
    systemName: 'AdminPlus',
    systemVersion: '1.0.0',
    osName: 'Linux',
    osVersion: '5.15.0',
    javaVersion: '21',
    jdkVersion: '21.0.1',
    jvmMemory: '512MB',
    cpuUsage: 25.5,
    memoryUsage: 50,
    diskUsage: 60,
    totalMemory: 512,
    usedMemory: 256,
    freeMemory: 256,
    databaseType: 'PostgreSQL',
    databaseVersion: '16',
    databaseConnections: 5,
    uptime: 86400,
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

describe('Config Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()

    vi.mocked(getSystemInfo).mockResolvedValue(
      mockApiResponse(makeSystemInfo()) as any
    )
    vi.mocked(getOnlineUsers).mockResolvedValue(
      mockApiResponse([makeOnlineUser()]) as any
    )
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountAndFlush = async (options = {}) => {
    wrapper = mount(Config, {
      global: { plugins: [pinia] },
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
      expect(wrapper.find('.space-y-6').exists()).toBe(true)
    })

    it('renders page title', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('系统监控')
    })

    it('renders online users card', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('在线用户')
    })

    it('renders refresh button', async () => {
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const refreshBtn = buttons.find(b => b.text().includes('刷新'))
      expect(refreshBtn).toBeDefined()
    })
  })

  // =========================================================================
  // 2. Data fetching on mount
  // =========================================================================
  describe('Data Fetching on Mount', () => {
    it('calls getSystemInfo and getOnlineUsers on mount', async () => {
      wrapper = await mountAndFlush()
      expect(getSystemInfo).toHaveBeenCalled()
      expect(getOnlineUsers).toHaveBeenCalled()
    })

    it('populates systemInfo from API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.systemInfo.systemName).toBe('AdminPlus')
      expect(vm.systemInfo.totalMemory).toBe(512)
    })

    it('populates onlineUsers from API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.onlineUsers.length).toBe(1)
      expect(vm.onlineUsers[0].username).toBe('admin')
    })
  })

  // =========================================================================
  // 3. memoryUsagePercent computed
  // =========================================================================
  describe('Memory Usage Percent', () => {
    it('calculates percentage from total and used memory', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      // 256/512 = 50%
      expect(vm.memoryUsagePercent).toBe(50)
    })

    it('returns 0 when totalMemory is 0', async () => {
      vi.mocked(getSystemInfo).mockResolvedValue(
        mockApiResponse(makeSystemInfo({ totalMemory: 0, usedMemory: 0 })) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.memoryUsagePercent).toBe(0)
    })

    it('caps at 100 when used > total', async () => {
      vi.mocked(getSystemInfo).mockResolvedValue(
        mockApiResponse(makeSystemInfo({ totalMemory: 100, usedMemory: 200 })) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.memoryUsagePercent).toBe(100)
    })

    it('returns 0 when systemInfo is null', async () => {
      vi.mocked(getSystemInfo).mockResolvedValue(mockApiResponse(null) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.memoryUsagePercent).toBe(0)
    })
  })

  // =========================================================================
  // 4. formatUptime helper
  // =========================================================================
  describe('Format Uptime', () => {
    it('formats days + hours + minutes', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      // 86400 + 3600 + 61 = 90061s = 1天 1小时 1分钟
      expect(vm.formatUptime(90061)).toBe('1天 1小时 1分钟')
    })

    it('formats hours + minutes when less than a day', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.formatUptime(3661)).toBe('1小时 1分钟')
    })

    it('formats minutes only when less than an hour', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.formatUptime(61)).toBe('1分钟')
    })

    it('returns dash for undefined', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.formatUptime(undefined)).toBe('-')
    })

    it('returns dash for zero', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.formatUptime(0)).toBe('-')
    })
  })

  // =========================================================================
  // 5. System info rendering
  // =========================================================================
  describe('System Info Rendering', () => {
    it('displays system name and version', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('AdminPlus')
      expect(wrapper.text()).toContain('1.0.0')
    })

    it('displays OS name', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('Linux')
    })

    it('displays JDK version', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('21.0.1')
    })

    it('displays database type', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('PostgreSQL')
    })

    it('displays database version', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('16')
    })

    it('displays database connections', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('5')
    })

    it('displays formatted uptime', async () => {
      wrapper = await mountAndFlush()
      // 86400s = 1天
      expect(wrapper.text()).toContain('1天')
    })
  })

  // =========================================================================
  // 6. Online users rendering
  // =========================================================================
  describe('Online Users Rendering', () => {
    it('renders user details', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('admin')
      expect(wrapper.text()).toContain('127.0.0.1')
    })

    it('shows dash for missing fields', async () => {
      vi.mocked(getOnlineUsers).mockResolvedValue(
        mockApiResponse([makeOnlineUser({ os: undefined })]) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('-')
    })

    it('renders multiple users', async () => {
      vi.mocked(getOnlineUsers).mockResolvedValue(
        mockApiResponse([makeOnlineUser({ userId: 'u1' }), makeOnlineUser({ userId: 'u2', username: 'guest' })]) as any
      )
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('admin')
      expect(wrapper.text()).toContain('guest')
    })

    it('shows empty text when no users', async () => {
      vi.mocked(getOnlineUsers).mockResolvedValue(mockApiResponse([]) as any)
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('暂无在线用户')
    })
  })

  // =========================================================================
  // 7. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('shows loading text while loading', async () => {
      vi.mocked(getSystemInfo).mockReturnValue(new Promise(() => {}))
      vi.mocked(getOnlineUsers).mockReturnValue(new Promise(() => {}))

      wrapper = mount(Config, {
        global: { plugins: [pinia] }
      })
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
  // 8. Refresh
  // =========================================================================
  describe('Refresh', () => {
    it('re-fetches data on refresh click', async () => {
      wrapper = await mountAndFlush()

      vi.clearAllMocks()
      vi.mocked(getSystemInfo).mockResolvedValue(mockApiResponse(makeSystemInfo()) as any)
      vi.mocked(getOnlineUsers).mockResolvedValue(mockApiResponse([makeOnlineUser()]) as any)

      const buttons = wrapper.findAll('button')
      const refreshBtn = buttons.find(b => b.text().includes('刷新'))
      if (refreshBtn) {
        await refreshBtn.trigger('click')
        await flushAsync()
        expect(getSystemInfo).toHaveBeenCalled()
        expect(getOnlineUsers).toHaveBeenCalled()
      }
    })
  })

  // =========================================================================
  // 9. Error handling
  // =========================================================================
  describe('Error Handling', () => {
    it('handles API error gracefully', async () => {
      vi.mocked(getSystemInfo).mockRejectedValue(new Error('系统信息获取失败'))
      vi.mocked(getOnlineUsers).mockRejectedValue(new Error('在线用户获取失败'))
      wrapper = await mountAndFlush()
      expect(toast.error).toHaveBeenCalled()
    })
  })
})
