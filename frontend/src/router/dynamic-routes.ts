import type { RouteRecordRaw, RouteRecordSingleView } from 'vue-router'
import type { Menu } from '@/types'

export const normalizeComponentPath = (component: string) => {
  return component.replace(/^\/+/, '')
}

export const buildRouteName = (menu: Menu) => {
  if (menu.path) {
    return menu.path
      .split('/')
      .filter(Boolean)
      .map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1))
      .join('')
  }
  return `Menu${menu.id}`
}

export const flattenMenuRoutes = (menus: Menu[]): Menu[] => {
  return menus
    .slice()
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .flatMap((menu) => {
      const children = menu.children ?? []
      const childRoutes = flattenMenuRoutes(children)
      if (menu.type === 1 && menu.path && menu.component) {
        return [menu, ...childRoutes]
      }
      return childRoutes
    })
}

export const buildDynamicChildRoutes = (
  menus: Menu[],
  resolveViewComponent: (component?: string) => NonNullable<RouteRecordRaw['component']>
): RouteRecordRaw[] => {
  return flattenMenuRoutes(menus).map((menu) => {
    const route: RouteRecordSingleView = {
      path: menu.path.replace(/^\//, ''),
      name: buildRouteName(menu),
      component: resolveViewComponent(menu.component),
      meta: {
        title: menu.name,
        permission: menu.permKey,
        menuId: menu.id
      }
    }
    return route
  })
}
