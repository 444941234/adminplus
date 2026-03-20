<script lang="ts" setup>
import { Toaster, toast } from 'vue-sonner'
import 'vue-sonner/style.css'

// 导出 toast 供其他组件使用
defineExpose({ toast })
</script>

<template>
  <Toaster
    position="top-right"
    :rich-colors="true"
    :close-button="true"
    :duration="3000"
  />
</template>