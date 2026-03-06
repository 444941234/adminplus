<template>
  <header class="bm-header">
    <!-- Left: Collapse button + Breadcrumbs -->
    <div class="bm-header__left">
      <button
        class="bm-header__toggle"
        :class="{ 'is-collapsed': collapsed }"
        @click="$emit('toggle')"
        aria-label="Toggle sidebar"
      >
        <span class="bm-icon">{{ collapsed ? '☰' : '☰' }}</span>
      </button>
      <nav v-if="breadcrumbs.length > 0" class="bm-header__breadcrumbs" aria-label="Breadcrumb">
        <ol class="bm-header__breadcrumb-list">
          <li
            v-for="(item, index) in breadcrumbs"
            :key="item.path"
            class="bm-header__breadcrumb-item"
          >
            <router-link
              v-if="index < breadcrumbs.length - 1"
              :to="item.path"
              class="bm-header__breadcrumb-link"
            >
              {{ item.title }}
            </router-link>
            <span v-else class="bm-header__breadcrumb-current">
              {{ item.title }}
            </span>
            <span
              v-if="index < breadcrumbs.length - 1"
              class="bm-header__breadcrumb-separator"
            >
              /
            </span>
          </li>
        </ol>
      </nav>
    </div>

    <!-- Center: Search -->
    <div class="bm-header__center">
      <div class="bm-header__search">
        <span class="bm-header__search-icon">🔍</span>
        <input
          v-model="searchText"
          type="text"
          class="bm-header__search-input"
          placeholder="Search..."
          @input="handleSearch"
        />
        <button
          v-if="searchText"
          class="bm-header__search-clear"
          @click="clearSearch"
          aria-label="Clear search"
        >
          ✕
        </button>
      </div>
    </div>

    <!-- Right: Notifications + User info -->
    <div class="bm-header__right">
      <!-- Notifications -->
      <button
        class="bm-header__notification"
        :class="{ 'has-badge': notificationCount > 0 }"
        @click="$emit('notification-click')"
        aria-label="Notifications"
      >
        <span class="bm-header__notification-icon">🔔</span>
        <span
          v-if="notificationCount > 0"
          class="bm-header__notification-badge"
        >
          {{ notificationCount > 99 ? '99+' : notificationCount }}
        </span>
      </button>

      <!-- User dropdown -->
      <div class="bm-header__user" ref="userDropdownRef">
        <button
          class="bm-header__user-button"
          @click="toggleUserMenu"
          aria-label="User menu"
          aria-haspopup="true"
          :aria-expanded="isUserMenuOpen"
        >
          <img
            :src="user.avatar"
            :alt="user.nickname"
            class="bm-header__user-avatar"
          />
          <span class="bm-header__user-name">{{ user.nickname }}</span>
          <span class="bm-header__user-arrow" :class="{ 'is-open': isUserMenuOpen }">
            ▼
          </span>
        </button>

        <Transition name="bm-dropdown">
          <div v-if="isUserMenuOpen" class="bm-header__user-dropdown">
            <ul class="bm-header__user-menu">
              <li>
                <button
                  class="bm-header__user-menu-item"
                  @click="handleCommand('profile')"
                >
                  <span class="bm-header__user-menu-icon">👤</span>
                  Profile
                </button>
              </li>
              <li>
                <button
                  class="bm-header__user-menu-item"
                  @click="handleCommand('settings')"
                >
                  <span class="bm-header__user-menu-icon">⚙️</span>
                  Settings
                </button>
              </li>
              <li class="bm-header__user-menu-divider"></li>
              <li>
                <button
                  class="bm-header__user-menu-item bm-header__user-menu-item--danger"
                  @click="handleCommand('logout')"
                >
                  <span class="bm-header__user-menu-icon">🚪</span>
                  Logout
                </button>
              </li>
            </ul>
          </div>
        </Transition>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

defineOptions({
  name: 'BmHeader'
});

export interface BreadcrumbItem {
  title: string;
  path: string;
}

export interface UserInfo {
  nickname: string;
  avatar: string;
}

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
  'notification-click': [];
  command: [command: string];
}>();

const searchText = ref('');
const isUserMenuOpen = ref(false);
const userDropdownRef = ref<HTMLElement | null>(null);

const handleSearch = () => {
  emit('search', searchText.value);
};

const clearSearch = () => {
  searchText.value = '';
  emit('search', '');
};

const toggleUserMenu = () => {
  isUserMenuOpen.value = !isUserMenuOpen.value;
};

const handleCommand = (command: string) => {
  isUserMenuOpen.value = false;
  emit('command', command);
};

// Close dropdown when clicking outside
const handleClickOutside = (event: MouseEvent) => {
  if (userDropdownRef.value && !userDropdownRef.value.contains(event.target as Node)) {
    isUserMenuOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped lang="scss">
.bm-header {
  height: var(--bm-header-height);
  background: var(--bm-bg-white);
  border-bottom: 1px solid var(--bm-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--bm-space-lg);
  position: sticky;
  top: 0;
  z-index: var(--bm-z-header);
  transition: all var(--bm-transition-normal);
}

.bm-header__left {
  display: flex;
  align-items: center;
  gap: var(--bm-space-md);
  flex: 0 0 auto;
}

.bm-header__toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: var(--bm-radius-sm);
  cursor: pointer;
  transition: all var(--bm-transition-fast);
  color: var(--bm-text-secondary);

  &:hover {
    background: var(--bm-bg-hover);
    color: var(--bm-primary);
  }

  &:active {
    background: var(--bm-bg-active);
  }

  .bm-icon {
    font-size: var(--bm-font-size-lg);
    line-height: 1;
  }
}

.bm-header__breadcrumbs {
  display: flex;
  align-items: center;
}

.bm-header__breadcrumb-list {
  display: flex;
  align-items: center;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: var(--bm-space-sm);
}

.bm-header__breadcrumb-item {
  display: flex;
  align-items: center;
  gap: var(--bm-space-sm);
  font-size: var(--bm-font-size-sm);
}

.bm-header__breadcrumb-link {
  color: var(--bm-text-secondary);
  text-decoration: none;
  transition: color var(--bm-transition-fast);

  &:hover {
    color: var(--bm-primary);
  }
}

.bm-header__breadcrumb-current {
  color: var(--bm-text-primary);
  font-weight: var(--bm-font-weight-medium);
}

.bm-header__breadcrumb-separator {
  color: var(--bm-text-tertiary);
}

.bm-header__center {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 var(--bm-space-xl);
  min-width: 0;
}

.bm-header__search {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 400px;
  background: var(--bm-bg-page);
  border: 1px solid var(--bm-border);
  border-radius: var(--bm-radius-lg);
  padding: 0 var(--bm-space-md);
  transition: all var(--bm-transition-normal);

  &:hover {
    border-color: var(--bm-primary-light);
  }

  &:focus-within {
    border-color: var(--bm-primary);
    box-shadow: 0 0 0 3px var(--bm-primary-light);
    background: var(--bm-bg-white);
  }
}

.bm-header__search-icon {
  color: var(--bm-text-tertiary);
  font-size: var(--bm-font-size-base);
  margin-right: var(--bm-space-sm);
}

.bm-header__search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: var(--bm-font-size-sm);
  color: var(--bm-text-primary);
  padding: var(--bm-space-sm) 0;

  &::placeholder {
    color: var(--bm-text-tertiary);
  }
}

.bm-header__search-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: none;
  background: var(--bm-bg-disabled);
  border-radius: var(--bm-radius-xs);
  cursor: pointer;
  color: var(--bm-text-tertiary);
  font-size: 12px;
  transition: all var(--bm-transition-fast);

  &:hover {
    background: var(--bm-border-dark);
    color: var(--bm-text-secondary);
  }
}

.bm-header__right {
  display: flex;
  align-items: center;
  gap: var(--bm-space-md);
  flex: 0 0 auto;
}

.bm-header__notification {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: var(--bm-radius-sm);
  cursor: pointer;
  transition: all var(--bm-transition-fast);
  color: var(--bm-text-secondary);

  &:hover {
    background: var(--bm-bg-hover);
    color: var(--bm-primary);
  }

  &:active {
    background: var(--bm-bg-active);
  }
}

.bm-header__notification-icon {
  font-size: var(--bm-font-size-lg);
  line-height: 1;
}

.bm-header__notification-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: var(--bm-danger);
  color: white;
  font-size: 10px;
  font-weight: var(--bm-font-weight-medium);
  border-radius: var(--bm-radius-xs);
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.bm-header__user {
  position: relative;
}

.bm-header__user-button {
  display: flex;
  align-items: center;
  gap: var(--bm-space-sm);
  padding: var(--bm-space-sm) var(--bm-space-md);
  border: none;
  background: transparent;
  border-radius: var(--bm-radius-lg);
  cursor: pointer;
  transition: all var(--bm-transition-fast);

  &:hover {
    background: var(--bm-bg-hover);
  }
}

.bm-header__user-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--bm-radius-md);
  object-fit: cover;
  border: 2px solid var(--bm-border-light);
  transition: all var(--bm-transition-fast);
}

.bm-header__user-button:hover .bm-header__user-avatar {
  border-color: var(--bm-primary);
}

.bm-header__user-name {
  font-size: var(--bm-font-size-sm);
  font-weight: var(--bm-font-weight-medium);
  color: var(--bm-text-primary);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bm-header__user-arrow {
  font-size: 10px;
  color: var(--bm-text-tertiary);
  transition: transform var(--bm-transition-fast);

  &.is-open {
    transform: rotate(180deg);
  }
}

.bm-header__user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 180px;
  background: var(--bm-bg-white);
  border: 1px solid var(--bm-border);
  border-radius: var(--bm-radius-md);
  box-shadow: var(--bm-shadow-lg);
  z-index: var(--bm-z-dropdown);
  overflow: hidden;
}

.bm-header__user-menu {
  list-style: none;
  margin: 0;
  padding: var(--bm-space-sm) 0;
}

.bm-header__user-menu-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: var(--bm-space-sm);
  padding: var(--bm-space-sm) var(--bm-space-md);
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: var(--bm-font-size-sm);
  color: var(--bm-text-primary);
  text-align: left;
  transition: all var(--bm-transition-fast);

  &:hover {
    background: var(--bm-bg-hover);
    color: var(--bm-primary);
  }

  &--danger {
    color: var(--bm-danger);

    &:hover {
      background: var(--bm-danger-light);
    }
  }
}

.bm-header__user-menu-icon {
  font-size: var(--bm-font-size-base);
  line-height: 1;
}

.bm-header__user-menu-divider {
  height: 1px;
  background: var(--bm-border-light);
  margin: var(--bm-space-sm) 0;
}

// Dropdown transition
.bm-dropdown-enter-active,
.bm-dropdown-leave-active {
  transition: all var(--bm-transition-normal);
}

.bm-dropdown-enter-from,
.bm-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

// Responsive
@media (max-width: 768px) {
  .bm-header {
    padding: 0 var(--bm-space-md);
  }

  .bm-header__center {
    display: none;
  }

  .bm-header__user-name {
    display: none;
  }

  .bm-header__breadcrumbs {
    display: none;
  }
}
</style>
