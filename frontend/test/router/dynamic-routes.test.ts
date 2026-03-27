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
        type: 0,
        path: '/system',
        component: '',
        children: [
          createMenu({ id: 'b', name: '菜单管理', path: '/system/menu', sortOrder: 2, component: 'system/Menu' }),
          createMenu({ id: 'a', name: '用户管理', path: '/system/user', sortOrder: 1, component: 'system/User' }),
          createMenu({ id: 'btn', type: 2, path: '', component: '', sortOrder: 3, permKey: 'user:add' })
        ]
      })
    ]

    expect(flattenMenuRoutes(menus).map((menu) => menu.id)).toEqual(['a', 'b'])
  })

  it('maps menus into layout child routes with permission metadata', () => {
    const resolveViewComponent = vi.fn((component?: string) => component ?? 'NotFound')
    const routes = buildDynamicChildRoutes(
      [createMenu({ id: 'workflow', path: '/workflow/definitions', component: 'workflow/Definitions', permKey: 'workflow:definition:list' })],
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
