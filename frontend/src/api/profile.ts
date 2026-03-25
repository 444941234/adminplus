import { get, put, post, upload } from '@/utils/request'
import type { Profile, ActivityStats, UserSettings } from '@/types'

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

// 获取活动统计
export function getActivityStats() {
  return get<ActivityStats>('/profile/activity')
}

// 获取用户设置
export function getUserSettings() {
  return get<UserSettings>('/profile/settings')
}

// 更新用户设置
export function updateUserSettings(data: Partial<UserSettings>) {
  return put<UserSettings>('/profile/settings', data)
}