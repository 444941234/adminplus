<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow
} from '@/components/ui'
import { getOnlineUsers, getStatistics, getSystemInfo } from '@/api'
import type { ChartData, OnlineUser, StatisticsData, SystemInfo } from '@/types'
import { toast } from 'vue-sonner'
import { Activity, Eye, TrendingUp, UserPlus } from 'lucide-vue-next'

const loading = ref(false)
const statistics = ref<StatisticsData | null>(null)
const onlineUsers = ref<OnlineUser[]>([])
const systemInfo = ref<SystemInfo | null>(null)

const summaryCards = computed(() => [
  {
    label: '用户总数',
    value: statistics.value?.totalUsers ?? 0,
    icon: Activity,
    accent: 'bg-blue-500'
  },
  {
    label: '今日访问',
    value: statistics.value?.todayVisits ?? 0,
    icon: Eye,
    accent: 'bg-emerald-500'
  },
  {
    label: '活跃用户',
    value: statistics.value?.activeUsers ?? 0,
    icon: TrendingUp,
    accent: 'bg-orange-500'
  },
  {
    label: '今日新增',
    value: statistics.value?.todayNewUsers ?? 0,
    icon: UserPlus,
    accent: 'bg-violet-500'
  }
])

const toNumbers = (values: Array<number | string> = []) => values.map((value) => Number(value) || 0)

const getMaxValue = (chart?: ChartData | null) => {
  if (!chart) return 1
  const values = toNumbers(chart.values)
  return Math.max(...values, 1)
}

import { formatDateTime } from '@/utils/format'

const fetchData = async () => {
  loading.value = true
  try {
    const [statisticsRes, onlineUsersRes, systemInfoRes] = await Promise.all([
      getStatistics(),
      getOnlineUsers(),
      getSystemInfo()
    ])
    statistics.value = statisticsRes.data
    onlineUsers.value = onlineUsersRes.data
    systemInfo.value = systemInfoRes.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取统计信息失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="space-y-6">
    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <Card v-for="card in summaryCards" :key="card.label">
        <CardContent class="flex items-center justify-between p-6">
          <div>
            <p class="text-sm text-muted-foreground">{{ card.label }}</p>
            <p class="mt-2 text-3xl font-semibold">{{ loading ? '...' : card.value }}</p>
          </div>
          <div :class="[card.accent, 'flex h-12 w-12 items-center justify-center rounded-xl text-white']">
            <component :is="card.icon" class="h-6 w-6" />
          </div>
        </CardContent>
      </Card>
    </div>

    <div class="grid gap-6 xl:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>用户增长趋势</CardTitle>
        </CardHeader>
        <CardContent>
          <div v-if="!statistics?.userGrowthData?.labels?.length" class="py-10 text-center text-sm text-muted-foreground">
            暂无趋势数据
          </div>
          <div v-else class="space-y-4">
            <div
              v-for="(label, index) in statistics.userGrowthData.labels"
              :key="`${label}-${index}`"
              class="space-y-2"
            >
              <div class="flex items-center justify-between text-sm">
                <span>{{ label }}</span>
                <span class="font-medium">{{ toNumbers(statistics.userGrowthData.values)[index] ?? 0 }}</span>
              </div>
              <div class="h-2 rounded-full bg-muted">
                <div
                  class="h-2 rounded-full bg-primary transition-all"
                  :style="{
                    width: `${(toNumbers(statistics.userGrowthData.values)[index] ?? 0) / getMaxValue(statistics.userGrowthData) * 100}%`
                  }"
                />
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>访问量趋势</CardTitle>
        </CardHeader>
        <CardContent>
          <div v-if="!statistics?.visitTrendData?.labels?.length" class="py-10 text-center text-sm text-muted-foreground">
            暂无访问数据
          </div>
          <div v-else class="space-y-4">
            <div
              v-for="(label, index) in statistics.visitTrendData.labels"
              :key="`${label}-${index}`"
              class="space-y-2"
            >
              <div class="flex items-center justify-between text-sm">
                <span>{{ label }}</span>
                <span class="font-medium">{{ toNumbers(statistics.visitTrendData.values)[index] ?? 0 }}</span>
              </div>
              <div class="h-2 rounded-full bg-muted">
                <div
                  class="h-2 rounded-full bg-emerald-500 transition-all"
                  :style="{
                    width: `${(toNumbers(statistics.visitTrendData.values)[index] ?? 0) / getMaxValue(statistics.visitTrendData) * 100}%`
                  }"
                />
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <div class="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
      <Card>
        <CardHeader>
          <CardTitle>在线用户</CardTitle>
        </CardHeader>
        <CardContent class="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>用户名</TableHead>
                <TableHead>IP</TableHead>
                <TableHead>浏览器</TableHead>
                <TableHead>操作系统</TableHead>
                <TableHead>登录时间</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-if="loading">
                <TableCell colspan="5" class="h-24 text-center text-muted-foreground">加载中...</TableCell>
              </TableRow>
              <TableRow v-else-if="onlineUsers.length === 0">
                <TableCell colspan="5" class="h-24 text-center text-muted-foreground">暂无在线用户</TableCell>
              </TableRow>
              <TableRow v-for="user in onlineUsers" :key="user.userId">
                <TableCell class="font-medium">{{ user.username }}</TableCell>
                <TableCell>{{ user.ip || '-' }}</TableCell>
                <TableCell>{{ user.browser || '-' }}</TableCell>
                <TableCell>{{ user.os || '-' }}</TableCell>
                <TableCell>{{ formatDateTime(user.loginTime) }}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>系统信息</CardTitle>
        </CardHeader>
        <CardContent class="space-y-4">
          <div class="grid grid-cols-[96px_1fr] gap-y-3 text-sm">
            <template v-if="systemInfo">
              <span class="text-muted-foreground">系统</span>
              <span>{{ systemInfo.osName }} {{ systemInfo.osVersion }}</span>
              <span class="text-muted-foreground">Java</span>
              <span>{{ systemInfo.javaVersion }}</span>
              <span class="text-muted-foreground">JVM 内存</span>
              <span>{{ systemInfo.jvmMemory }}</span>
              <span class="text-muted-foreground">CPU 使用率</span>
              <span>{{ systemInfo.cpuUsage }}%</span>
              <span class="text-muted-foreground">内存使用率</span>
              <span>{{ systemInfo.memoryUsage }}%</span>
              <span class="text-muted-foreground">磁盘使用率</span>
              <span>{{ systemInfo.diskUsage }}%</span>
            </template>
            <template v-else>
              <span class="col-span-2 py-8 text-center text-muted-foreground">暂无系统信息</span>
            </template>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
