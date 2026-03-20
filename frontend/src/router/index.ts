import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  buildDynamicChildRoutes,
  normalizeComponentPath
} from '@/router/dynamic-routes'
import { decideGuardAction } from '@/router/guard'

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
  console.log('[addDynamicRoutes] Building routes from menus:', menus)
  console.log('[addDynamicRoutes] Built child routes:', childRoutes)

  for (const childRoute of childRoutes) {
    const routeName = String(childRoute.name)
    console.log('[addDynamicRoutes] Processing route:', routeName, 'path:', childRoute.path)
    if (router.hasRoute(routeName)) {
      console.log('[addDynamicRoutes] Route already exists, skipping:', routeName)
      dynamicRouteNames.add(routeName)
      continue
    }

    router.addRoute('LayoutRoot', childRoute)
    console.log('[addDynamicRoutes] Added route:', routeName, 'to LayoutRoot')
    dynamicRouteNames.add(routeName)
  }

  console.log('[addDynamicRoutes] Current routes:', router.getRoutes().map(r => ({ name: r.name, path: r.path })))
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
  console.log('[ensureDynamicRoutes] Starting, token:', userStore.token)
  const hasToken = userStore.restoreToken()
  console.log('[ensureDynamicRoutes] After restore, token:', userStore.token, 'hasToken:', hasToken)

  if (!hasToken && !userStore.token) {
    console.log('[ensureDynamicRoutes] No token, resetting routes')
    resetDynamicRoutes()
    return
  }

  if (!dynamicRoutesLoaded) {
    try {
      console.log('[ensureDynamicRoutes] Loading user info...')
      // fetchUserInfo will check token.value internally
      await userStore.fetchUserInfo()
      console.log('[ensureDynamicRoutes] User info loaded, menus:', userStore.menus)
      addDynamicRoutes(userStore.menus)
      console.log('[ensureDynamicRoutes] Dynamic routes added')
      dynamicRoutesLoaded = true
    } catch (error) {
      console.error('Failed to load dynamic routes:', error)
      // Don't set dynamicRoutesLoaded to true on error, so we can retry
      // But also don't block navigation - let the user through to static routes
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

  console.log('[router.beforeEach] path:', to.path, 'matched:', to.matched.length, 'action:', guardAction, 'dynamicRoutesLoaded:', dynamicRoutesLoaded)

  if (guardAction === 'redirect-login') {
    resetDynamicRoutes()
    return '/login'
  }

  if (guardAction === 'redirect-dashboard') {
    await ensureDynamicRoutes()
    return '/dashboard'
  }

  if (guardAction === 'retry-navigation' || guardAction === 'ensure-routes') {
    await ensureDynamicRoutes()
    if (guardAction === 'retry-navigation') {
      console.log('[router.beforeEach] Retrying navigation to:', to.fullPath)
      return { path: to.fullPath, replace: true }
    }
  }

  return true
})

export default router
