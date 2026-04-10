import { get, post, del } from '@/utils/request'
import type { Log, PageResult } from '@/types'

// 获取日志列表
export function getLogList(params: {
  page?: number
  size?: number
  logType?: number
  username?: string
  module?: string
  operationType?: number
  status?: number
  startTime?: string
  endTime?: string
}) {
  return get<PageResult<Log>>('/sys/logs', { params })
}

// 获取日志详情
export function getLogById(id: string) {
  return get<Log>(`/sys/logs/${id}`)
}

// 删除日志
export function deleteLog(id: string) {
  return del<void>(`/sys/logs/${id}`)
}

// 批量删除日志
export function deleteLogsBatch(ids: string[]) {
  return del<void>('/sys/logs/batch', { config: { data: ids } })
}

// 按条件删除日志
export function deleteLogsByCondition(data: {
  page?: number
  size?: number
  logType?: number
  username?: string
  module?: string
  operationType?: number
  status?: number
  startTime?: string
  endTime?: string
}) {
  return del<number>('/sys/logs/condition', { config: { data } })
}

// 清理过期日志
export function cleanupExpiredLogs() {
  return post<number>('/sys/logs/cleanup')
}

// 获取日志统计
export function getLogStatistics() {
  return get<LogStatistics>('/sys/logs/statistics')
}

// 导出 Excel
export function exportLogExcel(params: Record<string, unknown>) {
  return `/api/sys/logs/export/excel?${new URLSearchParams(params as Record<string, string>).toString()}`
}

// 导出 CSV
export function exportLogCsv(params: Record<string, unknown>) {
  return `/api/sys/logs/export/csv?${new URLSearchParams(params as Record<string, string>).toString()}`
}

// 日志统计
export interface LogStatistics {
  totalCount: number
  loginCount: number
  operationCount: number
  todayCount: number
  successRate: number
}
