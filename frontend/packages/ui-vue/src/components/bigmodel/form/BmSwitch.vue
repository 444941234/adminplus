<template>
  <div
    class="bm-switch"
    :class="[
      {
        'is-checked': isChecked,
        'is-disabled': disabled,
        'is-loading': loading
      },
      size
    ]"
    @click="handleClick"
  >
    <input
      ref="input"
      class="bm-switch__input"
      type="checkbox"
      :name="name"
      :disabled="disabled"
      :checked="isChecked"
      @change="handleChange"
    />
    <span class="bm-switch__core">
      <span v-if="loading" class="bm-switch__action">⟳</span>
      <span v-else class="bm-switch__action"></span>
    </span>
    <span v-if="$slots.default || label" class="bm-switch__label">
      <slot>{{ label }}</slot>
    </span>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'BmSwitch'
});

interface Props {
  modelValue?: boolean;
  disabled?: boolean;
  loading?: boolean;
  size?: 'small' | 'medium' | 'large';
  name?: string;
  label?: string;
  beforeChange?: () => boolean | Promise<boolean>;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  disabled: false,
  loading: false,
  size: 'medium',
  name: '',
  label: ''
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  change: [value: boolean];
}>();

const isChecked = computed(() => props.modelValue);

const handleClick = () => {
  if (props.disabled || props.loading) return;
};

const handleChange = async () => {
  if (props.disabled || props.loading) return;

  if (props.beforeChange) {
    const shouldChange = await props.beforeChange();
    if (!shouldChange) return;
  }

  const newValue = !props.modelValue;
  emit('update:modelValue', newValue);
  emit('change', newValue);
};
</script>

<style scoped lang="scss">
.bm-switch {
  display: inline-flex;
  align-items: center;
  position: relative;
  font-size: 14px;
  line-height: 20px;
  height: 32px;
  vertical-align: middle;
  cursor: pointer;

  &.is-disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }

  &.is-loading {
    cursor: wait;
  }

  &.small {
    height: 24px;
    font-size: 12px;
    line-height: 16px;
  }

  &.large {
    height: 40px;
    font-size: 16px;
    line-height: 24px;
  }

  &__input {
    position: absolute;
    width: 0;
    height: 0;
    opacity: 0;
    margin: 0;
  }

  &__core {
    position: relative;
    display: inline-block;
    width: 44px;
    height: 22px;
    border-radius: var(--radius-full);
    background-color: var(--border-color);
    border: 1px solid var(--border-dark);
    transition: all var(--transition-normal);
    box-shadow: var(--shadow-inner);

    &::after {
      content: '';
      position: absolute;
      top: 1px;
      left: 1px;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      background-color: var(--bg-primary);
      transition: all var(--transition-normal);
      box-shadow: var(--shadow-sm);
    }
  }

  &__action {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: var(--primary-color);
    font-size: 12px;
    font-weight: bold;
    transition: all var(--transition-normal);
  }

  &.is-checked &__core {
    background: var(--primary-gradient);
    border-color: var(--primary-dark);

    &::after {
      left: calc(100% - 19px);
    }
  }

  &.small &__core {
    width: 36px;
    height: 18px;

    &::after {
      width: 14px;
      height: 14px;
    }
  }

  &.small.is-checked &__core::after {
    left: calc(100% - 15px);
  }

  &.large &__core {
    width: 52px;
    height: 26px;

    &::after {
      width: 22px;
      height: 22px;
    }
  }

  &.large.is-checked &__core::after {
    left: calc(100% - 23px);
  }

  &.is-loading &__core {
    background-color: var(--primary-light);
  }

  &__label {
    margin-left: var(--space-sm);
    color: var(--text-primary);
    transition: color var(--transition-normal);
  }

  &.is-disabled &__label {
    color: var(--text-disabled);
  }

  &:hover:not(.is-disabled):not(.is-loading) &__core {
    box-shadow: var(--shadow-md);
  }

  &:active:not(.is-disabled):not(.is-loading) &__core {
    transform: scale(0.95);
  }
}

// 暗黑主题适配
.theme-dark .bm-switch__core {
  background-color: var(--bg-tertiary);
  border-color: var(--border-dark);

  &::after {
    background-color: var(--text-primary);
  }
}

// 渐变主题增强
.theme-gradient .bm-switch.is-checked .bm-switch__core {
  background: var(--primary-gradient);
  box-shadow: 0 0 12px var(--primary-light);
}
</style>
