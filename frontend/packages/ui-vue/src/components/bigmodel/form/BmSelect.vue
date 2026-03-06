<template>
  <div
    ref="selectRef"
    class="bm-select"
    :class="[
      size,
      {
        disabled,
        focused: isFocused,
        opened: isOpen
      }
    ]"
    @click="handleToggle"
  >
    <!-- Select trigger -->
    <div class="bm-select__trigger">
      <!-- Selected value display -->
      <span v-if="selectedLabel" class="bm-select__value">
        {{ selectedLabel }}
      </span>
      <span v-else class="bm-select__placeholder">
        {{ placeholder }}
      </span>

      <!-- Clear button -->
      <span
        v-if="clearable && selectedValue && !disabled"
        class="bm-select__clear"
        @click.stop="handleClear"
      >
        ×
      </span>

      <!-- Arrow icon -->
      <span class="bm-select__arrow" :class="{ 'is-open': isOpen }">
        ▼
      </span>
    </div>

    <!-- Dropdown menu with Teleport -->
    <Teleport to="body">
      <Transition name="bm-select-dropdown">
        <div
          v-if="isOpen"
          ref="dropdownRef"
          class="bm-select__dropdown"
          :style="dropdownStyle"
        >
          <ul class="bm-select__options">
            <li
              v-for="option in options"
              :key="option.value"
              class="bm-select__option"
              :class="{
                'is-selected': option.value === selectedValue,
                'is-disabled': option.disabled
              }"
              @click.stop="handleSelect(option)"
            >
              {{ option.label }}
            </li>
          </ul>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue';

defineOptions({
  name: 'BmSelect'
});

interface SelectOption {
  label: string;
  value: string | number;
  disabled?: boolean;
}

interface Props {
  modelValue?: string | number;
  options: SelectOption[];
  placeholder?: string;
  size?: 'small' | 'medium' | 'large';
  disabled?: boolean;
  clearable?: boolean;
  teleported?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请选择',
  size: 'medium',
  disabled: false,
  clearable: false,
  teleported: true
});

const emit = defineEmits<{
  'update:modelValue': [value: string | number];
  'change': [value: string | number];
  'clear': [];
  'visible-change': [visible: boolean];
}>();

// Refs
const selectRef = ref<HTMLElement>();
const dropdownRef = ref<HTMLElement>();

// State
const isOpen = ref(false);
const isFocused = ref(false);
const selectedValue = ref(props.modelValue);
const dropdownStyle = ref({});

// Computed
const selectedLabel = computed(() => {
  const option = props.options.find(opt => opt.value === selectedValue.value);
  return option?.label || '';
});

// Methods
const handleToggle = () => {
  if (props.disabled) return;
  isOpen.value = !isOpen.value;
  updateDropdownPosition();
  emit('visible-change', isOpen.value);
};

const handleSelect = (option: SelectOption) => {
  if (option.disabled) return;
  selectedValue.value = option.value;
  isOpen.value = false;
  emit('update:modelValue', option.value);
  emit('change', option.value);
  emit('visible-change', false);
};

const handleClear = () => {
  selectedValue.value = '';
  emit('update:modelValue', '');
  emit('clear');
};

const updateDropdownPosition = () => {
  if (!selectRef.value || !isOpen.value) return;

  const rect = selectRef.value.getBoundingClientRect();
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;

  dropdownStyle.value = {
    position: 'absolute',
    top: `${rect.bottom + scrollTop + 4}px`,
    left: `${rect.left + scrollLeft}px`,
    width: `${rect.width}px`,
    zIndex: '1050'
  };
};

const handleClickOutside = (event: MouseEvent) => {
  if (
    selectRef.value &&
    !selectRef.value.contains(event.target as Node) &&
    dropdownRef.value &&
    !dropdownRef.value.contains(event.target as Node)
  ) {
    isOpen.value = false;
    emit('visible-change', false);
  }
};

const handleScroll = () => {
  if (isOpen.value) {
    updateDropdownPosition();
  }
};

const handleResize = () => {
  if (isOpen.value) {
    updateDropdownPosition();
  }
};

// Watchers
watch(() => props.modelValue, (newValue) => {
  selectedValue.value = newValue;
});

// Lifecycle
onMounted(() => {
  document.addEventListener('click', handleClickOutside);
  document.addEventListener('scroll', handleScroll, true);
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside);
  document.removeEventListener('scroll', handleScroll, true);
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped lang="scss">
.bm-select {
  position: relative;
  display: inline-block;
  width: 100%;
  font-family: var(--bm-font-family, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif);
  font-size: var(--bm-font-size-base, 14px);
  line-height: 1.5;
  color: var(--bm-text-primary, #1d2129);
  cursor: pointer;
  user-select: none;

  &.small {
    font-size: var(--bm-font-size-sm, 13px);
  }

  &.large {
    font-size: var(--bm-font-size-md, 16px);
  }

  &.disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }

  &__trigger {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    background: var(--bm-bg-white, #ffffff);
    border: 1px solid var(--bm-border, #e5e6eb);
    border-radius: var(--bm-radius-md, 8px);
    transition: all var(--bm-transition-fast, 100ms ease);

    .bm-select.focused & {
      border-color: var(--bm-primary, #165dff);
      box-shadow: 0 0 0 2px var(--bm-primary-light, rgba(22, 93, 255, 0.1));
    }

    .bm-select.opened & {
      border-color: var(--bm-primary, #165dff);
    }

    .bm-select.small & {
      padding: 6px 10px;
    }

    .bm-select.large & {
      padding: 10px 14px;
    }

    .bm-select.disabled & {
      background: var(--bm-bg-disabled, #f7f8fa);
      border-color: var(--bm-border-light, #f2f3f5);
    }
  }

  &__value {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__placeholder {
    flex: 1;
    color: var(--bm-text-tertiary, #86909c);
  }

  &__clear {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 16px;
    height: 16px;
    margin-right: 4px;
    font-size: 18px;
    color: var(--bm-text-tertiary, #86909c);
    border-radius: 50%;
    transition: all var(--bm-transition-fast, 100ms ease);

    &:hover {
      background: var(--bm-bg-hover, #f8f9fb);
      color: var(--bm-text-secondary, #4e5969);
    }
  }

  &__arrow {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: 8px;
    font-size: 10px;
    color: var(--bm-text-tertiary, #86909c);
    transition: transform var(--bm-transition-fast, 100ms ease);

    &.is-open {
      transform: rotate(180deg);
    }
  }

  &__dropdown {
    background: var(--bm-bg-white, #ffffff);
    border: 1px solid var(--bm-border, #e5e6eb);
    border-radius: var(--bm-radius-md, 8px);
    box-shadow: var(--bm-shadow-lg, 0 8px 24px rgba(0, 0, 0, 0.12));
    overflow: hidden;
    max-height: 274px;
    overflow-y: auto;
  }

  &__options {
    list-style: none;
    margin: 0;
    padding: 4px 0;
  }

  &__option {
    padding: 8px 12px;
    cursor: pointer;
    transition: all var(--bm-transition-fast, 100ms ease);

    &:hover:not(.is-disabled) {
      background: var(--bm-bg-hover, #f8f9fb);
      color: var(--bm-primary, #165dff);
    }

    &.is-selected {
      background: var(--bm-primary-light, rgba(22, 93, 255, 0.1));
      color: var(--bm-primary, #165dff);
      font-weight: var(--bm-font-weight-medium, 500);
    }

    &.is-disabled {
      cursor: not-allowed;
      color: var(--bm-text-disabled, #c9cdd4);
    }

    .bm-select.small & {
      padding: 6px 10px;
      font-size: var(--bm-font-size-sm, 13px);
    }

    .bm-select.large & {
      padding: 10px 14px;
      font-size: var(--bm-font-size-md, 16px);
    }
  }
}

// Dropdown transition
.bm-select-dropdown-enter-active,
.bm-select-dropdown-leave-active {
  transition: all var(--bm-transition-normal, 200ms ease);
  transform-origin: top center;
}

.bm-select-dropdown-enter-from,
.bm-select-dropdown-leave-to {
  opacity: 0;
  transform: scaleY(0.8) translateY(-8px);
}

.bm-select-dropdown-enter-to,
.bm-select-dropdown-leave-from {
  opacity: 1;
  transform: scaleY(1) translateY(0);
}

// Dark mode support
@media (prefers-color-scheme: dark) {
  .bm-select {
    color: var(--bm-text-primary, #e5e5e5);

    &__trigger {
      background: var(--bm-bg-page, #2a2a2a);
      border-color: var(--bm-border, #404040);
    }

    &__placeholder {
      color: var(--bm-text-tertiary, #86909c);
    }

    &__dropdown {
      background: var(--bm-bg-page, #2a2a2a);
      border-color: var(--bm-border, #404040);
    }

    &__option {
      &:hover:not(.is-disabled) {
        background: rgba(22, 93, 255, 0.15);
      }

      &.is-selected {
        background: rgba(22, 93, 255, 0.2);
      }
    }
  }
}

// Additional dark mode class support
.dark {
  .bm-select {
    color: var(--bm-text-primary, #e5e5e5);

    &__trigger {
      background: var(--bm-bg-page, #2a2a2a);
      border-color: var(--bm-border, #404040);
    }

    &__placeholder {
      color: var(--bm-text-tertiary, #86909c);
    }

    &__dropdown {
      background: var(--bm-bg-page, #2a2a2a);
      border-color: var(--bm-border, #404040);
    }

    &__option {
      &:hover:not(.is-disabled) {
        background: rgba(22, 93, 255, 0.15);
      }

      &.is-selected {
        background: rgba(22, 93, 255, 0.2);
      }
    }
  }
}
</style>
