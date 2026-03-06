<template>
  <div class="bm-layout">
    <!-- Sidebar -->
    <aside
      class="bm-layout__sidebar"
      :class="{
        'bm-layout__sidebar--collapsed': collapsed,
        'bm-layout__sidebar--mobile-open': mobileOpen
      }"
    >
      <slot name="sidebar">
        <div v-if="logo" class="bm-layout__logo">
          <component :is="logo" class="bm-layout__logo-icon" />
          <transition name="bm-fade">
            <span v-show="!collapsed" class="bm-layout__logo-text">{{ logoText }}</span>
          </transition>
        </div>
      </slot>
    </aside>

    <!-- Main content wrapper -->
    <div
      class="bm-layout__main"
      :class="{
        'bm-layout__main--collapsed': collapsed
      }"
    >
      <!-- Header -->
      <header class="bm-layout__header">
        <slot name="header">
          <div class="bm-layout__toggle">
            <button
              class="bm-layout__toggle-btn"
              :aria-label="collapsed ? 'Expand sidebar' : 'Collapse sidebar'"
              @click="handleToggle"
            >
              <span class="bm-layout__toggle-icon" :class="{ 'bm-layout__toggle-icon--collapsed': collapsed }"></span>
            </button>
          </div>
        </slot>
      </header>

      <!-- Content area -->
      <main class="bm-layout__content">
        <slot />
      </main>

      <!-- Footer (optional) -->
      <footer v-if="$slots.footer" class="bm-layout__footer">
        <slot name="footer" />
      </footer>
    </div>

    <!-- Mobile overlay -->
    <transition name="bm-fade">
      <div
        v-if="mobileOpen"
        class="bm-layout__overlay"
        @click="mobileOpen = false"
      ></div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';

defineOptions({
  name: 'BmLayout'
});

export interface MenuItem {
  id: string;
  name: string;
  path: string;
  icon?: string;
  children?: MenuItem[];
}

interface Props {
  collapsed?: boolean;
  logo?: any;
  logoText?: string;
}

const props = withDefaults(defineProps<Props>(), {
  collapsed: false,
  logoText: 'AdminPlus'
});

const emit = defineEmits<{
  toggle: [];
  'update:collapsed': [value: boolean];
}>();

const internalCollapsed = ref(props.collapsed);
const mobileOpen = ref(false);
const isMobile = ref(false);

const collapsed = computed({
  get: () => internalCollapsed.value,
  set: (value: boolean) => {
    internalCollapsed.value = value;
    emit('update:collapsed', value);
  }
});

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768;
  if (isMobile.value && !internalCollapsed.value) {
    internalCollapsed.value = true;
  }
};

const handleToggle = () => {
  if (isMobile.value) {
    mobileOpen.value = !mobileOpen.value;
  } else {
    internalCollapsed.value = !internalCollapsed.value;
    emit('toggle');
    emit('update:collapsed', internalCollapsed.value);
  }
};

onMounted(() => {
  checkMobile();
  window.addEventListener('resize', checkMobile);
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
});
</script>

<style scoped lang="scss">
.bm-layout {
  display: flex;
  min-height: 100vh;
  width: 100%;
  background: var(--bm-bg-page);
  position: relative;
}

// Sidebar styles
.bm-layout__sidebar {
  width: var(--bm-sidebar-width);
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  background: var(--bm-bg-white);
  border-right: 1px solid var(--bm-border);
  transition: width var(--bm-transition-normal), transform var(--bm-transition-normal);
  z-index: var(--bm-z-sidebar);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;

  &--collapsed {
    width: var(--bm-sidebar-collapsed-width);
  }

  @media (max-width: 767px) {
    transform: translateX(-100%);

    &--mobile-open {
      transform: translateX(0);
    }
  }
}

.bm-layout__logo {
  height: var(--bm-header-height);
  display: flex;
  align-items: center;
  padding: 0 var(--bm-space-lg);
  border-bottom: 1px solid var(--bm-border-light);
  transition: all var(--bm-transition-normal);
  cursor: pointer;

  &:hover {
    background: var(--bm-bg-hover);
  }
}

.bm-layout__logo-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  color: var(--bm-primary);
}

.bm-layout__logo-text {
  margin-left: var(--bm-space-md);
  font-size: var(--bm-font-size-lg);
  font-weight: var(--bm-font-weight-semibold);
  color: var(--bm-text-primary);
  white-space: nowrap;
}

// Main content area
.bm-layout__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  margin-left: var(--bm-sidebar-width);
  transition: margin-left var(--bm-transition-normal);
  min-height: 100vh;

  &--collapsed {
    margin-left: var(--bm-sidebar-collapsed-width);
  }

  @media (max-width: 767px) {
    margin-left: 0 !important;
  }
}

// Header styles
.bm-layout__header {
  height: var(--bm-header-height);
  background: var(--bm-bg-white);
  border-bottom: 1px solid var(--bm-border);
  display: flex;
  align-items: center;
  padding: 0 var(--bm-space-lg);
  position: sticky;
  top: 0;
  z-index: var(--bm-z-header);
  backdrop-filter: blur(8px);
  background: rgba(255, 255, 255, 0.9);
}

.bm-layout__toggle {
  display: flex;
  align-items: center;
}

.bm-layout__toggle-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--bm-border);
  border-radius: var(--bm-radius-sm);
  background: var(--bm-bg-white);
  cursor: pointer;
  transition: all var(--bm-transition-fast);
  outline: none;

  &:hover {
    border-color: var(--bm-primary);
    background: var(--bm-primary-light);
  }

  &:active {
    transform: scale(0.95);
  }
}

.bm-layout__toggle-icon {
  position: relative;
  width: 16px;
  height: 2px;
  background: var(--bm-text-primary);
  transition: all var(--bm-transition-normal);

  &::before,
  &::after {
    content: '';
    position: absolute;
    width: 16px;
    height: 2px;
    background: var(--bm-text-primary);
    transition: all var(--bm-transition-normal);
  }

  &::before {
    top: -5px;
  }

  &::after {
    top: 5px;
  }

  &--collapsed {
    background: transparent;

    &::before {
      top: 0;
      transform: rotate(45deg);
    }

    &::after {
      top: 0;
      transform: rotate(-45deg);
    }
  }
}

// Content area
.bm-layout__content {
  flex: 1;
  padding: var(--bm-space-lg);
  overflow-y: auto;
  background: var(--bm-bg-page);
}

// Footer
.bm-layout__footer {
  padding: var(--bm-space-lg);
  border-top: 1px solid var(--bm-border);
  background: var(--bm-bg-white);
}

// Mobile overlay
.bm-layout__overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: calc(var(--bm-z-sidebar) - 1);
  backdrop-filter: blur(2px);
}

// Transitions
.bm-fade-enter-active,
.bm-fade-leave-active {
  transition: opacity var(--bm-transition-normal);
}

.bm-fade-enter-from,
.bm-fade-leave-to {
  opacity: 0;
}

// Responsive adjustments
@media (max-width: 767px) {
  .bm-layout__content {
    padding: var(--bm-space-md);
  }

  .bm-layout__header {
    padding: 0 var(--bm-space-md);
  }
}
</style>
