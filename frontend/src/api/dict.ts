import { get, post, put, del } from '@/utils/request'
import type { Dict, DictItem, PageResult } from '@/types'

// 获取字典列表
export function getDictList(params: { page?: number; size?: number; keyword?: string }) {
  return get<PageResult<Dict>>('/sys/dicts', { params })
}

// 根据类型获取字典
export function getDictByType(dictType: string) {
  return get<Dict>(`/sys/dicts/type/${dictType}`)
}

// 获取字典详情
export function getDictById(id: string) {
  return get<Dict>(`/sys/dicts/${id}`)
}

// 创建字典
export function createDict(data: Partial<Dict>) {
  return post<Dict>('/sys/dicts', data)
}

// 更新字典
export function updateDict(id: string, data: Partial<Dict>) {
  return put<Dict>(`/sys/dicts/${id}`, data)
}

// 删除字典
export function deleteDict(id: string) {
  return del<void>(`/sys/dicts/${id}`)
}

// 获取字典项
export function getDictItems(dictType: string) {
  return get<DictItem[]>(`/sys/dicts/type/${dictType}/items`)
}

// 更新字典状态
export function updateDictStatus(id: string, status: number) {
  return put<void>(`/sys/dicts/${id}/status?status=${status}`)
}

// 获取字典项列表
export function getDictItemList(dictId: string) {
  return get<DictItem[]>(`/sys/dicts/${dictId}/items`)
}

// 获取字典项详情
export function getDictItemById(dictId: string, id: string) {
  return get<DictItem>(`/sys/dicts/${dictId}/items/${id}`)
}

// 创建字典项
export function createDictItem(dictId: string, data: Partial<DictItem>) {
  return post<DictItem>(`/sys/dicts/${dictId}/items`, { ...data, dictId })
}

// 更新字典项
export function updateDictItem(dictId: string, id: string, data: Partial<DictItem>) {
  return put<DictItem>(`/sys/dicts/${dictId}/items/${id}`, data)
}

// 删除字典项
export function deleteDictItem(dictId: string, id: string) {
  return del<void>(`/sys/dicts/${dictId}/items/${id}`)
}

// 更新字典项状态
export function updateDictItemStatus(dictId: string, id: string, status: number) {
  return put<void>(`/sys/dicts/${dictId}/items/${id}/status?status=${status}`)
}
