<template>
  <aside class="app-sidebar" :class="{ collapsed, 'mobile-open': mobileOpen }">
    <!-- Logo 区域 -->
    <div class="logo-area" @click="$emit('toggle')">
      <div class="logo-icon">
        <el-icon :size="28">
          <ElementPlus />
        </el-icon>
      </div>
      <transition name="fade">
        <span v-show="!collapsed" class="logo-text">AdminPlus</span>
      </transition>
    </div>

    <!-- 菜单列表 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :unique-opened="true"
      class="sidebar-menu"
      router
    >
      <template v-for="menu in menus" :key="menu.id">
        <!-- 有子菜单 -->
        <el-sub-menu v-if="menu.children && menu.children.length" :index="menu.id">
          <template #title>
            <el-icon v-if="menu.icon">
              <component :is="menu.icon" />
            </el-icon>
            <span>{{ menu.name }}</span>
          </template>
          <sidebar-menu-item
            v-for="subMenu in menu.children"
            :key="subMenu.id"
            :menu="subMenu"
          />
        </el-sub-menu>

        <!-- 无子菜单 -->
        <el-menu-item v-else :index="menu.path">
          <el-icon v-if="menu.icon">
            <component :is="menu.icon" />
          </el-icon>
          <template #title>
            <span>{{ menu.name }}</span>
          </template>
        </el-menu-item>
      </template>
    </el-menu>
  </aside>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue';
import { useRoute } from 'vue-router';
import { ElementPlus } from '@element-plus/icons-vue';
import SidebarMenuItem from './SidebarMenuItem.vue';

export interface MenuItem {
  id: string;
  name: string;
  path: string;
  icon?: string;
  children?: MenuItem[];
}

defineOptions({
  name: 'AppSidebar'
});

interface Props {
  collapsed?: boolean;
  mobileOpen?: boolean;
  menus: MenuItem[];
}

withDefaults(defineProps<Props>(), {
  collapsed: false,
  mobileOpen: false
});

defineEmits<{
  toggle: [];
}>();

const route = useRoute();
const activeMenu = computed(() => route.path);
</script>

<style scoped>
.app-sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background: var(--bg-dark);
  position: fixed;
  left: 0;
  top: 0;
  transition: width var(--transition-normal);
  z-index: var(--z-fixed);
  display: flex;
  flex-direction: column;
}

.app-sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

.logo-area {
  height: var(--header-height);
  display: flex;
  align-items: center;
  padding: 0 var(--space-lg);
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: var(--primary-gradient);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.logo-text {
  margin-left: var(--space-md);
  font-size: 20px;
  font-weight: bold;
  color: white;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: var(--sidebar-width);
}

:deep(.el-menu) {
  background: transparent !important;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}

:deep(.el-menu-item.is-active) {
  background: var(--primary-gradient) !important;
  color: white !important;
  border-radius: var(--radius-md);
  margin: 0 var(--space-md);
}

:deep(.el-sub-menu .el-menu-item) {
  background: rgba(0, 0, 0, 0.2) !important;
  padding-left: var(--space-2xl) !important;
}

:deep(.el-sub-menu .el-menu-item.is-active) {
  background: var(--primary-gradient) !important;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-fast);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 767px) {
  .app-sidebar {
    transform: translateX(-100%);
  }

  .app-sidebar.mobile-open {
    transform: translateX(0);
  }
}
</style>
