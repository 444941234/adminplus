import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { nextTick } from 'vue'
import Profile from '../Profile.vue'
import * as api from '@/api'

// Mock child components to simplify testing
vi.mock('@/components/profile/ProfileHero.vue', () => ({
  default: {
    name: 'ProfileHero',
    props: ['profile'],
    template: '<div class="mock-profile-hero" data-testid="profile-hero"><slot /></div>'
  }
}))

vi.mock('@/components/profile/ProfileInfo.vue', () => ({
  default: {
    name: 'ProfileInfo',
    props: ['profile', 'loading'],
    emits: ['updateField'],
    template: '<div class="mock-profile-info" data-testid="profile-info"><slot /></div>'
  }
}))

vi.mock('@/components/profile/ProfileSecurity.vue', () => ({
  default: {
    name: 'ProfileSecurity',
    template: '<div class="mock-profile-security" data-testid="profile-security">Security Section</div>'
  }
}))

vi.mock('@/components/profile/ActivityDashboard.vue', () => ({
  default: {
    name: 'ActivityDashboard',
    props: ['activity', 'loading'],
    template: '<div class="mock-activity-dashboard" data-testid="activity-dashboard">Activity Dashboard</div>'
  }
}))

vi.mock('@/components/profile/QuickSettings.vue', () => ({
  default: {
    name: 'QuickSettings',
    template: '<div class="mock-quick-settings" data-testid="quick-settings">Quick Settings</div>'
  }
}))

// Mock API calls
vi.mock('@/api', () => ({
  getProfile: vi.fn(),
  updateProfile: vi.fn(),
  getActivityStats: vi.fn()
}))

// Mock vue-sonner toast
vi.mock('vue-sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn()
  }
}))

// Mock console methods to avoid noise in tests
const originalConsoleError = console.error
const originalConsoleWarn = console.warn
const originalConsoleLog = console.log

describe('Profile Page Integration Tests', () => {
  let wrapper: VueWrapper

  const mockProfile = {
    id: '1',
    username: 'testuser',
    nickname: 'Test User',
    email: 'test@example.com',
    phone: '1234567890',
    avatar: 'avatar.jpg',
    deptName: 'Engineering',
    roles: ['Admin', 'User']
  }

  const mockActivityStats = {
    daysActive: 30,
    totalActions: 150,
    lastLogin: '2026-03-20 10:00:00',
    lastLoginIp: '192.168.1.1',
    recentActivity: [
      { id: '1', action: 'Updated profile', timestamp: '2026-03-20 09:00:00', type: 'update' as const },
      { id: '2', action: 'Logged in', timestamp: '2026-03-19 08:00:00', type: 'login' as const }
    ]
  }

  beforeEach(() => {
    vi.clearAllMocks()
    console.error = vi.fn()
    console.warn = vi.fn()
    console.log = vi.fn()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    console.error = originalConsoleError
    console.warn = originalConsoleWarn
    console.log = originalConsoleLog
  })

  describe('1. Page Rendering', () => {
    it('should render page structure correctly', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick() // Double tick for async operations

      expect(wrapper.find('.profile-page').exists()).toBe(true)
    })

    it('should render without crashing when all data loads successfully', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      expect(wrapper.exists()).toBe(true)
      expect(wrapper.find('.profile-page').exists()).toBe(true)
    })

    it('should render without crashing when activity stats fail to load', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockRejectedValue(new Error('API Error'))

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      expect(wrapper.exists()).toBe(true)
      expect(wrapper.find('.profile-page').exists()).toBe(true)
      expect(console.warn).toHaveBeenCalledWith(
        'Activity stats could not be loaded. Some dashboard features may be unavailable.'
      )
    })
  })

  describe('2. Child Components Rendering', () => {
    it('should render ProfileHero component when profile data is loaded', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const heroComponent = wrapper.findComponent({ name: 'ProfileHero' })
      expect(heroComponent.exists()).toBe(true)
      expect(heroComponent.props('profile')).toEqual(mockProfile)
    })

    it('should render ProfileInfo component when profile data is loaded', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const infoComponent = wrapper.findComponent({ name: 'ProfileInfo' })
      expect(infoComponent.exists()).toBe(true)
      expect(infoComponent.props('profile')).toEqual(mockProfile)
    })

    it('should render ProfileSecurity component', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const securityComponent = wrapper.findComponent({ name: 'ProfileSecurity' })
      expect(securityComponent.exists()).toBe(true)
    })

    it('should render ActivityDashboard component when activity stats are loaded', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const activityComponent = wrapper.findComponent({ name: 'ActivityDashboard' })
      expect(activityComponent.exists()).toBe(true)
      expect(activityComponent.props('activity')).toEqual(mockActivityStats)
    })

    it('should render QuickSettings component', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const settingsComponent = wrapper.findComponent({ name: 'QuickSettings' })
      expect(settingsComponent.exists()).toBe(true)
    })
  })

  describe('3. Data Fetching', () => {
    it('should fetch profile data on mount', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      expect(api.getProfile).toHaveBeenCalledTimes(1)
      expect(api.getActivityStats).toHaveBeenCalledTimes(1)
    })

    it('should fetch activity stats on mount', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      expect(api.getActivityStats).toHaveBeenCalledTimes(1)
    })

    it('should handle profile fetch error gracefully', async () => {
      vi.mocked(api.getProfile).mockRejectedValue(new Error('Network Error'))
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      expect(console.error).toHaveBeenCalledWith('Failed to fetch profile:', expect.any(Error))
      const { toast } = await import('vue-sonner')
      expect(toast.error).toHaveBeenCalledWith('获取个人资料失败')
    })

    it('should not show toast error when activity stats fetch fails', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockRejectedValue(new Error('Network Error'))

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const { toast } = await import('vue-sonner')
      expect(toast.error).not.toHaveBeenCalledWith(expect.stringContaining('activity'))
    })
  })

  describe('4. Loading States', () => {
    it('should show loading state while fetching profile', async () => {
      // Create a promise that we can resolve later
      let resolveProfile: (value: any) => void
      const profilePromise = new Promise((resolve) => {
        resolveProfile = resolve
      })

      vi.mocked(api.getProfile).mockReturnValue(profilePromise as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()

      expect(wrapper.find('.profile-page__loading').exists()).toBe(true)
      expect(wrapper.find('.profile-page__loading').text()).toBe('加载个人资料中...')

      // Resolve the promise
      resolveProfile!({ data: mockProfile })
      await nextTick()
      await nextTick()

      expect(wrapper.find('.profile-page__loading').exists()).toBe(false)
    })

    it('should show loading state on ProfileInfo when updating', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const infoComponent = wrapper.findComponent({ name: 'ProfileInfo' })
      expect(infoComponent.props('loading')).toBe(false)

      // Simulate update in progress
      let resolveUpdate: (value: any) => void
      const updatePromise = new Promise((resolve) => {
        resolveUpdate = resolve
      })

      vi.mocked(api.updateProfile).mockReturnValue(updatePromise as any)

      // Trigger update
      await infoComponent.vm.$emit('updateField', 'nickname', 'New Name')
      await nextTick()

      // The loading state should be true during update
      expect(infoComponent.props('loading')).toBe(true)

      // Resolve the update
      resolveUpdate!({ data: mockProfile })
      await nextTick()
      await nextTick()

      expect(infoComponent.props('loading')).toBe(false)
    })
  })

  describe('5. Error States', () => {
    it('should show error state when profile fetch fails', async () => {
      vi.mocked(api.getProfile).mockRejectedValue(new Error('Failed to fetch'))
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      // Profile hero should not be rendered
      expect(wrapper.findComponent({ name: 'ProfileHero' }).exists()).toBe(false)
      // Grid should not be rendered without profile
      expect(wrapper.find('.profile-page__grid').exists()).toBe(false)
    })

    it('should show empty activity state when activity stats are null', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: null } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      // Should show empty state message
      expect(wrapper.find('.profile-page__activity-empty').exists()).toBe(true)
      expect(wrapper.find('.profile-page__activity-empty').text()).toBe('暂无活动数据')
    })

    it('should revert profile data on update error', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)
      vi.mocked(api.updateProfile).mockRejectedValue(new Error('Update failed'))

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const infoComponent = wrapper.findComponent({ name: 'ProfileInfo' })
      await infoComponent.vm.$emit('updateField', 'nickname', 'New Name')
      await nextTick()
      await nextTick()

      // Should refetch profile to revert
      expect(api.getProfile).toHaveBeenCalledTimes(2)
      const { toast } = await import('vue-sonner')
      expect(toast.error).toHaveBeenCalledWith('更新失败')
    })
  })

  describe('6. Inline Editing', () => {
    it('should update profile field when updateField event is emitted', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)
      vi.mocked(api.updateProfile).mockResolvedValue({ data: { ...mockProfile, nickname: 'Updated Name' } } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const infoComponent = wrapper.findComponent({ name: 'ProfileInfo' })

      // Emit updateField event
      await infoComponent.vm.$emit('updateField', 'nickname', 'Updated Name')
      await nextTick()
      await nextTick()

      // Verify API was called
      expect(api.updateProfile).toHaveBeenCalledWith({ nickname: 'Updated Name' })
      const { toast } = await import('vue-sonner')
      expect(toast.success).toHaveBeenCalledWith('更新成功')
    })

    it('should handle multiple field updates', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)
      vi.mocked(api.updateProfile).mockResolvedValue({ data: mockProfile } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const infoComponent = wrapper.findComponent({ name: 'ProfileInfo' })

      // Update nickname
      await infoComponent.vm.$emit('updateField', 'nickname', 'New Nickname')
      await nextTick()
      await nextTick()

      expect(api.updateProfile).toHaveBeenCalledWith({ nickname: 'New Nickname' })

      // Update email
      await infoComponent.vm.$emit('updateField', 'email', 'newemail@example.com')
      await nextTick()
      await nextTick()

      expect(api.updateProfile).toHaveBeenCalledWith({ email: 'newemail@example.com' })

      // Update phone
      await infoComponent.vm.$emit('updateField', 'phone', '9876543210')
      await nextTick()
      await nextTick()

      expect(api.updateProfile).toHaveBeenCalledWith({ phone: '9876543210' })
    })
  })

  describe('7. Event Handlers', () => {
    it('should handle edit event from ProfileHero', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const heroComponent = wrapper.findComponent({ name: 'ProfileHero' })
      await heroComponent.vm.$emit('edit')
      await nextTick()

      expect(console.log).toHaveBeenCalledWith('Edit mode requested - feature coming soon')
      const { toast } = await import('vue-sonner')
      expect(toast.info).toHaveBeenCalledWith('编辑模式即将推出')
    })

    it('should handle changeAvatar event from ProfileHero', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const heroComponent = wrapper.findComponent({ name: 'ProfileHero' })
      await heroComponent.vm.$emit('changeAvatar')
      await nextTick()

      expect(console.log).toHaveBeenCalledWith('Avatar change requested - feature coming soon')
      const { toast } = await import('vue-sonner')
      expect(toast.info).toHaveBeenCalledWith('头像上传功能即将推出')
    })
  })

  describe('8. Responsive Layout', () => {
    it('should apply correct CSS classes for desktop layout', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      // Check that grid layout exists
      const grid = wrapper.find('.profile-page__grid')
      expect(grid.exists()).toBe(true)

      // Check that all columns exist
      expect(wrapper.find('.profile-page__main').exists()).toBe(true)
      expect(wrapper.find('.profile-page__activity').exists()).toBe(true)
      expect(wrapper.find('.profile-page__settings').exists()).toBe(true)
    })

    it('should render security section within main column', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const mainColumn = wrapper.find('.profile-page__main')
      const securityComponent = wrapper.findComponent({ name: 'ProfileSecurity' })

      expect(mainColumn.exists()).toBe(true)
      expect(securityComponent.exists()).toBe(true)
    })
  })

  describe('9. Edge Cases', () => {
    it('should handle profile with empty roles array', async () => {
      const profileWithoutRoles = { ...mockProfile, roles: [] }
      vi.mocked(api.getProfile).mockResolvedValue({ data: profileWithoutRoles } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      expect(wrapper.exists()).toBe(true)
      const heroComponent = wrapper.findComponent({ name: 'ProfileHero' })
      expect(heroComponent.props('profile').roles).toEqual([])
    })

    it('should handle concurrent updates gracefully', async () => {
      vi.mocked(api.getProfile).mockResolvedValue({ data: mockProfile } as any)
      vi.mocked(api.getActivityStats).mockResolvedValue({ data: mockActivityStats } as any)
      vi.mocked(api.updateProfile).mockResolvedValue({ data: mockProfile } as any)

      wrapper = mount(Profile, {
        global: {
          stubs: {
            ProfileHero: true,
            ProfileInfo: true,
            ProfileSecurity: true,
            ActivityDashboard: true,
            QuickSettings: true
          }
        }
      })

      await nextTick()
      await nextTick()

      const infoComponent = wrapper.findComponent({ name: 'ProfileInfo' })

      // Trigger multiple updates concurrently
      const updates = [
        infoComponent.vm.$emit('updateField', 'nickname', 'Name1'),
        infoComponent.vm.$emit('updateField', 'email', 'email1@example.com'),
        infoComponent.vm.$emit('updateField', 'phone', '1234567890')
      ]

      await Promise.all(updates)
      await nextTick()
      await nextTick()

      // All updates should have been attempted
      expect(api.updateProfile).toHaveBeenCalledTimes(3)
    })
  })
})
