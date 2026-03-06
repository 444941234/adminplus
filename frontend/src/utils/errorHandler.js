/**
 * 全局错误处理器
 */

import { ElMessage } from '@/utils/elementCompat'

/**
 * 处理 API 错误
 * @param {Error} error - 错误对象
 * @param {string} defaultMessage - 默认错误消息
 */
export function handleApiError(error, defaultMessage = '操作失败') {
  if (process.env.NODE_ENV === 'development') {
    console.error('[API Error]:', error)
  }

  if (error.response) {
    const { status, data } = error.response
    const message = data?.message || getHttpErrorMessage(status)
    ElMessage.error(message)
    return
  }

  if (error.message) {
    ElMessage.error(error.message)
    return
  }

  ElMessage.error(defaultMessage)
}

/**
 * 获取 HTTP 状态码对应的错误消息
 * @param {number} status - HTTP 状态码
 * @returns {string} - 错误消息
 */
function getHttpErrorMessage(status) {
  const messages = {
    400: '请求参数错误',
    401: '登录已过期，请重新登录',
    403: '无权访问',
    404: '资源不存在',
    405: '请求方法不被允许',
    500: '服务器错误',
    502: '网关错误',
    503: '服务不可用',
    504: '网关超时'
  }

  return messages[status] || `请求失败 (${status})`
}

/**
 * 全局错误处理函数
 * @param {Error} error - 错误对象
 */
export function globalErrorHandler(error) {
  if (process.env.NODE_ENV === 'development') {
    console.error('[Global Error]:', error)
  }

  ElMessage.error('系统错误，请稍后重试')

  // 可以在这里添加错误上报逻辑
  // sendErrorToMonitoring(error)
}

/**
 * 安装全局错误处理器
 * @param {Object} app - Vue 应用实例
 */
export function setupErrorHandler(app) {
  app.config.errorHandler = (err) => {
    globalErrorHandler(err)
  }

  // 捕获未处理的 Promise 拒绝
  window.addEventListener('unhandledrejection', (event) => {
    if (process.env.NODE_ENV === 'development') {
      console.error('[Unhandled Rejection]:', event.reason)
    }
    ElMessage.error('发生系统错误')
  })

  // 捕获全局错误
  window.addEventListener('error', (event) => {
    // 忽略 ResizeObserver 警告（这是无害的浏览器警告）
    if (event.message && event.message.includes('ResizeObserver loop')) {
      event.preventDefault()
      return
    }

    if (process.env.NODE_ENV === 'development') {
      console.error('[Global Error]:', {
        message: event.message,
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno,
        error: event.error,
        stack: event.error?.stack
      })
    }
    // 只在非 ResizeObserver 错误时显示消息
    ElMessage.error('发生系统错误')
    event.preventDefault()
  })
}