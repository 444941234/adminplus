import axios from 'axios'
import { ElMessage } from '@/utils/elementCompat'
import router from '@/router'
import { getEncryptedSession, setEncryptedSession } from '@/utils/encryption'

// Token 刷新机制
let isRefreshing = false
let refreshSubscribers = []

/**
 * 订阅 token 刷新事件
 * @param {Function} callback - token 刷新成功后的回调
 */
const subscribeTokenRefresh = (callback) => {
  refreshSubscribers.push(callback)
}

/**
 * 通知所有订阅者 token 已刷新
 * @param {string} newToken - 新的 token
 */
const onRefreshed = (newToken) => {
  refreshSubscribers.forEach(callback => callback(newToken))
  refreshSubscribers = []
}

/**
 * 获取 CSRF Token
 * @returns {Promise<string>} CSRF Token
 */
export const getCsrfToken = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL || '/api'}/csrf-token`, {
      withCredentials: true
    })
    const csrfToken = response.headers['x-csrf-token']
    if (csrfToken) {
      sessionStorage.setItem('csrfToken', csrfToken)
    }
    return csrfToken
  } catch (error) {
    console.error('[CSRF] 获取 CSRF Token 失败:', error)
    throw error
  }
}

/**
 * 刷新 token
 * @returns {Promise<string>} 新的 token
 */
const refreshToken = async () => {
  try {
    // 使用加密存储获取 refresh token
    const refreshTokenValue = await getEncryptedSession('refreshToken', null)
    if (!refreshTokenValue) {
      throw new Error('No refresh token available')
    }

    const response = await axios.post(
      `${import.meta.env.VITE_API_BASE_URL || '/api'}/auth/refresh`,
      { refreshToken: refreshTokenValue }
    )

    const { token, refreshToken: newRefreshToken } = response.data

    // 使用加密存储更新 sessionStorage
    await setEncryptedSession('token', token)
    if (newRefreshToken) {
      await setEncryptedSession('refreshToken', newRefreshToken)
    }

    return token
  } catch (error) {
    // 刷新失败，清除所有 token
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('refreshToken')
    throw error
  }
}

/**
 * 清除用户信息并跳转到登录页
 */
const clearUserInfoAndRedirect = () => {
  // 清除所有加密的 sessionStorage 数据
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('refreshToken')
  sessionStorage.removeItem('user')
  sessionStorage.removeItem('permissions')
  router.push('/login')
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  async config => {
    // 使用加密存储获取 token
    try {
      const token = await getEncryptedSession('token', '')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    } catch (error) {
      console.error('[Request] 获取 token 失败:', error)
    }

    // CSRF 防护：只在写操作（POST、PUT、DELETE、PATCH）时添加 CSRF Token
    const csrfToken = sessionStorage.getItem('csrfToken')
    if (csrfToken && ['post', 'put', 'delete', 'patch'].includes(config.method?.toLowerCase())) {
      config.headers['X-CSRF-TOKEN'] = csrfToken
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data

    // CSRF 防护：从响应头获取新的 CSRF token 并存储
    const csrfToken = response.headers['x-csrf-token']
    if (csrfToken) {
      sessionStorage.setItem('csrfToken', csrfToken)
    }

    if (code === 200) {
      return data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  async error => {
    if (error.response) {
      const { status, data } = error.response

      if (status === 401) {
        const originalConfig = error.config

        // 如果正在刷新 token，将请求加入队列
        if (isRefreshing) {
          return new Promise((resolve) => {
            subscribeTokenRefresh((newToken) => {
              originalConfig.headers.Authorization = `Bearer ${newToken}`
              resolve(request(originalConfig))
            })
          })
        }

        // 尝试刷新 token
        isRefreshing = true

        try {
          const newToken = await refreshToken()
          isRefreshing = false
          onRefreshed(newToken)

          // 重试原始请求
          originalConfig.headers.Authorization = `Bearer ${newToken}`
          return request(originalConfig)
        } catch {
          isRefreshing = false
          refreshSubscribers = []

          // 刷新失败，清除用户信息并跳转到登录页
          clearUserInfoAndRedirect()
        }
      } else if (status === 403) {
        ElMessage.error('无权访问')
      } else if (status === 500) {
        ElMessage.error('服务器错误')
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误')
    }

    return Promise.reject(error)
  }
)

export default request