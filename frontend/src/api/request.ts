import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types'

const instance: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    // 直接返回 response.data (ApiResponse)
    const data = response.data as ApiResponse<unknown>
    // 如果 code 不是 200，视为业务错误
    if (data.code && data.code !== 200) {
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return response.data
  },
  (error) => {
    console.error('请求错误:', error)
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      window.location.href = '/login'
    }
    // 返回更详细的错误信息
    const message = error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

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
