<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { Card, CardContent } from '@/components/ui'
import { Users, Shield, Menu, FileText, BarChart3, GitBranch, Clock3, FolderOpen, Server, Bell, Settings } from '@lucide/vue'
import { getStats, getRecentLogs } from '@/api'
import type { DashboardStats, OperationLog } from '@/types'
import { useUserStore } from '@/stores/user'
import { getDashboardQuickActions } from '@/lib/page-permissions'
import { useAsyncAction } from '@/composables/useAsyncAction'
import NotificationCenter from '@/components/notification/NotificationCenter.vue'

const { loading, run: runDashboard } = useAsyncAction('获取仪表盘数据失败')
const stats = ref<DashboardStats | null>(null)
const recentLogs = ref<OperationLog[]>([])
const userStore = useUserStore()

const fetchDashboard = () => runDashboard(async () => {
  const [statsRes, logsRes] = await Promise.all([getStats(), getRecentLogs()])
  stats.value = statsRes.data
  recentLogs.value = logsRes.data
})

const statCards = [
  { label: '用户总数', key: 'userCount' as const, icon: Users, color: 'bg-blue-500' },
  { label: '角色数量', key: 'roleCount' as const, icon: Shield, color: 'bg-green-500' },
  { label: '菜单数量', key: 'menuCount' as const, icon: Menu, color: 'bg-orange-500' },
  { label: '今日日志', key: 'logCount' as const, icon: FileText, color: 'bg-red-500' },
]

const iconMap = {
  Users,
  Shield,
  Menu,
  FileText,
  FolderOpen,
  Server,
  BarChart3,
  GitBranch,
  Clock3,
  Bell,
  Settings
}

const quickActions = computed(() =>
  getDashboardQuickActions(userStore.hasPermission).map((action) => ({
    ...action,
    icon: iconMap[action.icon as keyof typeof iconMap]
  }))
)

onMounted(fetchDashboard)
</script>

<template>
  <div class="space-y-6">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <Card
        v-for="stat in statCards"
        :key="stat.label"
        class="hover:shadow-lg transition-shadow"
      >
        <CardContent class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-muted-foreground">
                {{ stat.label }}
              </p>
              <p class="text-3xl font-bold mt-1">
                {{ loading ? '...' : stats?.[stat.key] ?? 0 }}
              </p>
            </div>
            <div :class="[stat.color, 'w-12 h-12 rounded-lg flex items-center justify-center']">
              <component
                :is="stat.icon"
                class="w-6 h-6 text-white"
              />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- 快捷操作 & 最近活动 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <Card>
        <CardContent class="p-6">
          <h3 class="text-lg font-semibold mb-4">
            快捷操作
          </h3>
          <div class="grid grid-cols-2 gap-3">
            <RouterLink
              v-for="action in quickActions"
              :key="action.path"
              :to="action.path"
              class="p-4 border rounded-lg hover:bg-gray-50 transition-colors text-center"
            >
              <component
                :is="action.icon"
                :class="['w-6 h-6 mx-auto mb-2', action.color]"
              />
              <span class="text-sm">{{ action.label }}</span>
            </RouterLink>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-6">
          <h3 class="text-lg font-semibold mb-4">
            最近活动
          </h3>
          <div
            v-if="loading"
            class="text-center py-8 text-gray-400"
          >
            加载中...
          </div>
          <div
            v-else-if="recentLogs.length === 0"
            class="text-center py-8 text-gray-400"
          >
            暂无数据
          </div>
          <div
            v-else
            class="space-y-4"
          >
            <div
              v-for="log in recentLogs"
              :key="log.id"
              class="flex items-center gap-3 text-sm"
            >
              <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary font-medium">
                {{ log.username?.charAt(0) || '?' }}
              </div>
              <div class="flex-1">
                <span class="font-medium">{{ log.username }}</span>
                <span class="text-muted-foreground ml-1">{{ log.description }}</span>
              </div>
              <span class="text-xs text-muted-foreground">{{ log.createTime }}</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- 通知中心 -->
    <NotificationCenter />
  </div>
</template>
