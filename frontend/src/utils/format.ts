/**
 * 格式化日期时间为本地字符串
 *
 * @param value ISO 日期字符串
 * @returns 格式化后的字符串，空值返回 '-'
 */
export const formatDateTime = (value?: string | null): string => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}
