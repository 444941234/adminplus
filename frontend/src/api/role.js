import request from '@/utils/request'

/**
 * 获取角色列表
 * @returns {Promise<Object>} 角色列表数据
 */
export const getRoleList = () => {
  return request({
    url: '/sys/roles',
    method: 'get'
  })
}

/**
 * 根据ID获取角色信息
 * @param {number} id - 角色ID
 * @returns {Promise<Object>} 角色信息
 */
export const getRoleById = (id) => {
  return request({
    url: `/sys/roles/${id}`,
    method: 'get'
  })
}

/**
 * 创建角色
 * @param {Object} data - 角色信息
 * @param {string} data.code - 角色编码
 * @param {string} data.name - 角色名称
 * @param {string} data.description - 描述
 * @param {number} data.dataScope - 数据权限范围
 * @param {number} data.status - 状态（1-启用，0-禁用）
 * @param {number} data.sortOrder - 排序
 * @returns {Promise<Object>} 创建的角色信息
 */
export const createRole = (data) => {
  return request({
    url: '/sys/roles',
    method: 'post',
    data
  })
}

/**
 * 更新角色信息
 * @param {number} id - 角色ID
 * @param {Object} data - 角色信息
 * @returns {Promise<Object>} 更新后的角色信息
 */
export const updateRole = (id, data) => {
  return request({
    url: `/sys/roles/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除角色
 * @param {number} id - 角色ID
 * @returns {Promise<void>}
 */
export const deleteRole = (id) => {
  return request({
    url: `/sys/roles/${id}`,
    method: 'delete'
  })
}

/**
 * 为角色分配菜单权限
 * @param {number} id - 角色ID
 * @param {number[]} menuIds - 菜单ID列表
 * @returns {Promise<void>}
 */
export const assignMenus = (id, menuIds) => {
  return request({
    url: `/sys/roles/${id}/menus`,
    method: 'put',
    data: menuIds
  })
}

/**
 * 获取角色的菜单ID列表
 * @param {number} id - 角色ID
 * @returns {Promise<number[]>} 菜单ID列表
 */
export const getRoleMenuIds = (id) => {
  return request({
    url: `/sys/roles/${id}/menus`,
    method: 'get'
  })
}