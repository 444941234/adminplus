import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types'
import { setupRequestInterceptor, setupResponseInterceptor, clearPendingRequests } from '@/composables/useApiInterceptors'

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // 只包含 /api，版本号在请求中指定
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
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

// 封装请求方法
async function request<T>(config: AxiosRequestConfig): Promise<T> {
  return instance.request<unknown, T>(config)
}

// 默认 API 版本（可通过环境变量配置）
const DEFAULT_VERSION = import.meta.env.VITE_API_VERSION || '/v1'

// GET 请求
export function get<T>(url: string, params?: Record<string, unknown>, version?: string): Promise<ApiResponse<T>> {
  const fullUrl = `${version || DEFAULT_VERSION}${url}`
  return request<ApiResponse<T>>({ method: 'GET', url: fullUrl, params })
}

// POST 请求
export function post<T>(url: string, data?: unknown, version?: string): Promise<ApiResponse<T>> {
  const fullUrl = `${version || DEFAULT_VERSION}${url}`
  return request<ApiResponse<T>>({ method: 'POST', url: fullUrl, data })
}

// PUT 请求
export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig, version?: string): Promise<ApiResponse<T>> {
  const fullUrl = `${version || DEFAULT_VERSION}${url}`
  return request<ApiResponse<T>>({ method: 'PUT', url: fullUrl, data, ...config })
}

// DELETE 请求
export function del<T>(url: string, data?: unknown, config?: AxiosRequestConfig, version?: string): Promise<ApiResponse<T>> {
  const fullUrl = `${version || DEFAULT_VERSION}${url}`
  return request<ApiResponse<T>>({ method: 'DELETE', url: fullUrl, data, ...config })
}

// 文件上传
export function upload<T>(url: string, file: File, fieldName = 'file', version?: string): Promise<ApiResponse<T>> {
  const fullUrl = `${version || DEFAULT_VERSION}${url}`
  const formData = new FormData()
  formData.append(fieldName, file)
  return request<ApiResponse<T>>({
    method: 'POST',
    url: fullUrl,
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 带认证的文件下载（返回 Blob）
// 注意：静态资源路径（如 /uploads/**）不需要 API 版本前缀
export async function downloadBlob(url: string): Promise<Blob> {
  const baseUrl = import.meta.env.VITE_API_BASE_URL

  // 如果 URL 已经是完整路径或以 /v 开头（带版本），直接使用
  const fullUrl = url.startsWith('http') || url.startsWith('/v')
    ? url
    : baseUrl + url

  const token = localStorage.getItem('token')
  const response = await fetch(fullUrl, {
    method: 'GET',
    headers: {
      Authorization: token ? `Bearer ${token}` : ''
    }
  })

  if (!response.ok) {
    throw new Error(`下载失败: ${response.status}`)
  }

  return response.blob()
}

export default instance
