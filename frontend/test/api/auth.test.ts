import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  login,
  getCurrentUser,
  getPermissions,
  logout,
  refreshToken,
  getCaptcha
} from '@/api/auth'

// Mock the request module
vi.mock('@/utils/request', () => ({
  get: vi.fn(),
  post: vi.fn()
}))

import { get, post } from '@/utils/request'

describe('Auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('login', () => {
    it('should login successfully with valid credentials', async () => {
      const mockLoginResp = {
        token: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token',
        user: {
          id: '1',
          username: 'admin',
          nickname: 'Administrator',
          email: 'admin@example.com'
        }
      }
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockLoginResp })

      const result = await login('admin', 'password123', 'captcha-id-123', '1234')

      expect(post).toHaveBeenCalledWith('/auth/login', {
        username: 'admin',
        password: 'password123',
        captchaId: 'captcha-id-123',
        captchaCode: '1234'
      })
      expect(result.data).toEqual(mockLoginResp)
    })

    it('should handle login failure', async () => {
      vi.mocked(post).mockRejectedValue(new Error('用户名或密码错误'))

      await expect(login('admin', 'wrong-password', 'captcha-id', '1234')).rejects.toThrow(
        '用户名或密码错误'
      )
    })
  })

  describe('getCurrentUser', () => {
    it('should fetch current user info', async () => {
      const mockUser = {
        id: '1',
        username: 'admin',
        nickname: 'Administrator',
        email: 'admin@example.com',
        avatar: 'avatar.jpg',
        deptName: 'IT Department',
        roles: ['Admin']
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockUser })

      const result = await getCurrentUser()

      expect(get).toHaveBeenCalledWith('/auth/me')
      expect(result.data).toEqual(mockUser)
    })
  })

  describe('getPermissions', () => {
    it('should fetch user permissions', async () => {
      const mockPermissions = ['user:add', 'user:edit', 'user:delete', 'role:list']
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockPermissions })

      const result = await getPermissions()

      expect(get).toHaveBeenCalledWith('/auth/permissions')
      expect(result.data).toEqual(mockPermissions)
    })
  })

  describe('logout', () => {
    it('should logout successfully', async () => {
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await logout()

      expect(post).toHaveBeenCalledWith('/auth/logout')
      expect(result.code).toBe(200)
    })
  })

  describe('refreshToken', () => {
    it('should refresh token successfully', async () => {
      const newToken = 'new-jwt-token'
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: newToken })

      const result = await refreshToken('old-refresh-token')

      expect(post).toHaveBeenCalledWith('/auth/refresh', { refreshToken: 'old-refresh-token' })
      expect(result.data).toBe(newToken)
    })

    it('should handle invalid refresh token', async () => {
      vi.mocked(post).mockRejectedValue(new Error('刷新令牌无效'))

      await expect(refreshToken('invalid-token')).rejects.toThrow('刷新令牌无效')
    })
  })

  describe('getCaptcha', () => {
    it('should fetch captcha image', async () => {
      const mockCaptcha = {
        captchaId: 'captcha-123',
        captchaImage: 'base64-encoded-image-data'
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockCaptcha })

      const result = await getCaptcha()

      expect(get).toHaveBeenCalledWith('/captcha')
      expect(result.data).toEqual(mockCaptcha)
      expect(result.data.captchaId).toBe('captcha-123')
      expect(result.data.captchaImage).toBeTruthy()
    })
  })
})
