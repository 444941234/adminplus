<template>
  <label class="bm-radio" :class="[size, { disabled, checked: isChecked }]" @click.prevent="handleClick">
    <span class="bm-radio__input">
      <span class="bm-radio__inner"></span>
      <input
        type="radio"
        class="bm-radio__original"
        :disabled="disabled"
        :value="label"
        v-model="model"
        @change="handleChange"
        @focus="handleFocus"
        @blur="handleBlur"
      />
    </span>
    <span v-if="$slots.default || label" class="bm-radio__label">
      <slot>{{ label }}</slot>
    </span>
  </label>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

defineOptions({
  name: 'BmRadio'
});

interface Props {
  modelValue?: string | number | boolean;
  label?: string | number | boolean;
  disabled?: boolean;
  size?: 'small' | 'medium' | 'large';
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  size: 'medium'
});

const emit = defineEmits<{
  'update:modelValue': [value: string | number | boolean];
  change: [value: string | number | boolean];
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
  return model.value === props.label;
});

const handleClick = () => {
  if (!props.disabled) {
    model.value = props.label;
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
.bm-radio {
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

  &:hover:not(.disabled) .bm-radio__inner {
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
    border-radius: var(--radius-full);
    box-sizing: border-box;
    background-color: var(--bg-primary);
    transition: all var(--transition-normal);
    width: 16px;
    height: 16px;

    &::after {
      content: '';
      position: absolute;
      width: 8px;
      height: 8px;
      border-radius: var(--radius-full);
      background-color: var(--bg-primary);
      left: 50%;
      top: 50%;
      transform: translate(-50%, -50%) scale(0);
      transition: transform var(--transition-fast);
    }
  }

  &.small &__inner {
    width: 14px;
    height: 14px;

    &::after {
      width: 6px;
      height: 6px;
    }
  }

  &.large &__inner {
    width: 18px;
    height: 18px;

    &::after {
      width: 10px;
      height: 10px;
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
    background-color: var(--bg-primary);
    border-color: var(--primary-color);

    &::after {
      transform: translate(-50%, -50%) scale(1);
      background-color: var(--primary-color);
    }
  }

  &.disabled &__inner {
    background-color: var(--bg-tertiary);
    border-color: var(--border-color);

    &::after {
      background-color: var(--text-disabled);
    }
  }

  &.disabled.checked &__inner {
    background-color: var(--bg-tertiary);
    border-color: var(--border-color);

    &::after {
      background-color: var(--text-disabled);
    }
  }
}
</style>
