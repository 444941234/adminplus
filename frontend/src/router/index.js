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

  // 检查 token 是否存在
  const token = userStore.token || sessionStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else if (to.meta.requiresAuth && token) {
    // 验证 token 有效性（可选：添加 decode 验证）
    try {
      // 简单的 token 格式验证
      if (typeof token === 'string' && token.length > 0) {
        next()
      } else {
        userStore.logout()
        next('/login')
      }
    } catch {
      userStore.logout()
      next('/login')
    }
  } else {
    next()
  }
})

export default router