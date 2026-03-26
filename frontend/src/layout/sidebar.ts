import type { Menu } from '@/types'

export interface SidebarMenuItemData {
  path: string
  icon: string
  label: string
}

export interface SidebarItemNode {
  kind: 'item'
  id: string
  path: string
  label: string
  icon: string
}

export interface SidebarGroupNode {
  kind: 'group'
  id: string
  label: string
  icon: string
  children: SidebarNode[]
}

export type SidebarNode = SidebarItemNode | SidebarGroupNode

const staticMenus: SidebarMenuItemData[] = [
  { path: '/dashboard', icon: 'LayoutDashboard', label: '仪表盘' },
  { path: '/system/user', icon: 'Users', label: '用户管理' },
  { path: '/system/role', icon: 'Shield', label: '角色管理' },
  { path: '/system/menu', icon: 'Menu', label: '菜单管理' },
  { path: '/system/dept', icon: 'Building2', label: '部门管理' },
  { path: '/system/dict', icon: 'BookOpen', label: '字典管理' },
  { path: '/system/log', icon: 'FileText', label: '日志管理' },
  { path: '/system/file', icon: 'FolderOpen', label: '文件管理' },
  { path: '/system/config', icon: 'Settings', label: '系统配置' },
  { path: '/analysis/statistics', icon: 'BarChart3', label: '数据统计' },
  { path: '/analysis/report', icon: 'FileBarChart', label: '报表中心' },
  { path: '/workflow/definitions', icon: 'GitBranch', label: '流程模板' },
  { path: '/workflow/my', icon: 'Workflow', label: '我的流程' },
  { path: '/workflow/pending', icon: 'Clock3', label: '待我审批' }
]

export const staticSidebarTree: SidebarNode[] = [
  {
    kind: 'item',
    id: 'dashboard',
    path: '/dashboard',
    label: '仪表盘',
    icon: 'LayoutDashboard'
  },
  {
    kind: 'group',
    id: 'system',
    label: '系统管理',
    icon: 'Settings',
    children: [
      { kind: 'item', id: 'system-user', path: '/system/user', label: '用户管理', icon: 'Users' },
      { kind: 'item', id: 'system-role', path: '/system/role', label: '角色管理', icon: 'Shield' },
      { kind: 'item', id: 'system-menu', path: '/system/menu', label: '菜单管理', icon: 'Menu' },
      { kind: 'item', id: 'system-dept', path: '/system/dept', label: '部门管理', icon: 'Building2' },
      { kind: 'item', id: 'system-dict', path: '/system/dict', label: '字典管理', icon: 'BookOpen' },
      { kind: 'item', id: 'system-log', path: '/system/log', label: '日志管理', icon: 'FileText' },
      { kind: 'item', id: 'system-file', path: '/system/file', label: '文件管理', icon: 'FolderOpen' },
      { kind: 'item', id: 'system-config', path: '/system/config', label: '系统配置', icon: 'Settings' }
    ]
  },
  {
    kind: 'group',
    id: 'analysis',
    label: '数据分析',
    icon: 'BarChart3',
    children: [
      { kind: 'item', id: 'analysis-statistics', path: '/analysis/statistics', label: '数据统计', icon: 'BarChart3' },
      { kind: 'item', id: 'analysis-report', path: '/analysis/report', label: '报表中心', icon: 'FileBarChart' }
    ]
  },
  {
    kind: 'group',
    id: 'workflow',
    label: '工作流管理',
    icon: 'Workflow',
    children: [
      { kind: 'item', id: 'workflow-definitions', path: '/workflow/definitions', label: '流程模板', icon: 'GitBranch' },
      { kind: 'item', id: 'workflow-my', path: '/workflow/my', label: '我的流程', icon: 'Workflow' },
      { kind: 'item', id: 'workflow-pending', path: '/workflow/pending', label: '待我审批', icon: 'Clock3' }
    ]
  }
]

const getMenuType = (menu: Menu) => menu.menuType ?? menu.type ?? 0
const getMenuLabel = (menu: Menu) => menu.menuName ?? menu.name ?? '未命名菜单'
const getMenuSort = (menu: Menu) => menu.sort ?? menu.sortOrder ?? 0
const getMenuId = (menu: Menu) => menu.id || menu.path || getMenuLabel(menu)
const getMenuIcon = (icon?: string) => icon || 'LayoutDashboard'

export const isSidebarItem = (node: SidebarNode): node is SidebarItemNode => node.kind === 'item'

export const isSidebarGroup = (node: SidebarNode): node is SidebarGroupNode => node.kind === 'group'

export const flattenSidebarMenus = (menus: Menu[]): SidebarMenuItemData[] => {
  return menus
    .slice()
    .sort((a, b) => getMenuSort(a) - getMenuSort(b))
    .flatMap((menu) => {
      const children = menu.children ?? []
      if (children.length > 0) {
        return flattenSidebarMenus(children)
      }
      return menu.path && getMenuType(menu) === 1
        ? [{ path: menu.path, icon: getMenuIcon(menu.icon), label: getMenuLabel(menu) }]
        : []
    })
}

export const buildSidebarTree = (menus: Menu[]): SidebarNode[] => {
  const nodes: SidebarNode[] = []
  for (const menu of menus.slice().sort((a, b) => getMenuSort(a) - getMenuSort(b))) {
    const children = buildSidebarTree(menu.children ?? [])
    const icon = getMenuIcon(menu.icon)
    if (getMenuType(menu) === 0) {
      if (children.length > 0) {
        nodes.push({
          kind: 'group',
          id: getMenuId(menu),
          label: getMenuLabel(menu),
          icon,
          children
        })
      }
      continue
    }

    if (getMenuType(menu) === 1 && menu.path) {
      nodes.push({
        kind: 'item',
        id: getMenuId(menu),
        path: menu.path,
        label: getMenuLabel(menu),
        icon
      })
    }
  }
  return nodes
}

export const getSidebarTree = (menus: Menu[]) => {
  const tree = buildSidebarTree(menus)
  return tree.length > 0 ? tree : staticSidebarTree
}

export const getSidebarMenuItems = (menus: Menu[]) => {
  const items = flattenSidebarMenus(menus)
  return items.length > 0 ? items : staticMenus
}

export const isRouteActive = (currentPath: string, path: string) => {
  return currentPath === path || currentPath.startsWith(`${path}/`)
}

const hasActiveChild = (node: SidebarNode, currentPath: string): boolean => {
  if (isSidebarItem(node)) {
    return isRouteActive(currentPath, node.path)
  }
  return node.children.some((child) => hasActiveChild(child, currentPath))
}

export const buildOpenGroupState = (
  nodes: SidebarNode[],
  currentPath: string,
  previousState: Record<string, boolean> = {}
) => {
  const nextState = { ...previousState }
  const visit = (node: SidebarNode) => {
    if (isSidebarGroup(node)) {
      if (hasActiveChild(node, currentPath)) {
        nextState[node.id] = true
      } else if (nextState[node.id] === undefined) {
        nextState[node.id] = false
      }
      node.children.forEach(visit)
    }
  }
  nodes.forEach(visit)
  return nextState
}
