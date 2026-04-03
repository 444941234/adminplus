import { ref, computed } from 'vue'
import type { User, CaptchaResp } from '@/types'
import { login as loginApi, logout as logoutApi, getCurrentUser, getCaptcha, refreshToken as refreshTokenApi } from '@/api'

interface LoginRequest {
  username: string
  password: string
  captchaId: string
  captchaCode: string
}

/**
 * 认证相关 composable
 * 提供登录、登出、token 管理、用户信息获取等功能
 *
 * @example
 * ```ts
 * const { login, logout, token, isLoggedIn, fetchUserInfo } = useAuth()
 * ```
 */
export function useAuth() {
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const userInfo = ref<User | null>(null)
  const captcha = ref<CaptchaResp | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  /**
   * 登录
   */
  const login = async (req: LoginRequest) => {
    const res = await loginApi(req.username, req.password, req.captchaId, req.captchaCode)
    if (!res || !res.data) {
      throw new Error(res?.message || '登录失败')
    }
    if (!res.data.token) {
      throw new Error('登录响应缺少token')
    }

    // 保存 token 和用户信息
    captcha.value = null
    token.value = res.data.token
    refreshToken.value = res.data.refreshToken || null
    userInfo.value = res.data.user || null

    // 持久化到 localStorage
    localStorage.setItem('token', res.data.token)
    if (res.data.refreshToken) {
      localStorage.setItem('refreshToken', res.data.refreshToken)
    }

    return res.data
  }

  /**
   * 登出
   */
  const logout = async () => {
    try {
      await logoutApi()
    } catch {
      // 忽略 API 错误，继续清理本地状态
    }

    // 清理状态
    token.value = null
    refreshToken.value = null
    userInfo.value = null
    captcha.value = null

    // 清理 localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  /**
   * 获取当前用户信息
   */
  const fetchUserInfo = async () => {
    // 确保 token 同步
    if (!token.value) {
      const savedToken = localStorage.getItem('token')
      if (savedToken) {
        token.value = savedToken
      } else {
        return
      }
    }

    const res = await getCurrentUser()
    userInfo.value = res.data
    return res.data
  }

  /**
   * 获取验证码
   */
  const fetchCaptcha = async () => {
    const res = await getCaptcha()
    captcha.value = res.data
    return res.data
  }

  /**
   * 刷新 token
   */
  const refreshAccessToken = async () => {
    if (!refreshToken.value) {
      throw new Error('无 refresh token')
    }

    const res = await refreshTokenApi(refreshToken.value)
    if (!res || !res.data) {
      throw new Error(res?.message || '刷新token失败')
    }

    token.value = res.data
    localStorage.setItem('token', res.data)
    return res.data
  }

  /**
   * 从 localStorage 恢复 token（用于刷新后同步状态）
   */
  const restoreToken = () => {
    const savedToken = localStorage.getItem('token')
    const savedRefreshToken = localStorage.getItem('refreshToken')
    if (savedToken && !token.value) {
      token.value = savedToken
    }
    if (savedRefreshToken && !refreshToken.value) {
      refreshToken.value = savedRefreshToken
    }
    return !!token.value
  }

  /**
   * 设置 token（用于外部设置）
   */
  const setToken = (newToken: string, newRefreshToken?: string) => {
    token.value = newToken
    if (newRefreshToken) {
      refreshToken.value = newRefreshToken
      localStorage.setItem('refreshToken', newRefreshToken)
    }
    localStorage.setItem('token', newToken)
  }

  /**
   * 清除认证状态
   */
  const clearAuth = () => {
    token.value = null
    refreshToken.value = null
    userInfo.value = null
    captcha.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  return {
    // 状态
    token,
    refreshToken,
    userInfo,
    captcha,
    // 计算属性
    isLoggedIn,
    username,
    nickname,
    avatar,
    // 方法
    login,
    logout,
    fetchUserInfo,
    fetchCaptcha,
    refreshAccessToken,
    restoreToken,
    setToken,
    clearAuth
  }
}

/**
 * 单例模式的认证 hook
 * 全局共享同一个认证状态
 */
let globalAuthHook: ReturnType<typeof useAuth> | null = null

export function useAuthSingleton() {
  if (!globalAuthHook) {
    globalAuthHook = useAuth()
  }
  return globalAuthHook
}
