import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { getUserMenuTree } from '@/api/menu';
import { menusToRoutes } from '@/utils/dynamic-routes';

// ============ 常量定义 ============
const ROUTE_PATH = {
  LOGIN: '/login',
  NOT_FOUND: '/404',
  ROOT: '/',
  DASHBOARD: '/dashboard',
  CATCH_ALL: '/:pathMatch(.*)*',
};

const ROUTE_NAME = {
  LOGIN: 'Login',
  NOT_FOUND: 'NotFound',
  LAYOUT: 'Layout',
  DASHBOARD: 'Dashboard',
};

// ============ 公共路由（不需要权限） ============
const publicRoutes = [
  {
    path: ROUTE_PATH.LOGIN,
    name: ROUTE_NAME.LOGIN,
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false, title: '登录' },
  },
  {
    path: ROUTE_PATH.NOT_FOUND,
    name: ROUTE_NAME.NOT_FOUND,
    component: () => import('@/views/NotFound.vue'),
    meta: { requiresAuth: false, title: '404' },
  },
];

// ============ Layout 内部固定路由 ============
const layoutRoutes = [
  {
    path: 'dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true, title: '首页', icon: 'HomeFilled' },
  },
  {
    path: 'profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true, title: '个人中心', hidden: true },
  },
];

// ============ 创建路由实例 ============
const router = createRouter({
  history: createWebHistory(),
  routes: publicRoutes,
});

// ============ 路由工具函数 ============

/**
 * 检查是否为白名单路由
 * @param {string} path - 路由路径
 * @returns {boolean}
 */
const isWhiteListRoute = (path) => {
  return publicRoutes.some((route) => route.path === path);
};

/**
 * 检查 Layout 路由是否已添加
 * @returns {boolean}
 */
const hasLayoutRoute = () => router.hasRoute(ROUTE_NAME.LAYOUT);

/**
 * 检查是否需要加载动态路由
 * @param {Object} userStore - 用户 store
 * @returns {boolean}
 */
const shouldLoadDynamicRoutes = (userStore) => {
  return !userStore.hasLoadedRoutes || !hasLayoutRoute();
};

/**
 * 处理动态路由加载失败
 * @param {Object} userStore - 用户 store
 * @param {Function} next - 路由 next 函数
 * @param {Error} error - 错误对象
 */
const handleRouteLoadError = (userStore, next, error) => {
  console.error('[Router] 动态路由加载失败:', error);
  userStore.logout();
  next(ROUTE_PATH.LOGIN);
};

/**
 * 加载并添加动态路由
 * @param {Object} userStore - 用户 store
 * @returns {Promise<void>}
 */
const loadDynamicRoutes = async (userStore) => {
  const menus = await getUserMenuTree();
  addDynamicRoutes(menus);
  userStore.setRoutesLoaded(true);
};

/**
 * 动态添加路由
 * @param {Object[]} menus - 菜单数据
 */
export const addDynamicRoutes = (menus) => {
  console.log('[Router] 开始添加动态路由，菜单数据:', menus);

  const dynamicRoutes = normalizeRoutesPaths(menusToRoutes(menus));
  console.log('[Router] 转换后的路由配置:', dynamicRoutes);

  const layoutRoute = {
    path: ROUTE_PATH.ROOT,
    name: ROUTE_NAME.LAYOUT,
    component: () => import('@/layout/Layout.vue'),
    redirect: ROUTE_PATH.DASHBOARD,
    meta: { requiresAuth: true },
    children: [...layoutRoutes, ...dynamicRoutes],
  };

  router.addRoute(layoutRoute);

  // 添加 404 路由（必须在最后）
  router.addRoute({
    path: ROUTE_PATH.CATCH_ALL,
    redirect: ROUTE_PATH.NOT_FOUND,
  });

  console.log('[Router] 动态路由添加完成');
};

/**
 * 标准化路由路径，移除前导斜杠使其成为相对路径
 * @param {Object[]} routes - 路由配置数组
 * @returns {Object[]} 标准化后的路由配置
 */
const normalizeRoutesPaths = (routes) => {
  return routes.map((route) => {
    const normalizedRoute = { ...route };

    // 处理路径：移除前导斜杠
    if (normalizedRoute.path.startsWith('/')) {
      normalizedRoute.path = normalizedRoute.path.substring(1);
    }

    // 递归处理子路由
    if (normalizedRoute.children && normalizedRoute.children.length > 0) {
      normalizedRoute.children = normalizeRoutesPaths(normalizedRoute.children);
    }

    return normalizedRoute;
  });
};

/**
 * 重置路由（用于登出时清除动态路由）
 */
export const resetRouter = () => {
  const newRouter = createRouter({
    history: createWebHistory(),
    routes: publicRoutes,
  });
  router.matcher = newRouter.matcher;
};

// ============ 路由守卫 ============

/**
 * 处理未匹配路由的情况
 * @param {Object} to - 目标路由
 * @param {Object} userStore - 用户 store
 * @param {Function} next - 路由 next 函数
 */
const handleUnmatchedRoute = async (to, userStore, next) => {
  const token = userStore.token;

  if (!token) {
    next(ROUTE_PATH.LOGIN);
    return;
  }

  if (shouldLoadDynamicRoutes(userStore)) {
    try {
      await loadDynamicRoutes(userStore);
      // 重新导航到目标路由
      next({ ...to, replace: true });
    } catch (error) {
      handleRouteLoadError(userStore, next, error);
    }
    return;
  }

  next(ROUTE_PATH.NOT_FOUND);
};

/**
 * 处理需要认证的路由
 * @param {Object} to - 目标路由
 * @param {Object} userStore - 用户 store
 * @param {Function} next - 路由 next 函数
 */
const handleAuthRequiredRoute = async (to, userStore, next) => {
  const token = userStore.token;

  if (!token) {
    next(ROUTE_PATH.LOGIN);
    return;
  }

  if (shouldLoadDynamicRoutes(userStore)) {
    try {
      await loadDynamicRoutes(userStore);
      next({ ...to, replace: true });
    } catch (error) {
      handleRouteLoadError(userStore, next, error);
    }
    return;
  }

  next();
};

/**
 * 处理无需认证的路由
 * @param {Object} to - 目标路由
 * @param {Object} userStore - 用户 store
 * @param {Function} next - 路由 next 函数
 */
const handlePublicRoute = (to, userStore, next) => {
  const token = userStore.token;

  // 已登录用户访问登录页，重定向到首页
  if (to.path === ROUTE_PATH.LOGIN && token) {
    next(ROUTE_PATH.ROOT);
    return;
  }

  next();
};

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();

  // 初始化用户 store
  await userStore.initialize();

  const isRouteMatched = to.matched.length > 0;
  const requiresAuth = to.meta.requiresAuth !== false;

  // 1. 处理未匹配的路由
  if (!isRouteMatched) {
    await handleUnmatchedRoute(to, userStore, next);
    return;
  }

  // 2. 处理无需认证的路由
  if (!requiresAuth) {
    handlePublicRoute(to, userStore, next);
    return;
  }

  // 3. 处理需要认证的路由
  await handleAuthRequiredRoute(to, userStore, next);
});

export default router;
