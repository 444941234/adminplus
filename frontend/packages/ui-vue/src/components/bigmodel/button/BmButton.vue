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
defineOptions({
  name: 'BmButton'
});

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
