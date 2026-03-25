import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

// Mock the API module
vi.mock('@/api', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  getCurrentUser: vi.fn(),
  getPermissions: vi.fn(),
  getCaptcha: vi.fn(),
  getUserMenuTree: vi.fn()
}))

// Mock the permissions library
vi.mock('@/lib/permissions', () => ({
  hasPermission: vi.fn((permissions: string[], permission: string) => permissions.includes(permission))
}))

import { login, logout, getCurrentUser, getPermissions, getCaptcha, getUserMenuTree } from '@/api'
import type { CaptchaResp } from '@/api/auth'

describe('User Store', () => {
  let store: ReturnType<typeof useUserStore>

  beforeEach(() => {
    // Clear localStorage
    localStorage.clear()
    // Create fresh pinia instance
    setActivePinia(createPinia())
    // Clear all mocks
    vi.clearAllMocks()
    // Create store instance
    store = useUserStore()
  })

  afterEach(() => {
    localStorage.clear()
  })

  describe('Initial State', () => {
    it('should have empty initial state', () => {
      expect(store.token).toBeNull()
      expect(store.refreshToken).toBeNull()
      expect(store.userInfo).toBeNull()
      expect(store.permissions).toEqual([])
      expect(store.menus).toEqual([])
      expect(store.captcha).toBeNull()
    })

    it('should restore token from localStorage on initialization', () => {
      localStorage.setItem('token', 'saved-token')
      localStorage.setItem('refreshToken', 'saved-refresh-token')

      setActivePinia(createPinia())
      const newStore = useUserStore()

      expect(newStore.token).toBe('saved-token')
      expect(newStore.refreshToken).toBe('saved-refresh-token')
    })
  })

  describe('Computed Properties', () => {
    it('isLoggedIn should return false when token is null', () => {
      expect(store.isLoggedIn).toBe(false)
    })

    it('isLoggedIn should return true when token exists', () => {
      localStorage.setItem('token', 'test-token')
      setActivePinia(createPinia())
      const storeWithToken = useUserStore()

      expect(storeWithToken.isLoggedIn).toBe(true)
    })

    it('username should return empty string when userInfo is null', () => {
      expect(store.username).toBe('')
    })

    it('username should return username from userInfo', () => {
      store.userInfo = { username: 'testuser' } as any
      expect(store.username).toBe('testuser')
    })

    it('nickname should return empty string when userInfo is null', () => {
      expect(store.nickname).toBe('')
    })

    it('nickname should return nickname from userInfo', () => {
      store.userInfo = { nickname: 'Test User' } as any
      expect(store.nickname).toBe('Test User')
    })

    it('avatar should return empty string when userInfo is null', () => {
      expect(store.avatar).toBe('')
    })

    it('avatar should return avatar from userInfo', () => {
      store.userInfo = { avatar: 'avatar.jpg' } as any
      expect(store.avatar).toBe('avatar.jpg')
    })
  })

  describe('login action', () => {
    it('should login successfully with valid credentials', async () => {
      const mockLoginResp = {
        token: 'jwt-token',
        refreshToken: 'refresh-token',
        user: { id: '1', username: 'admin', nickname: 'Administrator' },
        permissions: ['user:add', 'user:edit']
      }
      vi.mocked(login).mockResolvedValue({ code: 200, message: 'success', data: mockLoginResp } as any)

      const result = await store.login('admin', 'password123', 'captcha-id', '1234')

      expect(login).toHaveBeenCalledWith('admin', 'password123', 'captcha-id', '1234')
      expect(store.token).toBe('jwt-token')
      expect(store.refreshToken).toBe('refresh-token')
      expect(store.userInfo).toEqual(mockLoginResp.user)
      expect(store.permissions).toEqual(mockLoginResp.permissions)
      expect(localStorage.getItem('token')).toBe('jwt-token')
      expect(localStorage.getItem('refreshToken')).toBe('refresh-token')
    })

    it('should clear captcha on successful login', async () => {
      store.captcha = { captchaId: 'test', captchaImage: 'data:image' } as any
      const mockLoginResp = {
        token: 'jwt-token',
        user: { id: '1', username: 'admin' }
      }
      vi.mocked(login).mockResolvedValue({ code: 200, message: 'success', data: mockLoginResp } as any)

      await store.login('admin', 'password123', 'captcha-id', '1234')

      expect(store.captcha).toBeNull()
    })

    it('should throw error when login fails', async () => {
      vi.mocked(login).mockResolvedValue({ code: 401, message: '用户名或密码错误', data: null } as any)

      await expect(store.login('admin', 'wrong', 'captcha-id', '1234')).rejects.toThrow('用户名或密码错误')
    })

    it('should throw error when response is missing token', async () => {
      vi.mocked(login).mockResolvedValue({ code: 200, message: 'success', data: { user: {} } } as any)

      await expect(store.login('admin', 'password', 'captcha-id', '1234')).rejects.toThrow('登录响应缺少token')
    })
  })

  describe('logout action', () => {
    it('should logout and clear all state', async () => {
      localStorage.setItem('token', 'test-token')
      localStorage.setItem('refreshToken', 'test-refresh-token')
      setActivePinia(createPinia())
      const storeWithData = useUserStore()
      storeWithData.token = 'test-token'
      storeWithData.refreshToken = 'test-refresh-token'
      storeWithData.userInfo = { id: '1' } as any
      storeWithData.permissions = ['user:add']
      storeWithData.menus = [{ id: '1' } as any]

      vi.mocked(logout).mockResolvedValue({ code: 200, message: 'success', data: undefined } as any)
      await storeWithData.logout()

      expect(storeWithData.token).toBeNull()
      expect(storeWithData.refreshToken).toBeNull()
      expect(storeWithData.userInfo).toBeNull()
      expect(storeWithData.permissions).toEqual([])
      expect(storeWithData.menus).toEqual([])
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('refreshToken')).toBeNull()
    })

    it('should handle logout API error gracefully', async () => {
      localStorage.setItem('token', 'test-token')
      setActivePinia(createPinia())
      const storeWithData = useUserStore()
      storeWithData.token = 'test-token'

      vi.mocked(logout).mockRejectedValue(new Error('Network error'))
      await storeWithData.logout()

      // Should still clear state even if API call fails
      expect(storeWithData.token).toBeNull()
      expect(localStorage.getItem('token')).toBeNull()
    })
  })

  describe('fetchUserInfo action', () => {
    it('should fetch user info, permissions and menus', async () => {
      store.token = 'test-token'
      const mockUser = { id: '1', username: 'admin', nickname: 'Administrator' }
      const mockPermissions = ['user:add', 'user:edit']
      const mockMenus = [{ id: '1', menuName: '系统管理' }]

      vi.mocked(getCurrentUser).mockResolvedValue({ code: 200, message: 'success', data: mockUser } as any)
      vi.mocked(getPermissions).mockResolvedValue({ code: 200, message: 'success', data: mockPermissions } as any)
      vi.mocked(getUserMenuTree).mockResolvedValue({ code: 200, message: 'success', data: mockMenus } as any)

      await store.fetchUserInfo()

      expect(store.userInfo).toEqual(mockUser)
      expect(store.permissions).toEqual(mockPermissions)
      expect(store.menus).toEqual(mockMenus)
    })

    it('should restore token from localStorage if missing', async () => {
      localStorage.setItem('token', 'saved-token')
      setActivePinia(createPinia())
      const newStore = useUserStore()

      const mockUser = { id: '1', username: 'admin' }
      vi.mocked(getCurrentUser).mockResolvedValue({ code: 200, message: 'success', data: mockUser } as any)
      vi.mocked(getPermissions).mockResolvedValue({ code: 200, message: 'success', data: [] } as any)
      vi.mocked(getUserMenuTree).mockResolvedValue({ code: 200, message: 'success', data: [] } as any)

      await newStore.fetchUserInfo()

      expect(newStore.token).toBe('saved-token')
    })

    it('should return early if no token available', async () => {
      await store.fetchUserInfo()

      expect(getCurrentUser).not.toHaveBeenCalled()
    })

    it('should handle API errors', async () => {
      store.token = 'test-token'
      vi.mocked(getCurrentUser).mockRejectedValue(new Error('Unauthorized'))

      await expect(store.fetchUserInfo()).rejects.toThrow('Unauthorized')
    })
  })

  describe('fetchCaptcha action', () => {
    it('should fetch captcha successfully', async () => {
      const mockCaptcha: CaptchaResp = {
        captchaId: 'captcha-123',
        captchaImage: 'base64-image-data'
      }
      vi.mocked(getCaptcha).mockResolvedValue({ code: 200, message: 'success', data: mockCaptcha } as any)

      const result = await store.fetchCaptcha()

      expect(getCaptcha).toHaveBeenCalled()
      expect(store.captcha).toEqual(mockCaptcha)
      expect(result).toEqual(mockCaptcha)
    })
  })

  describe('hasPermission action', () => {
    it('should return true when user has permission', () => {
      store.permissions = ['user:add', 'user:edit', 'role:view']

      expect(store.hasPermission('user:add')).toBe(true)
    })

    it('should return false when user does not have permission', () => {
      store.permissions = ['user:add', 'user:edit']

      expect(store.hasPermission('user:delete')).toBe(false)
    })

    it('should return false when permissions array is empty', () => {
      store.permissions = []

      expect(store.hasPermission('user:add')).toBe(false)
    })
  })

  describe('hasRole action', () => {
    it('should return true when user has role', () => {
      store.userInfo = { roles: ['Admin', 'User'] } as any

      expect(store.hasRole('Admin')).toBe(true)
    })

    it('should return false when user does not have role', () => {
      store.userInfo = { roles: ['User'] } as any

      expect(store.hasRole('Admin')).toBe(false)
    })

    it('should return false when userInfo is null', () => {
      expect(store.hasRole('Admin')).toBe(false)
    })

    it('should return false when user has no roles', () => {
      store.userInfo = { roles: [] } as any

      expect(store.hasRole('Admin')).toBe(false)
    })
  })

  describe('restoreToken action', () => {
    it('should restore token from localStorage', () => {
      localStorage.setItem('token', 'saved-token')
      localStorage.setItem('refreshToken', 'saved-refresh-token')

      const result = store.restoreToken()

      expect(store.token).toBe('saved-token')
      expect(store.refreshToken).toBe('saved-refresh-token')
      expect(result).toBe(true)
    })

    it('should not overwrite existing token', () => {
      store.token = 'existing-token'
      localStorage.setItem('token', 'saved-token')

      store.restoreToken()

      expect(store.token).toBe('existing-token')
    })

    it('should return false when no token in localStorage', () => {
      const result = store.restoreToken()

      expect(result).toBe(false)
    })

    it('should restore only refreshToken if token already exists', () => {
      store.token = 'existing-token'
      localStorage.setItem('token', 'saved-token')
      localStorage.setItem('refreshToken', 'saved-refresh-token')

      store.restoreToken()

      expect(store.token).toBe('existing-token')
      expect(store.refreshToken).toBe('saved-refresh-token')
    })
  })
})
