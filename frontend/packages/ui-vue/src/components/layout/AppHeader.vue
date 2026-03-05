<template>
  <header class="app-header">
    <!-- 左侧：折叠按钮 + 面包屑 -->
    <div class="header-left">
      <el-button
        :icon="collapsed ? Expand : Fold"
        circle
        @click="$emit('toggle')"
      />
      <el-breadcrumb separator="/">
        <el-breadcrumb-item
          v-for="item in breadcrumbs"
          :key="item.path"
          :to="item.path"
        >
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 中间：搜索框 -->
    <div class="header-center">
      <el-input
        v-model="searchText"
        placeholder="AI 智能搜索..."
        :prefix-icon="Search"
        class="search-input"
        clearable
        @input="handleSearch"
      />
    </div>

    <!-- 右侧：用户信息 -->
    <div class="header-right">
      <!-- 通知 -->
      <el-badge :value="notificationCount" :max="99">
        <el-button :icon="Bell" circle />
      </el-badge>

      <!-- 用户下拉 -->
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :src="user.avatar" :size="36" />
          <span class="user-name">{{ user.nickname }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import {
  Expand,
  Fold,
  Search,
  Bell,
  User,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue';

export interface BreadcrumbItem {
  title: string;
  path: string;
}

export interface UserInfo {
  nickname: string;
  avatar: string;
}

defineOptions({
  name: 'AppHeader'
});

interface Props {
  collapsed?: boolean;
  user: UserInfo;
  breadcrumbs?: BreadcrumbItem[];
  notificationCount?: number;
}

withDefaults(defineProps<Props>(), {
  collapsed: false,
  breadcrumbs: () => [],
  notificationCount: 0
});

const emit = defineEmits<{
  toggle: [];
  search: [value: string];
  command: [command: string];
}>();

const searchText = ref('');

const handleSearch = (value: string) => {
  emit('search', value);
};

const handleCommand = (command: string) => {
  emit('command', command);
};
</script>

<style scoped>
.app-header {
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-lg);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  transition: all var(--transition-normal);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.header-left :deep(.el-button) {
  transition: all var(--transition-normal);
}

.header-left :deep(.el-button:hover) {
  transform: rotate(180deg);
}

.header-center {
  flex: 1;
  max-width: 500px;
  margin: 0;
}

.search-input {
  width: 100%;
}

:deep(.search-input .el-input__wrapper) {
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
  background: var(--bg-secondary);
}

:deep(.search-input .el-input__wrapper:hover) {
  box-shadow: var(--shadow-md);
  border-color: var(--border-color);
}

:deep(.search-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(91, 127, 255, 0.1);
  border-color: var(--primary-color);
  background: var(--bg-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.header-right :deep(.el-badge) {
  cursor: pointer;
}

.header-right :deep(.el-button) {
  transition: all var(--transition-normal);
}

.header-right :deep(.el-button:hover) {
  transform: scale(1.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
}

.user-info:hover {
  background: var(--bg-secondary);
}

.user-info :deep(.el-avatar) {
  border: 2px solid var(--border-light);
  transition: all var(--transition-normal);
}

.user-info:hover :deep(.el-avatar) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(91, 127, 255, 0.1);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 响应式 */
@media (max-width: 767px) {
  .app-header {
    padding: 0 var(--space-md);
  }

  .header-center {
    display: none;
  }

  .user-name {
    display: none;
  }
}
</style>
