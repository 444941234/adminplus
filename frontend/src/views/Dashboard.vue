<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>{{ greeting }}，{{ userStore.user?.nickname || 'Admin' }}</h2>
          <p>欢迎使用 AdminPlus 管理系统</p>
        </div>
        <el-button type="primary" class="welcome-btn" @click="handleBannerAction">查看详情</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div v-for="stat in stats" :key="stat.key" class="stat-card">
        <div class="stat-icon" :class="`stat-icon-${stat.type}`">
          <el-icon :size="24"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ loading ? '...' : stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
        <div class="stat-trend" :class="stat.trendUp ? 'trend-up' : 'trend-down'">{{ stat.trend }}</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <el-card shadow="hover" class="chart-card chart-card-main">
        <template #header><span class="card-title">用户增长趋势</span></template>
        <div ref="userGrowthChartRef" class="chart-container"></div>
      </el-card>
      <el-card shadow="hover" class="chart-card chart-card-side">
        <template #header><span class="card-title">角色分布</span></template>
        <div ref="roleDistributionChartRef" class="chart-container"></div>
      </el-card>
    </div>

    <!-- 快捷操作和系统信息 -->
    <div class="bottom-grid">
      <el-card shadow="hover" class="action-card">
        <template #header><span class="card-title">快捷操作</span></template>
        <div class="quick-actions">
          <el-button v-for="action in quickActions" :key="action.id" :type="action.type" @click="handleQuickAction(action.id)">
            <el-icon><component :is="action.icon" /></el-icon>
            {{ action.label }}
          </el-button>
        </div>
      </el-card>
      <el-card shadow="hover" class="system-info-card">
        <template #header><span class="card-title">系统信息</span></template>
        <div v-if="systemInfoLoading" class="system-info-loading">加载中...</div>
        <div v-else class="system-info">
          <div v-for="info in systemInfoList" :key="info.key" class="info-item">
            <div class="info-icon"><el-icon :size="20"><component :is="info.icon" /></el-icon></div>
            <div class="info-content">
              <div class="info-label">{{ info.label }}</div>
              <div class="info-value">{{ info.value }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, UserFilled, Menu, Document, Setting, House, Clock, Monitor } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

defineOptions({ name: 'Dashboard' })

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const systemInfoLoading = ref(false)

const stats = ref([
  { key: 'users', type: 'primary', icon: User, value: 1234, label: '用户总数', trend: '+12%', trendUp: true },
  { key: 'roles', type: 'success', icon: UserFilled, value: 56, label: '角色数量', trend: '+5%', trendUp: true },
  { key: 'menus', type: 'warning', icon: Menu, value: 23, label: '菜单数量', trend: '0%', trendUp: true },
  { key: 'logs', type: 'danger', icon: Document, value: 856, label: '日志总数', trend: '-3%', trendUp: false }
])

const quickActions = [
  { id: 'user', label: '添加用户', icon: User, type: 'primary' as const },
  { id: 'role', label: '添加角色', icon: UserFilled, type: 'success' as const },
  { id: 'menu', label: '添加菜单', icon: Menu, type: 'warning' as const },
  { id: 'system', label: '系统设置', icon: Setting, type: 'info' as const }
]

const systemInfo = ref({ systemName: 'AdminPlus', systemVersion: '1.0.0', osName: '', jdkVersion: '', uptime: 0 })

const systemInfoList = computed(() => [
  { key: 'name', label: '系统名称', value: systemInfo.value.systemName, icon: House },
  { key: 'version', label: '系统版本', value: systemInfo.value.systemVersion, icon: Document },
  { key: 'os', label: '操作系统', value: systemInfo.value.osName || '-', icon: Setting },
  { key: 'jdk', label: 'JDK版本', value: systemInfo.value.jdkVersion || '-', icon: Document },
  { key: 'uptime', label: '运行时间', value: formatUptime(systemInfo.value.uptime), icon: Clock },
  { key: 'memory', label: '内存使用', value: '512 MB / 2048 MB', icon: Monitor }
])

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '上午好，开始新的一天！'
  if (hour < 18) return '下午好，继续加油！'
  return '晚上好，注意休息！'
})

const userGrowthChartRef = ref<HTMLElement>()
const roleDistributionChartRef = ref<HTMLElement>()
let userGrowthChart: echarts.ECharts | null = null
let roleDistributionChart: echarts.ECharts | null = null

const initCharts = () => {
  if (userGrowthChartRef.value) {
    userGrowthChart = echarts.init(userGrowthChartRef.value)
    userGrowthChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
      yAxis: { type: 'value' },
      series: [{
        data: [120, 200, 150, 80, 70, 110], type: 'line', smooth: true,
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(59, 130, 246, 0.3)' }, { offset: 1, color: 'rgba(59, 130, 246, 0)' }]) },
        lineStyle: { color: '#3B82F6', width: 3 }, itemStyle: { color: '#3B82F6' }
      }]
    })
  }
  if (roleDistributionChartRef.value) {
    roleDistributionChart = echarts.init(roleDistributionChartRef.value)
    roleDistributionChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: [
          { value: 1048, name: '管理员', itemStyle: { color: '#3B82F6' } },
          { value: 735, name: '普通用户', itemStyle: { color: '#10B981' } },
          { value: 580, name: '访客', itemStyle: { color: '#F59E0B' } },
          { value: 484, name: '游客', itemStyle: { color: '#EF4444' } }
        ]
      }]
    })
  }
}

const handleResize = () => { userGrowthChart?.resize(); roleDistributionChart?.resize() }
const handleBannerAction = () => console.log('Banner action clicked')
const handleQuickAction = (id: string) => {
  const routes: Record<string, string> = { user: '/system/user', role: '/system/role', menu: '/system/menu', system: '/system/config' }
  if (routes[id]) router.push(routes[id])
}
const formatUptime = (seconds: number) => {
  if (!seconds) return '-'
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return `${days}天 ${hours}小时 ${minutes}分钟`
}

const fetchSystemInfo = async () => {
  systemInfoLoading.value = true
  try { systemInfo.value = { systemName: 'AdminPlus', systemVersion: '1.0.0', osName: 'Windows 11', jdkVersion: '21.0.1', uptime: 86400 * 3 + 3600 * 5 } }
  finally { systemInfoLoading.value = false }
}

onMounted(() => { initCharts(); fetchSystemInfo(); window.addEventListener('resize', handleResize) })
onUnmounted(() => { userGrowthChart?.dispose(); roleDistributionChart?.dispose(); window.removeEventListener('resize', handleResize) })
</script>

<style scoped lang="scss">
.dashboard { display: flex; flex-direction: column; gap: 24px; }
.welcome-banner { background: linear-gradient(135deg, #3B82F6 0%, #6366F1 100%); border-radius: 12px; padding: 24px; }
.welcome-content { display: flex; justify-content: space-between; align-items: center; }
.welcome-text { h2 { color: #fff; margin: 0 0 8px; font-size: 24px; font-weight: 600; } p { color: rgba(255, 255, 255, 0.9); margin: 0; font-size: 14px; } }
.welcome-btn { background: rgba(255, 255, 255, 0.2); border: none; &:hover { background: rgba(255, 255, 255, 0.3); } }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.stat-card { background: var(--bg-card); border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px; box-shadow: var(--shadow-sm); transition: all 0.3s ease; cursor: pointer; &:hover { transform: translateY(-4px); box-shadow: var(--shadow-md); } }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; &-primary { background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%); } &-success { background: linear-gradient(135deg, #10B981 0%, #34D399 100%); } &-warning { background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%); } &-danger { background: linear-gradient(135deg, #EF4444 0%, #F87171 100%); } }
.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: 600; color: var(--text-primary); }
.stat-label { font-size: 14px; color: var(--text-secondary); margin-top: 4px; }
.stat-trend { font-size: 14px; font-weight: 500; padding: 4px 8px; border-radius: 6px; }
.trend-up { color: #10B981; background: rgba(16, 185, 129, 0.1); }
.trend-down { color: #EF4444; background: rgba(239, 68, 68, 0.1); }
.charts-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }
.chart-card { border-radius: 12px; :deep(.el-card__header) { padding: 16px 20px; border-bottom: 1px solid var(--border-color); } :deep(.el-card__body) { padding: 20px; } }
.card-title { font-size: 16px; font-weight: 600; color: var(--text-primary); }
.chart-container { height: 300px; }
.bottom-grid { display: grid; grid-template-columns: 1fr 2fr; gap: 20px; }
.quick-actions { display: flex; flex-wrap: wrap; gap: 12px; }
.system-info-card { border-radius: 12px; :deep(.el-card__body) { padding: 20px; } }
.system-info-loading { display: flex; align-items: center; justify-content: center; padding: 40px; color: var(--text-secondary); }
.system-info { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.info-item { display: flex; align-items: center; gap: 12px; padding: 12px; background: var(--bg-hover); border-radius: 8px; transition: all 0.3s ease; &:hover { background: rgba(59, 130, 246, 0.1); transform: translateY(-2px); } }
.info-icon { width: 40px; height: 40px; background: linear-gradient(135deg, #3B82F6 0%, #6366F1 100%); color: #fff; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.info-content { flex: 1; min-width: 0; }
.info-label { font-size: 12px; color: var(--text-secondary); margin-bottom: 2px; }
.info-value { font-size: 14px; font-weight: 600; color: var(--text-primary); }
@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } .charts-grid { grid-template-columns: 1fr; } .bottom-grid { grid-template-columns: 1fr; } }
@media (max-width: 640px) { .stats-grid { grid-template-columns: 1fr; } .system-info { grid-template-columns: 1fr; } }
</style>