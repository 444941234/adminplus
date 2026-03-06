<template>
  <label class="bm-checkbox" :class="[size, { disabled, checked: isChecked }]" @click.prevent="handleClick">
    <span class="bm-checkbox__input">
      <span class="bm-checkbox__inner"></span>
      <input
        type="checkbox"
        class="bm-checkbox__original"
        :disabled="disabled"
        :value="label"
        v-model="model"
        @change="handleChange"
        @focus="handleFocus"
        @blur="handleBlur"
      />
    </span>
    <span v-if="$slots.default || label" class="bm-checkbox__label">
      <slot>{{ label }}</slot>
    </span>
  </label>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

defineOptions({
  name: 'BmCheckbox'
});

interface Props {
  modelValue?: boolean | string | number;
  label?: string | number | boolean;
  disabled?: boolean;
  size?: 'small' | 'medium' | 'large';
  indeterminate?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  size: 'medium',
  indeterminate: false
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean | string | number];
  change: [value: boolean | string | number];
}>();

const isFocused = ref(false);

const model = computed({
  get() {
    return props.modelValue;
  },
  set(val) {
    emit('update:modelValue', val);
  }
});

const isChecked = computed(() => {
  return Boolean(model.value);
});

const handleClick = () => {
  if (!props.disabled) {
    model.value = !isChecked.value;
  }
};

const handleChange = () => {
  emit('change', model.value);
};

const handleFocus = () => {
  isFocused.value = true;
};

const handleBlur = () => {
  isFocused.value = false;
};
</script>

<style scoped lang="scss">
.bm-checkbox {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
  color: var(--text-primary);
  transition: color var(--transition-fast);

  &.small {
    font-size: 12px;
    height: 24px;
  }

  &.medium {
    font-size: 14px;
    height: 32px;
  }

  &.large {
    font-size: 16px;
    height: 40px;
  }

  &.disabled {
    cursor: not-allowed;
    color: var(--text-disabled);
  }

  &:hover:not(.disabled) .bm-checkbox__inner {
    border-color: var(--primary-light);
  }

  &__input {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    white-space: nowrap;
    cursor: pointer;
    outline: none;
    line-height: 1;
    vertical-align: middle;
  }

  &__inner {
    display: inline-block;
    position: relative;
    border: 2px solid var(--border-color);
    border-radius: var(--radius-sm);
    box-sizing: border-box;
    background-color: var(--bg-primary);
    transition: all var(--transition-normal);
    width: 16px;
    height: 16px;

    &::after {
      box-sizing: content-box;
      content: '';
      border: 2px solid var(--bg-primary);
      border-left: 0;
      border-top: 0;
      height: 7px;
      left: 4px;
      position: absolute;
      top: 1px;
      transform: rotate(45deg) scaleY(0);
      width: 3px;
      transition: transform var(--transition-fast);
      transform-origin: center;
    }
  }

  &.small &__inner {
    width: 14px;
    height: 14px;

    &::after {
      height: 6px;
      left: 3px;
      width: 2px;
    }
  }

  &.large &__inner {
    width: 18px;
    height: 18px;

    &::after {
      height: 8px;
      left: 5px;
      width: 3px;
    }
  }

  &__original {
    position: absolute;
    opacity: 0;
    outline: none;
    margin: 0;
    width: 0;
    height: 0;
    z-index: -1;
  }

  &__label {
    margin-left: var(--space-sm);
    line-height: 1;
  }

  &.checked &__inner {
    background-color: var(--primary-color);
    border-color: var(--primary-color);

    &::after {
      transform: rotate(45deg) scaleY(1);
    }
  }

  &.disabled &__inner {
    background-color: var(--bg-tertiary);
    border-color: var(--border-color);

    &::after {
      border-color: var(--text-disabled);
    }
  }

  &.disabled.checked &__inner {
    background-color: var(--bg-tertiary);
    border-color: var(--border-color);

    &::after {
      border-color: var(--text-disabled);
    }
  }
}
</style>
