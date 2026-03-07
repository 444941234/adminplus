<template>
  <div class="app-header">
    <div class="header-left">
      <el-icon
        class="toggle-btn"
        @click="$emit('toggle')"
      >
        <Fold v-if="!collapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <!-- 主题切换 -->
      <el-tooltip :content="isDark ? '切换到浅色模式' : '切换到深色模式'" placement="bottom">
        <el-icon class="header-icon" @click="toggleTheme">
          <Sunny v-if="isDark" />
          <Moon v-else />
        </el-icon>
      </el-tooltip>

      <!-- 用户菜单 -->
      <el-dropdown trigger="click" @command="$emit('command', $event)">
        <div class="user-info">
          <el-avatar :size="32" :src="user.avatar" class="user-avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="user-name">{{ user.nickname }}</span>
          <el-icon class="arrow-icon"><ArrowDown /></el-icon>
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
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Fold, Expand, User, Setting, SwitchButton, ArrowDown, Sunny, Moon } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'

export interface UserInfo {
  nickname: string
  avatar?: string
}

interface Props {
  user: UserInfo
  collapsed: boolean
}

defineProps<Props>()
defineEmits<{
  toggle: []
  command: [command: string]
}>()

const route = useRoute()
const themeStore = useThemeStore()

const isDark = computed(() => themeStore.isDark)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta?.title as string
  }))
})

const toggleTheme = () => {
  themeStore.toggleTheme()
}
</script>

<style scoped lang="scss">
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 20px;
  background-color: var(--header-bg);
  border-bottom: 1px solid var(--header-border);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    color: var(--primary-color);
    background-color: var(--bg-hover);
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    color: var(--primary-color);
    background-color: var(--bg-hover);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: var(--bg-hover);
  }

  .user-avatar {
    background: linear-gradient(135deg, #3B82F6 0%, #6366F1 100%);
  }

  .user-name {
    color: var(--text-primary);
    font-size: 14px;
    font-weight: 500;
  }

  .arrow-icon {
    font-size: 12px;
    color: var(--text-secondary);
  }
}
</style>