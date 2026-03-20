import { get, post } from './request'
import type { LoginResp, User } from '@/types'

// 登录
export function login(username: string, password: string, captchaId: string, captchaCode: string) {
  return post<LoginResp>('/auth/login', { username, password, captchaId, captchaCode })
}

// 获取当前用户信息
export function getCurrentUser() {
  return get<User>('/auth/me')
}

// 获取当前用户权限
export function getPermissions() {
  return get<string[]>('/auth/permissions')
}

// 退出登录
export function logout() {
  return post<void>('/auth/logout')
}

// 刷新 Token
export function refreshToken(refreshToken: string) {
  return post<string>('/auth/refresh', { refreshToken })
}

// 验证码响应
export interface CaptchaResp {
  captchaId: string
  captchaImage: string
}

// 获取验证码
export function getCaptcha() {
  return get<CaptchaResp>('/captcha')
}