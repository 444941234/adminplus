import { describe, expect, it } from 'vitest'
import type { Menu } from '@/types'
import {
  buildOpenGroupState,
  buildSidebarTree,
  flattenSidebarMenus,
  getSidebarTree,
  isRouteActive
} from '@/layout/sidebar'

const createMenu = (overrides: Partial<Menu>): Menu => ({
  id: overrides.id ?? 'M1',
  menuName: overrides.menuName ?? '菜单',
  name: overrides.name,
  menuType: overrides.menuType ?? 1,
  type: overrides.type,
  parentId: overrides.parentId ?? '0',
  path: overrides.path ?? '/system/user',
  component: overrides.component ?? 'system/User',
  icon: overrides.icon ?? 'Users',
  sort: overrides.sort ?? 0,
  sortOrder: overrides.sortOrder,
  status: overrides.status ?? 1,
  visible: overrides.visible ?? 1,
  permission: overrides.permission ?? 'user:list',
  permKey: overrides.permKey,
  children: overrides.children
})

describe('sidebar helpers', () => {
  it('builds grouped sidebar tree from backend directory menus', () => {
    const tree = buildSidebarTree([
      createMenu({
        id: 'system',
        menuType: 0,
        path: '/system',
        component: '',
        icon: 'Settings',
        children: [
          createMenu({ id: 'user', menuName: '用户管理', path: '/system/user', sort: 2 }),
          createMenu({ id: 'role', menuName: '角色管理', path: '/system/role', sort: 1, icon: 'Shield' })
        ]
      })
    ])

    expect(tree).toEqual([
      expect.objectContaining({
        kind: 'group',
        id: 'system',
        label: '菜单',
        children: [
          expect.objectContaining({ id: 'role', path: '/system/role' }),
          expect.objectContaining({ id: 'user', path: '/system/user' })
        ]
      })
    ])
  })

  it('flattens only accessible leaf routes for collapsed sidebar mode', () => {
    const items = flattenSidebarMenus([
      createMenu({
        id: 'workflow',
        menuType: 0,
        path: '/workflow',
        component: '',
        children: [
          createMenu({ id: 'definitions', path: '/workflow/definitions', sort: 2, icon: 'GitBranch' }),
          createMenu({ id: 'pending', path: '/workflow/pending', sort: 1, icon: 'Clock3' }),
          createMenu({ id: 'approve-btn', menuType: 2, path: '', component: '', permission: 'workflow:approve' })
        ]
      })
    ])

    expect(items).toEqual([
      { path: '/workflow/pending', icon: 'Clock3', label: '菜单' },
      { path: '/workflow/definitions', icon: 'GitBranch', label: '菜单' }
    ])
  })

  it('marks active group branches as expanded', () => {
    const nodes = getSidebarTree([
      createMenu({
        id: 'analysis',
        menuType: 0,
        path: '/analysis',
        component: '',
        children: [
          createMenu({ id: 'statistics', path: '/analysis/statistics', icon: 'BarChart3' })
        ]
      })
    ])

    expect(buildOpenGroupState(nodes, '/analysis/statistics')).toEqual({
      analysis: true
    })
  })

  it('treats nested detail routes as active for their parent menu', () => {
    expect(isRouteActive('/workflow/detail/123', '/workflow')).toBe(true)
    expect(isRouteActive('/system/user', '/workflow')).toBe(false)
  })
})
