import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getStats,
  getUserGrowth,
  getRoleDistribution,
  getMenuDistribution,
  getRecentLogs,
  getVisitTrend,
  getOnlineUsers,
  getSystemInfo,
  getStatistics
} from '@/api/dashboard'

// Mock the request module
vi.mock('@/api/request', () => ({
  get: vi.fn()
}))

import { get } from '@/api/request'

describe('Dashboard API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getStats', () => {
    it('should fetch dashboard stats', async () => {
      const mockStats = {
        totalUsers: 150,
        totalRoles: 10,
        totalMenus: 50,
        totalDepts: 8,
        todayVisits: 234,
        onlineUsers: 12
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockStats })

      const result = await getStats()

      expect(get).toHaveBeenCalledWith('/sys/dashboard/stats')
      expect(result.data).toEqual(mockStats)
    })
  })

  describe('getUserGrowth', () => {
    it('should fetch user growth data', async () => {
      const mockChartData = {
        labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
        datasets: [
          {
            label: '用户增长',
            data: [10, 25, 45, 70, 95, 150]
          }
        ]
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockChartData })

      const result = await getUserGrowth()

      expect(get).toHaveBeenCalledWith('/sys/dashboard/user-growth')
      expect(result.data).toEqual(mockChartData)
    })
  })

  describe('getRoleDistribution', () => {
    it('should fetch role distribution data', async () => {
      const mockChartData = {
        labels: ['管理员', '普通用户', '访客'],
        datasets: [
          {
            label: '角色分布',
            data: [5, 120, 25]
          }
        ]
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockChartData })

      const result = await getRoleDistribution()

      expect(get).toHaveBeenCalledWith('/sys/dashboard/role-distribution')
      expect(result.data).toEqual(mockChartData)
    })
  })

  describe('getMenuDistribution', () => {
    it('should fetch menu distribution data', async () => {
      const mockChartData = {
        labels: ['系统管理', '用户管理', '角色管理', '菜单管理'],
        datasets: [
          {
            label: '菜单访问量',
            data: [450, 320, 280, 190]
          }
        ]
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockChartData })

      const result = await getMenuDistribution()

      expect(get).toHaveBeenCalledWith('/sys/dashboard/menu-distribution')
      expect(result.data).toEqual(mockChartData)
    })
  })

  describe('getRecentLogs', () => {
    it('should fetch recent operation logs', async () => {
      const mockLogs = [
        { id: '1', username: 'admin', module: '用户管理', operation: '新增用户', time: '2026-03-20 10:00:00' },
        { id: '2', username: 'admin', module: '角色管理', operation: '修改角色', time: '2026-03-20 09:55:00' }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockLogs })

      const result = await getRecentLogs()

      expect(get).toHaveBeenCalledWith('/sys/dashboard/recent-logs')
      expect(result.data).toEqual(mockLogs)
    })
  })

  describe('getVisitTrend', () => {
    it('should fetch visit trend data', async () => {
      const mockChartData = {
        labels: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
        datasets: [
          {
            label: '访问量',
            data: [12, 8, 45, 89, 67, 34]
          }
        ]
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockChartData })

      const result = await getVisitTrend()

      expect(get).toHaveBeenCalledWith('/sys/dashboard/visit-trend')
      expect(result.data).toEqual(mockChartData)
    })
  })

  describe('getOnlineUsers', () => {
    it('should fetch online users', async () => {
      const mockOnlineUsers = [
        { id: '1', username: 'admin', nickname: '管理员', loginTime: '2026-03-20 08:00:00', ip: '192.168.1.1' },
        { id: '2', username: 'user1', nickname: '用户1', loginTime: '2026-03-20 09:00:00', ip: '192.168.1.2' }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockOnlineUsers })

      const result = await getOnlineUsers()

      expect(get).toHaveBeenCalledWith('/sys/dashboard/online-users')
      expect(result.data).toEqual(mockOnlineUsers)
    })
  })

  describe('getSystemInfo', () => {
    it('should fetch system information', async () => {
      const mockSystemInfo = {
        os: 'Windows 11',
        javaVersion: 'JDK 21',
        springBootVersion: '3.5.0',
        cpuCores: 8,
        totalMemory: 16384,
        usedMemory: 8192,
        freeMemory: 8192
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockSystemInfo })

      const result = await getSystemInfo()

      expect(get).toHaveBeenCalledWith('/sys/dashboard/system-info')
      expect(result.data).toEqual(mockSystemInfo)
    })
  })

  describe('getStatistics', () => {
    it('should fetch statistics page data', async () => {
      const mockStatistics = {
        userStats: { total: 150, active: 140, inactive: 10 },
        roleStats: { total: 10, enabled: 8, disabled: 2 },
        menuStats: { total: 50, enabled: 45, disabled: 5 },
        deptStats: { total: 8, enabled: 8, disabled: 0 }
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockStatistics })

      const result = await getStatistics()

      expect(get).toHaveBeenCalledWith('/sys/dashboard/statistics')
      expect(result.data).toEqual(mockStatistics)
    })
  })
})
