<script setup lang="ts">
interface Props {
  /** 显示的行数 */
  rows?: number
  /** 是否启用动画 */
  animated?: boolean
}

withDefaults(defineProps<Props>(), {
  rows: 5,
  animated: true
})
</script>

<template>
  <div class="space-y-3 p-4">
    <div
      v-for="i in rows"
      :key="i"
      :class="['skeleton rounded', { 'animate-pulse': animated }]"
      :style="{ height: '48px' }"
    />
  </div>
</template>
