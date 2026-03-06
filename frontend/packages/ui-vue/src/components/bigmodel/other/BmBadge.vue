<template>
  <div class="bm-badge" :class="{ dot }">
    <slot />
    <sup
      v-if="showBadge"
      class="bm-badge__content"
      :class="[type, { dot }]"
    >
      <span v-if="!dot">{{ displayValue }}</span>
    </sup>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

defineOptions({
  name: 'BmBadge'
});

interface Props {
  value?: number | string;
  max?: number;
  dot?: boolean;
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
  showZero?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  max: 99,
  dot: false,
  type: 'danger',
  showZero: false
});

const displayValue = computed(() => {
  if (typeof props.value === 'string') {
    return props.value;
  }
  if (props.value > props.max) {
    return `${props.max}+`;
  }
  return props.value;
});

const showBadge = computed(() => {
  if (props.dot) {
    return true;
  }
  if (props.value === 0 || props.value === undefined) {
    return props.showZero;
  }
  return true;
});
</script>

<style scoped lang="scss">
@import '../../../styles/components/badge.scss';
</style>
