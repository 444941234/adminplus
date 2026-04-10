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
  return get<PageResult<ConfigGroup>>('/sys/config-groups', { params })
}

export function getAllConfigGroups() {
  return get<ConfigGroup[]>('/sys/config-groups/all')
}

export function getConfigGroupById(id: string) {
  return get<ConfigGroup>(`/sys/config-groups/${id}`)
}

export function getConfigGroupByCode(code: string) {
  return get<ConfigGroup>(`/sys/config-groups/code/${code}`)
}

export function createConfigGroup(data: {
  name: string
  code: string
  icon?: string
  sortOrder?: number
  description?: string
}) {
  return post<ConfigGroup>('/sys/config-groups', data)
}

export function updateConfigGroup(id: string, data: {
  name?: string
  icon?: string
  sortOrder?: number
  description?: string
}) {
  return put<ConfigGroup>(`/sys/config-groups/${id}`, data)
}

export function deleteConfigGroup(id: string) {
  return del<void>(`/sys/config-groups/${id}`)
}

export function updateConfigGroupStatus(id: string, status: number) {
  return put<void>(`/sys/config-groups/${id}/status?status=${status}`)
}

// ==================== 配置项 API ====================

export function getConfigList(params: {
  page?: number
  size?: number
  groupId?: string
  keyword?: string
}) {
  return get<PageResult<Config>>('/sys/configs', { params })
}

export function getConfigsByGroupId(groupId: string) {
  return get<Config[]>(`/sys/configs/group/${groupId}`)
}

export function getConfigsByGroupCode(groupCode: string) {
  return get<Config[]>(`/sys/configs/group-code/${groupCode}`)
}

export function getConfigById(id: string) {
  return get<Config>(`/sys/configs/${id}`)
}

export function getConfigByKey(key: string) {
  return get<Config>(`/sys/configs/key/${key}`)
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
  return post<Config>('/sys/configs', data)
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
  return put<Config>(`/sys/configs/${id}`, data)
}

export function batchUpdateConfigs(items: Array<{ id: string; value: string }>) {
  return put<void>('/sys/configs/batch', { items })
}

export function deleteConfig(id: string) {
  return del<void>(`/sys/configs/${id}`)
}

export function updateConfigStatus(id: string, status: number) {
  return put<void>(`/sys/configs/${id}/status?status=${status}`)
}

export function getConfigHistory(configId: string) {
  return get<ConfigHistory[]>(`/sys/configs/${configId}/history`)
}

export function rollbackConfig(configId: string, data: { historyId: string; remark?: string }) {
  return post<void>(`/sys/configs/${configId}/rollback`, data)
}

export function exportConfigs(params: { groupId?: string; format?: string }) {
  return get<ConfigExport>('/sys/configs/export', { params })
}

export function importConfigs(data: {
  content: string
  format: 'JSON' | 'YAML'
  mode?: 'OVERWRITE' | 'MERGE' | 'VALIDATE'
}) {
  return post<ConfigImportResult>('/sys/configs/import', data)
}

export function refreshConfigCache() {
  return post<void>('/sys/configs/refresh-cache', {})
}

export function getEffectInfo() {
  return get<ConfigEffectInfo>('/sys/configs/effect-info')
}
