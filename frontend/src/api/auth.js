import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise<Object>} 登录结果，包含 token、user、permissions
 */
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise<Object>} 用户信息
 */
export const getCurrentUser = () => {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}

/**
 * 获取当前用户权限列表
 * @returns {Promise<string[]>} 权限列表
 */
export const getCurrentUserPermissions = () => {
  return request({
    url: '/auth/permissions',
    method: 'get'
  })
}

/**
 * 用户登出
 * @returns {Promise<void>}
 */
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}