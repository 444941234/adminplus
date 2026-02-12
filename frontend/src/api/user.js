import request from '@/utils/request';

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 关键字（用户名/昵称）
 * @param {string} params.deptId - 部门ID
 * @returns {Promise<Object>} 用户列表数据
 */
export const getUserList = (params) => {
  return request({
    url: '/v1/sys/users',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取用户信息
 * @param {number} id - 用户ID
 * @returns {Promise<Object>} 用户信息
 */
export const getUserById = (id) => {
  return request({
    url: `/v1/sys/users/${id}`,
    method: 'get'
  })
}

/**
 * 创建用户
 * @param {Object} data - 用户信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.nickname - 昵称
 * @param {string} data.email - 邮箱
 * @param {string} data.phone - 手机号
 * @param {string} data.deptId - 部门ID
 * @returns {Promise<Object>} 创建的用户信息
 */
export const createUser = (data) => {
  return request({
    url: '/v1/sys/users',
    method: 'post',
    data
  })
}

/**
 * 更新用户信息
 * @param {number} id - 用户ID
 * @param {Object} data - 用户信息
 * @returns {Promise<Object>} 更新后的用户信息
 */
export const updateUser = (id, data) => {
  return request({
    url: `/v1/sys/users/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 * @param {number} id - 用户ID
 * @returns {Promise<void>}
 */
export const deleteUser = (id) => {
  return request({
    url: `/v1/sys/users/${id}`,
    method: 'delete'
  })
}

/**
 * 更新用户状态
 * @param {number} id - 用户ID
 * @param {number} status - 状态（1-启用，0-禁用）
 * @returns {Promise<void>}
 */
export const updateUserStatus = (id, status) => {
  return request({
    url: `/v1/sys/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 重置用户密码
 * @param {number} id - 用户ID
 * @param {string} password - 新密码
 * @returns {Promise<void>}
 */
export const resetPassword = (id, password) => {
  return request({
    url: `/v1/sys/users/${id}/password`,
    method: 'put',
    params: { password }
  })
}

/**
 * 为用户分配角色
 * @param {number} userId - 用户ID
 * @param {number[]} roleIds - 角色ID列表
 * @returns {Promise<void>}
 */
export const assignRoles = (userId, roleIds) => {
  return request({
    url: `/v1/sys/users/${userId}/roles`,
    method: 'put',
    data: roleIds,
  });
}

/**
 * 获取用户的角色ID列表
 * @param {number} userId - 用户ID
 * @returns {Promise<number[]>} 角色ID列表
 */
export const getUserRoleIds = (userId) => {
  return request({
    url: `/v1/sys/users/${userId}/roles`,
    method: 'get'
  })
}
