<template>
  <div class="bm-input" :class="[size, { disabled, error, 'has-prefix': prefixIcon, 'has-suffix': suffixIcon || clearable }]">
    <span v-if="prefixIcon" class="bm-input__prefix">
      <slot name="prefixIcon">
        <BmIcon :icon="prefixIcon" />
      </slot>
    </span>
    <input
      ref="inputRef"
      v-model="inputValue"
      class="bm-input__inner"
      :type="type"
      :placeholder="placeholder"
      :disabled="disabled"
      :readonly="readonly"
      :maxlength="maxlength"
      :autocomplete="autocomplete"
      @focus="handleFocus"
      @blur="handleBlur"
      @change="handleChange"
      @keyup="handleKeyup"
      @keydown="handleKeydown"
    />
    <span v-if="suffixIcon || clearable || $slots.suffix" class="bm-input__suffix">
      <slot name="suffix">
        <BmIcon
          v-if="clearable && inputValue && !disabled && !readonly"
          icon="✕"
          class="bm-input__clear"
          @click="handleClear"
        />
        <BmIcon v-if="suffixIcon" :icon="suffixIcon" />
      </slot>
    </span>
    <div v-if="error && errorMessage" class="bm-input__error-message">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import BmIcon from '../other/BmIcon.vue';

defineOptions({
  name: 'BmInput'
});

interface Props {
  modelValue?: string | number;
  type?: 'text' | 'password' | 'email' | 'number' | 'tel' | 'url';
  size?: 'small' | 'medium' | 'large';
  placeholder?: string;
  disabled?: boolean;
  readonly?: boolean;
  clearable?: boolean;
  maxlength?: number;
  autocomplete?: string;
  prefixIcon?: string;
  suffixIcon?: string;
  error?: boolean;
  errorMessage?: string;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  type: 'text',
  size: 'medium',
  placeholder: '',
  disabled: false,
  readonly: false,
  clearable: false,
  autocomplete: 'off',
  prefixIcon: '',
  suffixIcon: '',
  error: false,
  errorMessage: ''
});

const emit = defineEmits<{
  'update:modelValue': [value: string | number];
  focus: [event: FocusEvent];
  blur: [event: FocusEvent];
  change: [value: string | number];
  clear: [];
  keyup: [event: KeyboardEvent];
  keydown: [event: KeyboardEvent];
}>();

const inputRef = ref<HTMLInputElement>();
const focused = ref(false);

const inputValue = computed({
  get: () => props.modelValue,
  set: (val) => {
    emit('update:modelValue', val);
  }
});

const handleFocus = (event: FocusEvent) => {
  focused.value = true;
  emit('focus', event);
};

const handleBlur = (event: FocusEvent) => {
  focused.value = false;
  emit('blur', event);
};

const handleChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  emit('change', target.value);
};

const handleClear = () => {
  emit('update:modelValue', '');
  emit('clear');
  inputRef.value?.focus();
};

const handleKeyup = (event: KeyboardEvent) => {
  emit('keyup', event);
};

const handleKeydown = (event: KeyboardEvent) => {
  emit('keydown', event);
};

const focus = () => {
  inputRef.value?.focus();
};

const blur = () => {
  inputRef.value?.blur();
};

defineExpose({
  focus,
  blur
});
</script>

<style scoped lang="scss">
.bm-input {
  position: relative;
  display: inline-flex;
  align-items: center;
  width: 100%;
  font-size: var(--bm-font-size-base);
  color: var(--bm-text-primary);

  &__inner {
    flex: 1;
    width: 100%;
    padding: 0 var(--bm-space-md);
    font-family: var(--bm-font-family);
    font-size: inherit;
    color: inherit;
    background-color: var(--bm-bg-white);
    border: 1px solid var(--bm-border);
    border-radius: var(--bm-radius-md);
    outline: none;
    transition: all var(--bm-transition-normal);

    &::placeholder {
      color: var(--bm-text-tertiary);
    }

    &:hover:not(:disabled) {
      border-color: var(--bm-border-dark);
    }

    &:focus {
      border-color: var(--bm-primary);
      box-shadow: 0 0 0 2px var(--bm-primary-light);
    }

    &:disabled {
      background-color: var(--bm-bg-disabled);
      color: var(--bm-text-disabled);
      cursor: not-allowed;
    }
  }

  &__prefix,
  &__suffix {
    position: absolute;
    top: 50%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: var(--bm-text-tertiary);
    pointer-events: none;
    transform: translateY(-50%);
  }

  &__prefix {
    left: var(--bm-space-md);
  }

  &__suffix {
    right: var(--bm-space-md);
    pointer-events: auto;
  }

  &__clear {
    cursor: pointer;
    pointer-events: auto;
    transition: color var(--bm-transition-fast);

    &:hover {
      color: var(--bm-text-secondary);
    }
  }

  &__error-message {
    position: absolute;
    bottom: -20px;
    left: 0;
    font-size: var(--bm-font-size-xs);
    color: var(--bm-danger);
    white-space: nowrap;
  }

  &.has-prefix &__inner {
    padding-left: 32px;
  }

  &.has-suffix &__inner {
    padding-right: 32px;
  }

  &.small {
    font-size: var(--bm-font-size-sm);

    .bm-input__inner {
      padding: 0 var(--bm-space-sm);
      height: 28px;
    }

    &.has-prefix .bm-input__inner {
      padding-left: 28px;
    }

    &.has-suffix .bm-input__inner {
      padding-right: 28px;
    }
  }

  &.medium {
    .bm-input__inner {
      height: 36px;
    }
  }

  &.large {
    font-size: var(--bm-font-size-md);

    .bm-input__inner {
      padding: 0 var(--bm-space-lg);
      height: 44px;
    }

    &.has-prefix .bm-input__inner {
      padding-left: 40px;
    }

    &.has-suffix .bm-input__inner {
      padding-right: 40px;
    }
  }

  &.error {
    .bm-input__inner {
      border-color: var(--bm-danger);

      &:focus {
        border-color: var(--bm-danger);
        box-shadow: 0 0 0 2px var(--bm-danger-light);
      }
    }

    .bm-input__prefix,
    .bm-input__suffix {
      color: var(--bm-danger);
    }
  }

  &.disabled {
    .bm-input__prefix,
    .bm-input__suffix {
      color: var(--bm-text-disabled);
    }
  }
}
</style>
