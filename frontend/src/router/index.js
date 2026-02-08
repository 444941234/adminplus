import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'system',
        name: 'System',
        meta: { title: '系统管理' },
        children: [
          {
            path: 'user',
            name: 'SystemUser',
            component: () => import('@/views/system/User.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'role',
            name: 'SystemRole',
            component: () => import('@/views/system/Role.vue'),
            meta: { title: '角色管理' }
          },
          {
            path: 'menu',
            name: 'SystemMenu',
            component: () => import('@/views/system/Menu.vue'),
            meta: { title: '菜单管理' }
          },
          {
            path: 'dict',
            name: 'SystemDict',
            component: () => import('@/views/system/Dict.vue'),
            meta: { title: '字典管理' }
          },
          {
            path: 'dict/:dictId',
            name: 'DictItem',
            component: () => import('@/views/system/DictItem.vue'),
            meta: { title: '字典项管理' },
            props: true
          }
        ]
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 检查 token 是否存在（使用 .value 获取 ref 的实际值）
  const token = userStore.token.value || sessionStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    console.log('[Router] 未登录，跳转到登录页')
    next('/login')
  } else if (to.path === '/login' && token) {
    console.log('[Router] 已登录，跳转到首页')
    next('/')
  } else if (to.meta.requiresAuth && token) {
    console.log('[Router] 已登录，验证 token 有效性')
    // 验证 token 有效性（可选：添加 decode 验证）
    try {
      // 简单的 token 格式验证
      if (typeof token === 'string' && token.length > 0) {
        next()
      } else {
        console.error('[Router] Token 格式无效，跳转到登录页')
        userStore.logout()
        next('/login')
      }
    } catch (error) {
      console.error('[Router] Token 验证失败，跳转到登录页', error)
      userStore.logout()
      next('/login')
    }
  } else {
    console.log('[Router] 不需要认证，继续导航')
    next()
  }
})

export default router