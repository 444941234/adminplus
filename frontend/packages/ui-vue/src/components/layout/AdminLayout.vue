<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <app-sidebar
      :collapsed="collapsed"
      :mobile-open="mobileOpen"
      :menus="menus"
      @toggle="handleToggle"
    />

    <!-- 主内容区 -->
    <div class="main-wrapper" :class="{ 'sidebar-collapsed': collapsed }">
      <!-- 顶部导航 -->
      <app-header
        :collapsed="collapsed"
        :user="user"
        :breadcrumbs="breadcrumbs"
        :notification-count="notificationCount"
        @toggle="handleToggle"
        @search="handleSearch"
        @command="handleCommand"
      />

      <!-- 内容区域 -->
      <div class="content-area">
        <slot />
      </div>
    </div>

    <!-- 移动端遮罩 -->
    <transition name="fade">
      <div
        v-if="mobileOpen"
        class="mobile-overlay"
        @click="mobileOpen = false"
      ></div>
    </transition>

    <!-- 浮动面板 -->
    <floating-panel
      @theme-change="handleThemeChange"
      @config="handleConfig"
      @clear-cache="handleClearCache"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import AppSidebar from './AppSidebar.vue';
import AppHeader from './AppHeader.vue';
import FloatingPanel from './FloatingPanel.vue';
import type { MenuItem, UserInfo, BreadcrumbItem } from './AppHeader.vue';

defineOptions({
  name: 'AdminLayout'
});

interface Props {
  menus: MenuItem[];
  user: UserInfo;
  breadcrumbs?: BreadcrumbItem[];
  notificationCount?: number;
}

withDefaults(defineProps<Props>(), {
  breadcrumbs: () => [],
  notificationCount: 0
});

const emit = defineEmits<{
  search: [value: string];
  command: [command: string];
  themeChange: [];
  config: [];
  clearCache: [];
}>();

const collapsed = ref(false);
const mobileOpen = ref(false);
const isMobile = ref(false);

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768;
  if (isMobile.value) {
    collapsed.value = true;
  }
};

const handleToggle = () => {
  if (isMobile.value) {
    mobileOpen.value = !mobileOpen.value;
  } else {
    collapsed.value = !collapsed.value;
  }
};

const handleSearch = (value: string) => {
  emit('search', value);
};

const handleCommand = (command: string) => {
  emit('command', command);
};

const handleThemeChange = () => {
  emit('themeChange');
};

const handleConfig = () => {
  emit('config');
};

const handleClearCache = () => {
  emit('clearCache');
};

onMounted(() => {
  checkMobile();
  window.addEventListener('resize', checkMobile);
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
});
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  height: 100%;
  background: #f0f2f5;
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  margin-left: 0;
  transition: margin-left 0.2s ease;
}

.main-wrapper.sidebar-collapsed {
  margin-left: 0;
}

.content-area {
  flex: 1;
  padding: var(--space-lg);
  overflow-y: auto;
  background: #f0f2f5;
  min-height: calc(100vh - var(--header-height));
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: calc(var(--z-fixed) - 1);
  backdrop-filter: blur(2px);
}

@media (max-width: 767px) {
  .content-area {
    padding: var(--space-md);
  }
}
</style>
