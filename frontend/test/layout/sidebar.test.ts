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
  parentId: overrides.parentId ?? '0',
  type: overrides.type ?? 1,
  name: overrides.name ?? '菜单',
  path: overrides.path ?? '/system/user',
  component: overrides.component ?? 'system/User',
  permKey: overrides.permKey ?? 'user:list',
  icon: overrides.icon ?? 'Users',
  sortOrder: overrides.sortOrder ?? 0,
  status: overrides.status ?? 1,
  visible: overrides.visible ?? 1,
  children: overrides.children
})

describe('sidebar helpers', () => {
  it('builds grouped sidebar tree from backend directory menus', () => {
    const tree = buildSidebarTree([
      createMenu({
        id: 'system',
        type: 0,
        path: '/system',
        component: '',
        icon: 'Settings',
        children: [
          createMenu({ id: 'user', name: '用户管理', path: '/system/user', sortOrder: 2 }),
          createMenu({ id: 'role', name: '角色管理', path: '/system/role', sortOrder: 1, icon: 'Shield' })
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
        type: 0,
        path: '/workflow',
        component: '',
        children: [
          createMenu({ id: 'definitions', path: '/workflow/definitions', sortOrder: 2, icon: 'GitBranch' }),
          createMenu({ id: 'pending', path: '/workflow/pending', sortOrder: 1, icon: 'Clock3' }),
          createMenu({ id: 'approve-btn', type: 2, path: '', component: '', permKey: 'workflow:approve' })
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
        type: 0,
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
