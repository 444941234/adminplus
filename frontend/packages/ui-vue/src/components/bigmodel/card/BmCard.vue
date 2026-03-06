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
defineOptions({ name: 'BmCard' });

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
