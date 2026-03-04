import request from '@/utils/request';

/**
 * 获取日志列表（分页）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.username - 用户名
 * @param {string} params.module - 操作模块
 * @param {number} params.operation - 操作类型
 * @param {number} params.status - 状态
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 * @returns {Promise<Object>} 日志列表数据
 */
export const getLogList = (params) => {
  return request({
    url: '/v1/sys/logs',
    method: 'get',
    params
  })
}

/**
 * 获取日志详情
 * @param {number} id - 日志ID
 * @returns {Promise<Object>} 日志详情
 */
export const getLogById = (id) => {
  return request({
    url: `/v1/sys/logs/${id}`,
    method: 'get'
  })
}

/**
 * 删除日志
 * @param {number} id - 日志ID
 * @returns {Promise<void>}
 */
export const deleteLog = (id) => {
  return request({
    url: `/v1/sys/logs/${id}`,
    method: 'delete'
  })
}