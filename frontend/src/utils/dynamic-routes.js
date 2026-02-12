/**
 * 动态路由工具
 * 将后端返回的菜单数据转换为 Vue Router 路由配置
 */

import { getComponent } from './component-registry.js';

/**
 * 将菜单数据转换为路由配置
 * @param {Object[]} menus - 菜单数据数组
 * @returns {Object[]} 路由配置数组
 */
export const menusToRoutes = (menus) => {
  if (!menus || menus.length === 0) {
    return [];
  }

  return menus
    .filter((menu) => menu.status === 1 && menu.visible === 1) // 只处理启用且可见的菜单
    .map((menu) => {
      const route = {
        path: menu.path,
        name: generateRouteName(menu.name, menu.id),
        meta: {
          title: menu.name,
          icon: menu.icon,
          permission: menu.permKey,
          hidden: menu.visible === 0,
          type: menu.type, // 0=目录，1=菜单，2=按钮
          id: menu.id,
        },
      };

      // 设置组件（只有菜单类型需要组件）
      if (menu.type === 1 && menu.component) {
        route.component = getComponent(menu.component);
      }

      // 递归处理子菜单
      if (menu.children && menu.children.length > 0) {
        const childrenRoutes = menusToRoutes(menu.children);
        if (childrenRoutes.length > 0) {
          route.children = childrenRoutes;
        }
      }

      return route;
    });
};

/**
 * 生成路由名称
 * @param {string} menuName - 菜单名称
 * @param {string} menuId - 菜单ID
 * @returns {string} 路由名称
 */
const generateRouteName = (menuName, menuId) => {
  // 将菜单名称转换为驼峰命名
  const camelCaseName = menuName.replace(/[-_\s]+(.)?/g, (_, c) => (c ? c.toUpperCase() : ''));
  // 确保首字母大写
  const pascalCaseName = camelCaseName.charAt(0).toUpperCase() + camelCaseName.slice(1);
  // 添加ID后缀以确保唯一性
  return `${pascalCaseName}_${menuId}`;
};

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
    .filter((route) => {
      // 如果路由没有权限要求，直接放行
      if (!route.meta || !route.meta.permission) {
        return true;
      }
      // 检查用户是否有所需权限
      return permissions.includes(route.meta.permission);
    })
    .map((route) => {
      // 递归处理子路由
      if (route.children && route.children.length > 0) {
        const filteredChildren = filterRoutesByPermissions(route.children, permissions);
        if (filteredChildren.length > 0) {
          return { ...route, children: filteredChildren };
        }
      }
      return route;
    });
};

/**
 * 获取路由的完整路径（包含父路径）
 * @param {Object} route - 路由对象
 * @param {string} parentPath - 父路径
 * @returns {string} 完整路径
 */
export const getRouteFullPath = (route, parentPath = '') => {
  let fullPath = route.path;

  // 如果路径不是以 / 开头，拼接父路径
  if (!fullPath.startsWith('/') && parentPath) {
    fullPath = `${parentPath}/${fullPath}`;
  }

  return fullPath;
};