import { get } from '@/utils/request'
import type {
  DashboardStats,
  ChartData,
  OnlineUser,
  OperationLog,
  StatisticsData,
  SystemInfo
} from '@/types'

// 获取统计数据
export function getStats() {
  return get<DashboardStats>('/sys/dashboard/stats')
}

// 获取用户增长趋势
export function getUserGrowth() {
  return get<ChartData>('/sys/dashboard/user-growth')
}

// 获取角色分布
export function getRoleDistribution() {
  return get<ChartData>('/sys/dashboard/role-distribution')
}

// 获取菜单分布
export function getMenuDistribution() {
  return get<ChartData>('/sys/dashboard/menu-distribution')
}

// 获取最近操作日志
export function getRecentLogs() {
  return get<OperationLog[]>('/sys/dashboard/recent-logs')
}

// 获取访问趋势
export function getVisitTrend() {
  return get<ChartData>('/sys/dashboard/visit-trend')
}

// 获取在线用户
export function getOnlineUsers() {
  return get<OnlineUser[]>('/sys/dashboard/online-users')
}

// 获取系统信息
export function getSystemInfo() {
  return get<SystemInfo>('/sys/dashboard/system-info')
}

// 获取统计页数据
export function getStatistics() {
  return get<StatisticsData>('/sys/dashboard/statistics')
}
