import { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosError } from 'axios'
import { toast } from 'vue-sonner'

// 请求去重 Map
const pendingRequests = new Map<string, AbortController>()

/**
 * 判断是否是被取消的请求（请求去重的正常行为）
 */
export function isCanceledError(error: unknown): boolean {
  if (!error) return false
  if (error instanceof Error) {
    return error.message === 'canceled' || error.name === 'CanceledError'
  }
  // Axios 错误
  if (typeof error === 'object' && error !== null && 'code' in error) {
    return (error as { code?: string }).code === 'ERR_CANCELED'
  }
  return false
}

// 是否正在刷新 Token
let isRefreshing = false
// 等待刷新完成的请求队列
const refreshSubscribers: Array<(token: string) => void> = []

/**
 * 生成请求唯一标识
 */
function generateRequestKey(config: InternalAxiosRequestConfig): string {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

/**
 * 添加请求到待处理队列
 */
function addPendingRequest(config: InternalAxiosRequestConfig): void {
  const key = generateRequestKey(config)
  const controller = new AbortController()
  config.signal = controller.signal
  pendingRequests.set(key, controller)
}

/**
 * 移除请求从待处理队列
 */
function removePendingRequest(config: InternalAxiosRequestConfig): void {
  const key = generateRequestKey(config)
  const controller = pendingRequests.get(key)
  if (controller) {
    controller.abort()
    pendingRequests.delete(key)
  }
}

/**
 * 订阅 Token 刷新事件
 */
function subscribeTokenRefresh(callback: (token: string) => void): void {
  refreshSubscribers.push(callback)
}

/**
 * 通知所有订阅者 Token 已刷新
 */
function onTokenRefreshed(token: string): void {
  refreshSubscribers.forEach(callback => callback(token))
  refreshSubscribers.length = 0
}

/**
 * 刷新访问令牌
 */
async function refreshAccessToken(): Promise<string> {
  const refreshToken = localStorage.getItem('refreshToken')
  if (!refreshToken) {
    throw new Error('No refresh token available')
  }

  const response = await fetch(
    import.meta.env.VITE_API_BASE_URL + import.meta.env.VITE_API_VERSION + '/auth/refresh',
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    }
  )

  if (!response.ok) {
    throw new Error('Token refresh failed')
  }

  const data = await response.json()
  const newToken = data.data.accessToken

  localStorage.setItem('token', newToken)
  return newToken
}

/**
 * 请求拦截器配置
 */
export interface RequestInterceptorOptions {
  /** 是否启用请求去重 */
  enableDeduplication?: boolean
}

/**
 * 响应拦截器配置
 */
export interface ResponseInterceptorOptions {
  /** 是否启用自动 Token 刷新 */
  enableTokenRefresh?: boolean
  /** 是否启用重试 */
  enableRetry?: boolean
  /** 最大重试次数 */
  maxRetries?: number
  /** 重试延迟 (ms) */
  retryDelay?: number
}

/**
 * 配置请求拦截器
 */
export function setupRequestInterceptor(
  instance: AxiosInstance,
  options: RequestInterceptorOptions = {}
): void {
  const { enableDeduplication = true } = options

  instance.interceptors.request.use(
    (config) => {
      // 请求去重
      if (enableDeduplication) {
        removePendingRequest(config)
        addPendingRequest(config)
      }

      // 添加 Token
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }

      return config
    },
    (error) => Promise.reject(error)
  )
}

/**
 * 配置响应拦截器
 */
export function setupResponseInterceptor(
  instance: AxiosInstance,
  options: ResponseInterceptorOptions = {}
): void {
  const {
    enableTokenRefresh = true,
    enableRetry = true,
    maxRetries = 2,
    retryDelay = 1000
  } = options

  instance.interceptors.response.use(
    (response) => {
      // 移除待处理请求
      const config = response.config
      if (config) {
        const key = generateRequestKey(config)
        pendingRequests.delete(key)
      }

      // 业务错误处理
      const data = response.data
      if (data.code && data.code !== 200) {
        return Promise.reject(new Error(data.message || '请求失败'))
      }

      return response.data
    },
    async (error: AxiosError) => {
      const config = error.config as InternalAxiosRequestConfig & { _retry?: boolean; _retryCount?: number }

      // 移除待处理请求
      if (config) {
        const key = generateRequestKey(config)
        pendingRequests.delete(key)
      }

      // Token 过期处理
      if (error.response?.status === 401 && enableTokenRefresh && config) {
        // 如果已重试过，直接跳转登录
        if (config._retry) {
          handleAuthError()
          return Promise.reject(error)
        }

        // 标记正在重试
        config._retry = true

        if (isRefreshing) {
          // 等待刷新完成
          return new Promise((resolve) => {
            subscribeTokenRefresh((token: string) => {
              config.headers.Authorization = `Bearer ${token}`
              resolve(instance(config))
            })
          })
        }

        isRefreshing = true

        try {
          const newToken = await refreshAccessToken()
          onTokenRefreshed(newToken)
          config.headers.Authorization = `Bearer ${newToken}`
          return instance(config)
        } catch (refreshError) {
          handleAuthError()
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      }

      // 重试机制
      if (enableRetry && shouldRetry(error) && config) {
        config._retryCount = config._retryCount || 0
        if (config._retryCount < maxRetries) {
          config._retryCount++
          await sleep(retryDelay)
          return instance(config)
        }
      }

      // 错误提示
      showErrorToast(error)

      return Promise.reject(error)
    }
  )
}

/**
 * 判断是否应该重试
 */
function shouldRetry(error: AxiosError): boolean {
  const status = error.response?.status
  const retryableStatuses = [408, 429, 500, 502, 503, 504]
  return status !== undefined && retryableStatuses.includes(status)
}

/**
 * 延迟函数
 */
function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 处理认证错误
 */
function handleAuthError(): void {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  window.location.href = '/login'
}

/**
 * 显示错误提示（自动过滤取消请求）
 * 使用此函数替代 toast.error，避免显示 "canceled" 提示
 */
export function showErrorToast(error: unknown, fallbackMessage = '网络错误，请稍后重试'): void {
  // 被取消的请求不显示错误提示（这是正常的请求去重行为）
  if (isCanceledError(error)) {
    return
  }

  // Axios 错误处理
  if (error && typeof error === 'object' && 'response' in error) {
    const axiosError = error as AxiosError
    const message = (axiosError.response?.data as { message?: string })?.message
      || axiosError.message
      || fallbackMessage
    // 只显示用户友好的错误信息，401 由拦截器单独处理
    if (axiosError.response?.status !== 401) {
      toast.error(message)
    }
    return
  }

  // 通用错误处理
  const message = error instanceof Error ? error.message : fallbackMessage
  toast.error(message)
}

/**
 * 清空所有待处理请求
 */
export function clearPendingRequests(): void {
  pendingRequests.forEach(controller => controller.abort())
  pendingRequests.clear()
}
