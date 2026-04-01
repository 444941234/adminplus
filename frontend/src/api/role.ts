import { get, post, put, del } from '@/utils/request'
import type { Role } from '@/types'

// 获取角色列表
export function getRoleList() {
  return get<{ records: Role[]; total: number }>('/sys/roles')
}

// 获取角色详情
export function getRoleById(id: string) {
  return get<Role>(`/sys/roles/${id}`)
}

// 创建角色
export function createRole(data: Partial<Role>) {
  return post<Role>('/sys/roles', data)
}

// 更新角色
export function updateRole(id: string, data: Partial<Role>) {
  return put<Role>(`/sys/roles/${id}`, data)
}

// 删除角色
export function deleteRole(id: string) {
  return del<void>(`/sys/roles/${id}`)
}

// 分配菜单权限
export function assignMenus(id: string, menuIds: string[]) {
  return put<void>(`/sys/roles/${id}/menus`, menuIds)
}

// 获取角色菜单
export function getRoleMenus(id: string) {
  return get<string[]>(`/sys/roles/${id}/menus`)
}

// 更新角色状态
export function updateRoleStatus(id: string, status: number) {
  return put<void>(`/sys/roles/${id}/status?status=${status}`)
}