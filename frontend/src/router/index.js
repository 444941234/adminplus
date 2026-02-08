import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserMenuTree } from '@/api/menu'
import { menusToRoutes } from '@/utils/dynamic-routes'

// 静态路由（公共路由，不需要权限）
const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { requiresAuth: false, title: '404' }
  }
]

// 异步路由（需要权限）
const asyncRoutes = []

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

/**
 * 动态添加路由
 * @param {Object[]} menus - 菜单数据
 */
export const addDynamicRoutes = (menus) => {
  // 将菜单数据转换为路由配置
  const dynamicRoutes = menusToRoutes(menus)

  // 添加 Layout 路由作为父路由
  const layoutRoute = {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: dynamicRoutes
  }

  // 添加路由
  router.addRoute(layoutRoute)

  // 添加 404 路由（必须在最后）
  router.addRoute({
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  })
}

/**
 * 重置路由（用于登出时清除动态路由）
 */
export const resetRouter = () => {
  const newRouter = createRouter({
    history: createWebHistory(),
    routes: constantRoutes
  })
  router.matcher = newRouter.matcher
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 检查 token 是否存在
  const token = userStore.token.value || sessionStorage.getItem('token')

  // 不需要认证的路由（排除根路径，根路径需要特殊处理）
  if (!to.meta.requiresAuth && to.path !== '/') {
    if (to.path === '/login' && token) {
      // 已登录用户访问登录页，跳转到首页
      next('/')
    } else {
      next()
    }
    return
  }

  // 需要认证的路由或根路径
  if (to.path === '/') {
    // 特殊处理根路径
    if (!token) {
      // 未登录，跳转到登录页
      next('/login')
      return
    }

    // 已登录，检查是否已加载动态路由
    if (!userStore.hasLoadedRoutes) {
      try {
        console.log('[Router] 开始加载动态路由')

        // 获取用户菜单树
        const menus = await getUserMenuTree()

        // 动态添加路由
        addDynamicRoutes(menus)

        // 标记路由已加载
        userStore.setRoutesLoaded(true)

        console.log('[Router] 动态路由加载完成')

        // 重新进入当前路由（会自动重定向到 /dashboard）
        next({ ...to, replace: true })
      } catch (error) {
        console.error('[Router] 动态路由加载失败', error)
        // 加载失败，跳转到登录页
        userStore.logout()
        next('/login')
      }
    } else {
      // 路由已加载，直接放行（会自动重定向到 /dashboard）
      next()
    }
    return
  }

  // 其他需要认证的路由
  if (!token) {
    // 未登录，跳转到登录页
    console.log('[Router] 未登录，跳转到登录页')
    next('/login')
    return
  }

  // 已登录，检查是否已加载动态路由
  if (!userStore.hasLoadedRoutes) {
    try {
      console.log('[Router] 开始加载动态路由')

      // 获取用户菜单树
      const menus = await getUserMenuTree()

      // 动态添加路由
      addDynamicRoutes(menus)

      // 标记路由已加载
      userStore.setRoutesLoaded(true)

      console.log('[Router] 动态路由加载完成')

      // 重新进入当前路由
      next({ ...to, replace: true })
    } catch (error) {
      console.error('[Router] 动态路由加载失败', error)
      // 加载失败，跳转到登录页
      userStore.logout()
      next('/login')
    }
  } else {
    // 路由已加载，直接放行
    next()
  }
})

export default router