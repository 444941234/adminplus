// 通用 API 响应类型
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  timestamp?: number
}

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 用户
export interface User {
  id: string
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  status: number
  deptId: string
  deptName: string
  roles: string[]
  createTime: string
  updateTime: string
}

// 角色
export interface Role {
  id: string
  name: string
  code: string
  description: string
  status: number
  sortOrder: number
  dataScope: number
  createTime: string
  updateTime?: string
}

// 菜单
export interface Menu {
  id: string
  parentId: string
  type: number        // 0=目录, 1=菜单, 2=按钮
  name: string        // 菜单名称
  path: string
  component: string
  permKey: string     // 权限标识
  icon: string
  sortOrder: number   // 排序
  visible: number
  status: number
  children?: Menu[]
  createTime?: string
  updateTime?: string
}

// 部门
export interface Dept {
  id: string
  name: string
  code: string
  parentId: string
  leader: string
  phone: string
  email: string
  status: number
  sortOrder: number
  createTime?: string
  updateTime?: string
  children?: Dept[]
}

// 字典
export interface Dict {
  id: string
  dictName: string
  dictType: string
  description: string
  remark?: string
  status: number
  createTime: string
  updateTime?: string
}

// 字典项
export interface DictItem {
  id: string
  dictId: string
  dictType?: string
  parentId?: string
  label: string
  value: string
  sortOrder: number
  status: number
  remark?: string
  children?: DictItem[]
  createTime?: string
  updateTime?: string
}

// 日志
export interface Log {
  id: string
  logType: number
  username: string
  module: string
  operationType: number
  description: string
  requestMethod?: string
  method?: string
  requestUrl?: string
  requestParams?: string
  params?: string
  responseStatus: number
  ip: string
  location?: string
  status: number
  errorMsg?: string
  duration?: number
  costTime?: number
  createTime: string
}

export interface FileRecord {
  id: string
  originalName: string
  fileName: string
  fileExt?: string
  fileSize: number
  contentType?: string
  fileUrl: string
  storageType: string
  directory?: string
  status: number
  createTime?: string
  updateTime?: string
}

// 登录响应
export interface LoginResp {
  token: string
  refreshToken: string
  tokenType: string
  user: User
  permissions: string[]
}

// 验证码响应
export interface CaptchaResp {
  captchaId: string
  captchaImage: string
}

// Dashboard 统计
export interface DashboardStats {
  userCount: number
  roleCount: number
  menuCount: number
  logCount: number
  onlineUserCount: number
}

// 图表数据
export interface ChartData {
  labels: string[]
  values: Array<number | string>
}

// 操作日志
export interface OperationLog {
  id: string
  username: string
  module: string
  operationType: number
  description: string
  ip: string
  status: number
  createTime: string
}

// 个人资料
export interface Profile {
  id: string
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  deptName: string
  roles: string[]
}

// Activity statistics for profile dashboard
export interface ActivityStats {
  daysActive: number
  totalActions: number
  lastLogin: string
  lastLoginIp: string
  recentActivity: ActivityItem[]
}

// Single activity item
export interface ActivityItem {
  id: string
  action: string
  timestamp: string
  type: 'update' | 'create' | 'delete' | 'login'
}

// User preferences and settings
export interface UserSettings {
  notifications: boolean
  darkMode: boolean
  emailUpdates: boolean
  language: string
}

// 日志统计
export interface LogStatistics {
  totalCount: number
  loginCount: number
  operationCount: number
  todayCount: number
  successRate: number
}

export interface StatisticsData {
  totalUsers: number
  todayVisits: number
  activeUsers: number
  todayNewUsers: number
  userGrowthData: ChartData
  visitTrendData: ChartData
}

export interface OnlineUser {
  userId: string
  username: string
  loginTime: string
  ip: string
  browser?: string
  os?: string
}

export interface SystemInfo {
  systemName?: string
  systemVersion?: string
  osName: string
  osVersion?: string
  javaVersion?: string
  jdkVersion?: string
  jvmMemory?: string
  cpuUsage?: number
  memoryUsage?: number
  diskUsage?: number
  totalMemory?: number
  usedMemory?: number
  freeMemory?: number
  databaseType?: string
  databaseVersion?: string
  databaseConnections?: number
  uptime?: number
}

export interface WorkflowDefinition {
  id: string
  definitionName: string
  definitionKey: string
  category: string
  description: string
  status: number
  version: number
  formConfig: string | WorkflowFormConfig
  nodeCount?: number
  createTime: string
  updateTime: string
}

export interface FormTemplate {
  id: string
  templateName: string
  templateCode: string
  category: string
  description: string
  formConfig: string
  status: number
  createTime: string
  updateTime: string
}

export interface WorkflowInstance {
  id: string
  definitionId: string
  definitionName: string
  userId: string
  userName: string
  deptId: string
  deptName: string | null
  title: string
  businessData: string
  currentNodeId: string
  currentNodeName: string
  status: string
  submitTime: string | null
  finishTime: string | null
  remark: string
  createTime: string
  pendingApproval: boolean
  canApprove: boolean
  canWithdraw: boolean
  canCancel: boolean
  canUrge: boolean
  canEditDraft: boolean
  canSubmitDraft: boolean
}

export interface WorkflowNode {
  id: string
  definitionId: string
  nodeName: string
  nodeCode: string
  nodeOrder: number
  approverType: string
  approverId: string
  isCounterSign: boolean
  autoPassSameUser: boolean
  description: string
  createTime: string
  // 钩子字段（简单钩子 - SpEL表达式）
  preSubmitValidate?: string
  preApproveValidate?: string
  preRejectValidate?: string
  preRollbackValidate?: string
  preCancelValidate?: string
  preWithdrawValidate?: string
  preAddSignValidate?: string
  postSubmitAction?: string
  postApproveAction?: string
  postRejectAction?: string
  postRollbackAction?: string
  postCancelAction?: string
  postWithdrawAction?: string
  postAddSignAction?: string
}

// 工作流钩子
export interface WorkflowNodeHook {
  id: string
  nodeId: string
  hookPoint: string
  hookType: 'validate' | 'execute'
  executorType: 'spel' | 'bean' | 'http'
  executorConfig: string
  asyncExecution: boolean
  blockOnFailure: boolean
  failureMessage?: string
  priority: number
  conditionExpression?: string
  retryCount: number
  retryInterval: number
  hookName?: string
  description?: string
  createTime: string
  updateTime: string
}

// 工作流钩子日志
export interface WorkflowHookLog {
  id: string
  instanceId: string
  nodeId?: string
  hookId?: string
  hookSource: 'node_field' | 'hook_table'
  hookPoint: string
  executorType: string
  executorConfig: string
  success: boolean
  resultCode: string
  resultMessage: string
  executionTime?: number
  retryAttempts?: number
  async: boolean
  operatorId?: string
  operatorName?: string
  createTime: string
}

export interface WorkflowDefinitionReq {
  definitionName: string
  definitionKey: string
  category?: string
  description?: string
  status: number
  formConfig?: string | WorkflowFormConfig
}

export interface WorkflowNodeReq {
  nodeName: string
  nodeCode: string
  nodeOrder: number
  approverType: 'user' | 'role' | 'dept' | 'leader'
  approverId?: string
  isCounterSign: boolean
  autoPassSameUser: boolean
  description?: string
}

export interface WorkflowApproval {
  id: string
  instanceId: string
  nodeId: string
  nodeName: string
  approverId: string
  approverName: string
  approvalStatus: string
  comment: string | null
  attachments: string | null
  approvalTime: string | null
  createTime: string
}

export interface WorkflowCc {
  id: string
  instanceId: string
  nodeId: string
  nodeName: string
  userId: string
  userName: string
  ccType: string
  ccContent: string
  isRead: boolean
  readTime: string | null
  createTime: string
}

export interface WorkflowUrge {
  id: string
  instanceId: string
  nodeId: string
  nodeName: string
  urgeUserId: string
  urgeUserName: string
  urgeTargetId: string
  urgeTargetName: string
  urgeContent: string
  isRead: boolean
  readTime: string | null
  createTime: string
}

export interface WorkflowAddSign {
  id: string
  instanceId: string
  nodeId: string
  nodeName: string
  initiatorId: string
  initiatorName: string
  addUserId: string
  addUserName: string
  addType: string
  addReason: string
  originalApproverId?: string
  createTime: string
}

export interface WorkflowDetail {
  instance: WorkflowInstance
  approvals: WorkflowApproval[]
  nodes: WorkflowNode[]
  currentNode: WorkflowNode | null
  canApprove: boolean
  formConfig?: string | WorkflowFormConfig
  formData?: WorkflowFormValues
  ccRecords?: WorkflowCc[]
  addSignRecords?: WorkflowAddSign[]
}

export interface WorkflowFormOption {
  label: string
  value: string | number
}

export type WorkflowFormFieldComponent =
  | 'input'
  | 'textarea'
  | 'number'
  | 'select'
  | 'date'
  | 'daterange'
  | 'user'
  | 'dept'
  | 'file'

export interface WorkflowFormFieldRule {
  min?: number
  max?: number
  pattern?: string
}

export interface WorkflowFormField {
  field: string
  label: string
  component: WorkflowFormFieldComponent
  required?: boolean
  readonly?: boolean
  placeholder?: string
  defaultValue?: unknown
  options?: WorkflowFormOption[]
  rules?: WorkflowFormFieldRule
  description?: string
}

export interface WorkflowFormSection {
  key: string
  title: string
  fields: WorkflowFormField[]
}

export interface WorkflowFormConfig {
  sections: WorkflowFormSection[]
}

export type WorkflowFormValues = Record<string, unknown>

export interface WorkflowDraftDetail {
  instance: WorkflowInstance
  formConfig: string | WorkflowFormConfig
  formData: WorkflowFormValues
}

// 配置分组
export interface ConfigGroup {
  id: string
  name: string
  code: string
  icon: string
  sortOrder: number
  description: string
  status: number
  configCount: number
  createTime: string
  updateTime: string
}

// 配置项
export interface Config {
  id: string
  groupId: string
  groupName: string
  name: string
  key: string
  value: string
  valueType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON' | 'ARRAY' | 'SECRET' | 'FILE'
  effectType: 'IMMEDIATE' | 'MANUAL' | 'RESTART'
  defaultValue: string
  description: string
  isRequired: boolean
  validationRule: string
  sortOrder: number
  status: number
  createTime: string
  updateTime: string
}

// 配置历史
export interface ConfigHistory {
  id: string
  configId: string
  configKey: string
  oldValue: string
  newValue: string
  remark: string
  operatorName: string
  createTime: string
}

// 配置导出
export interface ConfigExport {
  exportVersion: string
  exportTime: string
  groups: ConfigExportGroup[]
}

export interface ConfigExportGroup {
  code: string
  name: string
  icon: string
  configs: ConfigExportItem[]
}

export interface ConfigExportItem {
  key: string
  name: string
  value: string
  valueType: string
  effectType: string
  description: string
}

// 配置导入结果
export interface ConfigImportResult {
  total: number
  success: number
  skipped: number
  failed: number
  details: ConfigImportDetail[]
}

export interface ConfigImportDetail {
  key: string
  status: 'success' | 'skipped' | 'failed'
  reason?: string
}

// 配置生效信息
export interface ConfigEffectInfo {
  pendingEffects: ConfigPendingEffect[]
  restartRequiredConfigs: string[]
}

export interface ConfigPendingEffect {
  key: string
  name: string
  newValue: string
  effectType: string
  updateTime: string
}
