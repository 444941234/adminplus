import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { login } from '@/api/auth'

// 全局登录对话框控制
let loginDialogVisible = false
let loginDialogResolve = null
let loginDialogReject = null
let pendingRequest = null // 保存当前失败的请求

// 显示登录对话框的方法
export const showLoginDialog = () => {
  return new Promise((resolve, reject) => {
    loginDialogVisible = true
    loginDialogResolve = resolve
    loginDialogReject = reject
    // 触发自定义事件通知组件显示
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
  })
}

// 关闭登录对话框的方法
export const hideLoginDialog = () => {
  loginDialogVisible = false
  loginDialogResolve = null
  loginDialogReject = null
  pendingRequest = null
  // 触发自定义事件通知组件隐藏
  window.dispatchEvent(new CustomEvent('hide-login-dialog'))
}

// 处理登录成功
export const handleLoginSuccess = (token, user, permissions) => {
  sessionStorage.setItem('token', token)
  sessionStorage.setItem('user', JSON.stringify(user))
  if (permissions) {
    sessionStorage.setItem('permissions', JSON.stringify(permissions))
  }
  if (loginDialogResolve) {
    loginDialogResolve({ token, user, permissions })
  }
  hideLoginDialog()
}

// 处理登录失败
export const handleLoginError = (error) => {
  if (loginDialogReject) {
    loginDialogReject(error)
  }
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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

    if (code === 200) {
      return data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  async error => {
    if (error.response) {
      const { status } = error.response

      if (status === 401) {
        // 如果已经显示登录对话框，不再重复显示
        if (!loginDialogVisible) {
          try {
            // 保存原始请求配置，用于登录成功后重试
            const originalConfig = error.config
            
            // 显示登录对话框，等待用户登录
            await showLoginDialog()
            
            // 登录成功后，重试原始请求
            if (originalConfig) {
              return request(originalConfig)
            }
          } catch (err) {
            // 用户取消登录或登录失败，跳转到登录页
            sessionStorage.removeItem('token')
            sessionStorage.removeItem('user')
            sessionStorage.removeItem('permissions')
            router.push('/login')
          }
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