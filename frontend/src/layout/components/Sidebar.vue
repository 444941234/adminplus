<template>
  <el-aside :width="sidebarWidth">
    <!-- Logo 区域 -->
    <div class="logo">
      <div class="logo-icon">
        <svg
          viewBox="0 0 24 24"
          fill="currentColor"
          class="logo-svg"
        >
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
        </svg>
      </div>
      <transition name="fade">
        <span
          v-show="!collapsed"
          class="logo-text"
        >AdminPlus</span>
      </transition>
    </div>

    <!-- 菜单 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      background-color="#FFFFFF"
      text-color="#333333"
      active-text-color="#0066FF"
    >
      <SidebarMenu
        :items="menus"
        :collapsed="collapsed"
      />
    </el-menu>

    <!-- 折叠按钮 -->
    <div class="collapse-btn">
      <el-button
        text
        @click="toggleCollapse"
      >
        <el-icon :size="18">
          <component :is="collapsed ? Expand : Fold" />
        </el-icon>
      </el-button>
    </div>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
// eslint-disable-next-line no-unused-vars
import { Expand, Fold } from '@element-plus/icons-vue';
import SidebarMenu from './SidebarMenu.vue';

defineOptions({
  name: 'Sidebar',
});

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false,
  },
  menus: {
    type: Array,
    required: true,
    default: () => [],
  },
});

const emit = defineEmits(['update:collapsed']);

const route = useRoute();

const activeMenu = computed(() => route.path);

const sidebarWidth = computed(() => {
  return props.collapsed ? '64px' : '240px';
});

const toggleCollapse = () => {
  emit('update:collapsed', !props.collapsed);
};
</script>

<style scoped lang="scss">
.el-aside {
  background-color: #ffffff;
  border-right: 1px solid #e5e7eb;
  overflow-x: hidden;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid #e5e7eb;
  overflow: hidden;
}

.logo-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0066ff 0%, #7b5fd6 100%);
  border-radius: 8px;
}

.logo-svg {
  width: 20px;
  height: 20px;
  color: #ffffff;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(135deg, #0066ff 0%, #7b5fd6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.el-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 菜单项样式 */
:deep(.el-menu-item) {
  border-right: 3px solid transparent;
  transition: all 0.3s;
  margin: 4px 8px;
  border-radius: 8px;
}

:deep(.el-menu-item.is-active) {
  background-color: #e8f0fe !important;
  color: #0066ff !important;
  font-weight: 600;
}

:deep(.el-menu-item:hover) {
  background-color: #f5f7fa;
  color: #0066ff;
}

:deep(.el-sub-menu__title) {
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #f5f7fa;
  color: #0066ff;
}

:deep(.el-sub-menu .el-menu-item) {
  margin: 4px 8px 4px 24px;
  border-radius: 6px;
}

/* 折叠状态样式 */
:deep(.el-menu--collapse) {
  .el-menu-item,
  .el-sub-menu__title {
    margin: 4px;
    padding-left: 20px !important;
    justify-content: center;
  }

  .el-sub-menu .el-menu-item {
    margin: 4px;
    padding-left: 20px !important;
  }
}

.collapse-btn {
  padding: 12px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: center;

  .el-button {
    width: 100%;
    height: 40px;
    border-radius: 8px;

    &:hover {
      background-color: #f5f7fa;
    }
  }
}
</style>
