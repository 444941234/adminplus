import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'

// Mock the API module
vi.mock('@/api', () => ({
  getStats: vi.fn(),
  getRecentLogs: vi.fn()
}))

// Mock the notification API module (used by NotificationCenter)
vi.mock('@/api/notification', () => ({
  getMyNotifications: vi.fn().mockResolvedValue({ code: 200, message: 'success', data: { content: [], totalElements: 0 } }),
  getUnreadCount: vi.fn().mockResolvedValue({ code: 200, message: 'success', data: 0 }),
  markAsRead: vi.fn(),
  markAllAsRead: vi.fn(),
  deleteNotification: vi.fn()
}))

// Mock the permissions library
vi.mock('@/lib/page-permissions', () => ({
  getDashboardQuickActions: vi.fn(() => [])
}))

// Mock vue-sonner
vi.mock('vue-sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn()
  }
}))

import Dashboard from '@/views/Dashboard.vue'
import { useUserStore } from '@/stores/user'
import { getStats, getRecentLogs } from '@/api'

describe('Dashboard Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper
  let userStore: ReturnType<typeof useUserStore>

  beforeEach(() => {
    // Create pinia instance
    pinia = createPinia()
    setActivePinia(pinia)

    // Get user store
    userStore = useUserStore()

    // Clear all mocks
    vi.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Page Structure', () => {
    it('should render dashboard page structure', async () => {
      const mockStats = { userCount: 150, roleCount: 10, menuCount: 50, logCount: 234 }
      const mockLogs = []

      vi.mocked(getStats).mockResolvedValue({ code: 200, message: 'success', data: mockStats } as any)
      vi.mocked(getRecentLogs).mockResolvedValue({ code: 200, message: 'success', data: mockLogs } as any)

      wrapper = mount(Dashboard, {
        global: {
          plugins: [pinia]
        }
      })

      await nextTick()
      await nextTick()

      expect(wrapper.find('.space-y-6').exists()).toBe(true)
    })
  })

  describe('Data Fetching', () => {
    it('should fetch dashboard data on mount', async () => {
      const mockStats = { userCount: 150, roleCount: 10, menuCount: 50, logCount: 234 }
      const mockLogs = [
        { id: '1', username: 'admin', description: '新增用户', createTime: '2026-03-20 10:00:00' }
      ]

      vi.mocked(getStats).mockResolvedValue({ code: 200, message: 'success', data: mockStats } as any)
      vi.mocked(getRecentLogs).mockResolvedValue({ code: 200, message: 'success', data: mockLogs } as any)

      wrapper = mount(Dashboard, {
        global: {
          plugins: [pinia]
        }
      })

      await nextTick()
      await nextTick()

      expect(getStats).toHaveBeenCalled()
      expect(getRecentLogs).toHaveBeenCalled()
      expect(wrapper.vm.stats).toEqual(mockStats)
      expect(wrapper.vm.recentLogs).toEqual(mockLogs)
    })

    it('should handle API errors gracefully', async () => {
      vi.mocked(getStats).mockRejectedValue(new Error('Network error'))
      vi.mocked(getRecentLogs).mockRejectedValue(new Error('Network error'))

      wrapper = mount(Dashboard, {
        global: {
          plugins: [pinia]
        }
      })

      await nextTick()
      await nextTick()

      const { toast } = await import('vue-sonner')
      expect(toast.error).toHaveBeenCalledWith('Network error')
      expect(wrapper.vm.loading).toBe(false)
    })
  })

  describe('Loading State', () => {
    it('should set loading to false after data fetch', async () => {
      const mockStats = { userCount: 150, roleCount: 10, menuCount: 50, logCount: 234 }
      const mockLogs = []

      vi.mocked(getStats).mockResolvedValue({ code: 200, message: 'success', data: mockStats } as any)
      vi.mocked(getRecentLogs).mockResolvedValue({ code: 200, message: 'success', data: mockLogs } as any)

      wrapper = mount(Dashboard, {
        global: {
          plugins: [pinia]
        }
      })

      expect(wrapper.vm.loading).toBe(true)

      // useAsyncAction adds extra async layers; flush all microtasks
      await new Promise(resolve => setTimeout(resolve, 0))
      await nextTick()
      await nextTick()
      await nextTick()

      expect(wrapper.vm.loading).toBe(false)
    })
  })

  describe('Empty States', () => {
    it('should handle empty logs array', async () => {
      const mockStats = { userCount: 150, roleCount: 10, menuCount: 50, logCount: 0 }
      const mockLogs = []

      vi.mocked(getStats).mockResolvedValue({ code: 200, message: 'success', data: mockStats } as any)
      vi.mocked(getRecentLogs).mockResolvedValue({ code: 200, message: 'success', data: mockLogs } as any)

      wrapper = mount(Dashboard, {
        global: {
          plugins: [pinia]
        }
      })

      await nextTick()
      await nextTick()

      expect(wrapper.vm.recentLogs).toEqual([])
      expect(wrapper.vm.recentLogs.length).toBe(0)
    })
  })

  describe('Quick Actions', () => {
    it('should compute quick actions', async () => {
      const mockStats = { userCount: 150, roleCount: 10, menuCount: 50, logCount: 234 }
      const mockLogs = []

      userStore.hasPermission = vi.fn(() => true)

      vi.mocked(getStats).mockResolvedValue({ code: 200, message: 'success', data: mockStats } as any)
      vi.mocked(getRecentLogs).mockResolvedValue({ code: 200, message: 'success', data: mockLogs } as any)

      wrapper = mount(Dashboard, {
        global: {
          plugins: [pinia]
        }
      })

      await nextTick()
      await nextTick()

      expect(wrapper.vm.quickActions).toBeDefined()
    })
  })
})
