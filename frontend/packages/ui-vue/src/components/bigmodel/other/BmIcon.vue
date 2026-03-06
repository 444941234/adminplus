<template>
  <span class="bm-icon" :class="[size, { clickable }]" @click="handleClick">
    {{ iconDisplay }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

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
