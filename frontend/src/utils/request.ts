import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types'
import { setupRequestInterceptor, setupResponseInterceptor, clearPendingRequests } from '@/composables/useApiInterceptors'

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL + import.meta.env.VITE_API_VERSION,
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

// GET 请求
export function get<T>(url: string, params?: Record<string, unknown>): Promise<ApiResponse<T>> {
  return request<ApiResponse<T>>({ method: 'GET', url, params })
}

// POST 请求
export function post<T>(url: string, data?: unknown): Promise<ApiResponse<T>> {
  return request<ApiResponse<T>>({ method: 'POST', url, data })
}

// PUT 请求
export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request<ApiResponse<T>>({ method: 'PUT', url, data, ...config })
}

// DELETE 请求
export function del<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request<ApiResponse<T>>({ method: 'DELETE', url, data, ...config })
}

// 文件上传
export function upload<T>(url: string, file: File, fieldName = 'file'): Promise<ApiResponse<T>> {
  const formData = new FormData()
  formData.append(fieldName, file)
  return request<ApiResponse<T>>({
    method: 'POST',
    url,
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export default instance
