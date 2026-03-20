<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Button } from '@/components/ui'
import { Home, ArrowLeft } from 'lucide-vue-next'

const router = useRouter()
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="text-center">
      <h1 class="text-9xl font-bold text-gray-200">404</h1>
      <p class="text-2xl font-semibold text-gray-700 mt-4">页面不存在</p>
      <p class="text-gray-500 mt-2">抱歉，您访问的页面不存在或已被删除</p>
      <div class="flex gap-4 justify-center mt-8">
        <Button @click="router.back()">
          <ArrowLeft class="w-4 h-4 mr-2" />
          返回上页
        </Button>
        <Button variant="outline" @click="router.push('/dashboard')">
          <Home class="w-4 h-4 mr-2" />
          返回首页
        </Button>
      </div>
    </div>
  </div>
</template>