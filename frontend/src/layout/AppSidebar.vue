<template>
  <div class="app-sidebar" :class="{ 'is-collapsed': collapsed }">
    <!-- Logo 区域 -->
    <div class="sidebar-logo">
      <div class="logo-icon">
        <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="32" height="32" rx="8" fill="url(#logo-gradient)" />
          <path d="M8 12L16 8L24 12V20L16 24L8 20V12Z" fill="white" fill-opacity="0.9" />
          <path d="M16 14V20M12 16L16 14L20 16" stroke="white" stroke-width="1.5" stroke-linecap="round" />
          <defs>
            <linearGradient id="logo-gradient" x1="0" y1="0" x2="32" y2="32" gradientUnits="userSpaceOnUse">
              <stop stop-color="#3B82F6" />
              <stop offset="1" stop-color="#6366F1" />
            </linearGradient>
          </defs>
        </svg>
      </div>
      <span v-show="!collapsed" class="logo-text">AdminPlus</span>
    </div>

    <!-- 菜单区域 -->
    <el-scrollbar class="sidebar-menu-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <template v-for="menu in menus" :key="menu.id">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
            <template #title>
              <el-icon v-if="menu.icon" class="menu-icon">
                <component :is="menu.icon" />
              </el-icon>
              <span class="menu-title">{{ menu.name }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.id"
              :index="child.path"
            >
              <el-icon v-if="child.icon" class="menu-icon">
                <component :is="child.icon" />
              </el-icon>
              <span class="menu-title">{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
          <!-- 无子菜单 -->
          <el-menu-item v-else :index="menu.path">
            <el-icon v-if="menu.icon" class="menu-icon">
              <component :is="menu.icon" />
            </el-icon>
            <span class="menu-title">{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

export interface MenuItem {
  id: string | number
  name: string
  path: string
  icon?: string
  children?: MenuItem[]
}

interface Props {
  menus: MenuItem[]
  collapsed: boolean
}

defineProps<Props>()
const route = useRoute()

const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.app-sidebar {
  height: 100%;
  background-color: var(--sidebar-bg);
  border-right: 1px solid var(--sidebar-border, #E5E7EB);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;

  &.is-collapsed {
    .logo-text {
      display: none;
    }
  }
}

.sidebar-logo {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid var(--sidebar-border, #E5E7EB);
  overflow: hidden;

  .logo-icon {
    width: 32px;
    height: 32px;
    flex-shrink: 0;

    svg {
      width: 100%;
      height: 100%;
    }
  }

  .logo-text {
    margin-left: 12px;
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    white-space: nowrap;
  }
}

.sidebar-menu-wrapper {
  flex: 1;
  overflow: hidden;
}

.sidebar-menu {
  border-right: none !important;
  background-color: transparent !important;

  &:not(.el-menu--collapse) {
    width: 220px;
  }
}

:deep(.el-menu) {
  background-color: transparent !important;

  .el-menu-item,
  .el-sub-menu__title {
    height: 48px;
    line-height: 48px;
    color: var(--sidebar-text);
    margin: 4px 8px;
    border-radius: 8px;
    transition: all 0.2s ease;

    &:hover {
      background-color: var(--sidebar-hover-bg);
    }

    .menu-icon {
      font-size: 18px;
      margin-right: 8px;
    }

    .menu-title {
      font-size: 14px;
      font-weight: 500;
    }
  }

  .el-menu-item.is-active {
    color: var(--sidebar-active-text);
    background-color: var(--sidebar-active-bg);

    .menu-icon {
      color: var(--sidebar-active-text);
    }
  }

  .el-sub-menu {
    .el-menu {
      background-color: transparent !important;
    }

    .el-menu-item {
      padding-left: 48px !important;
      min-width: auto;
    }

    &.is-active > .el-sub-menu__title {
      color: var(--sidebar-active-text);
    }
  }
}
</style>