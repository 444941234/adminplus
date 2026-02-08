import request from '@/utils/request'

/**
 * 获取统计数据
 * @returns {Promise} 返回统计数据（用户数、角色数、菜单数、日志数）
 */
export const getDashboardStats = () => {
  return request({
    url: '/v1/sys/dashboard/stats',
    method: 'get'
  })
}

/**
 * 获取用户增长趋势
 * @returns {Promise} 返回图表数据
 */
export const getUserGrowth = () => {
  return request({
    url: '/v1/sys/dashboard/user-growth',
    method: 'get'
  })
}

/**
 * 获取角色分布
 * @returns {Promise} 返回图表数据
 */
export const getRoleDistribution = () => {
  return request({
    url: '/v1/sys/dashboard/role-distribution',
    method: 'get'
  })
}

/**
 * 获取菜单类型分布
 * @returns {Promise} 返回图表数据
 */
export const getMenuDistribution = () => {
  return request({
    url: '/v1/sys/dashboard/menu-distribution',
    method: 'get'
  })
}

/**
 * 获取最近操作日志
 * @returns {Promise} 返回操作日志列表
 */
export const getRecentLogs = () => {
  return request({
    url: '/v1/sys/dashboard/recent-logs',
    method: 'get'
  })
}

/**
 * 获取系统信息
 * @returns {Promise} 返回系统信息
 */
export const getSystemInfo = () => {
  return request({
    url: '/v1/sys/dashboard/system-info',
    method: 'get'
  })
}

/**
 * 获取在线用户
 * @returns {Promise} 返回在线用户列表
 */
export const getOnlineUsers = () => {
  return request({
    url: '/v1/sys/dashboard/online-users',
    method: 'get'
  })
}