<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1 class="banner-title">
          欢迎回来,AdminPlus
        </h1>
        <p class="banner-subtitle">
          今天是 {{ formatDate() }},祝您工作愉快!
        </p>
      </div>
      <div class="banner-actions">
        <el-button
          type="primary"
          :icon="Refresh"
          circle
          size="large"
          @click="handleRefresh"
        />
      </div>
      <div class="banner-icon">
        <el-icon :size="80">
          <House />
        </el-icon>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row
      :gutter="20"
      class="stats-row"
    >
      <el-col :span="6">
        <el-card
          v-loading="loading"
          shadow="hover"
          class="stat-card-wrapper"
        >
          <div class="stat-card">
            <div class="stat-icon stat-icon-user">
              <el-icon :size="36">
                <User />
              </el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                {{ stats.userCount.toLocaleString() }}
              </div>
              <div class="stat-label">
                用户总数
              </div>
              <div class="stat-trend">
                <el-icon :size="12">
                  <CaretTop />
                </el-icon>
                <span>较上月 +12%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card
          v-loading="loading"
          shadow="hover"
          class="stat-card-wrapper"
        >
          <div class="stat-card">
            <div class="stat-icon stat-icon-role">
              <el-icon :size="36">
                <UserFilled />
              </el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                {{ stats.roleCount.toLocaleString() }}
              </div>
              <div class="stat-label">
                角色总数
              </div>
              <div class="stat-trend">
                <el-icon :size="12">
                  <CaretTop />
                </el-icon>
                <span>较上月 +5%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card
          v-loading="loading"
          shadow="hover"
          class="stat-card-wrapper"
        >
          <div class="stat-card">
            <div class="stat-icon stat-icon-menu">
              <el-icon :size="36">
                <Menu />
              </el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                {{ stats.menuCount.toLocaleString() }}
              </div>
              <div class="stat-label">
                菜单总数
              </div>
              <div class="stat-trend stable">
                <el-icon :size="12">
                  <Minus />
                </el-icon>
                <span>无变化</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card
          v-loading="loading"
          shadow="hover"
          class="stat-card-wrapper"
        >
          <div class="stat-card">
            <div class="stat-icon stat-icon-log">
              <el-icon :size="36">
                <Document />
              </el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                {{ stats.logCount.toLocaleString() }}
              </div>
              <div class="stat-label">
                日志总数
              </div>
              <div class="stat-trend">
                <el-icon :size="12">
                  <CaretTop />
                </el-icon>
                <span>今日 +{{ stats.todayLogCount }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row
      :gutter="20"
      class="charts-row"
    >
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
            </div>
          </template>
          <div
            ref="userGrowthChartRef"
            class="chart-container"
          />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>角色分布</span>
            </div>
          </template>
          <div
            ref="roleDistributionChartRef"
            class="chart-container"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-row
      :gutter="20"
      class="charts-row"
    >
      <el-col :span="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>菜单类型分布</span>
            </div>
          </template>
          <div
            ref="menuDistributionChartRef"
            class="chart-container chart-container-small"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作、系统信息、在线用户 -->
    <el-row
      :gutter="20"
      class="bottom-row"
    >
      <el-col :span="8">
        <el-card
          shadow="hover"
          class="quick-actions-card"
        >
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon">
                  <Operation />
                </el-icon>
                <span>快捷操作</span>
              </div>
            </div>
          </template>
          <div class="quick-actions">
            <div
              class="action-card"
              @click="handleQuickAction('user')"
            >
              <div class="action-icon action-icon-blue">
                <el-icon :size="24">
                  <User />
                </el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">
                  添加用户
                </div>
                <div class="action-desc">
                  创建新用户账号
                </div>
              </div>
            </div>
            <div
              class="action-card"
              @click="handleQuickAction('role')"
            >
              <div class="action-icon action-icon-purple">
                <el-icon :size="24">
                  <UserFilled />
                </el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">
                  添加角色
                </div>
                <div class="action-desc">
                  配置角色权限
                </div>
              </div>
            </div>
            <div
              class="action-card"
              @click="handleQuickAction('menu')"
            >
              <div class="action-icon action-icon-orange">
                <el-icon :size="24">
                  <Menu />
                </el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">
                  添加菜单
                </div>
                <div class="action-desc">
                  管理菜单权限
                </div>
              </div>
            </div>
            <div
              class="action-card"
              @click="handleQuickAction('system')"
            >
              <div class="action-icon action-icon-cyan">
                <el-icon :size="24">
                  <Setting />
                </el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">
                  系统设置
                </div>
                <div class="action-desc">
                  系统参数配置
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card
          shadow="hover"
          class="system-info-card"
        >
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon">
                  <Setting />
                </el-icon>
                <span>系统信息</span>
              </div>
            </div>
          </template>
          <div
            v-loading="systemInfoLoading"
            class="system-info"
          >
            <div class="info-grid">
              <div class="info-item">
                <div class="info-icon">
                  <el-icon><House /></el-icon>
                </div>
                <div class="info-content">
                  <div class="info-label">
                    系统名称
                  </div>
                  <div class="info-value">
                    {{ systemInfo.systemName || 'AdminPlus' }}
                  </div>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="info-content">
                  <div class="info-label">
                    系统版本
                  </div>
                  <div class="info-value">
                    {{ systemInfo.systemVersion || '1.0.0' }}
                  </div>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon">
                  <el-icon><Setting /></el-icon>
                </div>
                <div class="info-content">
                  <div class="info-label">
                    操作系统
                  </div>
                  <div class="info-value">
                    {{ systemInfo.osName || '-' }}
                  </div>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="info-content">
                  <div class="info-label">
                    JDK版本
                  </div>
                  <div class="info-value">
                    {{ systemInfo.jdkVersion || '-' }}
                  </div>
                </div>
              </div>
              <div class="info-item full-width">
                <div class="info-icon">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="info-content">
                  <div class="info-label">
                    运行时间
                  </div>
                  <div class="info-value">
                    {{ formatUptime(systemInfo.uptime) }}
                  </div>
                </div>
              </div>
              <div class="info-item full-width">
                <div class="memory-bar">
                  <div class="memory-label">
                    <span>内存使用</span>
                    <span class="memory-text">{{ systemInfo.usedMemory }}MB / {{ systemInfo.totalMemory }}MB</span>
                  </div>
                  <el-progress
                    :percentage="memoryPercentage"
                    :color="memoryColor"
                    :stroke-width="8"
                  />
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card
          shadow="hover"
          class="online-users-card"
        >
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon">
                  <User />
                </el-icon>
                <span>在线用户</span>
              </div>
              <el-tag
                type="success"
                size="small"
              >
                {{ onlineUsers.length }} 人在线
              </el-tag>
            </div>
          </template>
          <div
            v-loading="onlineUsersLoading"
            class="online-users"
          >
            <div
              v-if="onlineUsers.length === 0"
              class="empty-state"
            >
              <el-icon :size="48">
                <User />
              </el-icon>
              <p>暂无在线用户</p>
            </div>
            <div
              v-for="user in onlineUsers"
              :key="user.userId"
              class="online-user-item"
            >
              <div class="user-avatar">
                <el-avatar
                  :size="40"
                  :icon="UserFilled"
                />
                <div class="online-indicator" />
              </div>
              <div class="user-info">
                <div class="user-name">
                  {{ user.username }}
                </div>
                <div class="user-meta">
                  <el-icon :size="12">
                    <Location />
                  </el-icon>
                  <span>{{ user.ip }}</span>
                </div>
              </div>
              <el-button
                type="danger"
                size="small"
                link
                @click="handleForceOffline(user)"
              >
                <el-icon><Warning /></el-icon>
                强制下线
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近操作日志 -->
    <el-row
      :gutter="20"
      class="charts-row"
    >
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近操作日志</span>
              <el-button
                type="primary"
                link
                @click="handleViewAllLogs"
              >
                查看全部
              </el-button>
            </div>
          </template>
          <div class="table-container">
            <el-table
              v-loading="logsLoading"
              :data="recentLogs"
              stripe
              style="width: 100%"
            >
              <el-table-column
                prop="username"
                label="操作人"
                width="120"
              />
              <el-table-column
                prop="module"
                label="模块"
                width="120"
              />
              <el-table-column
                label="操作类型"
                width="100"
              >
                <template #default="{ row }">
                  <el-tag :type="getOperationTypeTag(row.operationType)">
                    {{ getOperationTypeName(row.operationType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="description"
                label="操作描述"
                show-overflow-tooltip
              />
              <el-table-column
                prop="ip"
                label="IP地址"
                width="140"
              />
              <el-table-column
                label="操作时间"
                width="180"
              >
                <template #default="{ row }">
                  {{ formatTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column
                label="状态"
                width="80"
              >
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                label="操作"
                width="100"
                fixed="right"
              >
                <template #default="{ row }">
                  <el-button
                    type="primary"
                    link
                    size="small"
                    @click="handleViewLogDetail(row)"
                  >
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="logDetailVisible"
      title="日志详情"
      width="600px"
    >
      <el-descriptions
        v-if="currentLog"
        :column="1"
        border
      >
        <el-descriptions-item label="日志ID">
          {{ currentLog.id }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ currentLog.username }}
        </el-descriptions-item>
        <el-descriptions-item label="操作模块">
          {{ currentLog.module }}
        </el-descriptions-item>
        <el-descriptions-item label="操作类型">
          {{ getOperationTypeName(currentLog.operationType) }}
        </el-descriptions-item>
        <el-descriptions-item label="操作描述">
          {{ currentLog.description }}
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">
          {{ currentLog.ip }}
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">
          {{ formatTime(currentLog.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ currentLog.status === 1 ? '成功' : '失败' }}
        </el-descriptions-item>
        <el-descriptions-item label="执行时长">
          {{ currentLog.costTime }}ms
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="logDetailVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CaretTop,
  Clock,
  Document,
  House,
  Location,
  Menu,
  Minus,
  Operation,
  Refresh,
  Setting,
  User,
  UserFilled,
  Warning
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getDashboardStats,
  getUserGrowth,
  getRoleDistribution,
  getMenuDistribution,
  getRecentLogs,
  getSystemInfo,
  getOnlineUsers
} from '@/api/dashboard'

const router = useRouter()

// 统计数据
const stats = ref({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  logCount: 0,
  todayLogCount: 0
})

// 加载状态
const loading = ref(false)
const logsLoading = ref(false)
const systemInfoLoading = ref(false)
const onlineUsersLoading = ref(false)

// 图表引用
const userGrowthChartRef = ref(null)
const roleDistributionChartRef = ref(null)
const menuDistributionChartRef = ref(null)

// 图表实例
let userGrowthChart = null
let roleDistributionChart = null
let menuDistributionChart = null

// 最近操作日志
const recentLogs = ref([])

// 系统信息
const systemInfo = ref({
  systemName: '',
  systemVersion: '',
  osName: '',
  jdkVersion: '',
  totalMemory: 0,
  usedMemory: 0,
  freeMemory: 0,
  databaseType: '',
  databaseVersion: '',
  databaseConnections: 0,
  uptime: 0
})

// 在线用户
const onlineUsers = ref([])

// 日志详情
const logDetailVisible = ref(false)
const currentLog = ref(null)

// 计算内存使用百分比
const memoryPercentage = ref(0)
const memoryColor = ref('#409EFF')

// 格式化日期
const formatDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const day = now.getDate()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const weekDay = weekDays[now.getDay()]
  return `${year}年${month}月${day}日 星期${weekDay}`
}

// 获取统计数据
const fetchStats = async () => {
  try {
    loading.value = true
    const data = await getDashboardStats()
    stats.value = data
  } catch {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 获取用户增长趋势
const fetchUserGrowth = async () => {
  try {
    const data = await getUserGrowth()

    // 检查数据是否为空
    if (!data || !data.labels || data.labels.length === 0 || !data.values || data.values.length === 0) {
      // 数据为空，显示空状态
      userGrowthChart.clear()
      userGrowthChart.setOption({
        title: {
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#999',
            fontSize: 16
          }
        }
      })
      return
    }

    const option = {
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.labels
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '新增用户',
          type: 'line',
          smooth: true,
          data: data.values,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(0, 102, 255, 0.3)' },
              { offset: 1, color: 'rgba(0, 102, 255, 0.05)' }
            ])
          },
          lineStyle: {
            color: '#0066FF',
            width: 3
          },
          itemStyle: {
            color: '#0066FF'
          }
        }
      ]
    }
    userGrowthChart.setOption(option)
  } catch {
    ElMessage.error('获取用户增长趋势失败')
  }
}

// 获取角色分布
const fetchRoleDistribution = async () => {
  try {
    const data = await getRoleDistribution()

    // 检查数据是否为空
    if (!data || !data.labels || data.labels.length === 0 || !data.values || data.values.length === 0) {
      // 数据为空，显示空状态
      roleDistributionChart.clear()
      roleDistributionChart.setOption({
        title: {
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#999',
            fontSize: 16
          }
        }
      })
      return
    }

    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center'
      },
      series: [
        {
          name: '角色分布',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 18,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: data.labels.map((label, index) => ({
            value: data.values[index],
            name: label,
            itemStyle: {
              color: ['#0066FF', '#7B5FD6', '#10B981', '#F59E0B', '#EF4444'][index % 5]
            }
          }))
        }
      ]
    }
    roleDistributionChart.setOption(option)
  } catch {
    ElMessage.error('获取角色分布失败')
  }
}

// 获取菜单类型分布
const fetchMenuDistribution = async () => {
  try {
    const data = await getMenuDistribution()

    // 检查数据是否为空
    if (!data || !data.labels || data.labels.length === 0 || !data.values || data.values.length === 0) {
      // 数据为空，显示空状态
      menuDistributionChart.clear()
      menuDistributionChart.setOption({
        title: {
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#999',
            fontSize: 16
          }
        }
      })
      return
    }

    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.labels
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '菜单数量',
          type: 'bar',
          barWidth: '60%',
          data: data.values,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#3385FF' },
              { offset: 0.5, color: '#0066FF' },
              { offset: 1, color: '#0052CC' }
            ]),
            borderRadius: [4, 4, 0, 0]
          }
        }
      ]
    }
    menuDistributionChart.setOption(option)
  } catch {
    ElMessage.error('获取菜单类型分布失败')
  }
}

// 获取最近操作日志
const fetchRecentLogs = async () => {
  try {
    logsLoading.value = true
    const data = await getRecentLogs()
    recentLogs.value = data
  } catch {
    ElMessage.error('获取操作日志失败')
  } finally {
    logsLoading.value = false
  }
}

// 获取系统信息
const fetchSystemInfo = async () => {
  try {
    systemInfoLoading.value = true
    const data = await getSystemInfo()
    systemInfo.value = data

    // 计算内存使用百分比
    if (data.totalMemory > 0) {
      const percentage = Math.round((data.usedMemory / data.totalMemory) * 100)
      memoryPercentage.value = percentage

      // 根据使用率设置颜色
      if (percentage < 50) {
        memoryColor.value = '#67C23A'
      } else if (percentage < 80) {
        memoryColor.value = '#E6A23C'
      } else {
        memoryColor.value = '#F56C6C'
      }
    }
  } catch {
    ElMessage.error('获取系统信息失败')
  } finally {
    systemInfoLoading.value = false
  }
}

// 获取在线用户
const fetchOnlineUsers = async () => {
  try {
    onlineUsersLoading.value = true
    const data = await getOnlineUsers()
    onlineUsers.value = data
  } catch {
    ElMessage.error('获取在线用户失败')
  } finally {
    onlineUsersLoading.value = false
  }
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    userGrowthChart = echarts.init(userGrowthChartRef.value)
    roleDistributionChart = echarts.init(roleDistributionChartRef.value)
    menuDistributionChart = echarts.init(menuDistributionChartRef.value)

    // 响应式调整
    window.addEventListener('resize', handleResize)
  })
}

// 处理窗口大小变化
const handleResize = () => {
  userGrowthChart && userGrowthChart.resize()
  roleDistributionChart && roleDistributionChart.resize()
  menuDistributionChart && menuDistributionChart.resize()
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 格式化运行时间
const formatUptime = (seconds) => {
  if (!seconds) return '-'
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return `${days}天 ${hours}小时 ${minutes}分钟`
}

// 获取操作类型名称
const getOperationTypeName = (type) => {
  const types = {
    1: '查询',
    2: '新增',
    3: '修改',
    4: '删除',
    5: '导出',
    6: '导入',
    7: '其他'
  }
  return types[type] || '未知'
}

// 获取操作类型标签样式
const getOperationTypeTag = (type) => {
  const tags = {
    1: 'info',
    2: 'success',
    3: 'warning',
    4: 'danger',
    5: 'primary',
    6: 'primary',
    7: 'info'
  }
  return tags[type] || 'info'
}

// 快捷操作
const handleQuickAction = (action) => {
  const routes = {
    user: '/system/users',
    role: '/system/roles',
    menu: '/system/menus',
    system: '/system/settings'
  }
  if (routes[action]) {
    router.push(routes[action])
  }
}

// 查看日志详情
const handleViewLogDetail = (log) => {
  currentLog.value = log
  logDetailVisible.value = true
}

// 查看全部日志
const handleViewAllLogs = () => {
  router.push('/system/logs')
}

// 强制下线
const handleForceOffline = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要强制用户 ${user.username} 下线吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    // TODO: 调用强制下线接口
    ElMessage.success('用户已强制下线')
    fetchOnlineUsers()
  } catch {
    // 用户取消操作
  }
}

// 手动刷新数据
const handleRefresh = async () => {
  ElMessage.info('正在刷新数据...')
  try {
    await Promise.all([
      fetchStats(),
      fetchUserGrowth(),
      fetchRoleDistribution(),
      fetchMenuDistribution(),
      fetchRecentLogs(),
      fetchSystemInfo(),
      fetchOnlineUsers()
    ])
    ElMessage.success('数据刷新成功')
  } catch {
    ElMessage.error('数据刷新失败')
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  // 先初始化图表
  initCharts()
  
  // 然后获取数据
  await fetchStats()
  await fetchUserGrowth()
  await fetchRoleDistribution()
  await fetchMenuDistribution()
  await fetchRecentLogs()
  await fetchSystemInfo()
  await fetchOnlineUsers()
})

// 组件卸载前清理
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  userGrowthChart && userGrowthChart.dispose()
  roleDistributionChart && roleDistributionChart.dispose()
  menuDistributionChart && menuDistributionChart.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background-color: #F7F8FA;
  min-height: 100%;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #0066FF 0%, #7B5FD6 100%);
  border-radius: 16px;
  padding: 32px 40px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
  box-shadow: 0 8px 24px rgba(0, 102, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.banner-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

.banner-actions {
  margin: 0 24px;
  position: relative;
  z-index: 1;
}

.banner-actions .el-button {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.banner-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  transform: rotate(180deg);
}

.banner-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.banner-subtitle {
  font-size: 15px;
  opacity: 0.9;
  margin: 0;
}

.banner-icon {
  opacity: 0.15;
  animation: float 3s ease-in-out infinite;
  position: relative;
  z-index: 0;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card-wrapper {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
  overflow: hidden;
  height: 100%;
}

.stat-card-wrapper:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(0, 102, 255, 0.22);
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 8px;
}

/* 统计卡片图标 */
.stat-icon {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 20px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  position: relative;
  overflow: hidden;
}

.stat-icon::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(255, 255, 255, 0.15),
    transparent
  );
  transform: rotate(45deg);
  animation: shine 3s infinite;
}

@keyframes shine {
  0% {
    left: -50%;
  }
  100% {
    left: 150%;
  }
}

.stat-icon-user {
  background: linear-gradient(135deg, #0066FF 0%, #3385FF 100%);
}

.stat-icon-role {
  background: linear-gradient(135deg, #7B5FD6 0%, #9F7AEA 100%);
}

.stat-icon-menu {
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
}

.stat-icon-log {
  background: linear-gradient(135deg, #10B981 0%, #34D399 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #1A1A1A 0%, #4A5568 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 4px;
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  margin-top: 8px;
  color: #67C23A;
  font-weight: 500;
}

.stat-trend.stable {
  color: #909399;
}

.stat-trend .el-icon {
  font-weight: bold;
}

/* 图表行 */
.charts-row {
  margin-top: 24px;
}

.chart-card {
  height: 100%;
}

.chart-container {
  height: 350px;
}

.chart-container-small {
  height: 300px;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  color: #1A1A1A;
}

.header-icon {
  color: #0066FF;
}

/* 快捷操作 */
.quick-actions-card {
  height: 100%;
}

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #F7F8FA 0%, #FFFFFF 100%);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 102, 255, 0.05), transparent);
  transition: left 0.5s;
}

.action-card:hover::before {
  left: 100%;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 102, 255, 0.18);
  border-color: rgba(0, 102, 255, 0.25);
}

.action-card:active {
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(0, 102, 255, 0.12);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1;
}

.action-icon::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, transparent 50%);
}

.action-icon-blue {
  background: linear-gradient(135deg, #0066FF 0%, #3385FF 100%);
}

.action-icon-purple {
  background: linear-gradient(135deg, #7B5FD6 0%, #9F7AEA 100%);
}

.action-icon-orange {
  background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%);
}

.action-icon-cyan {
  background: linear-gradient(135deg, #06B6D4 0%, #22D3EE 100%);
}

.action-content {
  flex: 1;
  min-width: 0;
  z-index: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A1A;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.4;
}

/* 系统信息 */
.system-info-card {
  height: 100%;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #F7F8FA;
  border-radius: 10px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
}

.info-item:hover {
  background: #E8F0FE;
  transform: translateX(4px);
  border-color: rgba(0, 102, 255, 0.2);
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.1);
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0066FF 0%, #7B5FD6 100%);
  color: white;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
  min-width: 0;
}

.info-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
  font-weight: 500;
}

.info-value {
  font-size: 15px;
  color: #1A1A1A;
  font-weight: 600;
  word-break: break-all;
}

.memory-bar {
  padding: 16px;
  background: #F7F8FA;
  border-radius: 10px;
  border: 1px solid transparent;
  transition: all 0.25s;
}

.memory-bar:hover {
  border-color: rgba(0, 102, 255, 0.2);
}

.memory-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.memory-text {
  color: #606266;
  font-weight: 500;
}

/* 在线用户 */
.online-users-card {
  height: 100%;
}

.online-users {
  max-height: 400px;
  overflow-y: auto;
  padding: 4px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  color: #C0C4CC;
}

.empty-state .el-icon {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.online-user-item {
  display: flex;
  align-items: center;
  padding: 16px;
  margin-bottom: 8px;
  background: #F7F8FA;
  border-radius: 12px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  gap: 12px;
  border: 1px solid transparent;
}

.online-user-item:hover {
  background: #E8F0FE;
  transform: translateX(4px);
  border-color: rgba(0, 102, 255, 0.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.user-avatar {
  position: relative;
  flex-shrink: 0;
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: #67C23A;
  border: 2px solid white;
  border-radius: 50%;
  animation: pulse 2s infinite;
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.5);
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.1);
  }
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #1A1A1A;
  margin-bottom: 4px;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.user-meta .el-icon {
  font-size: 14px;
}

/* 表格容器 */
.table-container {
  overflow-x: auto;
}

/* 底部行 */
.bottom-row {
  margin-top: 24px;
}

/* ========== 响应式布局 ========== */

/* 大屏幕 (>1400px) - 保持当前布局 */

/* 中等屏幕 (1200px-1400px) - 调整卡片间距和字体 */
@media (max-width: 1400px) {
  .stat-value {
    font-size: 32px;
  }

  .banner-title {
    font-size: 26px;
  }

  .banner-subtitle {
    font-size: 14px;
  }

  .stat-icon {
    width: 64px;
    height: 64px;
  }

  .action-icon {
    width: 50px;
    height: 50px;
  }

  .info-icon {
    width: 36px;
    height: 36px;
  }

  .charts-row {
    margin-top: 20px;
  }
}

/* 小屏幕 (992px-1200px) - 底部三列改为两列布局 */
@media (max-width: 1200px) {
  .dashboard {
    padding: 20px;
  }

  .welcome-banner {
    padding: 28px 32px;
  }

  .banner-title {
    font-size: 24px;
  }

  .stat-value {
    font-size: 30px;
  }

  .stat-label {
    font-size: 13px;
  }

  .stat-trend {
    font-size: 12px;
  }

  .stat-icon {
    width: 60px;
    height: 60px;
    margin-right: 16px;
  }

  .action-icon {
    width: 48px;
    height: 48px;
  }

  .action-title {
    font-size: 15px;
  }

  .action-desc {
    font-size: 12px;
  }

  .info-label {
    font-size: 12px;
  }

  .info-value {
    font-size: 14px;
  }

  .charts-row {
    margin-top: 20px;
  }

  .bottom-row {
    margin-top: 20px;
  }
}

/* 平板 (768px-992px) - 图表垂直堆叠,统计卡片改为2列 */
@media (max-width: 992px) {
  .dashboard {
    padding: 16px;
  }

  /* 欢迎横幅调整 */
  .welcome-banner {
    padding: 24px;
    border-radius: 12px;
  }

  .banner-title {
    font-size: 22px;
  }

  .banner-subtitle {
    font-size: 13px;
  }

  .banner-icon {
    opacity: 0.15;
  }

  /* 统计卡片调整为2列 */
  .stats-row :deep(.el-col) {
    width: 50% !important;
    max-width: 50%;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    margin-right: 0;
    margin-bottom: 12px;
  }

  .stat-value {
    font-size: 28px;
  }

  /* 快捷操作改为单列 */
  .quick-actions {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .action-card {
    padding: 16px;
  }

  /* 系统信息改为单列 */
  .info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .info-item {
    padding: 14px;
  }

  .info-icon {
    width: 36px;
    height: 36px;
  }

  /* 在线用户卡片 */
  .online-users {
    max-height: 300px;
  }

  .online-user-item {
    padding: 12px;
  }

  .user-name {
    font-size: 14px;
  }

  .user-meta {
    font-size: 11px;
  }

  .charts-row {
    margin-top: 16px;
  }

  .bottom-row {
    margin-top: 16px;
  }
}

/* 手机 (<768px) - 所有内容改为单列布局 */
@media (max-width: 768px) {
  .dashboard {
    padding: 12px;
  }

  /* 欢迎横幅 - 垂直居中 */
  .welcome-banner {
    padding: 20px;
    flex-direction: column;
    text-align: center;
    border-radius: 12px;
  }

  .banner-content {
    margin-bottom: 16px;
  }

  .banner-title {
    font-size: 20px;
  }

  .banner-subtitle {
    font-size: 13px;
  }

  .banner-icon {
    opacity: 0.15;
  }

  /* 统计卡片 - 改为单列或2列网格 */
  .stats-row :deep(.el-col) {
    width: 50% !important;
    max-width: 50%;
  }

  .stats-row {
    margin-bottom: 12px;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
    padding: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    margin-right: 0;
    margin-bottom: 10px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-label {
    font-size: 12px;
  }

  .stat-trend {
    font-size: 11px;
  }

  /* 图表 - 改为单列堆叠 */
  .charts-row {
    margin-top: 12px !important;
  }

  .charts-row :deep(.el-col) {
    width: 100% !important;
    max-width: 100%;
    margin-bottom: 12px;
  }

  .chart-container {
    height: 280px;
  }

  .chart-container-small {
    height: 240px;
  }

  .el-card {
    margin-bottom: 12px;
  }

  /* 快捷操作 - 单列 */
  .quick-actions {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .action-card {
    padding: 14px;
    gap: 12px;
  }

  .action-icon {
    width: 44px;
    height: 44px;
  }

  .action-title {
    font-size: 14px;
  }

  .action-desc {
    font-size: 12px;
  }

  /* 系统信息 - 单列显示 */
  .info-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .info-item {
    padding: 12px;
  }

  .info-icon {
    width: 32px;
    height: 32px;
  }

  .info-label {
    font-size: 11px;
  }

  .info-value {
    font-size: 13px;
  }

  .memory-label {
    font-size: 12px;
  }

  /* 在线用户 */
  .online-users {
    max-height: 250px;
  }

  .online-user-item {
    padding: 10px;
    gap: 10px;
  }

  .user-avatar :deep(.el-avatar) {
    width: 36px;
    height: 36px;
  }

  .user-name {
    font-size: 13px;
  }

  .user-meta {
    font-size: 11px;
  }

  .online-user-item .el-button {
    font-size: 11px;
    padding: 4px 8px;
  }

  /* 表格 - 支持横向滚动 */
  .table-container {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .el-table {
    min-width: 800px;
  }

  /* 卡片头部 */
  .header-title {
    font-size: 14px;
  }
}

/* 超小手机 (<480px) */
@media (max-width: 480px) {
  .dashboard {
    padding: 10px;
  }

  .welcome-banner {
    padding: 16px;
  }

  .banner-title {
    font-size: 18px;
  }

  .banner-subtitle {
    font-size: 12px;
  }

  .banner-icon :deep(.el-icon) {
    font-size: 60px !important;
  }

  /* 统计卡片改为单列 */
  .stats-row :deep(.el-col) {
    width: 100% !important;
    max-width: 100%;
  }

  .stat-card {
    padding: 14px;
  }

  .stat-icon {
    width: 44px;
    height: 44px;
  }

  .stat-value {
    font-size: 20px;
  }

  .stat-label {
    font-size: 11px;
  }

  .stat-trend {
    font-size: 10px;
  }

  /* 快捷操作 */
  .action-card {
    padding: 12px;
  }

  .action-icon {
    width: 40px;
    height: 40px;
  }

  .action-title {
    font-size: 13px;
  }

  .action-desc {
    font-size: 11px;
  }

  /* 系统信息 */
  .info-item {
    padding: 10px;
    gap: 10px;
  }

  .info-icon {
    width: 30px;
    height: 30px;
  }

  .info-label {
    font-size: 11px;
  }

  .info-value {
    font-size: 12px;
  }

  /* 在线用户 */
  .online-user-item {
    padding: 8px;
  }

  .user-avatar :deep(.el-avatar) {
    width: 32px;
    height: 32px;
  }

  .user-name {
    font-size: 12px;
  }

  .user-meta {
    font-size: 10px;
  }

  .online-user-item .el-button {
    font-size: 10px;
    padding: 3px 6px;
  }

  /* 对话框 */
  .el-dialog {
    width: 95% !important;
    margin: 0 auto;
  }
}

/* 滚动条样式 */
.online-users::-webkit-scrollbar {
  width: 4px;
}

.online-users::-webkit-scrollbar-thumb {
  background-color: #D1D5DB;
  border-radius: 4px;
}

.online-users::-webkit-scrollbar-thumb:hover {
  background-color: #9CA3AF;
}

.online-users::-webkit-scrollbar-track {
  background-color: transparent;
}

.table-container::-webkit-scrollbar {
  height: 6px;
}

.table-container::-webkit-scrollbar-thumb {
  background-color: #D1D5DB;
  border-radius: 3px;
}

.table-container::-webkit-scrollbar-thumb:hover {
  background-color: #9CA3AF;
}

.table-container::-webkit-scrollbar-track {
  background-color: #F7F8FA;
}
</style>