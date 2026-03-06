<template>
  <div class="bm-radio-group">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
import { provide, computed } from 'vue';

defineOptions({
  name: 'BmRadioGroup'
});

interface Props {
  modelValue?: string | number | boolean;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  'update:modelValue': [value: string | number | boolean];
}>();

// Provide the v-model to child radio components
provide('radioGroup', computed({
  get: () => props.modelValue,
  set: (value: string | number | boolean) => {
    emit('update:modelValue', value);
  }
}));
</script>

<style scoped lang="scss">
.bm-radio-group {
  display: inline-flex;
  flex-direction: column;
  gap: var(--space-sm);
}
</style>
