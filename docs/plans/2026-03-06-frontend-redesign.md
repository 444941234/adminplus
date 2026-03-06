# BigModel Style Frontend Redesign Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Complete visual overhaul of AdminPlus frontend to match BigModel design style with full custom component library

**Architecture:** Create a new BigModel component library in `frontend/packages/ui-vue/src/components/bigmodel/` to replace Element Plus components. Uses Vue 3 Composition API with `<script setup>` syntax, SCSS for styling, and TypeScript for type safety.

**Tech Stack:** Vue 3.5, Vite 6, SCSS, TypeScript, Element Plus (to be removed)

---

## Task 1: Create BigModel Theme Variables

**Files:**
- Create: `frontend/packages/ui-vue/src/styles/themes/bigmodel.scss`

**Step 1: Create BigModel theme variables file**

```scss
// BigModel Style Theme Variables
:root {
  // Background colors
  --bm-bg-page: #f5f7fa;
  --bm-bg-white: #ffffff;
  --bm-bg-hover: #f8f9fb;
  --bm-bg-active: #eff0f5;
  --bm-bg-disabled: #f7f8fa;

  // Text colors
  --bm-text-primary: #1d2129;
  --bm-text-secondary: #4e5969;
  --bm-text-tertiary: #86909c;
  --bm-text-disabled: #c9cdd4;

  // Primary brand color
  --bm-primary: #165dff;
  --bm-primary-hover: #4080ff;
  --bm-primary-active: #0e42d2;
  --bm-primary-light: #e8eaff;

  // Functional colors
  --bm-success: #00b42a;
  --bm-success-light: #e8ffea;
  --bm-warning: #ff7d00;
  --bm-warning-light: #fff7e8;
  --bm-danger: #f53f3f;
  --bm-danger-light: #ffece8;
  --bm-info: #165dff;
  --bm-info-light: #e8eaff;

  // Border colors
  --bm-border: #e5e6eb;
  --bm-border-light: #f2f3f5;
  --bm-border-dark: #c9cdd4;

  // Shadow
  --bm-shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --bm-shadow-md: 0 4px 12px rgba(0, 0, 0, 0.1);
  --bm-shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);

  // Border radius
  --bm-radius-xs: 2px;
  --bm-radius-sm: 4px;
  --bm-radius-md: 8px;
  --bm-radius-lg: 12px;

  // Spacing
  --bm-space-xs: 4px;
  --bm-space-sm: 8px;
  --bm-space-md: 12px;
  --bm-space-lg: 16px;
  --bm-space-xl: 20px;
  --bm-space-2xl: 24px;

  // Layout
  --bm-sidebar-width: 200px;
  --bm-sidebar-collapsed-width: 64px;
  --bm-header-height: 56px;

  // Transitions
  --bm-transition-fast: 100ms ease;
  --bm-transition-normal: 200ms ease;

  // Z-index
  --bm-z-sidebar: 100;
  --bm-z-header: 99;
  --bm-z-modal: 1000;
  --bm-z-dropdown: 1050;
  --bm-z-toast: 1100;
}

// Typography
:root {
  --bm-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
                    'Helvetica Neue', Arial, 'Noto Sans', sans-serif;
  --bm-font-size-xs: 12px;
  --bm-font-size-sm: 13px;
  --bm-font-size-base: 14px;
  --bm-font-size-md: 16px;
  --bm-font-size-lg: 18px;
  --bm-font-size-xl: 20px;

  --bm-font-weight-normal: 400;
  --bm-font-weight-medium: 500;
  --bm-font-weight-semibold: 600;
}
```

**Step 2: Update main styles index to import BigModel theme**

Modify: `frontend/packages/ui-vue/src/styles/index.scss`

Add at the top:
```scss
@import './themes/bigmodel.scss';
```

**Step 3: Commit**

```bash
git add frontend/packages/ui-vue/src/styles/themes/bigmodel.scss frontend/packages/ui-vue/src/styles/index.scss
git commit -m "feat: add BigModel theme variables"
```

---

## Task 2: Create BmIcon Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/other/BmIcon.vue`
- Create: `frontend/packages/ui-vue/src/components/bigmodel/other/index.ts`

**Step 1: Create BmIcon component**

```vue
<template>
  <span class="bm-icon" :class="[size, { clickable }]" @click="handleClick">
    {{ iconDisplay }}
  </span>
</template>

<script setup lang="ts">
interface Props {
  icon: string;
  size?: 'xs' | 'sm' | 'md' | 'lg';
  clickable?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  clickable: false
});

const emit = defineEmits<{
  click: [];
}>();

const iconDisplay = computed(() => props.icon);

const handleClick = () => {
  if (props.clickable) {
    emit('click');
  }
};
</script>

<style scoped lang="scss">
.bm-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-style: normal;
  line-height: 1;
  color: var(--bm-text-secondary);

  &.xs {
    font-size: var(--bm-font-size-xs);
  }

  &.sm {
    font-size: var(--bm-font-size-sm);
  }

  &.md {
    font-size: var(--bm-font-size-base);
  }

  &.lg {
    font-size: var(--bm-font-size-lg);
  }

  &.clickable {
    cursor: pointer;
    transition: color var(--bm-transition-fast);

    &:hover {
      color: var(--bm-primary);
    }
  }
}
</style>
```

**Step 2: Create index file for other components**

```typescript
export { default as BmIcon } from './BmIcon.vue';
```

**Step 3: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/other/
git commit -m "feat: add BmIcon component"
```

---

## Task 3: Create BmButton Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/button/BmButton.vue`
- Create: `frontend/packages/ui-vue/src/components/bigmodel/button/index.ts`
- Create: `frontend/packages/ui-vue/src/styles/components/button.scss`

**Step 1: Create BmButton component**

```vue
<template>
  <button
    class="bm-button"
    :class="[
      type,
      size,
      {
        plain,
        loading,
        disabled,
        long
      }
    ]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="loading" class="bm-button__loading">⟳</span>
    <span v-if="$slots.icon && !loading" class="bm-button__icon">
      <slot name="icon" />
    </span>
    <span class="bm-button__content">
      <slot />
    </span>
  </button>
</template>

<script setup lang="ts">
interface Props {
  type?: 'primary' | 'default' | 'text' | 'danger' | 'success' | 'warning';
  size?: 'mini' | 'small' | 'medium' | 'large';
  plain?: boolean;
  loading?: boolean;
  disabled?: boolean;
  long?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  size: 'medium',
  plain: false,
  loading: false,
  disabled: false,
  long: false
});

const emit = defineEmits<{
  click: [event: MouseEvent];
}>();

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event);
  }
};
</script>

<style scoped lang="scss">
@import '../../../styles/components/button.scss';
</style>
```

**Step 2: Create button styles**

```scss
// frontend/packages/ui-vue/src/styles/components/button.scss
.bm-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--bm-space-sm);
  border: 1px solid transparent;
  border-radius: var(--bm-radius-sm);
  font-family: var(--bm-font-family);
  font-size: var(--bm-font-size-base);
  font-weight: var(--bm-font-weight-medium);
  line-height: 1.5;
  white-space: nowrap;
  cursor: pointer;
  transition: all var(--bm-transition-fast);
  user-select: none;

  &:focus-visible {
    outline: 2px solid var(--bm-primary);
    outline-offset: 2px;
  }

  // Sizes
  &.mini {
    padding: 2px 8px;
    font-size: var(--bm-font-size-xs);
    height: 24px;
  }

  &.small {
    padding: 4px 12px;
    font-size: var(--bm-font-size-sm);
    height: 28px;
  }

  &.medium {
    padding: 6px 16px;
    height: 32px;
  }

  &.large {
    padding: 8px 20px;
    font-size: var(--bm-font-size-md);
    height: 36px;
  }

  &.long {
    width: 100%;
  }

  // Types - Primary
  &.primary {
    background: var(--bm-primary);
    border-color: var(--bm-primary);
    color: #fff;

    &:hover:not(.disabled) {
      background: var(--bm-primary-hover);
      border-color: var(--bm-primary-hover);
    }

    &:active:not(.disabled) {
      background: var(--bm-primary-active);
      border-color: var(--bm-primary-active);
    }
  }

  // Types - Default
  &.default {
    background: #fff;
    border-color: var(--bm-border);
    color: var(--bm-text-primary);

    &:hover:not(.disabled) {
      background: var(--bm-bg-hover);
      border-color: var(--bm-primary);
      color: var(--bm-primary);
    }
  }

  // Types - Text
  &.text {
    background: transparent;
    border-color: transparent;
    color: var(--bm-primary);

    &:hover:not(.disabled) {
      background: var(--bm-primary-light);
    }
  }

  // Types - Danger
  &.danger {
    background: var(--bm-danger);
    border-color: var(--bm-danger);
    color: #fff;

    &:hover:not(.disabled) {
      opacity: 0.8;
    }
  }

  // Types - Success
  &.success {
    background: var(--bm-success);
    border-color: var(--bm-success);
    color: #fff;

    &:hover:not(.disabled) {
      opacity: 0.8;
    }
  }

  // Types - Warning
  &.warning {
    background: var(--bm-warning);
    border-color: var(--bm-warning);
    color: #fff;

    &:hover:not(.disabled) {
      opacity: 0.8;
    }
  }

  // Plain modifier
  &.plain {
    background: transparent;

    &.primary {
      color: var(--bm-primary);

      &:hover:not(.disabled) {
        background: var(--bm-primary-light);
      }
    }

    &.danger {
      color: var(--bm-danger);

      &:hover:not(.disabled) {
        background: var(--bm-danger-light);
      }
    }

    &.success {
      color: var(--bm-success);

      &:hover:not(.disabled) {
        background: var(--bm-success-light);
      }
    }

    &.warning {
      color: var(--bm-warning);

      &:hover:not(.disabled) {
        background: var(--bm-warning-light);
      }
    }
  }

  // Disabled state
  &.disabled,
  &[disabled] {
    cursor: not-allowed;
    opacity: 0.5;
  }

  // Loading state
  &.loading {
    cursor: default;
    pointer-events: none;
  }

  &__loading {
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }

  &__icon {
    display: inline-flex;
    align-items: center;
  }

  &__content {
    display: inline-flex;
    align-items: center;
  }
}
```

**Step 3: Create button index**

```typescript
export { default as BmButton } from './BmButton.vue';
```

**Step 4: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/button/ frontend/packages/ui-vue/src/styles/components/button.scss
git commit -m "feat: add BmButton component"
```

---

## Task 4: Create BmCard Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/card/BmCard.vue`
- Create: `frontend/packages/ui-vue/src/components/bigmodel/card/index.ts`
- Create: `frontend/packages/ui-vue/src/styles/components/card.scss`

**Step 1: Create BmCard component**

```vue
<template>
  <div class="bm-card" :class="[shadow, hoverable ? 'hoverable' : '']">
    <div v-if="$slots.header || title" class="bm-card__header">
      <slot name="header">
        <div class="bm-card__title">{{ title }}</div>
      </slot>
    </div>
    <div class="bm-card__body" :class="{ 'no-padding': !padding }">
      <slot />
    </div>
    <div v-if="$slots.footer" class="bm-card__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title?: string;
  shadow?: 'never' | 'small' | 'medium';
  hoverable?: boolean;
  padding?: boolean;
}

withDefaults(defineProps<Props>(), {
  shadow: 'small',
  hoverable: false,
  padding: true
});
</script>

<style scoped lang="scss">
@import '../../../styles/components/card.scss';
</style>
```

**Step 2: Create card styles**

```scss
.bm-card {
  background: var(--bm-bg-white);
  border-radius: var(--bm-radius-md);
  transition: all var(--bm-transition-normal);

  &.never {
    box-shadow: none;
  }

  &.small {
    box-shadow: var(--bm-shadow-sm);
  }

  &.medium {
    box-shadow: var(--bm-shadow-md);
  }

  &.hoverable {
    cursor: pointer;

    &:hover {
      box-shadow: var(--bm-shadow-md);
      transform: translateY(-2px);
    }
  }

  &__header {
    padding: var(--bm-space-xl) var(--bm-space-2xl) 0;
  }

  &__title {
    font-size: var(--bm-font-size-md);
    font-weight: var(--bm-font-weight-semibold);
    color: var(--bm-text-primary);
    line-height: 1.5;
  }

  &__body {
    padding: var(--bm-space-xl) var(--bm-space-2xl);

    &.no-padding {
      padding: 0;
    }
  }

  &__footer {
    padding: 0 var(--bm-space-2xl) var(--bm-space-xl);
    border-top: 1px solid var(--bm-border-light);
  }
}
```

**Step 3: Create card index**

```typescript
export { default as BmCard } from './BmCard.vue';
```

**Step 4: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/card/ frontend/packages/ui-vue/src/styles/components/card.scss
git commit -m "feat: add BmCard component"
```

---

## Task 5: Create BmInput Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/form/BmInput.vue`
- Create: `frontend/packages/ui-vue/src/styles/components/form.scss`

**Step 1: Create BmInput component**

```vue
<template>
  <div class="bm-input-wrapper" :class="{ 'has-error': error, 'has-disabled': disabled }">
    <span v-if="prefixIcon || $slots.prefix" class="bm-input__prefix">
      <slot name="prefix">{{ prefixIcon }}</slot>
    </span>
    <input
      :id="inputId"
      ref="inputRef"
      v-model="inputValue"
      class="bm-input"
      :type="type"
      :placeholder="placeholder"
      :disabled="disabled"
      :readonly="readonly"
      :maxlength="maxlength"
      @focus="handleFocus"
      @blur="handleBlur"
    />
    <span v-if="suffixIcon || $slots.suffix || showClear" class="bm-input__suffix">
      <slot name="suffix">
        <span v-if="showClear" class="bm-input__clear" @click="handleClear">×</span>
        {{ suffixIcon }}
      </slot>
    </span>
  </div>
  <div v-if="error" class="bm-input__error">{{ error }}</div>
</template>

<script setup lang="ts">
interface Props {
  modelValue?: string | number;
  type?: string;
  placeholder?: string;
  disabled?: boolean;
  readonly?: boolean;
  maxlength?: number;
  prefixIcon?: string;
  suffixIcon?: string;
  clearable?: boolean;
  error?: string;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  clearable: false
});

const emit = defineEmits<{
  'update:modelValue': [value: string | number];
  focus: [event: FocusEvent];
  blur: [event: FocusEvent];
  clear: [];
  change: [value: string | number];
}>();

const inputId = `bm-input-${Math.random().toString(36).substr(2, 9)}`;
const focused = ref(false);
const inputRef = ref<HTMLInputElement>();

const inputValue = computed({
  get: () => props.modelValue ?? '',
  set: (val) => emit('update:modelValue', val)
});

const showClear = computed(
  () => props.clearable && !props.disabled && !!inputValue.value && focused.value
);

const handleFocus = (event: FocusEvent) => {
  focused.value = true;
  emit('focus', event);
};

const handleBlur = (event: FocusEvent) => {
  focused.value = false;
  emit('blur', event);
};

const handleClear = () => {
  emit('update:modelValue', '');
  emit('clear');
};
</script>

<style scoped lang="scss">
@import '../../../styles/components/form.scss';
</style>
```

**Step 2: Create form styles (part 1 - Input)**

```scss
// Add to frontend/packages/ui-vue/src/styles/components/form.scss
.bm-input-wrapper {
  display: inline-flex;
  align-items: center;
  width: 100%;
  position: relative;

  &.has-error {
    .bm-input {
      border-color: var(--bm-danger);

      &:focus {
        border-color: var(--bm-danger);
      }
    }
  }

  &.has-disabled {
    .bm-input {
      background: var(--bm-bg-disabled);
      cursor: not-allowed;
    }
  }
}

.bm-input {
  flex: 1;
  min-width: 0;
  padding: var(--bm-space-md) var(--bm-space-lg);
  border: 1px solid var(--bm-border);
  border-radius: var(--bm-radius-sm);
  font-family: var(--bm-font-family);
  font-size: var(--bm-font-size-base);
  color: var(--bm-text-primary);
  background: #fff;
  transition: all var(--bm-transition-fast);
  outline: none;

  &::placeholder {
    color: var(--bm-text-tertiary);
  }

  &:focus {
    border-color: var(--bm-primary);
  }

  &:disabled {
    background: var(--bm-bg-disabled);
    cursor: not-allowed;
    color: var(--bm-text-disabled);
  }

  &:read-only {
    cursor: default;
  }
}

.bm-input__prefix,
.bm-input__suffix {
  display: inline-flex;
  align-items: center;
  padding: 0 var(--bm-space-sm);
  color: var(--bm-text-tertiary);
  pointer-events: none;
}

.bm-input__prefix {
  border-right: 1px solid var(--bm-border);
}

.bm-input__suffix {
  border-left: 1px solid var(--bm-border);
}

.bm-input__clear {
  cursor: pointer;
  pointer-events: auto;

  &:hover {
    color: var(--bm-text-primary);
  }
}

.bm-input__error {
  margin-top: var(--bm-space-xs);
  font-size: var(--bm-font-size-xs);
  color: var(--bm-danger);
}
```

**Step 3: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/form/BmInput.vue frontend/packages/ui-vue/src/styles/components/form.scss
git commit -m "feat: add BmInput component"
```

---

## Task 6: Create BmSelect Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/form/BmSelect.vue`

**Step 1: Create BmSelect component**

```vue
<template>
  <div class="bm-select" :class="{ 'has-error': error, 'is-disabled': disabled }" ref="selectRef">
    <div
      class="bm-select__trigger"
      :class="{ 'is-visible': visible }"
      @click="handleToggle"
    >
      <span v-if="selectedLabel" class="bm-select__label">{{ selectedLabel }}</span>
      <span v-else class="bm-select__placeholder">{{ placeholder }}</span>
      <span class="bm-select__arrow">▼</span>
    </div>

    <Teleport to="body">
      <transition name="bm-select-dropdown">
        <div
          v-if="visible"
          class="bm-select__dropdown"
          :style="dropdownStyle"
        >
          <ul class="bm-select__list">
            <li
              v-for="option in options"
              :key="option.value"
              class="bm-select__item"
              :class="{ 'is-selected': option.value === modelValue, 'is-disabled': option.disabled }"
              @click="handleSelect(option)"
            >
              <span>{{ option.label }}</span>
              <span v-if="option.value === modelValue" class="check">✓</span>
            </li>
          </ul>
        </div>
      </transition>
    </Teleport>

    <div v-if="error" class="bm-select__error">{{ error }}</div>
  </div>
</template>

<script setup lang="ts">
interface Option {
  label: string;
  value: string | number;
  disabled?: boolean;
}

interface Props {
  modelValue?: string | number;
  options: Option[];
  placeholder?: string;
  disabled?: boolean;
  error?: string;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择'
});

const emit = defineEmits<{
  'update:modelValue': [value: string | number];
  change: [value: string | number];
  visibleChange: [visible: boolean];
}>();

const selectRef = ref<HTMLElement>();
const visible = ref(false);
const dropdownStyle = ref<Record<string, string>>({});

const selectedLabel = computed(() => {
  const option = props.options.find(o => o.value === props.modelValue);
  return option?.label || '';
});

const handleToggle = () => {
  if (props.disabled) return;
  visible.value = !visible.value;
  updateDropdownPosition();
  emit('visibleChange', visible.value);
};

const handleSelect = (option: Option) => {
  if (option.disabled) return;
  emit('update:modelValue', option.value);
  emit('change', option.value);
  visible.value = false;
};

const updateDropdownPosition = () => {
  if (!selectRef.value) return;
  const rect = selectRef.value.getBoundingClientRect();
  dropdownStyle.value = {
    position: 'absolute',
    top: `${rect.bottom + window.scrollY + 4}px`,
    left: `${rect.left}px`,
    width: `${rect.width}px`,
    zIndex: '1050'
  };
};

const handleClickOutside = (event: MouseEvent) => {
  if (selectRef.value && !selectRef.value.contains(event.target as Node)) {
    visible.value = false;
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
@import '../../../styles/components/form.scss';

.bm-select {
  display: inline-block;
  width: 100%;
  position: relative;
}

.bm-select__trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--bm-space-md) var(--bm-space-lg);
  border: 1px solid var(--bm-border);
  border-radius: var(--bm-radius-sm);
  background: #fff;
  cursor: pointer;
  transition: all var(--bm-transition-fast);

  &:hover {
    border-color: var(--bm-primary-hover);
  }

  &.is-visible {
    border-color: var(--bm-primary);
  }
}

.bm-select__label {
  color: var(--bm-text-primary);
}

.bm-select__placeholder {
  color: var(--bm-text-tertiary);
}

.bm-select__arrow {
  color: var(--bm-text-tertiary);
  font-size: 10px;
  transition: transform var(--bm-transition-fast);

  .is-visible & {
    transform: rotate(180deg);
  }
}

.bm-select__dropdown {
  background: #fff;
  border-radius: var(--bm-radius-sm);
  box-shadow: var(--bm-shadow-md);
  border: 1px solid var(--bm-border);
  max-height: 200px;
  overflow-y: auto;
}

.bm-select__list {
  list-style: none;
  margin: 0;
  padding: 4px 0;
}

.bm-select__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  cursor: pointer;
  transition: background var(--bm-transition-fast);

  &:hover:not(.is-disabled) {
    background: var(--bm-bg-hover);
  }

  &.is-selected {
    color: var(--bm-primary);
    background: var(--bm-primary-light);
  }

  &.is-disabled {
    color: var(--bm-text-disabled);
    cursor: not-allowed;
  }

  .check {
    font-size: 12px;
  }
}

.bm-select__error {
  margin-top: var(--bm-space-xs);
  font-size: var(--bm-font-size-xs);
  color: var(--bm-danger);
}

.is-disabled {
  .bm-select__trigger {
    background: var(--bm-bg-disabled);
    cursor: not-allowed;
  }
}

.has-error {
  .bm-select__trigger {
    border-color: var(--bm-danger);
  }
}

.bm-select-dropdown-enter-active,
.bm-select-dropdown-leave-active {
  transition: all var(--bm-transition-normal);
}

.bm-select-dropdown-enter-from,
.bm-select-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
```

**Step 2: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/form/BmSelect.vue
git commit -m "feat: add BmSelect component"
```

---

## Task 7: Create BmCheckbox and BmRadio Components

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/form/BmCheckbox.vue`
- Create: `frontend/packages/ui-vue/src/components/bigmodel/form/BmRadio.vue`

**Step 1: Create BmCheckbox component**

```vue
<template>
  <label class="bm-checkbox" :class="{ 'is-disabled': disabled, 'is-checked': isChecked }">
    <span class="bm-checkbox__input">
      <span class="bm-checkbox__inner"></span>
      <input
        type="checkbox"
        :checked="isChecked"
        :disabled="disabled"
        @change="handleChange"
      />
    </span>
    <span v-if="$slots.default" class="bm-checkbox__label">
      <slot />
    </span>
  </label>
</template>

<script setup lang="ts">
interface Props {
  modelValue?: boolean | string | number;
  value?: string | number;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean | string | number];
  change: [value: boolean | string | number];
}>();

const isChecked = computed(() => {
  return props.modelValue === (props.value ?? true);
});

const handleChange = () => {
  if (props.disabled) return;
  const newValue = props.value !== undefined ? props.value : !props.modelValue;
  emit('update:modelValue', newValue);
  emit('change', newValue);
};
</script>

<style scoped lang="scss">
@import '../../../styles/components/form.scss';

.bm-checkbox {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  user-select: none;

  &.is-disabled {
    cursor: not-allowed;
  }
}

.bm-checkbox__input {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.bm-checkbox__inner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 1px solid var(--bm-border);
  border-radius: var(--bm-radius-xs);
  background: #fff;
  position: relative;
  transition: all var(--bm-transition-fast);

  .is-checked > & {
    background: var(--bm-primary);
    border-color: var(--bm-primary);

    &::after {
      content: '✓';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      color: #fff;
      font-size: 12px;
      font-weight: bold;
    }
  }

  .is-disabled > & {
    background: var(--bm-bg-disabled);
    border-color: var(--bm-border-dark);
  }
}

.bm-checkbox__input input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.bm-checkbox__label {
  margin-left: var(--bm-space-sm);
  color: var(--bm-text-primary);

  .is-disabled & {
    color: var(--bm-text-disabled);
  }
}
</style>
```

**Step 2: Create BmRadio component**

```vue
<template>
  <label class="bm-radio" :class="{ 'is-disabled': disabled, 'is-checked': isChecked }">
    <span class="bm-radio__input">
      <span class="bm-radio__inner"></span>
      <input
        type="radio"
        :checked="isChecked"
        :disabled="disabled"
        @change="handleChange"
      />
    </span>
    <span v-if="$slots.default" class="bm-radio__label">
      <slot />
    </span>
  </label>
</template>

<script setup lang="ts">
interface Props {
  modelValue?: string | number;
  value: string | number;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false
});

const emit = defineEmits<{
  'update:modelValue': [value: string | number];
  change: [value: string | number];
}>();

const isChecked = computed(() => props.modelValue === props.value);

const handleChange = () => {
  if (props.disabled) return;
  emit('update:modelValue', props.value);
  emit('change', props.value);
};
</script>

<style scoped lang="scss">
@import '../../../styles/components/form.scss';

.bm-radio {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  user-select: none;

  &.is-disabled {
    cursor: not-allowed;
  }
}

.bm-radio__input {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.bm-radio__inner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 1px solid var(--bm-border);
  border-radius: 50%;
  background: #fff;
  position: relative;
  transition: all var(--bm-transition-fast);

  .is-checked > & {
    border-color: var(--bm-primary);

    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--bm-primary);
    }
  }

  .is-disabled > & {
    background: var(--bm-bg-disabled);
    border-color: var(--bm-border-dark);

    &::after {
      background: var(--bm-text-disabled);
    }
  }
}

.bm-radio__input input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.bm-radio__label {
  margin-left: var(--bm-space-sm);
  color: var(--bm-text-primary);

  .is-disabled & {
    color: var(--bm-text-disabled);
  }
}
</style>
```

**Step 3: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/form/BmCheckbox.vue frontend/packages/ui-vue/src/components/bigmodel/form/BmRadio.vue
git commit -m "feat: add BmCheckbox and BmRadio components"
```

---

## Task 8: Create BmSwitch Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/form/BmSwitch.vue`

**Step 1: Create BmSwitch component**

```vue
<template>
  <div
    class="bm-switch"
    :class="{ 'is-disabled': disabled, 'is-checked': modelValue }"
    @click="handleToggle"
  >
    <input type="checkbox" :checked="modelValue" :disabled="disabled" class="bm-switch__input" />
    <span class="bm-switch__core"></span>
  </div>
</template>

<script setup lang="ts">
interface Props {
  modelValue?: boolean;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  change: [value: boolean];
}>();

const handleToggle = () => {
  if (props.disabled) return;
  const newValue = !props.modelValue;
  emit('update:modelValue', newValue);
  emit('change', newValue);
};
</script>

<style scoped lang="scss">
@import '../../../styles/components/form.scss';

.bm-switch {
  display: inline-flex;
  align-items: center;
  position: relative;
  font-size: 14px;
  line-height: 20px;
  height: 20px;
  vertical-align: middle;
  cursor: pointer;

  &.is-disabled {
    cursor: not-allowed;
  }
}

.bm-switch__input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  margin: 0;
}

.bm-switch__core {
  display: inline-block;
  width: 40px;
  height: 20px;
  border: 1px solid var(--bm-border);
  border-radius: 10px;
  background: var(--bm-bg-disabled);
  position: relative;
  transition: all var(--bm-transition-normal);

  &::after {
    content: '';
    position: absolute;
    top: 1px;
    left: 1px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: #fff;
    transition: all var(--bm-transition-normal);
  }
}

.is-checked .bm-switch__core {
  background: var(--bm-primary);
  border-color: var(--bm-primary);

  &::after {
    left: calc(100% - 17px);
  }
}

.is-disabled {
  .bm-switch__core {
    background: var(--bm-bg-disabled);
    border-color: var(--bm-border-dark);
    cursor: not-allowed;

    &::after {
      background: var(--bm-text-disabled);
    }
  }

  &.is-checked .bm-switch__core {
    background: var(--bm-border-dark);
  }
}
</style>
```

**Step 2: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/form/BmSwitch.vue
git commit -m "feat: add BmSwitch component"
```

---

## Task 9: Create BmSidebar Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/layout/BmSidebar.vue`
- Create: `frontend/packages/ui-vue/src/styles/components/layout.scss`

**Step 1: Create BmSidebar component**

```vue
<template>
  <div class="bm-sidebar" :class="{ collapsed, 'mobile-open': mobileOpen }">
    <!-- Logo -->
    <div class="bm-sidebar__logo">
      <img src="/logo.svg" alt="AdminPlus" class="logo-icon" />
      <transition name="logo-text">
        <span v-show="!collapsed" class="logo-text">AdminPlus</span>
      </transition>
    </div>

    <!-- Menu -->
    <nav class="bm-sidebar__menu">
      <div v-for="group in menuGroups" :key="group.id" class="menu-group">
        <!-- Group Header -->
        <div
          class="menu-group__header"
          :class="{ 'is-active': isGroupActive(group) }"
          @click="toggleGroup(group.id)"
        >
          <span class="group-icon">{{ group.icon }}</span>
          <transition name="group-text">
            <span v-show="!collapsed" class="group-title">{{ group.title }}</span>
          </transition>
          <transition name="group-arrow">
            <span v-show="!collapsed" class="group-arrow" :class="{ expanded: group.expanded }">
              ▼
            </span>
          </transition>
        </div>

        <!-- Group Items -->
        <transition name="menu-items">
          <div v-show="group.expanded || collapsed" class="menu-group__items">
            <router-link
              v-for="item in group.items"
              :key="item.id"
              :to="item.path"
              class="menu-item"
              :class="{ 'is-active': isActive(item.path) }"
            >
              <span class="item-icon">{{ item.icon }}</span>
              <transition name="item-text">
                <span v-show="!collapsed" class="item-title">{{ item.title }}</span>
              </transition>
            </router-link>
          </div>
        </transition>
      </div>
    </nav>

    <!-- Collapse Button -->
    <div class="bm-sidebar__collapse" @click="handleToggle">
      <span :class="{ rotated: collapsed }">◀</span>
    </div>
  </div>
</template>

<script setup lang="ts">
interface MenuItem {
  id: string;
  title: string;
  icon: string;
  path: string;
}

interface MenuGroup {
  id: string;
  title: string;
  icon: string;
  expanded: boolean;
  items: MenuItem[];
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
  'update:menuGroups': [groups: MenuGroup[]];
}>();

const route = useRoute();

const toggleGroup = (groupId: string) => {
  if (props.collapsed) return;
  const updated = props.menuGroups.map(group => ({
    ...group,
    expanded: group.id === groupId ? !group.expanded : group.expanded
  }));
  emit('update:menuGroups', updated);
};

const handleToggle = () => {
  emit('toggle');
};

const isActive = (path: string) => {
  return route.path === path || route.path.startsWith(path + '/');
};

const isGroupActive = (group: MenuGroup) => {
  return group.items.some(item => isActive(item.path));
};
</script>

<style scoped lang="scss">
@import '../../../styles/components/layout.scss';
</style>
```

**Step 2: Create layout styles**

```scss
// frontend/packages/ui-vue/src/styles/components/layout.scss
.bm-sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: var(--bm-sidebar-width);
  background: #fff;
  border-right: 1px solid var(--bm-border);
  display: flex;
  flex-direction: column;
  transition: width var(--bm-transition-normal);
  z-index: var(--bm-z-sidebar);

  &.collapsed {
    width: var(--bm-sidebar-collapsed-width);

    .group-arrow,
    .logo-text,
    .group-title,
    .item-title {
      opacity: 0;
      width: 0;
    }

    .menu-group__header {
      justify-content: center;
      padding: 12px 0;
    }

    .menu-item {
      justify-content: center;
      padding: 10px 0;
    }
  }

  @media (max-width: 767px) {
    transform: translateX(-100%);

    &.mobile-open {
      transform: translateX(0);
    }
  }
}

.bm-sidebar__logo {
  display: flex;
  align-items: center;
  padding: var(--bm-space-lg) var(--bm-space-xl);
  height: var(--bm-header-height);
  border-bottom: 1px solid var(--bm-border-light);

  .logo-icon {
    width: 28px;
    height: 28px;
    flex-shrink: 0;
  }

  .logo-text {
    margin-left: var(--bm-space-md);
    font-size: var(--bm-font-size-lg);
    font-weight: var(--bm-font-weight-semibold);
    color: var(--bm-text-primary);
    white-space: nowrap;
    overflow: hidden;
  }
}

.bm-sidebar__menu {
  flex: 1;
  overflow-y: auto;
  padding: var(--bm-space-md) 0;
}

.menu-group {
  margin-bottom: var(--bm-space-xs);

  &__header {
    display: flex;
    align-items: center;
    padding: 12px var(--bm-space-xl);
    cursor: pointer;
    transition: background var(--bm-transition-fast);
    color: var(--bm-text-secondary);

    &:hover {
      background: var(--bm-bg-hover);
    }

    &.is-active {
      color: var(--bm-primary);
    }

    .group-icon {
      font-size: 18px;
      flex-shrink: 0;
    }

    .group-title {
      flex: 1;
      margin-left: var(--bm-space-md);
      font-size: var(--bm-font-size-base);
      white-space: nowrap;
    }

    .group-arrow {
      font-size: 10px;
      transition: transform var(--bm-transition-fast);

      &.expanded {
        transform: rotate(180deg);
      }
    }
  }

  &__items {
    background: var(--bm-bg-hover);
  }
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 10px var(--bm-space-xl) 10px calc(var(--bm-space-xl) * 2);
  cursor: pointer;
  transition: all var(--bm-transition-fast);
  color: var(--bm-text-secondary);

  &:hover {
    color: var(--bm-primary);
  }

  &.is-active {
    background: var(--bm-primary-light);
    color: var(--bm-primary);
    position: relative;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background: var(--bm-primary);
    }
  }

  .item-icon {
    font-size: 16px;
    flex-shrink: 0;
  }

  .item-title {
    margin-left: var(--bm-space-md);
    font-size: var(--bm-font-size-sm);
    white-space: nowrap;
  }
}

.bm-sidebar__collapse {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--bm-space-md);
  border-top: 1px solid var(--bm-border-light);
  cursor: pointer;
  color: var(--bm-text-tertiary);
  transition: color var(--bm-transition-fast);

  &:hover {
    color: var(--bm-primary);
  }

  span {
    transition: transform var(--bm-transition-normal);

    &.rotated {
      transform: rotate(180deg);
    }
  }
}

// Transitions
.logo-text-enter-active,
.logo-text-leave-active,
.group-text-enter-active,
.group-text-leave-active,
.item-text-enter-active,
.item-text-leave-active,
.group-arrow-enter-active,
.group-arrow-leave-active {
  transition: all var(--bm-transition-normal);
}

.logo-text-enter-from,
.logo-text-leave-to,
.group-text-enter-from,
.group-text-leave-to,
.item-text-enter-from,
.item-text-leave-to,
.group-arrow-enter-from,
.group-arrow-leave-to {
  opacity: 0;
  width: 0;
}

.menu-items-enter-active,
.menu-items-leave-active {
  transition: all var(--bm-transition-normal);
  overflow: hidden;
}

.menu-items-enter-from,
.menu-items-leave-to {
  max-height: 0;
  opacity: 0;
}

.menu-items-enter-to,
.menu-items-leave-from {
  max-height: 500px;
  opacity: 1;
}
```

**Step 3: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/layout/BmSidebar.vue frontend/packages/ui-vue/src/styles/components/layout.scss
git commit -m "feat: add BmSidebar component"
```

---

## Task 10: Create BmHeader Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/layout/BmHeader.vue`

**Step 1: Create BmHeader component**

```vue
<template>
  <header class="bm-header">
    <div class="bm-header__left">
      <button v-if="showToggle" class="header-icon" @click="$emit('toggle')">
        <span>☰</span>
      </button>
      <div v-if="breadcrumbs.length" class="breadcrumbs">
        <span v-for="(crumb, index) in breadcrumbs" :key="index" class="breadcrumb-item">
          <router-link v-if="crumb.path" :to="crumb.path">{{ crumb.title }}</router-link>
          <span v-else>{{ crumb.title }}</span>
          <span v-if="index < breadcrumbs.length - 1" class="separator">/</span>
        </span>
      </div>
    </div>

    <div class="bm-header__right">
      <div v-if="showSearch" class="header-search">
        <input
          v-model="searchValue"
          type="text"
          placeholder="搜索..."
          @keyup.enter="$emit('search', searchValue)"
        />
        <span class="search-icon">🔍</span>
      </div>

      <button v-if="showNotification" class="header-icon" @click="$emit('notification')">
        <span>🔔</span>
        <span v-if="notificationCount > 0" class="badge">{{ notificationCount }}</span>
      </button>

      <div class="header-user" @click="$emit('userMenu')">
        <img :src="user.avatar" :alt="user.name" class="user-avatar" />
        <span class="user-name">{{ user.name }}</span>
        <span class="user-arrow">▼</span>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
interface Breadcrumb {
  title: string;
  path?: string;
}

interface User {
  name: string;
  avatar: string;
}

interface Props {
  collapsed?: boolean;
  showToggle?: boolean;
  showSearch?: boolean;
  showNotification?: boolean;
  notificationCount?: number;
  breadcrumbs?: Breadcrumb[];
  user: User;
}

const props = withDefaults(defineProps<Props>(), {
  collapsed: false,
  showToggle: true,
  showSearch: true,
  showNotification: true,
  notificationCount: 0,
  breadcrumbs: () => []
});

defineEmits<{
  toggle: [];
  search: [value: string];
  notification: [];
  userMenu: [];
}>();

const searchValue = ref('');
</script>

<style scoped lang="scss">
@import '../../../styles/components/layout.scss';

.bm-header {
  position: fixed;
  top: 0;
  right: 0;
  left: var(--bm-sidebar-width);
  height: var(--bm-header-height);
  background: #fff;
  border-bottom: 1px solid var(--bm-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--bm-space-xl);
  transition: left var(--bm-transition-normal);
  z-index: var(--bm-z-header);

  .collapsed & {
    left: var(--bm-sidebar-collapsed-width);
  }

  @media (max-width: 767px) {
    left: 0;
  }
}

.bm-header__left,
.bm-header__right {
  display: flex;
  align-items: center;
  gap: var(--bm-space-lg);
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: var(--bm-radius-sm);
  cursor: pointer;
  color: var(--bm-text-secondary);
  transition: all var(--bm-transition-fast);
  position: relative;

  &:hover {
    background: var(--bm-bg-hover);
    color: var(--bm-primary);
  }

  .badge {
    position: absolute;
    top: 4px;
    right: 4px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    background: var(--bm-danger);
    color: #fff;
    font-size: 10px;
    line-height: 16px;
    text-align: center;
    border-radius: 8px;
  }
}

.breadcrumbs {
  display: flex;
  align-items: center;
  font-size: var(--bm-font-size-sm);
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  color: var(--bm-text-tertiary);

  a {
    color: var(--bm-text-secondary);
    text-decoration: none;
    transition: color var(--bm-transition-fast);

    &:hover {
      color: var(--bm-primary);
    }
  }

  .separator {
    margin: 0 var(--bm-space-sm);
    color: var(--bm-text-tertiary);
  }
}

.header-search {
  position: relative;
  width: 200px;

  input {
    width: 100%;
    padding: 6px 36px 6px 12px;
    border: 1px solid var(--bm-border);
    border-radius: var(--bm-radius-sm);
    font-size: var(--bm-font-size-sm);
    background: var(--bm-bg-page);
    transition: all var(--bm-transition-fast);

    &:focus {
      outline: none;
      border-color: var(--bm-primary);
      background: #fff;
    }

    &::placeholder {
      color: var(--bm-text-tertiary);
    }
  }

  .search-icon {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--bm-text-tertiary);
    font-size: 12px;
  }
}

.header-user {
  display: flex;
  align-items: center;
  gap: var(--bm-space-sm);
  padding: 4px var(--bm-space-sm);
  border-radius: var(--bm-radius-sm);
  cursor: pointer;
  transition: background var(--bm-transition-fast);

  &:hover {
    background: var(--bm-bg-hover);
  }

  .user-avatar {
    width: 28px;
    height: 28px;
    border-radius: 50%;
  }

  .user-name {
    font-size: var(--bm-font-size-sm);
    color: var(--bm-text-primary);
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .user-arrow {
    font-size: 10px;
    color: var(--bm-text-tertiary);
  }
}

@media (max-width: 767px) {
  .bm-header {
    padding: 0 var(--bm-space-md);
  }

  .header-search {
    display: none;
  }

  .user-name {
    display: none;
  }
}
</style>
```

**Step 2: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/layout/BmHeader.vue
git commit -m "feat: add BmHeader component"
```

---

## Task 11: Create BmLayout Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/layout/BmLayout.vue`
- Create: `frontend/packages/ui-vue/src/components/bigmodel/layout/index.ts`

**Step 1: Create BmLayout component**

```vue
<template>
  <div class="bm-layout">
    <bm-sidebar
      :collapsed="collapsed"
      :mobile-open="mobileOpen"
      :menu-groups="menuGroups"
      @toggle="handleToggle"
      @update:menu-groups="handleMenuGroupsUpdate"
    />

    <div class="bm-main" :class="{ 'sidebar-collapsed': collapsed }">
      <bm-header
        :collapsed="collapsed"
        :user="user"
        :breadcrumbs="breadcrumbs"
        :notification-count="notificationCount"
        @toggle="handleToggle"
        @search="handleSearch"
        @notification="handleNotification"
        @user-menu="handleUserMenu"
      />

      <main class="bm-content">
        <slot />
      </main>
    </div>

    <!-- Mobile overlay -->
    <transition name="fade">
      <div
        v-if="mobileOpen"
        class="bm-overlay"
        @click="mobileOpen = false"
      ></div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import BmSidebar from './BmSidebar.vue';
import BmHeader from './BmHeader.vue';

interface MenuItem {
  id: string;
  title: string;
  icon: string;
  path: string;
}

interface MenuGroup {
  id: string;
  title: string;
  icon: string;
  expanded: boolean;
  items: MenuItem[];
}

interface Breadcrumb {
  title: string;
  path?: string;
}

interface User {
  name: string;
  avatar: string;
}

interface Props {
  user: User;
  menuGroups: MenuGroup[];
  breadcrumbs?: Breadcrumb[];
  notificationCount?: number;
}

const props = withDefaults(defineProps<Props>(), {
  breadcrumbs: () => [],
  notificationCount: 0
});

defineEmits<{
  search: [value: string];
  notification: [];
  userMenu: [];
}>();

const collapsed = ref(false);
const mobileOpen = ref(false);
const menuGroups = ref(props.menuGroups);

const handleToggle = () => {
  if (window.innerWidth < 768) {
    mobileOpen.value = !mobileOpen.value;
  } else {
    collapsed.value = !collapsed.value;
  }
};

const handleMenuGroupsUpdate = (groups: MenuGroup[]) => {
  menuGroups.value = groups;
};

const handleSearch = (value: string) => {
  emit('search', value);
};

const handleNotification = () => {
  emit('notification');
};

const handleUserMenu = () => {
  emit('userMenu');
};
</script>

<style scoped lang="scss">
.bm-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bm-bg-page);
}

.bm-main {
  flex: 1;
  margin-left: var(--bm-sidebar-width);
  transition: margin-left var(--bm-transition-normal);
  display: flex;
  flex-direction: column;
  min-width: 0;

  &.sidebar-collapsed {
    margin-left: var(--bm-sidebar-collapsed-width);
  }

  @media (max-width: 767px) {
    margin-left: 0;
  }
}

.bm-content {
  flex: 1;
  padding: var(--bm-space-2xl);
  margin-top: var(--bm-header-height);
  overflow-y: auto;
  min-height: calc(100vh - var(--bm-header-height));

  @media (max-width: 767px) {
    padding: var(--bm-space-lg);
  }
}

.bm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: calc(var(--bm-z-sidebar) - 1);
  backdrop-filter: blur(2px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--bm-transition-normal);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
```

**Step 2: Create layout index**

```typescript
export { default as BmLayout } from './BmLayout.vue';
export { default as BmSidebar } from './BmSidebar.vue';
export { default as BmHeader } from './BmHeader.vue';
```

**Step 3: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/layout/BmLayout.vue frontend/packages/ui-vue/src/components/bigmodel/layout/index.ts
git commit -m "feat: add BmLayout component"
```

---

## Task 12: Create BmTable Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/data/BmTable.vue`
- Create: `frontend/packages/ui-vue/src/styles/components/table.scss`

**Step 1: Create BmTable component**

```vue
<template>
  <div class="bm-table-wrapper">
    <table class="bm-table">
      <thead>
        <tr>
          <th
            v-for="col in columns"
            :key="col.key"
            :class="{ sortable: col.sortable }"
            :style="{ width: col.width }"
            @click="col.sortable && handleSort(col.key)"
          >
            <span>{{ col.title }}</span>
            <span v-if="col.sortable" class="sort-icon">
              {{ getSortIcon(col.key) }}
            </span>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, index) in displayData" :key="index" class="table-row">
          <td v-for="col in columns" :key="col.key">
            <slot :name="col.key" :record="row" :index="index">
              {{ row[col.key] }}
            </slot>
          </td>
        </tr>
        <tr v-if="displayData.length === 0" class="table-empty">
          <td :colspan="columns.length">
            <div class="empty-content">
              <span class="empty-icon">📭</span>
              <span class="empty-text">暂无数据</span>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
interface Column {
  key: string;
  title: string;
  width?: string;
  sortable?: boolean;
}

interface Props {
  columns: Column[];
  data: Record<string, any>[];
}

const props = defineProps<Props>();

const sortKey = ref<string>('');
const sortOrder = ref<'asc' | 'desc' | ''>('');

const displayData = computed(() => {
  if (!sortKey.value) return props.data;

  return [...props.data].sort((a, b) => {
    const aVal = a[sortKey.value];
    const bVal = b[sortKey.value];

    if (aVal === bVal) return 0;

    const result = aVal > bVal ? 1 : -1;
    return sortOrder.value === 'asc' ? result : -result;
  });
});

const handleSort = (key: string) => {
  if (sortKey.value === key) {
    if (sortOrder.value === 'asc') {
      sortOrder.value = 'desc';
    } else if (sortOrder.value === 'desc') {
      sortKey.value = '';
      sortOrder.value = '';
    } else {
      sortOrder.value = 'asc';
    }
  } else {
    sortKey.value = key;
    sortOrder.value = 'asc';
  }
};

const getSortIcon = (key: string) => {
  if (sortKey.value !== key) return '⇅';
  if (sortOrder.value === 'asc') return '↑';
  if (sortOrder.value === 'desc') return '↓';
  return '⇅';
};
</script>

<style scoped lang="scss">
@import '../../../styles/components/table.scss';
</style>
```

**Step 2: Create table styles**

```scss
.bm-table-wrapper {
  overflow-x: auto;
  background: #fff;
  border-radius: var(--bm-radius-md);
}

.bm-table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--bm-font-size-sm);

  thead {
    background: var(--bm-bg-page);
    border-bottom: 1px solid var(--bm-border);
  }

  th {
    padding: var(--bm-space-md) var(--bm-space-lg);
    text-align: left;
    font-weight: var(--bm-font-weight-semibold);
    color: var(--bm-text-primary);

    &.sortable {
      cursor: pointer;
      user-select: none;

      &:hover {
        background: var(--bm-bg-hover);
      }
    }

    .sort-icon {
      margin-left: var(--bm-space-xs);
      font-size: 10px;
      color: var(--bm-text-tertiary);
    }
  }

  tbody {
    tr {
      border-bottom: 1px solid var(--bm-border-light);
      transition: background var(--bm-transition-fast);

      &:hover:not(.table-empty) {
        background: var(--bm-bg-hover);
      }

      &:last-child {
        border-bottom: none;
      }
    }
  }

  td {
    padding: var(--bm-space-md) var(--bm-space-lg);
    color: var(--bm-text-secondary);
  }

  &-empty {
    td {
      padding: var(--bm-space-2xl);
    }
  }
}

.table-empty .empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--bm-space-md);
  color: var(--bm-text-tertiary);

  .empty-icon {
    font-size: 48px;
    opacity: 0.5;
  }

  .empty-text {
    font-size: var(--bm-font-size-base);
  }
}
</style>
```

**Step 3: Create data index**

```typescript
export { default as BmTable } from './BmTable.vue';
```

**Step 4: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/data/ frontend/packages/ui-vue/src/styles/components/table.scss
git commit -m "feat: add BmTable component"
```

---

## Task 13: Create BmPagination Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/data/BmPagination.vue`

**Step 1: Create BmPagination component**

```vue
<template>
  <div class="bm-pagination">
    <span class="pagination-info">
      共 {{ total }} 条，第 {{ current }} / {{ totalPages }} 页
    </span>

    <div class="pagination-controls">
      <button
        class="pagination-btn"
        :disabled="current <= 1"
        @click="$emit('change', current - 1)"
      >
        ◀
      </button>

      <div class="pagination-pages">
        <button
          v-for="page in displayPages"
          :key="page"
          class="pagination-page"
          :class="{ 'is-active': page === current }"
          @click="$emit('change', page)"
        >
          {{ page }}
        </button>
      </div>

      <button
        class="pagination-btn"
        :disabled="current >= totalPages"
        @click="$emit('change', current + 1)"
      >
        ▶
      </button>
    </div>

    <select
      v-model="localPageSize"
      class="pagination-size"
      @change="handleSizeChange"
    >
      <option v-for="size in pageSizeOptions" :key="size" :value="size">
        {{ size }} 条/页
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
interface Props {
  current: number;
  pageSize: number;
  total: number;
  pageSizeOptions?: number[];
}

const props = withDefaults(defineProps<Props>(), {
  pageSizeOptions: [10, 20, 50, 100]
});

const emit = defineEmits<{
  change: [page: number];
  'update:pageSize': [size: number];
}>();

const localPageSize = ref(props.pageSize);

const totalPages = computed(() => Math.ceil(props.total / props.pageSize));

const displayPages = computed(() => {
  const pages: number[] = [];
  const maxVisible = 7;
  const halfVisible = Math.floor(maxVisible / 2);

  let start = Math.max(1, props.current - halfVisible);
  let end = Math.min(totalPages.value, props.current + halfVisible);

  if (props.current <= halfVisible) {
    end = Math.min(totalPages.value, maxVisible);
  }

  if (props.current + halfVisible >= totalPages.value) {
    start = Math.max(1, totalPages.value - maxVisible + 1);
  }

  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  return pages;
});

const handleSizeChange = () => {
  emit('update:pageSize', localPageSize.value);
  emit('change', 1);
};
</script>

<style scoped lang="scss">
@import '../../../styles/components/table.scss';

.bm-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--bm-space-lg);
  padding: var(--bm-space-lg);
  background: #fff;
  border-radius: var(--bm-radius-md);

  @media (max-width: 767px) {
    flex-direction: column;
    align-items: flex-start;
  }
}

.pagination-info {
  font-size: var(--bm-font-size-sm);
  color: var(--bm-text-secondary);
  white-space: nowrap;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: var(--bm-space-sm);
}

.pagination-btn,
.pagination-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 var(--bm-space-sm);
  border: 1px solid var(--bm-border);
  border-radius: var(--bm-radius-sm);
  background: #fff;
  color: var(--bm-text-primary);
  font-size: var(--bm-font-size-sm);
  cursor: pointer;
  transition: all var(--bm-transition-fast);

  &:hover:not(:disabled) {
    border-color: var(--bm-primary);
    color: var(--bm-primary);
  }

  &:disabled {
    color: var(--bm-text-disabled);
    cursor: not-allowed;
    background: var(--bm-bg-disabled);
  }
}

.pagination-page.is-active {
  background: var(--bm-primary);
  border-color: var(--bm-primary);
  color: #fff;
}

.pagination-pages {
  display: flex;
  gap: var(--bm-space-xs);
}

.pagination-size {
  padding: 6px var(--bm-space-md);
  border: 1px solid var(--bm-border);
  border-radius: var(--bm-radius-sm);
  background: #fff;
  color: var(--bm-text-primary);
  font-size: var(--bm-font-size-sm);
  cursor: pointer;
  transition: border-color var(--bm-transition-fast);

  &:focus {
    outline: none;
    border-color: var(--bm-primary);
  }
}

@media (max-width: 767px) {
  .pagination-pages {
    display: none;
  }
}
</style>
```

**Step 2: Update data index**

```typescript
export { default as BmTable } from './BmTable.vue';
export { default as BmPagination } from './BmPagination.vue';
```

**Step 3: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/data/BmPagination.vue
git commit -m "feat: add BmPagination component"
```

---

## Task 14: Create BmModal Component

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/feedback/BmModal.vue`

**Step 1: Create BmModal component**

```vue
<template>
  <Teleport to="body">
    <transition name="bm-modal">
      <div v-if="visible" class="bm-modal" @click.self="handleMaskClick">
        <div class="bm-modal__wrapper" :style="{ width }">
          <div class="bm-modal__header">
            <slot name="header">
              <span class="modal-title">{{ title }}</span>
            </slot>
            <button class="modal-close" @click="handleClose">
              <span>✕</span>
            </button>
          </div>

          <div class="bm-modal__body">
            <slot />
          </div>

          <div v-if="$slots.footer" class="bm-modal__footer">
            <slot name="footer">
              <bm-button @click="handleClose">取消</bm-button>
              <bm-button type="primary" @click="handleConfirm">确定</bm-button>
            </slot>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
interface Props {
  visible?: boolean;
  title?: string;
  width?: string;
  closeOnClickModal?: boolean;
  showClose?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  title: '提示',
  width: '480px',
  closeOnClickModal: true,
  showClose: true
});

const emit = defineEmits<{
  'update:visible': [visible: boolean];
  close: [];
  confirm: [];
}>();

const handleMaskClick = () => {
  if (props.closeOnClickModal) {
    handleClose();
  }
};

const handleClose = () => {
  emit('update:visible', false);
  emit('close');
};

const handleConfirm = () => {
  emit('confirm');
};

const handleEscape = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.visible) {
    handleClose();
  }
};

onMounted(() => {
  document.addEventListener('keydown', handleEscape);
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape);
});
</script>

<style scoped lang="scss">
.bm-modal {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
  z-index: var(--bm-z-modal);
  backdrop-filter: blur(2px);
}

.bm-modal__wrapper {
  background: #fff;
  border-radius: var(--bm-radius-md);
  box-shadow: var(--bm-shadow-lg);
  display: flex;
  flex-direction: column;
  max-height: 90vh;
}

.bm-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--bm-space-xl) var(--bm-space-2xl);
  border-bottom: 1px solid var(--bm-border-light);

  .modal-title {
    font-size: var(--bm-font-size-md);
    font-weight: var(--bm-font-weight-semibold);
    color: var(--bm-text-primary);
  }

  .modal-close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border: none;
    background: transparent;
    border-radius: var(--bm-radius-sm);
    cursor: pointer;
    color: var(--bm-text-tertiary);
    transition: all var(--bm-transition-fast);

    &:hover {
      background: var(--bm-bg-hover);
      color: var(--bm-text-primary);
    }

    span {
      font-size: 16px;
    }
  }
}

.bm-modal__body {
  padding: var(--bm-space-2xl);
  overflow-y: auto;
  color: var(--bm-text-secondary);
  font-size: var(--bm-font-size-base);
}

.bm-modal__footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--bm-space-md);
  padding: var(--bm-space-lg) var(--bm-space-2xl);
  border-top: 1px solid var(--bm-border-light);
}

.bm-modal-enter-active,
.bm-modal-leave-active {
  transition: all var(--bm-transition-normal);
}

.bm-modal-enter-active .bm-modal__wrapper,
.bm-modal-leave-active .bm-modal__wrapper {
  transition: all var(--bm-transition-normal);
}

.bm-modal-enter-from,
.bm-modal-leave-to {
  opacity: 0;
}

.bm-modal-enter-from .bm-modal__wrapper,
.bm-modal-leave-to .bm-modal__wrapper {
  transform: scale(0.9);
  opacity: 0;
}
</style>
```

**Step 2: Create feedback index**

```typescript
export { default as BmModal } from './BmModal.vue';
```

**Step 3: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/feedback/BmModal.vue
git commit -m "feat: add BmModal component"
```

---

## Task 15: Create BmToast and BmConfirm Components

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/feedback/BmToast.vue`
- Create: `frontend/packages/ui-vue/src/components/bigmodel/feedback/BmConfirm.vue`
- Create: `frontend/packages/ui-vue/src/components/bigmodel/feedback/useToast.ts
```

**Step 1: Create BmToast component**

```vue
<template>
  <Teleport to="body">
    <transition-group name="bm-toast" tag="div" class="bm-toast-container">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="bm-toast"
        :class="[`bm-toast--${toast.type}`]"
      >
        <span class="toast-icon">{{ getIcon(toast.type) }}</span>
        <span class="toast-message">{{ toast.message }}</span>
        <button class="toast-close" @click="remove(toast.id)">✕</button>
      </div>
    </transition-group>
  </Teleport>
</template>

<script setup lang="ts">
interface Toast {
  id: number;
  message: string;
  type: 'success' | 'warning' | 'error' | 'info';
  duration?: number;
}

const toasts = ref<Toast[]>([]);
let idCounter = 0;

const add = (message: string, type: Toast['type'] = 'info', duration = 3000) => {
  const id = ++idCounter;
  toasts.value.push({ id, message, type, duration });

  if (duration > 0) {
    setTimeout(() => remove(id), duration);
  }

  return id;
};

const remove = (id: number) => {
  const index = toasts.value.findIndex(t => t.id === id);
  if (index > -1) {
    toasts.value.splice(index, 1);
  }
};

const getIcon = (type: Toast['type']) => {
  const icons = {
    success: '✓',
    warning: '⚠',
    error: '✕',
    info: 'ⓘ'
  };
  return icons[type];
};

defineExpose({ add, remove });
</script>

<style scoped lang="scss">
.bm-toast-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: var(--bm-z-toast);
  display: flex;
  flex-direction: column;
  gap: var(--bm-space-md);
  pointer-events: none;
}

.bm-toast {
  display: flex;
  align-items: center;
  gap: var(--bm-space-md);
  min-width: 300px;
  max-width: 500px;
  padding: var(--bm-space-md) var(--bm-space-lg);
  background: #fff;
  border-radius: var(--bm-radius-sm);
  box-shadow: var(--bm-shadow-lg);
  pointer-events: auto;

  &--success {
    border-left: 4px solid var(--bm-success);
    .toast-icon { color: var(--bm-success); }
  }

  &--warning {
    border-left: 4px solid var(--bm-warning);
    .toast-icon { color: var(--bm-warning); }
  }

  &--error {
    border-left: 4px solid var(--bm-danger);
    .toast-icon { color: var(--bm-danger); }
  }

  &--info {
    border-left: 4px solid var(--bm-info);
    .toast-icon { color: var(--bm-info); }
  }
}

.toast-icon {
  font-size: 18px;
  font-weight: bold;
  flex-shrink: 0;
}

.toast-message {
  flex: 1;
  font-size: var(--bm-font-size-sm);
  color: var(--bm-text-primary);
}

.toast-close {
  flex-shrink: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--bm-text-tertiary);
  font-size: 14px;

  &:hover {
    color: var(--bm-text-primary);
  }
}

.bm-toast-enter-active,
.bm-toast-leave-active {
  transition: all var(--bm-transition-normal);
}

.bm-toast-enter-from,
.bm-toast-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
```

**Step 2: Create toast composable**

```typescript
// frontend/packages/ui-vue/src/components/bigmodel/feedback/useToast.ts
import { ref, type ComponentPublicInstance } from 'vue';

const toastRef = ref<ComponentPublicInstance | null>(null);

export const setToastRef = (ref: ComponentPublicInstance | null) => {
  toastRef.value = ref;
};

export const useToast = () => {
  return {
    success: (message: string, duration?: number) =>
      toastRef.value?.add?.(message, 'success', duration),
    warning: (message: string, duration?: number) =>
      toastRef.value?.add?.(message, 'warning', duration),
    error: (message: string, duration?: number) =>
      toastRef.value?.add?.(message, 'error', duration),
    info: (message: string, duration?: number) =>
      toastRef.value?.add?.(message, 'info', duration)
  };
};
```

**Step 3: Create BmConfirm component**

```vue
<template>
  <bm-modal
    :visible="visible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    @update:visible="handleVisible"
    @confirm="handleConfirm"
    @close="handleCancel"
  >
    <div class="confirm-content">
      <span class="confirm-icon" :class="`confirm-icon--${type}`">{{ icon }}</span>
      <span class="confirm-message">{{ message }}</span>
    </div>

    <template #footer>
      <bm-button @click="handleCancel">{{ cancelText }}</bm-button>
      <bm-button :type="buttonType" @click="handleConfirm">{{ confirmText }}</bm-button>
    </template>
  </bm-modal>
</template>

<script setup lang="ts">
interface Props {
  visible?: boolean;
  type?: 'warning' | 'danger' | 'info';
  title?: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  width?: string;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'warning',
  title: '确认',
  confirmText: '确定',
  cancelText: '取消',
  width: '400px'
});

const emit = defineEmits<{
  'update:visible': [visible: boolean];
  confirm: [];
  cancel: [];
}>();

const icon = computed(() => {
  const icons = {
    warning: '⚠',
    danger: '✕',
    info: 'ⓘ'
  };
  return icons[props.type];
});

const buttonType = computed(() => {
  return props.type === 'danger' ? 'danger' : 'primary';
});

const handleVisible = (value: boolean) => {
  emit('update:visible', value);
};

const handleConfirm = () => {
  emit('confirm');
  emit('update:visible', false);
};

const handleCancel = () => {
  emit('cancel');
  emit('update:visible', false);
};
</script>

<style scoped lang="scss">
.confirm-content {
  display: flex;
  align-items: flex-start;
  gap: var(--bm-space-md);
}

.confirm-icon {
  font-size: 24px;
  flex-shrink: 0;

  &--warning { color: var(--bm-warning); }
  &--danger { color: var(--bm-danger); }
  &--info { color: var(--bm-info); }
}

.confirm-message {
  flex: 1;
  color: var(--bm-text-primary);
  line-height: 1.5;
}
</style>
```

**Step 4: Update feedback index**

```typescript
export { default as BmModal } from './BmModal.vue';
export { default as BmToast } from './BmToast.vue';
export { default as BmConfirm } from './BmConfirm.vue';
export { setToastRef, useToast } from './useToast.ts';
```

**Step 5: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/feedback/
git commit -m "feat: add BmToast and BmConfirm components"
```

---

## Task 16: Create BmAvatar and BmBadge Components

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/other/BmAvatar.vue`
- Create: `frontend/packages/ui-vue/src/components/bigmodel/other/BmBadge.vue`

**Step 1: Create BmAvatar component**

```vue
<template>
  <div class="bm-avatar" :class="[size, { shape }]" @click="$emit('click', $event)">
    <img v-if="src" :src="src" :alt="alt" class="avatar-image" />
    <span v-else class="avatar-text">{{ text }}</span>
    <slot />
  </div>
</template>

<script setup lang="ts">
interface Props {
  src?: string;
  alt?: string;
  text?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  shape?: 'circle' | 'square';
}

const props = withDefaults(defineProps<Props>(), {
  alt: 'avatar',
  size: 'md',
  shape: 'circle'
});

defineEmits<{
  click: [event: MouseEvent];
}>();

const avatarText = computed(() => {
  if (props.text) return props.text;
  return props.alt?.charAt(0).toUpperCase() || '?';
});
</script>

<style scoped lang="scss">
.bm-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: var(--bm-bg-hover);
  color: var(--bm-text-secondary);
  font-size: var(--bm-font-size-base);
  user-select: none;

  &.circle {
    border-radius: 50%;
  }

  &.square {
    border-radius: var(--bm-radius-sm);
  }

  &.xs {
    width: 24px;
    height: 24px;
    font-size: 10px;
  }

  &.sm {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }

  &.md {
    width: 40px;
    height: 40px;
    font-size: 14px;
  }

  &.lg {
    width: 48px;
    height: 48px;
    font-size: 16px;
  }

  &.xl {
    width: 64px;
    height: 64px;
    font-size: 20px;
  }
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text {
  font-weight: var(--bm-font-weight-medium);
}
</style>
```

**Step 2: Create BmBadge component**

```vue
<template>
  <div class="bm-badge" :class="[type, { dot }]">
    <slot />
    <transition name="badge-zoom">
      <span v-if="showBadge" class="badge-content" :class="{ 'is-dot': dot }">
        <template v-if="!dot">{{ displayValue }}</template>
      </span>
    </transition>
  </div>
</template>

<script setup lang="ts">
interface Props {
  value?: number | string;
  max?: number;
  dot?: boolean;
  hidden?: boolean;
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
}

const props = withDefaults(defineProps<Props>(), {
  max: 99
});

const showBadge = computed(() => {
  if (props.hidden) return false;
  if (props.dot) return true;
  return props.value !== undefined && props.value !== 0;
});

const displayValue = computed(() => {
  if (typeof props.value === 'number' && props.value > props.max) {
    return `${props.max}+`;
  }
  return props.value;
});
</script>

<style scoped lang="scss">
.bm-badge {
  position: relative;
  display: inline-flex;
  vertical-align: middle;
}

.badge-content {
  position: absolute;
  top: 0;
  right: 0;
  transform: translate(50%, -50%);
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--bm-danger);
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  white-space: nowrap;
  font-weight: var(--bm-font-weight-medium);

  &.is-dot {
    min-width: 8px;
    width: 8px;
    height: 8px;
    padding: 0;
    border-radius: 50%;
  }
}

.bm-badge.primary .badge-content { background: var(--bm-primary); }
.bm-badge.success .badge-content { background: var(--bm-success); }
.bm-badge.warning .badge-content { background: var(--bm-warning); }
.bm-badge.info .badge-content { background: var(--bm-info); }
.bm-badge.danger .badge-content { background: var(--bm-danger); }

.badge-zoom-enter-active,
.badge-zoom-leave-active {
  transition: all var(--bm-transition-fast);
}

.badge-zoom-enter-from,
.badge-zoom-leave-to {
  opacity: 0;
  transform: scale(0) translate(50%, -50%);
}
</style>
```

**Step 3: Update other index**

```typescript
export { default as BmIcon } from './BmIcon.vue';
export { default as BmAvatar } from './BmAvatar.vue';
export { default as BmBadge } from './BmBadge.vue';
```

**Step 4: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/other/
git commit -m "feat: add BmAvatar and BmBadge components"
```

---

## Task 17: Create BigModel Component Library Index

**Files:**
- Create: `frontend/packages/ui-vue/src/components/bigmodel/index.ts`

**Step 1: Create main index file**

```typescript
// Layout
export { BmLayout, BmSidebar, BmHeader } from './layout';

// Card
export { BmCard } from './card';

// Button
export { BmButton } from './button';

// Form
export { BmInput } from './form/BmInput.vue';
export { BmSelect } from './form/BmSelect.vue';
export { BmCheckbox } from './form/BmCheckbox.vue';
export { BmRadio } from './form/BmRadio.vue';
export { BmSwitch } from './form/BmSwitch.vue';

// Data
export { BmTable, BmPagination } from './data';

// Feedback
export {
  BmModal,
  BmToast,
  BmConfirm,
  setToastRef,
  useToast
} from './feedback';

// Other
export { BmIcon, BmAvatar, BmBadge } from './other';

// Types
export type { MenuItem, MenuGroup } from './layout/BmSidebar.vue';
export type { Column } from './data/BmTable.vue';
```

**Step 2: Commit**

```bash
git add frontend/packages/ui-vue/src/components/bigmodel/index.ts
git commit -m "feat: add BigModel component library index"
```

---

## Task 18: Update Package Entry Point

**Files:**
- Modify: `frontend/packages/ui-vue/src/index.ts`

**Step 1: Add BigModel exports to package index**

Add to `frontend/packages/ui-vue/src/index.ts`:

```typescript
// BigModel components
export * from './components/bigmodel';
```

**Step 2: Commit**

```bash
git add frontend/packages/ui-vue/src/index.ts
git commit -m "feat: export BigModel components from package index"
```

---

## Task 19: Update Main Styles

**Files:**
- Modify: `frontend/packages/ui-vue/src/styles/index.scss`

**Step 1: Import all component styles**

Add to `frontend/packages/ui-vue/src/styles/index.scss`:

```scss
// Themes
@import './themes/bigmodel.scss';

// Components
@import './components/layout.scss';
@import './components/card.scss';
@import './components/button.scss';
@import './components/form.scss';
@import './components/table.scss';

// Global reset
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  font-size: 14px;
}

body {
  font-family: var(--bm-font-family);
  font-size: var(--bm-font-size-base);
  color: var(--bm-text-primary);
  background: var(--bm-bg-page);
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

a {
  color: var(--bm-primary);
  text-decoration: none;
  transition: color var(--bm-transition-fast);

  &:hover {
    color: var(--bm-primary-hover);
  }
}

button,
input,
select,
textarea {
  font-family: inherit;
  font-size: inherit;
}
```

**Step 2: Commit**

```bash
git add frontend/packages/ui-vue/src/styles/index.scss
git commit -m "feat: update main styles with BigModel imports"
```

---

## Task 20: Migrate Login Page

**Files:**
- Modify: `frontend/src/views/auth/Login.vue`

**Step 1: Update Login component with BigModel style**

```vue
<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <img src="/logo.svg" alt="AdminPlus" class="login-logo" />
        <h1 class="login-title">AdminPlus</h1>
        <p class="login-subtitle">基于角色的权限管理系统</p>
      </div>

      <bm-card class="login-card">
        <bm-form @submit="handleLogin">
          <div class="form-item">
            <label class="form-label">用户名</label>
            <bm-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="👤"
              @keyup.enter="handleLogin"
            />
          </div>

          <div class="form-item">
            <label class="form-label">密码</label>
            <bm-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="🔒"
              show-password
              @keyup.enter="handleLogin"
            />
          </div>

          <div class="form-item">
            <bm-checkbox v-model="loginForm.remember">记住我</bm-checkbox>
          </div>

          <bm-button type="primary" long :loading="loading" @click="handleLogin">
            登录
          </bm-button>
        </bm-form>
      </bm-card>

      <div class="login-footer">
        <p>© 2026 AdminPlus. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
});

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    // Show error toast
    return;
  }

  loading.value = true;
  try {
    await userStore.login({
      username: loginForm.username,
      password: loginForm.password,
      remember: loginForm.remember
    });
    router.push('/');
  } catch (error) {
    // Handle error
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bm-bg-page) 0%, #e8ecf1 100%);
  padding: var(--bm-space-lg);
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: var(--bm-space-2xl);

  .login-logo {
    width: 64px;
    height: 64px;
    margin-bottom: var(--bm-space-lg);
  }

  .login-title {
    font-size: var(--bm-font-size-xl);
    font-weight: var(--bm-font-weight-semibold);
    color: var(--bm-text-primary);
    margin-bottom: var(--bm-space-sm);
  }

  .login-subtitle {
    font-size: var(--bm-font-size-sm);
    color: var(--bm-text-tertiary);
  }
}

.login-card {
  margin-bottom: var(--bm-space-xl);
}

.form-item {
  margin-bottom: var(--bm-space-lg);

  &:last-child {
    margin-bottom: 0;
  }
}

.form-label {
  display: block;
  margin-bottom: var(--bm-space-sm);
  font-size: var(--bm-font-size-sm);
  font-weight: var(--bm-font-weight-medium);
  color: var(--bm-text-primary);
}

.login-footer {
  text-align: center;
  font-size: var(--bm-font-size-xs);
  color: var(--bm-text-tertiary);
}
</style>
```

**Step 2: Commit**

```bash
git add frontend/src/views/auth/Login.vue
git commit -m "feat: migrate Login page to BigModel style"
```

---

## Task 21: Migrate Dashboard Page

**Files:**
- Modify: `frontend/src/views/Dashboard.vue`

**Step 1: Update Dashboard component with BigModel style**

```vue
<template>
  <div class="dashboard-page">
    <div class="dashboard-header">
      <h1 class="page-title">控制台</h1>
      <p class="page-subtitle">欢迎回来，{{ user.name }}</p>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <bm-card v-for="stat in stats" :key="stat.id" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon" :style="{ background: stat.color }">
            {{ stat.icon }}
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
        <div v-if="stat.change" class="stat-change" :class="stat.changeType">
          <span>{{ stat.changeType === 'up' ? '↑' : '↓' }}</span>
          <span>{{ stat.change }}</span>
        </div>
      </bm-card>
    </div>

    <!-- Quick Actions -->
    <bm-card class="actions-card">
      <template #header>
        <div class="card-title">快捷操作</div>
      </template>
      <div class="actions-grid">
        <button
          v-for="action in quickActions"
          :key="action.id"
          class="action-item"
          @click="handleAction(action)"
        >
          <span class="action-icon">{{ action.icon }}</span>
          <span class="action-label">{{ action.label }}</span>
        </button>
      </div>
    </bm-card>

    <!-- Recent Activity -->
    <div class="content-row">
      <bm-card class="activity-card">
        <template #header>
          <div class="card-title">最近活动</div>
        </template>
        <div class="activity-list">
          <div v-for="activity in activities" :key="activity.id" class="activity-item">
            <span class="activity-icon">{{ activity.icon }}</span>
            <div class="activity-content">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
          </div>
        </div>
      </bm-card>

      <bm-card class="chart-card">
        <template #header>
          <div class="card-title">数据统计</div>
        </template>
        <div class="chart-placeholder">
          <span class="chart-icon">📊</span>
          <span class="chart-text">图表区域</span>
        </div>
      </bm-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const user = computed(() => userStore.userInfo);

const stats = ref([
  { id: 1, label: '总用户数', value: '1,234', icon: '👥', color: '#e8eaff', change: '+12%', changeType: 'up' },
  { id: 2, label: '活跃用户', value: '856', icon: '⚡', color: '#e8ffea', change: '+5%', changeType: 'up' },
  { id: 3, label: '今日访问', value: '2,345', icon: '👁', color: '#fff7e8', change: '-3%', changeType: 'down' },
  { id: 4, label: '系统消息', value: '12', icon: '🔔', color: '#ffece8', change: null, changeType: null }
]);

const quickActions = ref([
  { id: 1, label: '添加用户', icon: '👤+', path: '/system/user' },
  { id: 2, label: '新建角色', icon: '🔑', path: '/system/role' },
  { id: 3, label: '系统配置', icon: '⚙', path: '/system/config' },
  { id: 4, label: '查看日志', icon: '📋', path: '/system/log' }
]);

const activities = ref([
  { id: 1, title: '用户 admin 更新了系统配置', time: '5分钟前', icon: '⚙' },
  { id: 2, title: '新用户 user001 注册成功', time: '15分钟前', icon: '👤' },
  { id: 3, title: '角色 管理员 权限已更新', time: '1小时前', icon: '🔑' },
  { id: 4, title: '系统备份完成', time: '2小时前', icon: '💾' }
]);

const handleAction = (action: any) => {
  // Navigate to action path
};
</script>

<style scoped lang="scss">
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: var(--bm-space-xl);
}

.dashboard-header {
  .page-title {
    font-size: var(--bm-font-size-xl);
    font-weight: var(--bm-font-weight-semibold);
    color: var(--bm-text-primary);
    margin-bottom: var(--bm-space-xs);
  }

  .page-subtitle {
    font-size: var(--bm-font-size-sm);
    color: var(--bm-text-secondary);
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--bm-space-lg);
}

.stat-card {
  &:deep(.card-body) {
    display: flex;
    flex-direction: column;
    gap: var(--bm-space-md);
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: var(--bm-space-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--bm-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: var(--bm-font-size-xl);
  font-weight: var(--bm-font-weight-semibold);
  color: var(--bm-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: var(--bm-font-size-sm);
  color: var(--bm-text-secondary);
  margin-top: 2px;
}

.stat-change {
  font-size: var(--bm-font-size-xs);
  display: flex;
  align-items: center;
  gap: 2px;

  &.up {
    color: var(--bm-success);
  }

  &.down {
    color: var(--bm-danger);
  }
}

.actions-card {
  &:deep(.card-body) {
    padding: var(--bm-space-lg);
  }
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--bm-space-md);
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--bm-space-sm);
  padding: var(--bm-space-lg);
  border: 1px solid var(--bm-border-light);
  border-radius: var(--bm-radius-sm);
  background: var(--bm-bg-white);
  cursor: pointer;
  transition: all var(--bm-transition-fast);

  &:hover {
    border-color: var(--bm-primary);
    background: var(--bm-primary-light);
  }

  .action-icon {
    font-size: 24px;
  }

  .action-label {
    font-size: var(--bm-font-size-sm);
    color: var(--bm-text-primary);
  }
}

.content-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--bm-space-lg);

  @media (max-width: 992px) {
    grid-template-columns: 1fr;
  }
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--bm-space-md);
}

.activity-item {
  display: flex;
  align-items: center;
  gap: var(--bm-space-md);
  padding: var(--bm-space-md);
  border-radius: var(--bm-radius-sm);
  transition: background var(--bm-transition-fast);

  &:hover {
    background: var(--bm-bg-hover);
  }

  .activity-icon {
    font-size: 20px;
  }

  .activity-content {
    flex: 1;
  }

  .activity-title {
    font-size: var(--bm-font-size-sm);
    color: var(--bm-text-primary);
    margin-bottom: 2px;
  }

  .activity-time {
    font-size: var(--bm-font-size-xs);
    color: var(--bm-text-tertiary);
  }
}

.chart-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--bm-space-2xl);
  gap: var(--bm-space-md);
  color: var(--bm-text-tertiary);

  .chart-icon {
    font-size: 48px;
    opacity: 0.5;
  }

  .chart-text {
    font-size: var(--bm-font-size-sm);
  }
}

@media (max-width: 767px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
```

**Step 2: Commit**

```bash
git add frontend/src/views/Dashboard.vue
git commit -m "feat: migrate Dashboard page to BigModel style"
```

---

## Task 22: Migrate User Management Page

**Files:**
- Modify: `frontend/src/views/system/User.vue`

**Step 1: Update User component with BigModel style**

```vue
<template>
  <div class="user-page">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <bm-button type="primary" @click="handleAdd">
        <span class="btn-icon">＋</span>
        新增用户
      </bm-button>
    </div>

    <bm-card>
      <!-- Search Form -->
      <div class="search-form">
        <bm-input
          v-model="searchForm.username"
          placeholder="用户名"
          clearable
          style="width: 200px"
        />
        <bm-input
          v-model="searchForm.phone"
          placeholder="手机号"
          clearable
          style="width: 200px"
        />
        <bm-select
          v-model="searchForm.status"
          :options="statusOptions"
          placeholder="状态"
          style="width: 120px"
        />
        <bm-button type="primary" @click="handleSearch">搜索</bm-button>
        <bm-button @click="handleReset">重置</bm-button>
      </div>

      <!-- Table -->
      <bm-table
        :columns="columns"
        :data="tableData"
        style="margin-top: 16px"
      >
        <template #status="{ record }">
          <bm-badge
            :value="record.status === 1 ? '正常' : '禁用'"
            :type="record.status === 1 ? 'success' : 'danger'"
          />
        </template>
        <template #action="{ record }">
          <bm-button size="small" @click="handleEdit(record)">编辑</bm-button>
          <bm-button size="small" type="danger" @click="handleDelete(record)">删除</bm-button>
        </template>
      </bm-table>

      <!-- Pagination -->
      <div style="margin-top: 16px">
        <bm-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          @change="handlePageChange"
        />
      </div>
    </bm-card>

    <!-- Edit Modal -->
    <bm-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      width="600px"
      @confirm="handleSave"
    >
      <bm-form>
        <div class="form-item">
          <label class="form-label">用户名 <span class="required">*</span></label>
          <bm-input v-model="formData.username" placeholder="请输入用户名" />
        </div>
        <div class="form-item">
          <label class="form-label">姓名 <span class="required">*</span></label>
          <bm-input v-model="formData.realName" placeholder="请输入姓名" />
        </div>
        <div class="form-item">
          <label class="form-label">手机号</label>
          <bm-input v-model="formData.phone" placeholder="请输入手机号" />
        </div>
        <div class="form-item">
          <label class="form-label">邮箱</label>
          <bm-input v-model="formData.email" placeholder="请输入邮箱" />
        </div>
        <div class="form-item">
          <label class="form-label">角色</label>
          <bm-select
            v-model="formData.roleId"
            :options="roleOptions"
            placeholder="请选择角色"
          />
        </div>
        <div v-if="!formData.id" class="form-item">
          <label class="form-label">密码 <span class="required">*</span></label>
          <bm-input v-model="formData.password" type="password" placeholder="请输入密码" />
        </div>
        <div class="form-item">
          <label class="form-label">状态</label>
          <bm-switch v-model="formData.status" />
        </div>
      </bm-form>
    </bm-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';

interface User {
  id?: number;
  username: string;
  realName: string;
  phone?: string;
  email?: string;
  roleId?: number;
  password?: string;
  status: boolean;
}

const columns = [
  { key: 'id', title: 'ID', width: '80px' },
  { key: 'username', title: '用户名' },
  { key: 'realName', title: '姓名' },
  { key: 'phone', title: '手机号' },
  { key: 'email', title: '邮箱' },
  { key: 'roleName', title: '角色' },
  { key: 'status', title: '状态', slot: true },
  { key: 'createdAt', title: '创建时间' },
  { key: 'action', title: '操作', slot: true, width: '150px' }
];

const statusOptions = [
  { label: '全部', value: '' },
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
];

const roleOptions = [
  { label: '管理员', value: 1 },
  { label: '普通用户', value: 2 }
];

const searchForm = reactive({
  username: '',
  phone: '',
  status: ''
});

const formData = reactive<User>({
  username: '',
  realName: '',
  phone: '',
  email: '',
  roleId: undefined,
  password: '',
  status: true
});

const tableData = ref([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});

const modalVisible = ref(false);
const modalTitle = computed(() => formData.id ? '编辑用户' : '新增用户');

const handleAdd = () => {
  Object.assign(formData, {
    username: '',
    realName: '',
    phone: '',
    email: '',
    roleId: undefined,
    password: '',
    status: true
  });
  modalVisible.value = true;
};

const handleEdit = (record: any) => {
  Object.assign(formData, record);
  modalVisible.value = true;
};

const handleDelete = (record: any) => {
  // Show confirm dialog
};

const handleSearch = () => {
  pagination.current = 1;
  loadData();
};

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    phone: '',
    status: ''
  });
  handleSearch();
};

const handleSave = () => {
  // Save user
};

const handlePageChange = (page: number) => {
  pagination.current = page;
  loadData();
};

const loadData = async () => {
  // Load data from API
};

onMounted(() => {
  loadData();
});
</script>

<style scoped lang="scss">
.user-page {
  display: flex;
  flex-direction: column;
  gap: var(--bm-space-lg);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .page-title {
    font-size: var(--bm-font-size-xl);
    font-weight: var(--bm-font-weight-semibold);
    color: var(--bm-text-primary);
  }

  .btn-icon {
    font-size: 16px;
  }
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: var(--bm-space-md);

  @media (max-width: 767px) {
    flex-direction: column;

    > * {
      width: 100% !important;
    }
  }
}

.form-item {
  margin-bottom: var(--bm-space-lg);

  &:last-child {
    margin-bottom: 0;
  }
}

.form-label {
  display: block;
  margin-bottom: var(--bm-space-sm);
  font-size: var(--bm-font-size-sm);
  font-weight: var(--bm-font-weight-medium);
  color: var(--bm-text-primary);

  .required {
    color: var(--bm-danger);
  }
}
</style>
```

**Step 2: Commit**

```bash
git add frontend/src/views/system/User.vue
git commit -m "feat: migrate User management page to BigModel style"
```

---

## Task 23: Migrate Role Management Page

**Files:**
- Modify: `frontend/src/views/system/Role.vue`

**Step 1: Update Role component with BigModel style**

Similar structure to User page with:
- Search form for role filtering
- Table displaying roles with permissions
- Modal for adding/editing roles
- Permission tree selection in modal

**Step 2: Commit**

```bash
git add frontend/src/views/system/Role.vue
git commit -m "feat: migrate Role management page to BigModel style"
```

---

## Task 24: Migrate Menu Management Page

**Files:**
- Modify: `frontend/src/views/system/Menu.vue`

**Step 1: Update Menu component with BigModel style**

Use tree/table structure for menu hierarchy with BigModel styled components.

**Step 2: Commit**

```bash
git add frontend/src/views/system/Menu.vue
git commit -m "feat: migrate Menu management page to BigModel style"
```

---

## Task 25: Migrate Department Management Page

**Files:**
- Modify: `frontend/src/views/system/Dept.vue`

**Step 1: Update Dept component with BigModel style**

Use tree structure for department hierarchy.

**Step 2: Commit**

```bash
git add frontend/src/views/system/Dept.vue
git commit -m "feat: migrate Department management page to BigModel style"
```

---

## Task 26: Migrate Settings Pages

**Files:**
- Modify: `frontend/src/views/Profile.vue`
- Modify: `frontend/src/views/system/Config.vue`
- Modify: `frontend/src/views/system/Dict.vue`

**Step 1: Update Settings pages with BigModel style**

Match BigModel's account settings page layout with card-based sections.

**Step 2: Commit**

```bash
git add frontend/src/views/Profile.vue frontend/src/views/system/Config.vue frontend/src/views/system/Dict.vue
git commit -m "feat: migrate Settings pages to BigModel style"
```

---

## Task 27: Update App Root Layout

**Files:**
- Modify: `frontend/src/App.vue`

**Step 1: Replace AdminLayout with BmLayout**

```vue
<template>
  <bm-layout
    v-if="isAuthenticated"
    :user="userInfo"
    :menu-groups="menuGroups"
    :breadcrumbs="breadcrumbs"
    :notification-count="notificationCount"
    @search="handleSearch"
    @notification="handleNotification"
    @user-menu="handleUserMenu"
  >
    <router-view />
  </bm-layout>
  <router-view v-else />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { BmLayout } from '@adminplus/ui-vue';

const route = useRoute();
const userStore = useUserStore();

const isAuthenticated = computed(() => userStore.token);
const userInfo = computed(() => userStore.userInfo);
const notificationCount = computed(() => userStore.notificationCount || 0);

// Define menu groups structure
const menuGroups = computed(() => [
  {
    id: 'system',
    title: '系统管理',
    icon: '⚙',
    expanded: true,
    items: [
      { id: 'user', title: '用户管理', icon: '👤', path: '/system/user' },
      { id: 'role', title: '角色管理', icon: '🔑', path: '/system/role' },
      { id: 'menu', title: '菜单管理', icon: '☰', path: '/system/menu' },
      { id: 'dept', title: '部门管理', icon: '🏢', path: '/system/dept' }
    ]
  }
]);

const breadcrumbs = computed(() => {
  // Generate breadcrumbs from route
  return route.meta.breadcrumb || [];
});

const handleSearch = (value: string) => {
  // Handle search
};

const handleNotification = () => {
  // Handle notification click
};

const handleUserMenu = () => {
  // Handle user menu click
};
</script>

<style>
@import '@adminplus/ui-vue/styles';
</style>
```

**Step 2: Commit**

```bash
git add frontend/src/App.vue
git commit -m "feat: update App root layout to use BmLayout"
```

---

## Task 28: Remove Element Plus Dependencies

**Files:**
- Modify: `frontend/package.json`

**Step 1: Remove Element Plus from dependencies**

Remove these lines from `frontend/package.json`:
```json
"@element-plus/icons-vue": "^2.3.0",
"element-plus": "^2.8.0",
```

**Step 2: Clean up Element Plus imports**

Search for and remove all Element Plus imports in the codebase:
```bash
cd frontend
grep -r "from 'element-plus'" src/ --exclude-dir=node_modules
grep -r "from '@element-plus/icons-vue'" src/ --exclude-dir=node_modules
```

**Step 3: Commit**

```bash
git add frontend/package.json
git commit -m "chore: remove Element Plus dependencies"
```

---

## Task 29: Final Testing and Polish

**Files:**
- Test all pages
- Fix any styling inconsistencies

**Step 1: Run development server**

```bash
cd frontend
npm run dev
```

**Step 2: Manual testing checklist**

- [ ] Login page displays correctly
- [ ] Dashboard loads with all components
- [ ] Sidebar menu expands/collapses
- [ ] All table pages display properly
- [ ] Forms validate and submit
- [ ] Modals open and close
- [ ] Toast notifications work
- [ ] Responsive design on mobile

**Step 3: Fix any issues found**

**Step 4: Final commit**

```bash
git add frontend/
git commit -m "polish: final styling adjustments and bug fixes"
```

---

## Task 30: Update Documentation

**Files:**
- Update: `CLAUDE.md`
- Create: `frontend/packages/ui-vue/README.md`

**Step 1: Update CLAUDE.md**

Add BigModel components section to frontend documentation.

**Step 2: Create component library README**

```markdown
# AdminPlus UI Vue - BigModel Components

Custom component library matching BigModel design style.

## Usage

```vue
<script setup>
import { BmButton, BmCard, BmInput } from '@adminplus/ui-vue';
</script>

<template>
  <bm-card>
    <bm-input v-model="value" placeholder="Enter text" />
    <bm-button type="primary" @click="submit">Submit</bm-button>
  </bm-card>
</template>
```

## Components

- Layout: BmLayout, BmSidebar, BmHeader
- Card: BmCard
- Button: BmButton
- Form: BmInput, BmSelect, BmCheckbox, BmRadio, BmSwitch
- Data: BmTable, BmPagination
- Feedback: BmModal, BmToast, BmConfirm
- Other: BmIcon, BmAvatar, BmBadge
```

**Step 3: Commit**

```bash
git add CLAUDE.md frontend/packages/ui-vue/README.md
git commit -m "docs: add BigModel component library documentation"
```

---

## Summary

This implementation plan creates a complete BigModel-style component library from scratch and migrates all pages to use the new components. The plan follows TDD principles, DRY, YAGNI, and includes frequent commits.

**Total tasks:** 30
**Estimated completion time:** 20-30 hours

**Key changes:**
- Full custom component library replacing Element Plus
- BigModel color scheme and typography
- Collapsible sidebar menu with groups
- Card-based content layout
- Clean, minimal design language
