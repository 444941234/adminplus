import { get, post, put, del } from '@/utils/request'
import type {
  ConfigGroup,
  Config,
  ConfigHistory,
  ConfigExport,
  ConfigImportResult,
  ConfigEffectInfo,
  PageResult
} from '@/types'

// ==================== 配置分组 API ====================

export function getConfigGroupList(params: {
  page?: number
  size?: number
  keyword?: string
}) {
  return get<PageResult<ConfigGroup>>('/config-groups', params)
}

export function getAllConfigGroups() {
  return get<ConfigGroup[]>('/config-groups/all')
}

export function getConfigGroupById(id: string) {
  return get<ConfigGroup>(`/config-groups/${id}`)
}

export function getConfigGroupByCode(code: string) {
  return get<ConfigGroup>(`/config-groups/code/${code}`)
}

export function createConfigGroup(data: {
  name: string
  code: string
  icon?: string
  sortOrder?: number
  description?: string
}) {
  return post<ConfigGroup>('/config-groups', data)
}

export function updateConfigGroup(id: string, data: {
  name?: string
  icon?: string
  sortOrder?: number
  description?: string
}) {
  return put<ConfigGroup>(`/config-groups/${id}`, data)
}

export function deleteConfigGroup(id: string) {
  return del<void>(`/config-groups/${id}`)
}

export function updateConfigGroupStatus(id: string, status: number) {
  return put<void>(`/config-groups/${id}/status?status=${status}`)
}

// ==================== 配置项 API ====================

export function getConfigList(params: {
  page?: number
  size?: number
  groupId?: string
  keyword?: string
}) {
  return get<PageResult<Config>>('/configs', params)
}

export function getConfigsByGroupId(groupId: string) {
  return get<Config[]>(`/configs/group/${groupId}`)
}

export function getConfigById(id: string) {
  return get<Config>(`/configs/${id}`)
}

export function getConfigByKey(key: string) {
  return get<Config>(`/configs/key/${key}`)
}

export function createConfig(data: {
  groupId: string
  name: string
  key: string
  value?: string
  valueType: string
  effectType?: string
  defaultValue?: string
  description?: string
  isRequired?: boolean
  validationRule?: string
  sortOrder?: number
}) {
  return post<Config>('/configs', data)
}

export function updateConfig(id: string, data: {
  name?: string
  value?: string
  valueType?: string
  effectType?: string
  defaultValue?: string
  description?: string
  isRequired?: boolean
  validationRule?: string
  sortOrder?: number
  status?: number
}) {
  return put<Config>(`/configs/${id}`, data)
}

export function batchUpdateConfigs(items: Array<{ id: string; value: string }>) {
  return put<void>('/configs/batch', { items })
}

export function deleteConfig(id: string) {
  return del<void>(`/configs/${id}`)
}

export function updateConfigStatus(id: string, status: number) {
  return put<void>(`/configs/${id}/status?status=${status}`)
}

export function getConfigHistory(configId: string) {
  return get<ConfigHistory[]>(`/configs/${configId}/history`)
}

export function rollbackConfig(configId: string, data: { historyId: string; remark?: string }) {
  return post<void>(`/configs/${configId}/rollback`, data)
}

export function exportConfigs(params: { groupId?: string; format?: string }) {
  return get<ConfigExport>('/configs/export', params)
}

export function importConfigs(data: {
  content: string
  format: 'JSON' | 'YAML'
  mode?: 'OVERWRITE' | 'MERGE' | 'VALIDATE'
}) {
  return post<ConfigImportResult>('/configs/import', data)
}

export function refreshConfigCache() {
  return post<void>('/configs/refresh-cache', {})
}

export function getEffectInfo() {
  return get<ConfigEffectInfo>('/configs/effect-info')
}
