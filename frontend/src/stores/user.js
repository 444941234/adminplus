import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getCurrentUser, getCurrentUserPermissions } from '@/api/auth'
import { assignRoles as assignRolesApi, getUserRoleIds as getUserRoleIdsApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(sessionStorage.getItem('token') || '')
  const user = ref(JSON.parse(sessionStorage.getItem('user') || 'null'))
  const permissions = ref(JSON.parse(sessionStorage.getItem('permissions') || '[]'))

  /**
   * 设置 token
   * @param {string} val - token 值
   */
  const setToken = (val) => {
    token.value = val
    sessionStorage.setItem('token', val)
  }

  /**
   * 设置用户信息
   * @param {Object} val - 用户信息
   */
  const setUser = (val) => {
    user.value = val
    sessionStorage.setItem('user', JSON.stringify(val))
  }

  /**
   * 设置权限列表
   * @param {string[]} val - 权限列表
   */
  const setPermissions = (val) => {
    permissions.value = val || []
    sessionStorage.setItem('permissions', JSON.stringify(val || []))
  }

  /**
   * 用户登录
   * @param {string} username - 用户名
   * @param {string} password - 密码
   * @returns {Promise<Object>} 登录结果，包含 token、user、permissions
   */
  const login = async (username, password) => {
    const data = await loginApi({ username, password })
    setToken(data.token)
    setUser(data.user)
    setPermissions(data.permissions || [])
    return data
  }

  /**
   * 获取当前用户信息
   * @returns {Promise<Object>} 用户信息
   */
  const getUserInfo = async () => {
    const data = await getCurrentUser()
    setUser(data)
    return data
  }

  /**
   * 刷新用户权限
   * @returns {Promise<string[]>} 权限列表
   */
  const refreshPermissions = async () => {
    const data = await getCurrentUserPermissions()
    setPermissions(data || [])
    return data
  }

  /**
   * 用户登出
   */
  const logout = () => {
    token.value = ''
    user.value = null
    permissions.value = []
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
    sessionStorage.removeItem('permissions')
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
    setToken,
    setUser,
    setPermissions,
    login,
    getUserInfo,
    refreshPermissions,
    logout,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    assignRoles,
    getUserRoleIds
  }
})