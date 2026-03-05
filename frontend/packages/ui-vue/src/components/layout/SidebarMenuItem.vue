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
  UserFilled,
  Document,
  DocumentCopy,
  DataAnalysis,
  List,
  Management,
  Lock,
  Notebook,
  Files,
  OfficeBuilding,
  Monitor,
  Bell,
  ChatDotRound,
  Histogram,
  PieChart,
  TrendCharts,
  DataBoard,
  Grid,
  Menu,
  Tools,
  Notification,
  SettingFilled,
  ManagementFilled,
  NotebookFilled,
  FilesFilled,
  FolderOpened,
  DataLine,
  Operation,
  SetUp,
  Stamp,
  NotebookFilled2,
  MessageBox,
  Message,
  ChatLineSquare,
  Reading,
  ReadingFilled,
  Avatar,
  Checked,
  ElementPlus
} from '@element-plus/icons-vue';

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

// 图标映射
const iconMap: Record<string, any> = {
  HomeFilled,
  House: HomeFilled,
  Home: HomeFilled,
  Setting,
  SettingFilled,
  SetUp,
  User,
  UserFilled,
  Document,
  DocumentCopy,
  Files,
  FilesFilled,
  FolderOpened,
  DataAnalysis,
  DataLine,
  List,
  Management,
  ManagementFilled,
  Operation,
  Lock,
  Notebook,
  NotebookFilled,
  NotebookFilled2,
  OfficeBuilding,
  Monitor,
  Bell,
  Notification,
  ChatDotRound,
  ChatLineSquare,
  MessageBox,
  Message,
  Histogram,
  PieChart,
  TrendCharts,
  DataBoard,
  Grid,
  Menu,
  Tools,
  Stamp,
  Reading,
  ReadingFilled,
  Avatar,
  Checked,
  ElementPlus
};

const getIcon = (iconName?: string) => {
  if (!iconName) return undefined;
  return iconMap[iconName];
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
