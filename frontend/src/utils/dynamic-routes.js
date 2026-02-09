/**
 * 动态路由工具
 * 将后端返回的菜单数据转换为 Vue Router 路由配置
 */

import { getComponent } from './component-registry.js'

/**
 * 组件映射表（兼容性保留）
 */
const componentMap = {
  // 保留原有映射用于兼容性
  '/views/system/User.vue': () => import('@/views/system/User.vue'),
  '/views/system/Role.vue': () => import('@/views/system/Role.vue'),
  '/views/system/Menu.vue': () => import('@/views/system/Menu.vue'),
  '/views/system/Dict.vue': () => import('@/views/system/Dict.vue'),
  '/views/system/DictItem.vue': () => import('@/views/system/DictItem.vue'),
  '/views/system/Config.vue': () => import('@/views/system/Config.vue'),
  '/views/analysis/Statistics.vue': () => import('@/views/analysis/Statistics.vue'),
  '/views/analysis/Report.vue': () => import('@/views/analysis/Report.vue'),
  '/views/Dashboard.vue': () => import('@/views/Dashboard.vue'),
  '/views/Profile.vue': () => import('@/views/Profile.vue')
}

/**
 * 将菜单数据转换为路由配置
 * @param {Object[]} menus - 菜单数据数组
 * @returns {Object[]} 路由配置数组
 */
export const menusToRoutes = (menus) => {
  if (!menus || menus.length === 0) {
    return []
  }

  return menus
    .filter(menu => menu.type !== 2) // 过滤掉按钮类型的菜单（type=2）
    .map(menu => {
      const route = {
        path: menu.path,
        name: menu.name,
        meta: {
          title: menu.name,
          icon: menu.icon,
          permission: menu.permKey,
          hidden: menu.visible === 0,
          type: menu.type // 0=目录，1=菜单，2=按钮
        }
      }

      // 设置组件（只有菜单类型需要组件）
      if (menu.type === 1 && menu.component) {
        route.component = getComponent(menu.component)
      }

      // 递归处理子菜单
      if (menu.children && menu.children.length > 0) {
        const childrenRoutes = menusToRoutes(menu.children)
        if (childrenRoutes.length > 0) {
          route.children = childrenRoutes
        }
      }

      return route
    })
}

/**
 * 生成扁平化路由列表（用于权限验证）
 * @param {Object[]} routes - 路由配置数组
 * @returns {Object[]} 扁平化的路由列表
 */
export const flattenRoutes = (routes) => {
  const flatRoutes = []

  const traverse = (routeList) => {
    routeList.forEach(route => {
      // 只添加有 path 的路由（过滤掉只有子菜单的目录）
      if (route.path && route.component) {
        flatRoutes.push(route)
      }
      if (route.children && route.children.length > 0) {
        traverse(route.children)
      }
    })
  }

  traverse(routes)
  return flatRoutes
}

/**
 * 根据权限过滤路由
 * @param {Object[]} routes - 路由配置数组
 * @param {string[]} permissions - 权限列表
 * @returns {Object[]} 过滤后的路由列表
 */
export const filterRoutesByPermissions = (routes, permissions) => {
  return routes
    .filter(route => {
      // 如果路由没有权限要求，直接放行
      if (!route.meta || !route.meta.permission) {
        return true
      }
      // 检查用户是��有所需权限
      return permissions.includes(route.meta.permission)
    })
    .map(route => {
      // 递归处理子路由
      if (route.children && route.children.length > 0) {
        const filteredChildren = filterRoutesByPermissions(route.children, permissions)
        if (filteredChildren.length > 0) {
          return { ...route, children: filteredChildren }
        }
      }
      return route
    })
}