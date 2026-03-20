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
  roleName?: string
  roleCode?: string
  name?: string
  code?: string
  description: string
  status: number
  sort?: number
  sortOrder?: number
  dataScope?: number
  createTime: string
  updateTime?: string
}

// 菜单
export interface Menu {
  id: string
  menuName: string
  name?: string
  menuType: number
  type?: number
  parentId: string
  path: string
  component: string
  icon: string
  sort: number
  sortOrder?: number
  status: number
  visible: number
  permission: string
  permKey?: string
  children?: Menu[]
}

// 部门
export interface Dept {
  id: string
  deptName?: string
  name?: string
  deptCode?: string
  code?: string
  parentId: string
  leader: string
  phone: string
  email: string
  status: number
  sort?: number
  sortOrder?: number
  createTime?: string
  updateTime?: string
  children?: Dept[]
}

// 字典
export interface Dict {
  id: string
  dictName: string
  dictType: string
  description?: string
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
  itemLabel?: string
  label?: string
  itemValue?: string
  value?: string
  sort?: number
  sortOrder?: number
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
  formConfig: string
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
  title: string
  businessData: string
  currentNodeId: string
  currentNodeName: string
  status: string
  submitTime: string
  finishTime: string
  remark: string
  createTime: string
  pendingApproval: boolean
  canApprove: boolean
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
}

export interface WorkflowApproval {
  id: string
  instanceId: string
  nodeId: string
  nodeName: string
  approverId: string
  approverName: string
  approvalStatus: string
  comment: string
  attachments: string
  approvalTime: string
  createTime: string
}

export interface WorkflowDetail {
  instance: WorkflowInstance
  approvals: WorkflowApproval[]
  nodes: WorkflowNode[]
  currentNode: WorkflowNode | null
  canApprove: boolean
}
