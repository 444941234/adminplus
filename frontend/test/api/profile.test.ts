import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getActivityStats, getUserSettings, updateUserSettings } from '@/api/profile'

// Mock the request module
vi.mock('@/utils/request', () => ({
  get: vi.fn(),
  put: vi.fn()
}))

import { get, put } from '@/utils/request'

describe('Profile API - Activity & Settings', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getActivityStats', () => {
    it('should fetch activity stats successfully', async () => {
      const mockStats = {
        daysActive: 127,
        totalActions: 2341,
        lastLogin: '2025-03-20T09:42:00',
        lastLoginIp: '192.168.1.100',
        recentActivity: [
          { id: '1', action: 'Updated profile', timestamp: '2025-03-20T10:23:00', type: 'update' }
        ]
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockStats })

      const result = await getActivityStats()

      expect(get).toHaveBeenCalledWith('/profile/activity')
      expect(result.data).toEqual(mockStats)
    })
  })

  describe('getUserSettings', () => {
    it('should fetch user settings', async () => {
      const mockSettings = { notifications: true, darkMode: false, emailUpdates: true, language: 'zh-CN' }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockSettings })

      const result = await getUserSettings()

      expect(get).toHaveBeenCalledWith('/profile/settings')
      expect(result.data).toEqual(mockSettings)
    })
  })

  describe('updateUserSettings', () => {
    it('should update user settings', async () => {
      const updates = { darkMode: true }
      const mockSettings = { notifications: true, darkMode: true, emailUpdates: true, language: 'zh-CN' }
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: mockSettings })

      const result = await updateUserSettings(updates)

      expect(put).toHaveBeenCalledWith('/profile/settings', updates)
      expect(result.data.darkMode).toBe(true)
    })
  })
})
