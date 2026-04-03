import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { useAuth } from '@/composables/useAuth'

const apiMocks = vi.hoisted(() => ({
  loginApi: vi.fn(),
  logoutApi: vi.fn(),
  getCurrentUser: vi.fn(),
  getCaptcha: vi.fn(),
  refreshTokenApi: vi.fn()
}))

vi.mock('@/api', () => ({
  login: apiMocks.loginApi,
  logout: apiMocks.logoutApi,
  getCurrentUser: apiMocks.getCurrentUser,
  getCaptcha: apiMocks.getCaptcha,
  refreshToken: apiMocks.refreshTokenApi
}))

const makeUser = (overrides: Partial<Record<string, any>> = {}) => ({
  id: 'user-001',
  username: 'admin',
  nickname: '管理员',
  avatar: 'avatar.jpg',
  email: 'admin@example.com',
  roles: ['ROLE_ADMIN'],
  permissions: ['*'],
  ...overrides
})

const makeLoginResponse = (overrides: Partial<Record<string, any>> = {}) => ({
  data: {
    token: 'access-token-123',
    refreshToken: 'refresh-token-456',
    user: makeUser(),
    ...overrides
  },
  message: 'success'
})

describe('useAuth', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Clear localStorage
    localStorage.clear()
  })

  // =========================================================================
  // 1. Initial State
  // =========================================================================
  describe('Initial State', () => {
    it('initializes with token from localStorage', () => {
      localStorage.setItem('token', 'stored-token')
      const { token } = useAuth()

      expect(token.value).toBe('stored-token')
    })

    it('initializes with null token when localStorage empty', () => {
      const { token } = useAuth()

      expect(token.value).toBeNull()
    })

    it('initializes isLoggedIn computed based on token', () => {
      localStorage.setItem('token', 'test-token')
      const { isLoggedIn } = useAuth()

      expect(isLoggedIn.value).toBe(true)
    })

    it('initializes isLoggedIn as false when no token', () => {
      const { isLoggedIn } = useAuth()

      expect(isLoggedIn.value).toBe(false)
    })

    it('initializes with null userInfo', () => {
      const { userInfo } = useAuth()

      expect(userInfo.value).toBeNull()
    })

    it('initializes with null captcha', () => {
      const { captcha } = useAuth()

      expect(captcha.value).toBeNull()
    })
  })

  // =========================================================================
  // 2. Computed Properties
  // =========================================================================
  describe('Computed Properties', () => {
    it('username returns user username from userInfo', () => {
      const { userInfo, username } = useAuth()
      userInfo.value = makeUser({ username: 'testuser' })

      expect(username.value).toBe('testuser')
    })

    it('username returns empty string when no userInfo', () => {
      const { username } = useAuth()

      expect(username.value).toBe('')
    })

    it('nickname returns user nickname from userInfo', () => {
      const { userInfo, nickname } = useAuth()
      userInfo.value = makeUser({ nickname: 'Test Nickname' })

      expect(nickname.value).toBe('Test Nickname')
    })

    it('nickname returns empty string when no userInfo', () => {
      const { nickname } = useAuth()

      expect(nickname.value).toBe('')
    })

    it('avatar returns user avatar from userInfo', () => {
      const { userInfo, avatar } = useAuth()
      userInfo.value = makeUser({ avatar: 'avatar.jpg' })

      expect(avatar.value).toBe('avatar.jpg')
    })

    it('avatar returns empty string when no userInfo', () => {
      const { avatar } = useAuth()

      expect(avatar.value).toBe('')
    })
  })

  // =========================================================================
  // 3. Login
  // =========================================================================
  describe('login', () => {
    it('successfully logs in user', async () => {
      apiMocks.loginApi.mockResolvedValue(makeLoginResponse())
      const { login, token, refreshToken, userInfo, isLoggedIn } = useAuth()

      const req = {
        username: 'admin',
        password: 'admin123',
        captchaId: 'captcha-001',
        captchaCode: '1234'
      }

      await login(req)

      expect(apiMocks.loginApi).toHaveBeenCalledWith('admin', 'admin123', 'captcha-001', '1234')
      expect(token.value).toBe('access-token-123')
      expect(refreshToken.value).toBe('refresh-token-456')
      expect(userInfo.value).toEqual(makeUser())
      expect(isLoggedIn.value).toBe(true)
      expect(localStorage.getItem('token')).toBe('access-token-123')
      expect(localStorage.getItem('refreshToken')).toBe('refresh-token-456')
    })

    it('throws error when API returns no data', async () => {
      apiMocks.loginApi.mockResolvedValue(null)
      const { login } = useAuth()

      const req = {
        username: 'admin',
        password: 'admin123',
        captchaId: 'captcha-001',
        captchaCode: '1234'
      }

      await expect(login(req)).rejects.toThrow('登录失败')
    })

    it('throws error when response has no token', async () => {
      apiMocks.loginApi.mockResolvedValue({
        data: { user: makeUser() }
      })
      const { login } = useAuth()

      const req = {
        username: 'admin',
        password: 'admin123',
        captchaId: 'captcha-001',
        captchaCode: '1234'
      }

      await expect(login(req)).rejects.toThrow('登录响应缺少token')
    })

    it('clears captcha after successful login', async () => {
      apiMocks.loginApi.mockResolvedValue(makeLoginResponse())
      const { login, captcha } = useAuth()

      captcha.value = { id: 'captcha-001', image: 'captcha.png' }

      await login({ username: 'test', password: 'test', captchaId: 'captcha-001', captchaCode: '1234' })

      expect(captcha.value).toBeNull()
    })
  })

  // =========================================================================
  // 4. Logout
  // =========================================================================
  describe('logout', () => {
    it('clears all auth state and localStorage', async () => {
      localStorage.setItem('token', 'test-token')
      localStorage.setItem('refreshToken', 'test-refresh')
      apiMocks.logoutApi.mockResolvedValue({})

      const { logout, token, refreshToken, userInfo, captcha, isLoggedIn } = useAuth()

      token.value = 'test-token'
      refreshToken.value = 'test-refresh'
      userInfo.value = makeUser()
      captcha.value = { id: 'captcha-001', image: 'captcha.png' }

      expect(isLoggedIn.value).toBe(true)

      await logout()

      expect(apiMocks.logoutApi).toHaveBeenCalled()
      expect(token.value).toBeNull()
      expect(refreshToken.value).toBeNull()
      expect(userInfo.value).toBeNull()
      expect(captcha.value).toBeNull()
      expect(isLoggedIn.value).toBe(false)
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('refreshToken')).toBeNull()
    })

    it('ignores API errors and still clears local state', async () => {
      localStorage.setItem('token', 'test-token')
      apiMocks.logoutApi.mockRejectedValue(new Error('API Error'))

      const { logout, token, refreshToken } = useAuth()

      await logout()

      expect(token.value).toBeNull()
      expect(refreshToken.value).toBeNull()
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('refreshToken')).toBeNull()
    })
  })

  // =========================================================================
  // 5. fetchUserInfo
  // =========================================================================
  describe('fetchUserInfo', () => {
    it('fetches and sets user info', async () => {
      apiMocks.getCurrentUser.mockResolvedValue({ data: makeUser() })

      const { fetchUserInfo, userInfo, token } = useAuth()
      token.value = 'test-token'

      await fetchUserInfo()

      expect(apiMocks.getCurrentUser).toHaveBeenCalled()
      expect(userInfo.value).toEqual(makeUser())
    })

    it('returns early when no token available', async () => {
      apiMocks.getCurrentUser.mockResolvedValue({ data: makeUser() })

      const { fetchUserInfo, userInfo } = useAuth()

      await fetchUserInfo()

      expect(apiMocks.getCurrentUser).not.toHaveBeenCalled()
      expect(userInfo.value).toBeNull()
    })

    it('uses saved token from localStorage when state token is null', async () => {
      // Set localStorage BEFORE calling useAuth()
      localStorage.setItem('token', 'saved-token')
      apiMocks.getCurrentUser.mockResolvedValue({ data: makeUser() })

      const { fetchUserInfo, token, userInfo } = useAuth()

      // Token is initialized from localStorage
      expect(token.value).toBe('saved-token')

      await fetchUserInfo()

      expect(token.value).toBe('saved-token')
      expect(apiMocks.getCurrentUser).toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 6. fetchCaptcha
  // =========================================================================
  describe('fetchCaptcha', () => {
    it('fetches and sets captcha', async () => {
      const captchaData = { id: 'captcha-001', image: 'data:image/png;base64,abc123' }
      apiMocks.getCaptcha.mockResolvedValue({ data: captchaData })

      const { fetchCaptcha, captcha } = useAuth()

      await fetchCaptcha()

      expect(apiMocks.getCaptcha).toHaveBeenCalled()
      expect(captcha.value).toEqual(captchaData)
    })
  })

  // =========================================================================
  // 7. refreshAccessToken
  // =========================================================================
  describe('refreshAccessToken', () => {
    it('refreshes access token', async () => {
      const newToken = 'new-access-token'
      apiMocks.refreshTokenApi.mockResolvedValue({ data: newToken })

      const { refreshAccessToken, token, refreshToken } = useAuth()
      refreshToken.value = 'test-refresh'

      await refreshAccessToken()

      expect(apiMocks.refreshTokenApi).toHaveBeenCalledWith('test-refresh')
      expect(token.value).toBe(newToken)
      expect(localStorage.getItem('token')).toBe(newToken)
    })

    it('throws error when no refresh token', async () => {
      const { refreshAccessToken } = useAuth()

      await expect(refreshAccessToken()).rejects.toThrow('无 refresh token')
    })

    it('throws error when API fails', async () => {
      apiMocks.refreshTokenApi.mockResolvedValue(null)
      const { refreshAccessToken, refreshToken } = useAuth()
      refreshToken.value = 'test-refresh'

      await expect(refreshAccessToken()).rejects.toThrow('刷新token失败')
    })
  })

  // =========================================================================
  // 8. restoreToken
  // =========================================================================
  describe('restoreToken', () => {
    it('restores token from localStorage when state token is null', () => {
      localStorage.setItem('token', 'stored-token')
      const { token, restoreToken } = useAuth()

      // Token is initialized from localStorage in useAuth constructor
      expect(token.value).toBe('stored-token')

      const result = restoreToken()

      expect(token.value).toBe('stored-token')
      expect(result).toBe(true)
    })

    it('does not overwrite existing token', () => {
      const { token, restoreToken } = useAuth()
      token.value = 'existing-token'

      const result = restoreToken()

      expect(token.value).toBe('existing-token')
    })

    it('restores refresh token', () => {
      localStorage.setItem('refreshToken', 'stored-refresh')
      const { refreshToken, restoreToken } = useAuth()

      restoreToken()

      expect(refreshToken.value).toBe('stored-refresh')
    })

    it('returns false when no token in localStorage', () => {
      const { restoreToken } = useAuth()

      const result = restoreToken()

      expect(result).toBe(false)
    })
  })

  // =========================================================================
  // 9. setToken
  // =========================================================================
  describe('setToken', () => {
    it('sets token and persists to localStorage', () => {
      const { setToken, token } = useAuth()

      setToken('new-token')

      expect(token.value).toBe('new-token')
      expect(localStorage.getItem('token')).toBe('new-token')
    })

    it('sets refresh token when provided', () => {
      const { setToken, refreshToken } = useAuth()

      setToken('new-token', 'new-refresh')

      expect(refreshToken.value).toBe('new-refresh')
      expect(localStorage.getItem('refreshToken')).toBe('new-refresh')
    })

    it('does not update refresh token when not provided', () => {
      const { setToken, refreshToken } = useAuth()
      refreshToken.value = 'existing-refresh'

      setToken('new-token')

      expect(refreshToken.value).toBe('existing-refresh')
    })
  })

  // =========================================================================
  // 10. clearAuth
  // =========================================================================
  describe('clearAuth', () => {
    it('clears all auth state and localStorage', () => {
      localStorage.setItem('token', 'test-token')
      localStorage.setItem('refreshToken', 'test-refresh')

      const { clearAuth, token, refreshToken, userInfo, captcha, isLoggedIn } = useAuth()
      token.value = 'test-token'
      refreshToken.value = 'test-refresh'
      userInfo.value = makeUser()
      captcha.value = { id: 'captcha-001', image: 'captcha.png' }

      clearAuth()

      expect(token.value).toBeNull()
      expect(refreshToken.value).toBeNull()
      expect(userInfo.value).toBeNull()
      expect(captcha.value).toBeNull()
      expect(isLoggedIn.value).toBe(false)
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('refreshToken')).toBeNull()
    })
  })
})
