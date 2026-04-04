import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  buildDynamicChildRoutes,
  normalizeComponentPath
} from '@/router/dynamic-routes'
import { decideGuardAction } from '@/router/guard'
import { logError } from '@/utils/logger'

const viewModules = import.meta.glob('../views/**/*.vue')

const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'LayoutRoot',
    component: () => import('@/layout/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'RootRedirect',
        redirect: () => (localStorage.getItem('token') ? '/dashboard' : '/login')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人资料' }
      },
      {
        path: 'workflow/detail/:id',
        name: 'WorkflowDetail',
        component: () => import('@/views/workflow/WorkflowDetail.vue'),
        meta: { title: '流程详情' }
      },
      {
        path: 'workflow/cc',
        name: 'WorkflowCc',
        component: () => import('@/views/workflow/MyCc.vue'),
        meta: { title: '抄送我的' }
      },
      {
        path: 'workflow/urge',
        name: 'WorkflowUrge',
        component: () => import('@/views/workflow/UrgeCenter.vue'),
        meta: { title: '催办中心' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes
})

const publicPaths = new Set(['/login'])
const dynamicRouteNames = new Set<string>()
let dynamicRoutesLoaded = false

const resolveViewComponent = (component?: string) => {
  if (!component) {
    return viewModules['../views/NotFound.vue']
  }

  const normalized = normalizeComponentPath(component)
  const module = viewModules[`../views/${normalized}.vue`]
  return module ?? viewModules['../views/NotFound.vue']
}

const addDynamicRoutes = (menus: Parameters<typeof buildDynamicChildRoutes>[0]) => {
  const childRoutes = buildDynamicChildRoutes(menus, resolveViewComponent)

  for (const childRoute of childRoutes) {
    const routeName = String(childRoute.name)
    if (router.hasRoute(routeName)) {
      dynamicRouteNames.add(routeName)
      continue
    }

    router.addRoute('LayoutRoot', childRoute)
    dynamicRouteNames.add(routeName)
  }
}

export const resetDynamicRoutes = () => {
  for (const routeName of dynamicRouteNames) {
    if (router.hasRoute(routeName)) {
      router.removeRoute(routeName)
    }
  }
  dynamicRouteNames.clear()
  dynamicRoutesLoaded = false
}

export const ensureDynamicRoutes = async () => {
  const userStore = useUserStore()
  // Restore token from localStorage if needed (syncs store with localStorage)
  const hasToken = userStore.restoreToken()

  if (!hasToken && !userStore.token) {
    resetDynamicRoutes()
    return
  }

  if (!dynamicRoutesLoaded) {
    try {
      await userStore.fetchUserInfo()
      addDynamicRoutes(userStore.menus)
      dynamicRoutesLoaded = true
    } catch (error) {
      logError('加载动态路由失败', error as Error, 'Router')
      // Don't set dynamicRoutesLoaded to true on error, so we can retry
    }
  }
}

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('token')
  const guardAction = decideGuardAction({
    hasToken: !!token,
    isLoginPage: to.path === '/login',
    isPublicRoute: publicPaths.has(to.path),
    hasMatchedRoutes: to.matched.length > 0
  })

  if (guardAction === 'redirect-login') {
    resetDynamicRoutes()
    return '/login'
  }

  if (guardAction === 'redirect-dashboard') {
    await ensureDynamicRoutes()
    return '/dashboard'
  }

  if (guardAction === 'retry-navigation' || guardAction === 'ensure-routes') {
    const wasLoaded = dynamicRoutesLoaded
    await ensureDynamicRoutes()

    // If routes were just loaded, retry navigation to match the newly added routes
    if (!wasLoaded && dynamicRoutesLoaded) {
      return { path: to.fullPath, replace: true }
    }

    if (guardAction === 'retry-navigation') {
      return { path: to.fullPath, replace: true }
    }
  }

  return true
})

export default router
