import request from '@/utils/request'

/**
 * 获取当前用户的权限列表
 * @returns {Promise<string[]>} 权限列表
 */
export const getCurrentUserPermissions = () => {
  return request({
    url: '/auth/permissions',
    method: 'get'
  })
}

/**
 * 获取所有可用权限（用于分配）
 * @returns {Promise<Object[]>} 权限列表
 */
export const getAllPermissions = () => {
  return request({
    url: '/sys/permissions/all',
    method: 'get'
  })
}

/**
 * 获取当前用户的角色列表
 * @returns {Promise<Object[]>} 角色列表
 */
export const getCurrentUserRoles = () => {
  return request({
    url: '/sys/permissions/current/roles',
    method: 'get'
  })
}