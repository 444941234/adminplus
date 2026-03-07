<template>
  <el-container class="layout-container">
    <el-aside :width="collapsed ? '64px' : '220px'" class="layout-aside">
      <AppSidebar :menus="menus" :collapsed="collapsed" />
    </el-aside>
    <el-container class="layout-main-container">
      <el-header class="layout-header">
        <AppHeader
          :user="userInfo"
          :collapsed="collapsed"
          @toggle="handleToggle"
          @command="handleCommand"
        />
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import AppSidebar from './AppSidebar.vue'
import AppHeader from './AppHeader.vue'
import type { MenuItem, UserInfo } from './layout.types'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'

defineOptions({ name: 'Layout' })

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const collapsed = computed(() => themeStore.sidebarCollapsed)

const userInfo = computed<UserInfo>(() => ({
  nickname: userStore.user?.nickname || 'Admin',
  avatar: userStore.user?.avatar || ''
}))

const handleToggle = () => themeStore.toggleSidebar()

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      await router.push('/profile')
      break
    case 'settings':
      await router.push('/system/config')
      break
    case 'logout':
      userStore.logout()
      await router.push('/login')
      break
  }
}

const menus = computed<MenuItem[]>(() => {
  const allRoutes = router.getRoutes()
  const layoutRoute = allRoutes.find((r) => r.name === 'Layout')
  if (!layoutRoute?.children) return []
  return convertRoutesToMenus(filterUniqueRoutes(layoutRoute.children))
})

const filterUniqueRoutes = (routes: any[]) => {
  const seen = new Set()
  return routes.filter((r) => {
    if (r.meta?.hidden) return false
    const isDirectory = r.meta?.type === 0
    if (!isDirectory && !r.components?.default && !r.component) return false
    if (seen.has(r.path)) return false
    seen.add(r.path)
    return true
  })
}

const convertRoutesToMenus = (routes: any[], parentPath = ''): MenuItem[] => {
  return routes.map((r) => {
    let fullPath = r.path
    if (parentPath && !fullPath.startsWith('/')) fullPath = `${parentPath}/${fullPath}`
    if (!fullPath.startsWith('/')) fullPath = `/${fullPath}`
    return {
      id: r.meta?.id || r.name,
      name: r.meta?.title || r.name,
      path: fullPath,
      icon: r.meta?.icon,
      children: r.children ? convertRoutesToMenus(r.children, fullPath) : []
    }
  })
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  background-color: var(--bg-color);
}

.layout-aside {
  background-color: var(--sidebar-bg);
  transition: width 0.3s ease;
  overflow: hidden;
  flex-shrink: 0;
}

.layout-main-container {
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1;
}

.layout-header {
  height: 56px;
  padding: 0;
  background-color: var(--header-bg);
  z-index: 10;
}

.layout-main {
  background-color: var(--bg-color);
  padding: 24px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}
</style>