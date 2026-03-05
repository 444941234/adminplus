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
  position: sticky;
  top: 0;
  transition: width var(--transition-normal);
  z-index: var(--z-sticky);
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-xl);
  flex-shrink: 0;
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
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.logo-area::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--primary-gradient);
  transform: scaleX(0);
  transition: transform var(--transition-normal);
}

.logo-area:hover::after {
  transform: scaleX(1);
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
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.4);
  transition: all var(--transition-normal);
}

.logo-area:hover .logo-icon {
  transform: rotate(5deg) scale(1.05);
}

.logo-text {
  margin-left: var(--space-md);
  font-size: 20px;
  font-weight: 700;
  color: white;
  white-space: nowrap;
  letter-spacing: -0.5px;
  opacity: 1;
  transition: opacity var(--transition-normal);
}

.collapsed .logo-text {
  opacity: 0;
  pointer-events: none;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  overflow-y: auto;
  overflow-x: hidden;
  padding: var(--space-sm) 0;
}

/* 自定义滚动条 */
.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-full);
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.sidebar-menu:not(.el-menu--collapse) {
  width: var(--sidebar-width);
}

:deep(.el-menu) {
  background: transparent !important;
  border: none !important;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.7) !important;
  margin: 2px var(--space-md);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  color: inherit;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}

:deep(.el-menu-item.is-active) {
  background: var(--primary-gradient) !important;
  color: white !important;
  margin: 2px var(--space-md);
  border-radius: var(--radius-md);
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.4);
}

:deep(.el-sub-menu .el-menu-item) {
  background: rgba(0, 0, 0, 0.2) !important;
  padding-left: var(--space-2xl) !important;
  margin: 2px var(--space-md) 2px calc(var(--space-md) + var(--space-md));
}

:deep(.el-sub-menu .el-menu-item.is-active) {
  background: var(--primary-gradient) !important;
}

:deep(.el-sub-menu__title:hover) {
  color: white !important;
}

/* 子菜单展开动画 */
:deep(.el-sub-menu__icon-arrow) {
  transition: transform var(--transition-normal);
}

:deep(.el-sub-menu.is-opened > .el-sub-menu__title .el-sub-menu__icon-arrow) {
  transform: rotate(180deg);
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
    position: fixed;
    left: 0;
    transform: translateX(-100%);
  }

  .app-sidebar.mobile-open {
    transform: translateX(0);
  }
}
</style>
