import { get, post, put, del } from '@/utils/request'
import type { User, PageResult } from '@/types'

// 获取用户列表
export function getUserList(params: {
  page?: number
  size?: number
  keyword?: string
  deptId?: string
}) {
  return get<PageResult<User>>('/sys/users', { params })
}

// 获取用户详情
export function getUserById(id: string) {
  return get<User>(`/sys/users/${id}`)
}

// 创建用户
export function createUser(data: Partial<User> & { password: string }) {
  return post<User>('/sys/users', data)
}

// 更新用户
export function updateUser(id: string, data: Partial<User>) {
  return put<User>(`/sys/users/${id}`, data)
}

// 删除用户
export function deleteUser(id: string) {
  return del<void>(`/sys/users/${id}`)
}

// 更新用户状态
export function updateUserStatus(id: string, status: number) {
  return put<void>(`/sys/users/${id}/status?status=${status}`)
}

// 重置密码
export function resetPassword(id: string, password: string) {
  return put<void>(`/sys/users/${id}/password`, { password })
}

// 分配角色
export function assignRoles(id: string, roleIds: string[]) {
  return put<void>(`/sys/users/${id}/roles`, { roleIds })
}

// 获取用户角色
export function getUserRoles(id: string) {
  return get<string[]>(`/sys/users/${id}/roles`)
}