<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Button, Input, Card, CardHeader, CardTitle, CardContent } from '@/components/ui'
import { toast } from 'vue-sonner'
import { useUserStore } from '@/stores/user'
import { ensureDynamicRoutes } from '@/router'
import { logError } from '@/utils/logger'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  captchaCode: ''
})

const handleLogin = async () => {
  if (!form.username) { toast.warning('请输入用户名'); return }
  if (!form.password) { toast.warning('请输入密码'); return }
  if (!form.captchaCode) { toast.warning('请输入验证码'); return }
  if (!userStore.captcha?.captchaId) { toast.warning('请先获取验证码'); return }

  // 防止重复点击
  if (loading.value) return
  
  loading.value = true
  try {
    await userStore.login(form.username, form.password, userStore.captcha.captchaId, form.captchaCode)
    await ensureDynamicRoutes()
    toast.success('登录成功，正在跳转...')
    // 延迟跳转，让 toast 有时间显示
    setTimeout(() => {
      router.replace('/dashboard')
    }, 500)
  } catch (error: unknown) {
    const err = error as Error
    toast.error(err.message || '登录失败')
    // 登录失败时刷新验证码
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

const refreshCaptcha = async () => {
  try {
    await userStore.fetchCaptcha()
    form.captchaCode = ''
  } catch (error) {
    logError('获取验证码失败', error as Error, 'Login')
    toast.error('获取验证码失败')
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<template>
  <div class="min-h-screen flex">
    <!-- 左侧品牌区 -->
    <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-blue-500 to-indigo-600 relative overflow-hidden">
      <div class="absolute top-20 left-20 w-64 h-64 bg-white/10 rounded-full animate-pulse" />
      <div
        class="absolute bottom-20 right-20 w-48 h-48 bg-white/10 rounded-full animate-pulse"
        style="animation-delay: 1s"
      />
      
      <div class="relative z-10 flex flex-col items-center justify-center w-full text-white px-12">
        <div class="w-20 h-20 mb-6">
          <svg
            viewBox="0 0 80 80"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect
              width="80"
              height="80"
              rx="16"
              fill="white"
              fill-opacity="0.2"
            />
            <path
              d="M20 30L40 20L60 30V50L40 60L20 50V30Z"
              fill="white"
              fill-opacity="0.9"
            />
          </svg>
        </div>
        <h1 class="text-4xl font-bold mb-4">
          AdminPlus
        </h1>
        <p class="text-lg opacity-90 mb-8">
          全栈 RBAC 权限管理系统
        </p>
        <div class="space-y-3 text-sm opacity-80">
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 bg-white rounded-full" />
            Spring Boot 3.5 + JDK 21
          </div>
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 bg-white rounded-full" />
            Vue 3.5 + Tailwind CSS
          </div>
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 bg-white rounded-full" />
            shadcn-vue 组件库
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="w-full lg:w-1/2 flex items-center justify-center p-8 bg-gray-50">
      <Card class="w-full max-w-md">
        <CardHeader class="text-center">
          <CardTitle class="text-2xl">
            欢迎登录
          </CardTitle>
          <p class="text-sm text-muted-foreground mt-2">
            请输入您的账号信息
          </p>
        </CardHeader>
        
        <CardContent>
          <form
            class="space-y-4"
            @submit.prevent="handleLogin"
          >
            <div>
              <label
                for="username"
                class="block text-sm font-medium mb-1.5"
              >
                用户名
                <span
                  class="text-destructive"
                  aria-hidden="true"
                >*</span>
              </label>
              <Input
                id="username"
                v-model="form.username"
                placeholder="请输入用户名"
                autocomplete="username"
                required
                aria-required="true"
              />
            </div>

            <div>
              <label
                for="password"
                class="block text-sm font-medium mb-1.5"
              >
                密码
                <span
                  class="text-destructive"
                  aria-hidden="true"
                >*</span>
              </label>
              <Input
                id="password"
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                autocomplete="current-password"
                required
                aria-required="true"
              />
            </div>

            <div>
              <label
                for="captchaCode"
                class="block text-sm font-medium mb-1.5"
              >
                验证码
                <span
                  class="text-destructive"
                  aria-hidden="true"
                >*</span>
              </label>
              <div class="flex gap-3">
                <Input
                  id="captchaCode"
                  v-model="form.captchaCode"
                  placeholder="请输入验证码"
                  class="flex-1"
                  required
                  aria-required="true"
                  @keyup.enter="handleLogin"
                />
                <img
                  v-if="userStore.captcha?.captchaImage"
                  :src="userStore.captcha.captchaImage"
                  class="h-9 w-28 cursor-pointer rounded border hover:border-primary"
                  title="点击刷新验证码"
                  alt="验证码"
                  @click="refreshCaptcha"
                >
              </div>
            </div>

            <Button
              type="submit"
              :disabled="loading"
              class="w-full h-11"
            >
              <span
                v-if="loading"
                class="flex items-center gap-2"
              >
                <svg
                  class="animate-spin h-4 w-4"
                  viewBox="0 0 24 24"
                >
                  <circle
                    class="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="4"
                    fill="none"
                  />
                  <path
                    class="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  />
                </svg>
                登录中...
              </span>
              <span v-else>登 录</span>
            </Button>
          </form>

          <div class="mt-6 pt-4 border-t border-gray-200 text-center text-sm text-muted-foreground">
            默认账号：<code class="bg-gray-100 px-2 py-0.5 rounded">admin</code> / 
            <code class="bg-gray-100 px-2 py-0.5 rounded">admin123</code>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
