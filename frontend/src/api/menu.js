import request from '@/utils/request'

/**
 * 获取菜单树形结构
 * @returns {Promise<Object[]>} 菜单树形结构
 */
export const getMenuTree = () => {
  return request({
    url: '/v1/sys/menus/tree',
    method: 'get'
  })
}

/**
 * 根据ID获取菜单信息
 * @param {number} id - 菜单ID
 * @returns {Promise<Object>} 菜单信息
 */
export const getMenuById = (id) => {
  return request({
    url: `/v1/sys/menus/${id}`,
    method: 'get'
  })
}

/**
 * 创建菜单
 * @param {Object} data - 菜单信息
 * @param {number} data.parentId - 父菜单ID
 * @param {number} data.menuType - 菜单类型（0-目录，1-菜单，2-按钮）
 * @param {string} data.menuName - 菜单名称
 * @param {string} data.path - 路由路径
 * @param {string} data.component - 组件路径
 * @param {string} data.permission - 权限标识
 * @param {string} data.icon - 图标
 * @param {number} data.sortOrder - 排序
 * @param {number} data.visible - 是否显示（1-显示，0-隐藏）
 * @param {number} data.status - 状态（1-启用，0-禁用）
 * @returns {Promise<Object>} 创建的菜单信息
 */
export const createMenu = (data) => {
  return request({
    url: '/v1/sys/menus',
    method: 'post',
    data
  })
}

/**
 * 更新菜单信息
 * @param {number} id - 菜单ID
 * @param {Object} data - 菜单信息
 * @returns {Promise<Object>} 更新后的菜单信息
 */
export const updateMenu = (id, data) => {
  return request({
    url: `/v1/sys/menus/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除菜单
 * @param {number} id - 菜单ID
 * @returns {Promise<void>}
 */
export const deleteMenu = (id) => {
  return request({
    url: `/v1/sys/menus/${id}`,
    method: 'delete'
  })
}