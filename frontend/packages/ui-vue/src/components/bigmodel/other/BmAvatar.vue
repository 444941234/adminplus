<template>
  <div class="bm-avatar" :class="[size, shape, { clickable }]" @click="handleClick">
    <img v-if="src && !imageError" :src="src" :alt="alt" @error="handleImageError" />
    <span v-else class="bm-avatar__text">{{ displayText }}</span>
    <slot />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

defineOptions({
  name: 'BmAvatar'
});

interface Props {
  src?: string;
  text?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  shape?: 'circle' | 'square';
  alt?: string;
  clickable?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  shape: 'circle',
  alt: 'avatar',
  clickable: false
});

const emit = defineEmits<{
  click: [event: MouseEvent];
}>();

const imageError = ref(false);

const displayText = computed(() => {
  if (props.text) {
    return props.text;
  }
  // Generate initials from alt text if no text provided
  return props.alt
    .split(' ')
    .map(word => word.charAt(0).toUpperCase())
    .join('')
    .slice(0, 2);
});

const handleImageError = () => {
  imageError.value = true;
};

const handleClick = (event: MouseEvent) => {
  if (props.clickable) {
    emit('click', event);
  }
};
</script>

<style scoped lang="scss">
@import '../../../styles/components/avatar.scss';
</style>
