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
              <component :is="getIcon(menu.icon)" />
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
            <component :is="getIcon(menu.icon)" />
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
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import {
  HomeFilled,
  Setting,
  User,
  Document,
  Files,
  OfficeBuilding,
  Monitor,
  Bell,
  ChatDotRound,
  Histogram,
  PieChart,
  TrendCharts,
  Grid,
  Menu,
  Tools,
  Notification,
  FolderOpened,
  Operation,
  Lock,
  Notebook,
  Message,
  Avatar,
  Checked,
  Clock,
  Calendar,
  Timer,
  DataLine,
  Management,
  List
} from '../../utils/icons';

// ElementPlus icon - using emoji fallback
const ElementPlus = { template: '<span>A+</span>' };
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

// 图标映射：只使用 Element Plus 确定存在的图标
const iconMap: Record<string, any> = {
  // 常用图标
  HomeFilled,
  House: HomeFilled,
  Home: HomeFilled,
  Setting,
  SetUp: Setting,
  User,
  Document,
  DocumentCopy: Document,
  Files,
  Folder,
  FolderOpened,
  DataAnalysis,
  DataLine,
  DataBoard: DataAnalysis,
  List,
  Management,
  Operation,
  Lock,
  Notebook,
  OfficeBuilding,
  Monitor,
  Bell,
  Notification,
  ChatDotRound,
  ChatLineSquare: ChatDotRound,
  MessageBox: Message,
  Message,
  Histogram,
  PieChart,
  TrendCharts,
  Grid,
  Menu,
  Tools,
  Stamp: Tools,
  Reading,
  Avatar,
  Checked,
  Clock,
  Calendar,
  Timer,
  ElementPlus
};

// 获取图标组件
const getIcon = (iconName?: string) => {
  if (!iconName) return undefined;
  return iconMap[iconName] || iconMap['Menu']; // 默认图标
};
</script>

<style scoped>
.app-sidebar {
  width: var(--sidebar-width);
  height: 100%;
  background: linear-gradient(180deg, #001529 0%, #002140 100%);
  position: relative;
  transition: width var(--transition-normal);
  z-index: var(--z-sticky);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.app-sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

.logo-area {
  height: var(--header-height);
  display: flex;
  align-items: center;
  padding: 0 var(--space-md);
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
  width: 32px;
  height: 32px;
  background: var(--primary-gradient);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  transition: all var(--transition-normal);
}

.logo-area:hover .logo-icon {
  transform: rotate(5deg) scale(1.05);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
}

.logo-text {
  margin-left: var(--space-md);
  font-size: 18px;
  font-weight: 600;
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
  padding: var(--space-xs) 0;
}

/* 自定义滚动条 */
.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-full);
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.25);
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
  height: 48px;
  line-height: 48px;
  color: rgba(255, 255, 255, 0.65) !important;
  margin: 2px var(--space-sm);
  border-radius: var(--radius-sm);
  transition: all 0.2s ease;
  position: relative;
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  color: inherit;
  font-size: 16px;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: white !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.2) 0%, rgba(99, 102, 241, 0.2) 100%) !important;
  color: #40a9ff !important;
  margin: 2px var(--space-sm);
  border-radius: var(--radius-sm);
  box-shadow: none;
  position: relative;
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 24px;
  background: var(--primary-color);
  border-radius: 0 2px 2px 0;
}

:deep(.el-sub-menu .el-menu-item) {
  background: rgba(0, 0, 0, 0.15) !important;
  padding-left: calc(var(--space-2xl) + 4px) !important;
  margin: 2px var(--space-sm) 2px var(--space-md);
  height: 44px;
  line-height: 44px;
}

:deep(.el-sub-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.25) 0%, rgba(99, 102, 241, 0.25) 100%) !important;
  color: #69c0ff !important;
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
  transition: opacity 0.15s ease;
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
