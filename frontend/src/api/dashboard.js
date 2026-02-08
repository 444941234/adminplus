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