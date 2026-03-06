<template>
  <aside class="bm-sidebar" :class="{ collapsed, 'mobile-open': mobileOpen }">
    <!-- Logo 区域 -->
    <div class="bm-sidebar__logo" @click="handleLogoClick">
      <div class="bm-sidebar__logo-icon">
        <span class="logo-text">B</span>
      </div>
      <transition name="bm-sidebar-fade">
        <span v-show="!collapsed" class="bm-sidebar__logo-text">BigModel</span>
      </transition>
    </div>

    <!-- 菜单列表 -->
    <nav class="bm-sidebar__menu">
      <div
        v-for="group in menuGroups"
        :key="group.id"
        class="bm-sidebar__group"
      >
        <!-- 分组标题 (仅在未折叠时显示) -->
        <transition name="bm-sidebar-fade">
          <div v-show="!collapsed && group.title" class="bm-sidebar__group-title">
            {{ group.title }}
          </div>
        </transition>

        <!-- 菜单项 -->
        <div class="bm-sidebar__items">
          <template v-for="menu in group.menus" :key="menu.id">
            <!-- 有子菜单 -->
            <div
              v-if="menu.children && menu.children.length"
              class="bm-sidebar__submenu"
              :class="{ 'is-opened': isOpened(menu.id), 'is-active': isMenuActive(menu) }"
            >
              <div
                class="bm-sidebar__submenu-title"
                @click="toggleSubmenu(menu.id)"
              >
                <bm-icon
                  v-if="menu.icon"
                  :icon="menu.icon"
                  class="bm-sidebar__menu-icon"
                />
                <span class="bm-sidebar__menu-text">{{ menu.name }}</span>
                <span
                  class="bm-sidebar__arrow"
                  :class="{ 'is-rotated': isOpened(menu.id) }"
                >
                  <bm-icon icon="›" />
                </span>
              </div>

              <transition name="bm-sidebar-slide">
                <div v-show="isOpened(menu.id)" class="bm-sidebar__submenu-content">
                  <div
                    v-for="subMenu in menu.children"
                    :key="subMenu.id"
                    class="bm-sidebar__submenu-item"
                    :class="{ 'is-active': isActive(subMenu.path) }"
                    @click="handleMenuClick(subMenu)"
                  >
                    <bm-icon
                      v-if="subMenu.icon"
                      :icon="subMenu.icon"
                      class="bm-sidebar__menu-icon"
                    />
                    <span class="bm-sidebar__menu-text">{{ subMenu.name }}</span>
                  </div>
                </div>
              </transition>
            </div>

            <!-- 无子菜单 -->
            <div
              v-else
              class="bm-sidebar__menu-item"
              :class="{ 'is-active': isActive(menu.path) }"
              @click="handleMenuClick(menu)"
            >
              <bm-icon
                v-if="menu.icon"
                :icon="menu.icon"
                class="bm-sidebar__menu-icon"
              />
              <span class="bm-sidebar__menu-text">{{ menu.name }}</span>
            </div>
          </template>
        </div>
      </div>
    </nav>

    <!-- 折叠按钮 -->
    <div class="bm-sidebar__toggle" @click="$emit('toggle')">
      <span class="bm-sidebar__toggle-icon" :class="{ 'is-rotated': collapsed }">
        <bm-icon icon="‹" />
      </span>
    </div>

    <!-- 移动端遮罩 -->
    <div
      v-if="mobileOpen"
      class="bm-sidebar__overlay"
      @click="$emit('close-mobile')"
    ></div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import BmIcon from '../other/BmIcon.vue';

defineOptions({
  name: 'BmSidebar'
});

export interface MenuItem {
  id: string;
  name: string;
  path: string;
  icon?: string;
  children?: MenuItem[];
}

export interface MenuGroup {
  id: string;
  title?: string;
  menus: MenuItem[];
}

interface Props {
  collapsed?: boolean;
  mobileOpen?: boolean;
  menuGroups: MenuGroup[];
}

const props = withDefaults(defineProps<Props>(), {
  collapsed: false,
  mobileOpen: false
});

const emit = defineEmits<{
  toggle: [];
  'close-mobile': [];
  'menu-click': [menu: MenuItem];
}>();

const route = useRoute();
const openedSubmenus = ref<Set<string>>(new Set());

// 当前激活的菜单
const activeMenu = computed(() => route.path);

// 判断菜单是否激活
const isActive = (path: string) => {
  return activeMenu.value === path;
};

// 判断菜单或其子菜单是否激活
const isMenuActive = (menu: MenuItem) => {
  if (menu.path && isActive(menu.path)) return true;
  if (menu.children) {
    return menu.children.some(child => child.path && isActive(child.path));
  }
  return false;
};

// 判断子菜单是否打开
const isOpened = (menuId: string) => {
  return openedSubmenus.value.has(menuId);
};

// 切换子菜单
const toggleSubmenu = (menuId: string) => {
  if (openedSubmenus.value.has(menuId)) {
    openedSubmenus.value.delete(menuId);
  } else {
    openedSubmenus.value.add(menuId);
  }
  // 触发响应式更新
  openedSubmenus.value = new Set(openedSubmenus.value);
};

// 处理菜单点击
const handleMenuClick = (menu: MenuItem) => {
  emit('menu-click', menu);
};

// 处理 Logo 点击
const handleLogoClick = () => {
  emit('menu-click', {
    id: 'home',
    name: 'Home',
    path: '/'
  });
};
</script>

<style scoped lang="scss">
.bm-sidebar {
  position: relative;
  width: var(--bm-sidebar-width);
  height: 100%;
  background: var(--bm-bg-white);
  border-right: 1px solid var(--bm-border);
  display: flex;
  flex-direction: column;
  transition: width var(--bm-transition-normal);
  z-index: var(--bm-z-sidebar);
  flex-shrink: 0;

  &.collapsed {
    width: var(--bm-sidebar-collapsed-width);

    .bm-sidebar__logo-text,
    .bm-sidebar__group-title,
    .bm-sidebar__menu-text,
    .bm-sidebar__arrow {
      opacity: 0;
      pointer-events: none;
    }

    .bm-sidebar__submenu-content {
      display: none !important;
    }
  }

  @media (max-width: 767px) {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    transform: translateX(-100%);
    transition: transform var(--bm-transition-normal);

    &.mobile-open {
      transform: translateX(0);
    }
  }
}

.bm-sidebar__logo {
  height: var(--bm-header-height);
  display: flex;
  align-items: center;
  padding: 0 var(--bm-space-lg);
  cursor: pointer;
  border-bottom: 1px solid var(--bm-border-light);
  transition: all var(--bm-transition-normal);
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: var(--bm-primary-gradient);
    transform: scaleX(0);
    transition: transform var(--bm-transition-normal);
  }

  &:hover::after {
    transform: scaleX(1);
  }
}

.bm-sidebar__logo-icon {
  width: 32px;
  height: 32px;
  background: var(--bm-primary-gradient);
  border-radius: var(--bm-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: var(--bm-font-weight-semibold);
  font-size: var(--bm-font-size-lg);
  flex-shrink: 0;
  box-shadow: var(--bm-shadow-sm);
  transition: all var(--bm-transition-normal);

  .logo-text {
    line-height: 1;
  }
}

.bm-sidebar:hover .bm-sidebar__logo-icon {
  transform: scale(1.05);
  box-shadow: var(--bm-shadow-md);
}

.bm-sidebar__logo-text {
  margin-left: var(--bm-space-md);
  font-size: var(--bm-font-size-lg);
  font-weight: var(--bm-font-weight-semibold);
  color: var(--bm-text-primary);
  white-space: nowrap;
  transition: opacity var(--bm-transition-normal);
}

.bm-sidebar__menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: var(--bm-space-sm) 0;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--bm-border);
    border-radius: var(--bm-radius-sm);

    &:hover {
      background: var(--bm-border-dark);
    }
  }
}

.bm-sidebar__group {
  margin-bottom: var(--bm-space-sm);
}

.bm-sidebar__group-title {
  padding: var(--bm-space-sm) var(--bm-space-lg);
  font-size: var(--bm-font-size-xs);
  font-weight: var(--bm-font-weight-medium);
  color: var(--bm-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.bm-sidebar__items {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.bm-sidebar__menu-item,
.bm-sidebar__submenu-title {
  display: flex;
  align-items: center;
  padding: var(--bm-space-sm) var(--bm-space-lg);
  margin: 0 var(--bm-space-sm);
  border-radius: var(--bm-radius-md);
  cursor: pointer;
  color: var(--bm-text-secondary);
  transition: all var(--bm-transition-fast);
  position: relative;
  user-select: none;

  &:hover {
    background: var(--bm-bg-hover);
    color: var(--bm-text-primary);
  }

  &.is-active {
    background: var(--bm-primary-light);
    color: var(--bm-primary);
    font-weight: var(--bm-font-weight-medium);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background: var(--bm-primary);
      border-radius: 0 var(--bm-radius-sm) var(--bm-radius-sm) 0;
    }
  }
}

.bm-sidebar__menu-icon {
  font-size: var(--bm-font-size-lg);
  flex-shrink: 0;
  margin-right: var(--bm-space-sm);
}

.bm-sidebar__menu-text {
  flex: 1;
  font-size: var(--bm-font-size-base);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: opacity var(--bm-transition-normal);
}

.bm-sidebar__arrow {
  margin-left: var(--bm-space-sm);
  transition: transform var(--bm-transition-normal), opacity var(--bm-transition-normal);

  &.is-rotated {
    transform: rotate(90deg);
  }
}

.bm-sidebar__submenu {
  &.is-active .bm-sidebar__submenu-title {
    color: var(--bm-primary);
  }
}

.bm-sidebar__submenu-content {
  overflow: hidden;
  padding-left: var(--bm-space-lg);
}

.bm-sidebar__submenu-item {
  display: flex;
  align-items: center;
  padding: var(--bm-space-xs) var(--bm-space-md);
  margin: 0 var(--bm-space-sm) 2px var(--bm-space-md);
  border-radius: var(--bm-radius-sm);
  cursor: pointer;
  color: var(--bm-text-secondary);
  transition: all var(--bm-transition-fast);
  font-size: var(--bm-font-size-sm);

  &:hover {
    background: var(--bm-bg-hover);
    color: var(--bm-text-primary);
  }

  &.is-active {
    background: var(--bm-primary-light);
    color: var(--bm-primary);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 2px;
      height: 16px;
      background: var(--bm-primary);
      border-radius: 0 var(--bm-radius-xs) var(--bm-radius-xs) 0;
    }
  }

  .bm-sidebar__menu-icon {
    font-size: var(--bm-font-size-base);
    margin-right: var(--bm-space-xs);
  }

  .bm-sidebar__menu-text {
    font-size: var(--bm-font-size-sm);
  }
}

.bm-sidebar__toggle {
  height: var(--bm-header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid var(--bm-border-light);
  cursor: pointer;
  transition: all var(--bm-transition-fast);

  &:hover {
    background: var(--bm-bg-hover);
  }
}

.bm-sidebar__toggle-icon {
  transition: transform var(--bm-transition-normal);

  &.is-rotated {
    transform: rotate(180deg);
  }
}

.bm-sidebar__overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: calc(var(--bm-z-sidebar) - 1);

  @media (min-width: 768px) {
    display: none;
  }
}

// 过渡动画
.bm-sidebar-fade-enter-active,
.bm-sidebar-fade-leave-active {
  transition: opacity var(--bm-transition-fast);
}

.bm-sidebar-fade-enter-from,
.bm-sidebar-fade-leave-to {
  opacity: 0;
}

.bm-sidebar-slide-enter-active,
.bm-sidebar-slide-leave-active {
  transition: all var(--bm-transition-normal);
}

.bm-sidebar-slide-enter-from,
.bm-sidebar-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
