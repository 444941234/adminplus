export type PermissionChecker = (permission: string) => boolean

export interface DashboardQuickAction {
  path: string
  label: string
  icon: string
  color: string
  permissions: string[]
}

const hasAnyPermission = (hasPermission: PermissionChecker, permissions: string[]) => {
  return permissions.some((permission) => hasPermission(permission))
}

const dashboardQuickActions: DashboardQuickAction[] = [
  {
    path: '/system/user',
    label: '用户管理',
    icon: 'Users',
    color: 'text-blue-500',
    permissions: ['system:user:list', 'user:query']
  },
  {
    path: '/system/role',
    label: '角色管理',
    icon: 'Shield',
    color: 'text-green-500',
    permissions: ['system:role:list', 'role:query']
  },
  {
    path: '/system/menu',
    label: '菜单管理',
    icon: 'Menu',
    color: 'text-orange-500',
    permissions: ['system:menu:list', 'menu:list']
  },
  {
    path: '/system/log',
    label: '日志管理',
    icon: 'FileText',
    color: 'text-red-500',
    permissions: ['system:log:list', 'log:query']
  },
  {
    path: '/system/file',
    label: '文件管理',
    icon: 'FolderOpen',
    color: 'text-cyan-500',
    permissions: ['file:list']
  },
  {
    path: '/system/config',
    label: '系统监控',
    icon: 'Server',
    color: 'text-slate-500',
    permissions: ['system:config:list']
  },
  {
    path: '/analysis/statistics',
    label: '数据统计',
    icon: 'BarChart3',
    color: 'text-violet-500',
    permissions: ['analysis:statistics:view']
  },
  {
    path: '/workflow/definitions',
    label: '流程模板',
    icon: 'GitBranch',
    color: 'text-sky-500',
    permissions: ['workflow:definition:list']
  },
  {
    path: '/workflow/pending',
    label: '待我审批',
    icon: 'Clock3',
    color: 'text-amber-500',
    permissions: ['workflow:pending:list', 'workflow:approve']
  },
  {
    path: '/workflow/cc',
    label: '抄送我的',
    icon: 'Users',
    color: 'text-cyan-500',
    permissions: ['workflow:cc:list']
  },
  {
    path: '/workflow/urge',
    label: '催办中心',
    icon: 'Bell',
    color: 'text-rose-500',
    permissions: ['workflow:urge:list', 'workflow:urge']
  }
]

export const getDashboardQuickActions = (hasPermission: PermissionChecker) => {
  return dashboardQuickActions.filter((action) => hasAnyPermission(hasPermission, action.permissions))
}

export const getWorkflowPermissionState = (
  hasPermission: PermissionChecker,
  canApproveDetail = false
) => {
  const canStart = hasPermission('workflow:start') || hasPermission('workflow:create')
  const canDraft = hasPermission('workflow:draft') || hasPermission('workflow:create')
  const canApprove = hasPermission('workflow:approve')
  const canReject = hasPermission('workflow:reject') || canApprove
  const canRollback = hasPermission('workflow:rollback') || canApprove
  const canAddSign = hasPermission('workflow:add-sign') || canApprove
  const canUrge = hasPermission('workflow:urge') || hasPermission('workflow:create')
  const canWithdraw = hasPermission('workflow:withdraw') || hasPermission('workflow:create')
  const canCancel = hasPermission('workflow:cancel') || hasPermission('workflow:create')
  const canViewCc = hasPermission('workflow:cc:list')
  const canMarkCcRead = hasPermission('workflow:cc:read') || canViewCc
  const canViewUrge = hasPermission('workflow:urge:list') || hasPermission('workflow:urge')
  const canMarkUrgeRead = hasPermission('workflow:urge:read') || canViewUrge
  const canViewDefinitions = hasPermission('workflow:definition:list')
  const canCreateDefinition = hasPermission('workflow:definition:create') || hasPermission('workflow:create')
  const canEditDefinition = hasPermission('workflow:definition:update') || hasPermission('workflow:update')
  const canDeleteDefinition = hasPermission('workflow:definition:delete') || hasPermission('workflow:delete')

  return {
    canStartWorkflow: canStart,
    canDraftWorkflow: canDraft,
    canApprovePendingActions: canApprove,
    canApproveDetail: canApprove && canApproveDetail,
    canRejectDetail: canReject && canApproveDetail,
    canRollbackDetail: canRollback && canApproveDetail,
    canAddSignDetail: canAddSign && canApproveDetail,
    canUrgeWorkflow: canUrge,
    canWithdrawWorkflow: canWithdraw,
    canCancelWorkflow: canCancel,
    canViewCc,
    canMarkCcRead,
    canViewUrge,
    canMarkUrgeRead,
    canViewDefinitions,
    canCreateDefinition,
    canEditDefinition,
    canDeleteDefinition
  }
}

export const getUserPagePermissionState = (hasPermission: PermissionChecker) => ({
  canAddUser: hasPermission('user:add'),
  canEditUser: hasPermission('user:edit'),
  canDeleteUser: hasPermission('user:delete'),
  canAssignUser: hasPermission('user:assign')
})

export const getRolePagePermissionState = (hasPermission: PermissionChecker) => ({
  canAddRole: hasPermission('role:add'),
  canEditRole: hasPermission('role:edit'),
  canDeleteRole: hasPermission('role:delete'),
  canAssignRole: hasPermission('role:assign')
})
