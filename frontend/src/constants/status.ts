/**
 * 状态常量
 *
 * 统一管理系统中的状态值和相关标签
 */

/** 状态值常量 */
export const STATUS_ACTIVE = 1
export const STATUS_INACTIVE = 0

/** 状态标签映射 */
export const STATUS_LABELS: Record<number, string> = {
  [STATUS_ACTIVE]: '正常',
  [STATUS_INACTIVE]: '禁用'
}

/** 获取状态切换按钮的标签 */
export function getStatusToggleLabel(status: number): string {
  return status === STATUS_ACTIVE ? '禁用' : '启用'
}

/** 获取状态标签 */
export function getStatusLabel(status: number): string {
  return STATUS_LABELS[status] ?? '未知'
}
