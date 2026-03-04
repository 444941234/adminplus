import request from '@/utils/request';

/**
 * 获取日志列表（分页）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {number} params.logType - 日志类型（1=操作日志，2=登录日志，3=系统日志）
 * @param {string} params.username - 用户名
 * @param {string} params.module - 操作模块
 * @param {number} params.operationType - 操作类型
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

/**
 * 批量删除日志
 * @param {Array<number>} ids - 日志ID列表
 * @returns {Promise<void>}
 */
export const deleteLogsBatch = (ids) => {
  return request({
    url: '/v1/sys/logs/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 根据条件删除日志
 * @param {Object} query - 查询条件
 * @returns {Promise<number>} 删除的记录数
 */
export const deleteLogsByCondition = (query) => {
  return request({
    url: '/v1/sys/logs/condition',
    method: 'delete',
    data: query
  })
}

/**
 * 清理过期日志
 * @returns {Promise<number>} 清理的记录数
 */
export const cleanupExpiredLogs = () => {
  return request({
    url: '/v1/sys/logs/cleanup',
    method: 'post'
  })
}

/**
 * 获取日志统计
 * @returns {Promise<Object>} 统计数据
 */
export const getLogStatistics = () => {
  return request({
    url: '/v1/sys/logs/statistics',
    method: 'get'
  })
}