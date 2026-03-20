<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Badge, Button, Card, CardContent, CardHeader, CardTitle } from '@/components/ui'
import { RefreshCw, Server, Users } from 'lucide-vue-next'
import { getOnlineUsers, getSystemInfo } from '@/api'
import type { OnlineUser, SystemInfo } from '@/types'
import { toast } from 'vue-sonner'

const loading = ref(false)
const systemInfo = ref<SystemInfo | null>(null)
const onlineUsers = ref<OnlineUser[]>([])

const memoryUsagePercent = computed(() => {
  if (!systemInfo.value?.totalMemory || !systemInfo.value?.usedMemory) return 0
  return Math.min(100, Math.round((systemInfo.value.usedMemory / systemInfo.value.totalMemory) * 100))
})

const formatUptime = (uptime?: number) => {
  if (!uptime) return '-'
  const days = Math.floor(uptime / 86400)
  const hours = Math.floor((uptime % 86400) / 3600)
  const minutes = Math.floor((uptime % 3600) / 60)
  if (days > 0) return `${days}天 ${hours}小时 ${minutes}分钟`
  if (hours > 0) return `${hours}小时 ${minutes}分钟`
  return `${minutes}分钟`
}

const fetchData = async () => {
  loading.value = true
  try {
    const [systemRes, usersRes] = await Promise.all([getSystemInfo(), getOnlineUsers()])
    systemInfo.value = systemRes.data
    onlineUsers.value = usersRes.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取系统监控数据失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-semibold">系统监控</h2>
        <p class="text-sm text-muted-foreground">查看系统运行信息与在线用户概况</p>
      </div>
      <Button variant="outline" :disabled="loading" @click="fetchData">
        <RefreshCw class="mr-2 h-4 w-4" />
        刷新
      </Button>
    </div>

    <div class="grid gap-4 md:grid-cols-4">
      <Card>
        <CardContent class="p-4">
          <p class="text-sm text-muted-foreground">系统名称</p>
          <p class="mt-2 text-lg font-semibold">{{ loading ? '-' : systemInfo?.systemName || 'AdminPlus' }}</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent class="p-4">
          <p class="text-sm text-muted-foreground">运行时长</p>
          <p class="mt-2 text-lg font-semibold">{{ loading ? '-' : formatUptime(systemInfo?.uptime) }}</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent class="p-4">
          <p class="text-sm text-muted-foreground">内存占用</p>
          <p class="mt-2 text-lg font-semibold">{{ loading ? '-' : `${memoryUsagePercent}%` }}</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent class="p-4">
          <p class="text-sm text-muted-foreground">在线用户</p>
          <p class="mt-2 text-lg font-semibold">{{ loading ? '-' : onlineUsers.length }}</p>
        </CardContent>
      </Card>
    </div>

    <div class="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
      <Card>
        <CardHeader>
          <CardTitle class="flex items-center gap-2">
            <Server class="h-5 w-5" />
            系统信息
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div v-if="loading" class="py-8 text-center text-muted-foreground">加载中...</div>
          <div v-else class="grid gap-4 md:grid-cols-2">
            <div class="rounded-lg border p-4">
              <p class="text-sm text-muted-foreground">系统版本</p>
              <p class="mt-1 font-medium">{{ systemInfo?.systemVersion || '-' }}</p>
            </div>
            <div class="rounded-lg border p-4">
              <p class="text-sm text-muted-foreground">操作系统</p>
              <p class="mt-1 font-medium">{{ systemInfo?.osName || '-' }}</p>
            </div>
            <div class="rounded-lg border p-4">
              <p class="text-sm text-muted-foreground">JDK 版本</p>
              <p class="mt-1 font-medium">{{ systemInfo?.jdkVersion || '-' }}</p>
            </div>
            <div class="rounded-lg border p-4">
              <p class="text-sm text-muted-foreground">数据库</p>
              <p class="mt-1 font-medium">{{ systemInfo?.databaseType || '-' }} {{ systemInfo?.databaseVersion || '' }}</p>
            </div>
            <div class="rounded-lg border p-4">
              <p class="text-sm text-muted-foreground">总内存</p>
              <p class="mt-1 font-medium">{{ systemInfo?.totalMemory ?? '-' }} MB</p>
            </div>
            <div class="rounded-lg border p-4">
              <p class="text-sm text-muted-foreground">已用 / 空闲内存</p>
              <p class="mt-1 font-medium">
                {{ systemInfo?.usedMemory ?? '-' }} / {{ systemInfo?.freeMemory ?? '-' }} MB
              </p>
            </div>
            <div class="rounded-lg border p-4 md:col-span-2">
              <div class="flex items-center justify-between text-sm">
                <span class="text-muted-foreground">内存占用率</span>
                <span class="font-medium">{{ memoryUsagePercent }}%</span>
              </div>
              <div class="mt-3 h-2 rounded-full bg-muted">
                <div class="h-2 rounded-full bg-primary transition-all" :style="{ width: `${memoryUsagePercent}%` }" />
              </div>
            </div>
            <div class="rounded-lg border p-4 md:col-span-2">
              <p class="text-sm text-muted-foreground">数据库连接数</p>
              <p class="mt-1 font-medium">{{ systemInfo?.databaseConnections ?? '-' }}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle class="flex items-center gap-2">
            <Users class="h-5 w-5" />
            在线用户
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div v-if="loading" class="py-8 text-center text-muted-foreground">加载中...</div>
          <div v-else-if="onlineUsers.length === 0" class="py-8 text-center text-muted-foreground">暂无在线用户</div>
          <div v-else class="space-y-3">
            <div v-for="user in onlineUsers" :key="`${user.userId}-${user.loginTime}`" class="rounded-lg border p-4">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="font-medium">{{ user.username }}</p>
                  <p class="text-sm text-muted-foreground">{{ user.ip }}</p>
                </div>
                <Badge variant="secondary">在线</Badge>
              </div>
              <div class="mt-3 grid gap-2 text-sm text-muted-foreground">
                <p>登录时间：{{ user.loginTime }}</p>
                <p>浏览器：{{ user.browser || '-' }}</p>
                <p>操作系统：{{ user.os || '-' }}</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
