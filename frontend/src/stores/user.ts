import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, Menu } from '@/types'
import { login as loginApi, logout as logoutApi, getCurrentUser, getPermissions, getCaptcha, getUserMenuTree } from '@/api'
import type { CaptchaResp } from '@/api/auth'
import { hasPermission as checkPermission } from '@/lib/permissions'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const userInfo = ref<User | null>(null)
  const permissions = ref<string[]>([])
  const menus = ref<Menu[]>([])
  const captcha = ref<CaptchaResp | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  // 登录
  const login = async (username: string, password: string, captchaId: string, captchaCode: string) => {
    const res = await loginApi(username, password, captchaId, captchaCode)
    if (!res || !res.data) {
      throw new Error(res?.message || '登录失败')
    }
    if (!res.data.token) {
      throw new Error('登录响应缺少token')
    }
    // 登录成功，立即清空验证码
    captcha.value = null
    token.value = res.data.token
    refreshToken.value = res.data.refreshToken || null
    userInfo.value = res.data.user || null
    permissions.value = res.data.permissions || []
    localStorage.setItem('token', res.data.token)
    if (res.data.refreshToken) {
      localStorage.setItem('refreshToken', res.data.refreshToken)
    }
    return res
  }

  // 退出
  const logout = async () => {
    try {
      await logoutApi()
    } catch {
      // ignore
    }
    token.value = null
    refreshToken.value = null
    userInfo.value = null
    permissions.value = []
    menus.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    // Ensure token is synced from localStorage before making API calls
    if (!token.value) {
      const savedToken = localStorage.getItem('token')
      if (savedToken) {
        token.value = savedToken
      } else {
        return // No token available, cannot fetch user info
      }
    }

    const [userRes, permRes, menuRes] = await Promise.all([
      getCurrentUser(),
      getPermissions(),
      getUserMenuTree()
    ])
    userInfo.value = userRes.data
    permissions.value = permRes.data
    menus.value = menuRes.data
  }

  // 获取验证码
  const fetchCaptcha = async () => {
    const res = await getCaptcha()
    captcha.value = res.data
    return res.data
  }

  // 检查权限
  const hasPermission = (permission: string) => {
    return checkPermission(permissions.value, permission)
  }

  // 检查角色
  const hasRole = (roleCode: string) => {
    return userInfo.value?.roles?.includes(roleCode) || false
  }

  // 从 localStorage 恢复 token（用于刷新后同步状态）
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

  return {
    token,
    refreshToken,
    userInfo,
    permissions,
    menus,
    captcha,
    isLoggedIn,
    username,
    nickname,
    avatar,
    login,
    logout,
    fetchUserInfo,
    fetchCaptcha,
    hasPermission,
    hasRole,
    restoreToken
  }
})
