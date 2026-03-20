import { describe, expect, it, vi } from 'vitest'
import type { Menu } from '@/types'
import {
  buildDynamicChildRoutes,
  buildRouteName,
  flattenMenuRoutes,
  normalizeComponentPath
} from '@/router/dynamic-routes'

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

describe('dynamic route helpers', () => {
  it('normalizes component paths without changing nested view paths', () => {
    expect(normalizeComponentPath('/system/User')).toBe('system/User')
    expect(normalizeComponentPath('workflow/Definitions')).toBe('workflow/Definitions')
  })

  it('builds route names from menu paths', () => {
    expect(buildRouteName(createMenu({ path: '/workflow/pending' }))).toBe('WorkflowPending')
  })

  it('flattens only routable menus and keeps sort order', () => {
    const menus: Menu[] = [
      createMenu({
        id: 'root',
        menuType: 0,
        path: '/system',
        component: '',
        children: [
          createMenu({ id: 'b', menuName: '菜单管理', path: '/system/menu', sort: 2, component: 'system/Menu' }),
          createMenu({ id: 'a', menuName: '用户管理', path: '/system/user', sort: 1, component: 'system/User' }),
          createMenu({ id: 'btn', menuType: 2, path: '', component: '', sort: 3, permission: 'user:add' })
        ]
      })
    ]

    expect(flattenMenuRoutes(menus).map((menu) => menu.id)).toEqual(['a', 'b'])
  })

  it('maps menus into layout child routes with permission metadata', () => {
    const resolveViewComponent = vi.fn((component?: string) => component ?? 'NotFound')
    const routes = buildDynamicChildRoutes(
      [createMenu({ id: 'workflow', path: '/workflow/definitions', component: 'workflow/Definitions', permission: 'workflow:definition:list' })],
      resolveViewComponent
    )

    expect(resolveViewComponent).toHaveBeenCalledWith('workflow/Definitions')
    expect(routes).toEqual([
      expect.objectContaining({
        path: 'workflow/definitions',
        name: 'WorkflowDefinitions',
        meta: expect.objectContaining({
          title: '菜单',
          permission: 'workflow:definition:list',
          menuId: 'workflow'
        })
      })
    ])
  })
})
