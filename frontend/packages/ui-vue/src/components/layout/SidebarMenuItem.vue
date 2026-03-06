<template>
  <!-- 有子菜单 - 递归渲染 -->
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
  <el-menu-item v-else :index="menu.path" class="submenu-item">
    <el-icon v-if="menu.icon">
      <component :is="getIcon(menu.icon)" />
    </el-icon>
    <template #title>
      <span>{{ menu.name }}</span>
    </template>
  </el-menu-item>
</template>

<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
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

export interface MenuItem {
  id: string;
  name: string;
  path: string;
  icon?: string;
  children?: MenuItem[];
}

defineOptions({
  name: 'SidebarMenuItem'
});

interface Props {
  menu: MenuItem;
}

defineProps<Props>();

// 递归组件
const SidebarMenuItem = defineAsyncComponent(() => import('./SidebarMenuItem.vue'));

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
  Timer
};

const getIcon = (iconName?: string) => {
  if (!iconName) return undefined;
  return iconMap[iconName] || iconMap['Menu']; // 默认图标
};
</script>

<style scoped>
.submenu-item {
  padding-left: var(--space-2xl) !important;
}

:deep(.submenu-item:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
}

:deep(.submenu-item.is-active) {
  background: var(--primary-gradient) !important;
  border-radius: var(--radius-md);
  margin: 0 var(--space-md);
}
</style>
