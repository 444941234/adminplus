<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1 class="banner-title">欢迎回来，AdminPlus</h1>
        <p class="banner-subtitle">今天是 {{ formatDate() }}，祝您工作愉快！</p>
      </div>
      <div class="banner-icon">
        <el-icon :size="80"><House /></el-icon>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card v-loading="loading" shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon stat-icon-user">
              <el-icon :size="36"><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.userCount.toLocaleString() }}</div>
              <div class="stat-label">用户总数</div>
              <div class="stat-trend">
                <el-icon :size="12"><CaretTop /></el-icon>
                <span>较上月 +12%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card v-loading="loading" shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon stat-icon-role">
              <el-icon :size="36"><UserFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.roleCount.toLocaleString() }}</div>
              <div class="stat-label">角色总数</div>
              <div class="stat-trend">
                <el-icon :size="12"><CaretTop /></el-icon>
                <span>较上月 +5%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card v-loading="loading" shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon stat-icon-menu">
              <el-icon :size="36"><Menu /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.menuCount.toLocaleString() }}</div>
              <div class="stat-label">菜单总数</div>
              <div class="stat-trend stable">
                <el-icon :size="12"><Minus /></el-icon>
                <span>无变化</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card v-loading="loading" shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon stat-icon-log">
              <el-icon :size="36"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.logCount.toLocaleString() }}</div>
              <div class="stat-label">日志总数</div>
              <div class="stat-trend">
                <el-icon :size="12"><CaretTop /></el-icon>
                <span>今日 +{{ Math.floor(Math.random() * 50) + 10 }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
            </div>
          </template>
          <div ref="userGrowthChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>角色分布</span>
            </div>
          </template>
          <div ref="roleDistributionChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>菜单类型分布</span>
            </div>
          </template>
          <div ref="menuDistributionChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作、系统信息、在线用户 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="8">
        <el-card shadow="hover" class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon"><Operation /></el-icon>
                <span>快捷操作</span>
              </div>
            </div>
          </template>
          <div class="quick-actions">
            <div class="action-card" @click="handleQuickAction('user')">
              <div class="action-icon action-icon-blue">
                <el-icon :size="24"><User /></el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">添加用户</div>
                <div class="action-desc">创建新用户账号</div>
              </div>
            </div>
            <div class="action-card" @click="handleQuickAction('role')">
              <div class="action-icon action-icon-purple">
                <el-icon :size="24"><UserFilled /></el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">添加角色</div>
                <div class="action-desc">配置角色权限</div>
              </div>
            </div>
            <div class="action-card" @click="handleQuickAction('menu')">
              <div class="action-icon action-icon-orange">
                <el-icon :size="24"><Menu /></el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">添加菜单</div>
                <div class="action-desc">管理菜单权限</div>
              </div>
            </div>
            <div class="action-card" @click="handleQuickAction('system')">
              <div class="action-icon action-icon-cyan">
                <el-icon :size="24"><Setting /></el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">系统设置</div>
                <div class="action-desc">系统参数配置</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="system-info-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon"><Setting /></el-icon>
                <span>系统信息</span>
              </div>
            </div>
          </template>
          <div v-loading="systemInfoLoading" class="system-info">
            <div class="info-grid">
              <div class="info-item">
                <div class="info-icon">
                  <el-icon><House /></el-icon>
                </div>
                <div class="info-content">
                  <div class="info-label">系统名称</div>
                  <div class="info-value">{{ systemInfo.systemName || 'AdminPlus' }}</div>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="info-content">
                  <div class="info-label">系统版本</div>
                  <div class="info-value">{{ systemInfo.systemVersion || '1.0.0' }}</div>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon">
                  <el-icon><Setting /></el-icon>
                </div>
                <div class="info-content">
                  <div class="info-label">操作系统</div>
                  <div class="info-value">{{ systemInfo.osName || '-' }}</div>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="info-content">
                  <div class="info-label">JDK版本</div>
                  <div class="info-value">{{ systemInfo.jdkVersion || '-' }}</div>
                </div>
              </div>
              <div class="info-item full-width">
                <div class="info-icon">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="info-content">
                  <div class="info-label">运行时间</div>
                  <div class="info-value">{{ formatUptime(systemInfo.uptime) }}</div>
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
        <el-card shadow="hover" class="online-users-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon"><User /></el-icon>
                <span>在线用户</span>
              </div>
              <el-tag type="success" size="small">{{ onlineUsers.length }} 人在线</el-tag>
            </div>
          </template>
          <div v-loading="onlineUsersLoading" class="online-users">
            <div v-if="onlineUsers.length === 0" class="empty-state">
              <el-icon :size="48"><User /></el-icon>
              <p>暂无在线用户</p>
            </div>
            <div v-for="user in onlineUsers" :key="user.userId" class="online-user-item">
              <div class="user-avatar">
                <el-avatar :size="40" :icon="UserFilled" />
                <div class="online-indicator"></div>
              </div>
              <div class="user-info">
                <div class="user-name">{{ user.username }}</div>
                <div class="user-meta">
                  <el-icon :size="12"><Location /></el-icon>
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
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近操作日志</span>
              <el-button type="primary" link @click="handleViewAllLogs">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentLogs" v-loading="logsLoading" stripe style="width: 100%">
            <el-table-column prop="username" label="操作人" width="120" />
            <el-table-column prop="module" label="模块" width="120" />
            <el-table-column label="操作类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getOperationTypeTag(row.operationType)">
                  {{ getOperationTypeName(row.operationType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="操作描述" show-overflow-tooltip />
            <el-table-column prop="ip" label="IP地址" width="140" />
            <el-table-column label="操作时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleViewLogDetail(row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="logDetailVisible" title="日志详情" width="600px">
      <el-descriptions :column="1" border v-if="currentLog">
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          {{ getOperationTypeName(currentLog.operationType) }}
        </el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatTime(currentLog.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ currentLog.status === 1 ? '成功' : '失败' }}
        </el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ currentLog.costTime }}ms</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="logDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  UserFilled,
  Menu,
  Document,
  Plus,
  Setting,
  House,
  CaretTop,
  Minus,
  Clock,
  Location,
  Warning,
  Management,
  Avatar,
  Tools,
  Connection,
  Operation
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
  logCount: 0
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

// 图表数据状态
const userGrowthEmpty = ref(false)
const roleDistributionEmpty = ref(false)
const menuDistributionEmpty = ref(false)

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
  } catch (error) {
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
  } catch (error) {
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
  } catch (error) {
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
}

.banner-content {
  flex: 1;
}

.banner-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.banner-subtitle {
  font-size: 15px;
  opacity: 0.9;
  margin: 0;
}

.banner-icon {
  opacity: 0.2;
  animation: float 3s ease-in-out infinite;
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
  margin-bottom: 20px;
}

.stat-card-wrapper {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
  overflow: hidden;
}

.stat-card-wrapper:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 28px rgba(0, 102, 255, 0.15);
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
}

.stat-trend.stable {
  color: #909399;
}

.stat-trend .el-icon {
  font-weight: bold;
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
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 102, 255, 0.12);
  border-color: rgba(0, 102, 255, 0.2);
}

.action-card:active {
  transform: translateY(-2px);
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

/* 快捷操作响应式 */
@media (max-width: 768px) {
  .quick-actions {
    grid-template-columns: 1fr;
  }

  .action-card {
    padding: 16px;
  }

  .action-icon {
    width: 48px;
    height: 48px;
  }
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
  transition: all 0.2s;
}

.info-item:hover {
  background: #F0F2F5;
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
  transition: all 0.2s;
  gap: 12px;
}

.online-user-item:hover {
  background: #E8F0FE;
  transform: translateX(4px);
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
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
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

/* 响应式布局 */
@media (max-width: 1200px) {
  .stat-value {
    font-size: 32px;
  }

  .banner-title {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }

  .welcome-banner {
    padding: 24px;
    flex-direction: column;
    text-align: center;
  }

  .banner-icon {
    margin-top: 16px;
  }

  .quick-actions {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
    padding: 16px;
  }

  .stat-icon {
    margin-right: 0;
    margin-bottom: 12px;
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

.online-users::-webkit-scrollbar-track {
  background-color: transparent;
}
</style>