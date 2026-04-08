<script setup lang="ts">
import { computed, defineComponent, h, Transition, TransitionGroup } from 'vue'

interface Props {
  /** 是否启用过渡动画 */
  enabled?: boolean
  /** 过渡模式: 'fade' | 'list' | 'slide' */
  mode?: 'fade' | 'list' | 'slide'
  /** 是否为 TransitionGroup（用于列表） */
  group?: boolean
  /** 自定义标签名 */
  tag?: string
}

const props = withDefaults(defineProps<Props>(), {
  enabled: true,
  mode: 'fade',
  group: false,
  tag: 'div'
})

// Fade transition
const FadeTransition = defineComponent({
  name: 'FadeTransition',
  setup(_, { slots }) {
    return () => h(
      Transition,
      {
        name: 'fade',
        enterActiveClass: 'transition-opacity duration-200 ease-out',
        enterFromClass: 'opacity-0',
        enterToClass: 'opacity-100',
        leaveActiveClass: 'transition-opacity duration-150 ease-in',
        leaveFromClass: 'opacity-100',
        leaveToClass: 'opacity-0'
      },
      slots
    )
  }
})

// List transition (staggered)
const ListTransition = defineComponent({
  name: 'ListTransition',
  setup(_, { slots }) {
    return () => h(
      TransitionGroup,
      {
        name: 'list',
        tag: props.tag,
        enterActiveClass: 'transition-all duration-200 ease-out',
        enterFromClass: 'opacity-0 translate-y-2',
        enterToClass: 'opacity-100 translate-y-0',
        leaveActiveClass: 'transition-all duration-150 ease-in',
        leaveFromClass: 'opacity-100 translate-y-0',
        leaveToClass: 'opacity-0 -translate-y-2',
        moveClass: 'transition-transform duration-200 ease-out'
      },
      slots
    )
  }
})

// Slide transition
const SlideTransition = defineComponent({
  name: 'SlideTransition',
  setup(_, { slots }) {
    return () => h(
      Transition,
      {
        name: 'slide',
        enterActiveClass: 'transition-all duration-250 ease-out-expo',
        enterFromClass: 'opacity-0 translate-y-4 scale-95',
        enterToClass: 'opacity-100 translate-y-0 scale-100',
        leaveActiveClass: 'transition-all duration-200 ease-in',
        leaveFromClass: 'opacity-100 translate-y-0 scale-100',
        leaveToClass: 'opacity-0 -translate-y-4 scale-95'
      },
      slots
    )
  }
})

const transitionComponent = computed(() => {
  if (!props.enabled) return props.group ? TransitionGroup : Transition

  switch (props.mode) {
    case 'list':
      return ListTransition
    case 'slide':
      return SlideTransition
    case 'fade':
    default:
      return FadeTransition
  }
})
</script>

<template>
  <component :is="transitionComponent">
    <slot />
  </component>
</template>
