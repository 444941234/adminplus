import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types'
import { setupRequestInterceptor, setupResponseInterceptor, clearPendingRequests } from '@/composables/useApiInterceptors'

// 默认 API 版本（通过请求头传递）
const DEFAULT_VERSION = import.meta.env.VITE_API_VERSION || '1.0'

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // /api
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
    'X-API-Version': DEFAULT_VERSION  // 默认版本
  }
})

// 配置请求拦截器（请求去复、自动添加 Token）
setupRequestInterceptor(instance, {
  enableDeduplication: true
})

// 配置响应拦截器（Token 刷新、重试、错误处理）
setupResponseInterceptor(instance, {
  enableTokenRefresh: true,
  enableRetry: true,
  maxRetries: 2,
  retryDelay: 1000
})

// 页面卸载时清空待处理请求
if (typeof window !== 'undefined') {
  window.addEventListener('beforeunload', clearPendingRequests)
}

// 请求选项类型
interface RequestOptions {
  params?: Record<string, unknown>
  version?: string
  config?: AxiosRequestConfig
}

// 封装请求方法（支持选项对象）
async function request<T>(method: string, url: string, options?: RequestOptions): Promise<T> {
  const config: AxiosRequestConfig = {
    method,
    url,
    params: options?.params,
    ...options?.config
  }

  // 如果指定了版本，覆盖默认版本
  if (options?.version) {
    config.headers = {
      ...config.headers,
      'X-API-Version': options.version
    }
  }

  return instance.request<unknown, T>(config)
}

/**
 * GET 请求
 * @param url 请求路径
 * @param options 请求选项（params、version、config）
 * @example
 * // 默认版本
 * get('/sys/users')
 * get('/sys/users', { params: { page: 1 } })
 * // 指定版本
 * get('/sys/users', { version: '2.0' })
 * get('/sys/users', { params: { page: 1 }, version: '2.0' })
 */
export function get<T>(url: string, options?: RequestOptions): Promise<ApiResponse<T>> {
  return request<ApiResponse<T>>('GET', url, options)
}

/**
 * POST 请求
 * @param url 请求路径
 * @param data 请求体
 * @param options 请求选项（version、config）
 * @example
 * // 默认版本
 * post('/auth/login', { username, password })
 * // 指定版本
 * post('/auth/login', { username, password }, { version: '2.0' })
 */
export function post<T>(url: string, data?: unknown, options?: RequestOptions): Promise<ApiResponse<T>> {
  return request<ApiResponse<T>>('POST', url, { ...options, config: { ...options?.config, data } })
}

/**
 * PUT 请求
 * @param url 请求路径
 * @param data 请求体
 * @param options 请求选项
 */
export function put<T>(url: string, data?: unknown, options?: RequestOptions): Promise<ApiResponse<T>> {
  return request<ApiResponse<T>>('PUT', url, { ...options, config: { ...options?.config, data } })
}

/**
 * DELETE 请求
 * @param url 请求路径
 * @param options 请求选项
 */
export function del<T>(url: string, options?: RequestOptions): Promise<ApiResponse<T>> {
  return request<ApiResponse<T>>('DELETE', url, options)
}

/**
 * 文件上传
 * @param url 请求路径
 * @param file 文件
 * @param options 请求选项
 */
export function upload<T>(url: string, file: File, options?: RequestOptions): Promise<ApiResponse<T>> {
  const fieldName = options?.config?.headers?.['fieldName'] as string || 'file'
  const formData = new FormData()
  formData.append(fieldName, file)

  return request<ApiResponse<T>>('POST', url, {
    ...options,
    config: {
      ...options?.config,
      data: formData,
      headers: {
        ...options?.config?.headers,
        'Content-Type': 'multipart/form-data'
      }
    }
  })
}

/**
 * 带认证的文件下载（返回 Blob）
 * @param url 请求路径
 * @param options 请求选项
 */
export async function downloadBlob(url: string, options?: RequestOptions): Promise<Blob> {
  const baseUrl = import.meta.env.VITE_API_BASE_URL
  const fullUrl = url.startsWith('http') ? url : baseUrl + url

  const token = localStorage.getItem('token')
  const apiVersion = options?.version || DEFAULT_VERSION

  const response = await fetch(fullUrl, {
    method: 'GET',
    headers: {
      Authorization: token ? `Bearer ${token}` : '',
      'X-API-Version': apiVersion
    }
  })

  if (!response.ok) {
    throw new Error(`下载失败: ${response.status}`)
  }

  return response.blob()
}

// 导出默认版本常量，供特殊场景使用
export { DEFAULT_VERSION }

export default instance
