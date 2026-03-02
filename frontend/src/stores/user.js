import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getCurrentUser, getCurrentUserPermissions } from '@/api/auth'
import { assignRoles as assignRolesApi, getUserRoleIds as getUserRoleIdsApi } from '@/api/user'
import { setEncryptedSession, getEncryptedSession } from '@/utils/encryption'

export const useUserStore = defineStore('user', () => {
  // 从 sessionStorage 中读取数据（使用加密存储）
  const token = ref('')
  const user = ref(null)
  const permissions = ref([])
  const hasLoadedRoutes = ref(false)

  /**
   * 设置 token（加密存储）
   * @param {string} val - token 值
   */
  const setToken = async (val) => {
    token.value = val
    await setEncryptedSession('token', val)
  }

  /**
   * 设置用户信息（加密存储）
   * @param {Object} val - 用户信息
   */
  const setUser = async (val) => {
    user.value = val
    await setEncryptedSession('user', val)
  }

  /**
   * 设置权限列表（加密存储）
   * @param {string[]} val - 权限列表
   */
  const setPermissions = async (val) => {
    permissions.value = val || []
    await setEncryptedSession('permissions', val || [])
  }

  /**
   * 标记路由已加载
   * @param {boolean} loaded - 是否已加载
   */
  const setRoutesLoaded = (loaded) => {
    hasLoadedRoutes.value = loaded
    sessionStorage.setItem('hasLoadedRoutes', loaded.toString())
  }

  /**
   * 用户登录
   * @param {string} username - 用户名
   * @param {string} password - 密码
   * @param {string} captchaCode - 验证码
   * @param {string} captchaId - 验证码ID
   * @returns {Promise<Object>} 登录结果，包含 token、user、permissions
   */
  const login = async (username, password, captchaCode, captchaId) => {
    const data = await loginApi({ username, password, captchaCode, captchaId })
    await setToken(data.token)
    await setUser(data.user)
    await setPermissions(data.permissions || [])
    // 登录后重置路由加载状态
    setRoutesLoaded(false)
    return data
  }

  /**
   * 获取当前用户信息
   * @returns {Promise<Object>} 用户信息
   */
  const getUserInfo = async () => {
    const data = await getCurrentUser()
    await setUser(data)
    return data
  }

  /**
   * 刷新用户权限
   * @returns {Promise<string[]>} 权限列表
   */
  const refreshPermissions = async () => {
    const data = await getCurrentUserPermissions()
    await setPermissions(data || [])
    return data
  }

  /**
   * 用户登出
   */
  const logout = () => {
    token.value = ''
    user.value = null
    permissions.value = []
    hasLoadedRoutes.value = false
    // 清除加密的 sessionStorage 数据
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
    sessionStorage.removeItem('permissions')
    sessionStorage.removeItem('hasLoadedRoutes')
    // 同时清除 refreshToken（如果存在）
    sessionStorage.removeItem('refreshToken')
  }

  /**
   * 初始化 store，从加密的 sessionStorage 中加载数据
   */
  const initialize = async () => {
    try {
      token.value = await getEncryptedSession('token', '')
      user.value = await getEncryptedSession('user', null)
      permissions.value = await getEncryptedSession('permissions', [])

      // 路由只在内存中，页面刷新后需要重新加载
      // 但 initialize 可能在短时间内被多次调用（路由守卫每次都调用）
      // 所以只在用户信息为空时（首次初始化）才重置 hasLoadedRoutes
      if (!user.value) {
        hasLoadedRoutes.value = false
      }
    } catch (error) {
      console.error('[UserStore] 初始化失败:', error)
      // 如果解密失败，清除所有数据
      logout()
    }
  }

  /**
   * 检查是否有指定权限
   * @param {string} permission - 权限标识
   * @returns {boolean} 是否有权限
   */
  const hasPermission = (permission) => {
    if (!permission) return true
    if (!permissions.value || permissions.value.length === 0) return false
    return permissions.value.includes(permission)
  }

  /**
   * 检查是否有任意一个权限
   * @param {string[]} permissionList - 权限列表
   * @returns {boolean} 是否有任意一个权限
   */
  const hasAnyPermission = (permissionList) => {
    if (!permissionList || permissionList.length === 0) return true
    return permissionList.some(permission => hasPermission(permission))
  }

  /**
   * 检查是否有所有权限
   * @param {string[]} permissionList - 权限列表
   * @returns {boolean} 是否有所有权限
   */
  const hasAllPermissions = (permissionList) => {
    if (!permissionList || permissionList.length === 0) return true
    return permissionList.every(permission => hasPermission(permission))
  }

  /**
   * 为用户分配角色
   * @param {number} userId - 用户ID
   * @param {number[]} roleIds - 角色ID列表
   * @returns {Promise<void>}
   */
  const assignRoles = async (userId, roleIds) => {
    return await assignRolesApi(userId, roleIds)
  }

  /**
   * 获取用户的角色ID列表
   * @param {number} userId - 用户ID
   * @returns {Promise<number[]>} 角色ID列表
   */
  const getUserRoleIds = async (userId) => {
    return await getUserRoleIdsApi(userId)
  }

  return {
    token,
    user,
    permissions,
    hasLoadedRoutes,
    setToken,
    setUser,
    setPermissions,
    setRoutesLoaded,
    login,
    getUserInfo,
    refreshPermissions,
    logout,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    assignRoles,
    getUserRoleIds,
    initialize
  }
})