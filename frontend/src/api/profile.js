import request from '@/utils/request'

/**
 * 获取当前用户详细信息
 * @returns {Promise<Object>} 用户详细信息
 */
export const getProfile = () => {
  return request({
    url: '/v1/profile',
    method: 'get'
  })
}

/**
 * 更新当前用户信息
 * @param {Object} data - 用户信息
 * @param {string} data.nickname - 昵称
 * @param {string} data.email - 邮箱
 * @param {string} data.phone - 手机号
 * @returns {Promise<Object>} 更新后的用户信息
 */
export const updateProfile = (data) => {
  return request({
    url: '/v1/profile',
    method: 'put',
    data
  })
}

/**
 * 修改当前用户密码
 * @param {Object} data - 密码信息
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise<void>}
 */
export const changePassword = (data) => {
  return request({
    url: '/v1/profile/password',
    method: 'post',
    data
  })
}

/**
 * 上传用户头像
 * @param {FormData} formData - 表单数据，包含文件
 * @returns {Promise<Object>} 上传后的头像 URL
 */
export const uploadAvatar = (formData) => {
  return request({
    url: '/v1/profile/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 更新用户设置
 * @param {Object} data - 设置信息
 * @param {string} data.theme - 主题（light/dark）
 * @param {string} data.language - 语言（zh-CN/en-US）
 * @returns {Promise<Object>} 更新后的设置
 */
export const updateSettings = (data) => {
  return request({
    url: '/v1/profile/settings',
    method: 'put',
    data
  })
}

/**
 * 获取用户设置
 * @returns {Promise<Object>} 用户设置
 */
export const getSettings = () => {
  return request({
    url: '/v1/profile/settings',
    method: 'get'
  })
}