import type { RouteRecordRaw, RouteRecordSingleView } from 'vue-router'
import type { Menu } from '@/types'

export const getMenuType = (menu: Menu) => menu.menuType ?? menu.type ?? 0

export const getMenuTitle = (menu: Menu) => menu.menuName ?? menu.name ?? '未命名菜单'

export const getMenuSort = (menu: Menu) => menu.sort ?? menu.sortOrder ?? 0

export const getMenuPermission = (menu: Menu) => menu.permission ?? menu.permKey ?? ''

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
    .sort((a, b) => getMenuSort(a) - getMenuSort(b))
    .flatMap((menu) => {
      const children = menu.children ?? []
      const childRoutes = flattenMenuRoutes(children)
      if (getMenuType(menu) === 1 && menu.path && menu.component) {
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
        title: getMenuTitle(menu),
        permission: getMenuPermission(menu),
        menuId: menu.id
      }
    }
    return route
  })
}
