import { get, post, put, del } from './request'
import type { Menu } from '@/types'

// 获取菜单树
export function getMenuTree() {
  return get<Menu[]>('/sys/menus/tree')
}

// 获取当前用户菜单树
export function getUserMenuTree() {
  return get<Menu[]>('/sys/menus/user/tree')
}

// 获取菜单详情
export function getMenuById(id: string) {
  return get<Menu>(`/sys/menus/${id}`)
}

// 创建菜单
export function createMenu(data: Partial<Menu>) {
  return post<Menu>('/sys/menus', data)
}

// 更新菜单
export function updateMenu(id: string, data: Partial<Menu>) {
  return put<Menu>(`/sys/menus/${id}`, data)
}

// 删除菜单
export function deleteMenu(id: string) {
  return del<void>(`/sys/menus/${id}`)
}

// 批量更新状态
export function batchUpdateStatus(ids: string[], status: number) {
  return put<void>('/sys/menus/batch/status', { ids, status })
}

// 批量删除
export function batchDelete(ids: string[]) {
  return del<void>('/sys/menus/batch', { ids })
}
