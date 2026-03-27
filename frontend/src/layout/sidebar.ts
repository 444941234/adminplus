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

const getMenuIcon = (icon?: string) => icon || 'LayoutDashboard'

export const isSidebarItem = (node: SidebarNode): node is SidebarItemNode => node.kind === 'item'

export const isSidebarGroup = (node: SidebarNode): node is SidebarGroupNode => node.kind === 'group'

export const flattenSidebarMenus = (menus: Menu[]): SidebarMenuItemData[] => {
  return menus
    .slice()
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .flatMap((menu) => {
      const children = menu.children ?? []
      if (children.length > 0) {
        return flattenSidebarMenus(children)
      }
      return menu.path && menu.type === 1
        ? [{ path: menu.path, icon: getMenuIcon(menu.icon), label: menu.name }]
        : []
    })
}

export const buildSidebarTree = (menus: Menu[]): SidebarNode[] => {
  const nodes: SidebarNode[] = []
  for (const menu of menus.slice().sort((a, b) => a.sortOrder - b.sortOrder)) {
    const children = buildSidebarTree(menu.children ?? [])
    const icon = getMenuIcon(menu.icon)
    if (menu.type === 0) {
      if (children.length > 0) {
        nodes.push({
          kind: 'group',
          id: menu.id,
          label: menu.name,
          icon,
          children
        })
      }
      continue
    }

    if (menu.type === 1 && menu.path) {
      nodes.push({
        kind: 'item',
        id: menu.id,
        path: menu.path,
        label: menu.name,
        icon
      })
    }
  }
  return nodes
}

export const getSidebarTree = (menus: Menu[]) => {
  return buildSidebarTree(menus)
}

export const getSidebarMenuItems = (menus: Menu[]) => {
  return flattenSidebarMenus(menus)
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
