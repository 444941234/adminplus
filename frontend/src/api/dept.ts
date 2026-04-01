import { get, post, put, del } from '@/utils/request'
import type { Dept } from '@/types'

// 获取部门树
export function getDeptTree() {
  return get<Dept[]>('/sys/depts/tree')
}

// 获取部门详情
export function getDeptById(id: string) {
  return get<Dept>(`/sys/depts/${id}`)
}

// 创建部门
export function createDept(data: Partial<Dept>) {
  return post<Dept>('/sys/depts', data)
}

// 更新部门
export function updateDept(id: string, data: Partial<Dept>) {
  return put<Dept>(`/sys/depts/${id}`, data)
}

// 删除部门
export function deleteDept(id: string) {
  return del<void>(`/sys/depts/${id}`)
}

// 更新部门状态
export function updateDeptStatus(id: string, status: number) {
  return put<void>(`/sys/depts/${id}/status?status=${status}`)
}