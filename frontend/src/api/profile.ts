import { get, put, post, upload } from './request'
import type { Profile } from '@/types'

// 获取个人资料
export function getProfile() {
  return get<Profile>('/profile')
}

// 更新个人资料
export function updateProfile(data: Partial<Profile>) {
  return put<Profile>('/profile', data)
}

// 修改密码
export function changePassword(oldPassword: string, newPassword: string) {
  return post<void>('/profile/password', { oldPassword, newPassword })
}

// 上传头像
export function uploadAvatar(file: File) {
  return upload<{ avatarUrl: string }>('/profile/avatar', file)
}

// 用户设置
export interface Settings {
  theme: 'light' | 'dark' | 'auto'
  language: string
  notification: boolean
  emailNotification: boolean
}

// 获取设置
export function getSettings() {
  return get<Settings>('/profile/settings')
}

// 更新设置
export function updateSettings(data: Partial<Settings>) {
  return put<Settings>('/profile/settings', data)
}