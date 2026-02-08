<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card v-loading="loading" shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.userCount.toLocaleString() }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card v-loading="loading" shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="40"><UserFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.roleCount.toLocaleString() }}</div>
              <div class="stat-label">角色总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card v-loading="loading" shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="40"><Menu /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.menuCount.toLocaleString() }}</div>
              <div class="stat-label">菜单总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card v-loading="loading" shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="40"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.logCount.toLocaleString() }}</div>
              <div class="stat-label">日志总数</div>
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
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" :icon="Plus" @click="handleQuickAction('user')">
              添加用户
            </el-button>
            <el-button type="success" :icon="Plus" @click="handleQuickAction('role')">
              添加角色
            </el-button>
            <el-button type="warning" :icon="Plus" @click="handleQuickAction('menu')">
              添加菜单
            </el-button>
            <el-button type="info" :icon="Setting" @click="handleQuickAction('system')">
              系统设置
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <div v-loading="systemInfoLoading" class="system-info">
            <div class="info-item">
              <span class="info-label">系统名称</span>
              <span class="info-value">{{ systemInfo.systemName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">系统版本</span>
              <span class="info-value">{{ systemInfo.systemVersion }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">操作系统</span>
              <span class="info-value">{{ systemInfo.osName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">JDK版本</span>
              <span class="info-value">{{ systemInfo.jdkVersion }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">内存使用</span>
              <span class="info-value">{{ systemInfo.usedMemory }}MB / {{ systemInfo.totalMemory }}MB</span>
            </div>
            <div class="info-item">
              <span class="info-label">数据库</span>
              <span class="info-value">{{ systemInfo.databaseType }} {{ systemInfo.databaseVersion }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">运行时间</span>
              <span class="info-value">{{ formatUptime(systemInfo.uptime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>在线用户</span>
              <el-tag size="small">{{ onlineUsers.length }}</el-tag>
            </div>
          </template>
          <div v-loading="onlineUsersLoading" class="online-users">
            <div v-if="onlineUsers.length === 0" class="empty-text">
              暂无在线用户
            </div>
            <div v-for="user in onlineUsers" :key="user.userId" class="online-user-item">
              <el-avatar :size="32" :icon="UserFilled" />
              <div class="user-info">
                <div class="user-name">{{ user.username }}</div>
                <div class="user-ip">{{ user.ip }}</div>
              </div>
              <el-button
                type="danger"
                size="small"
                link
                @click="handleForceOffline(user)"
              >
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
  Setting
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
              { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
              { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
            ])
          },
          lineStyle: {
            color: '#667eea',
            width: 3
          },
          itemStyle: {
            color: '#667eea'
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
            name: label
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
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
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
  await fetchStats()
  await fetchUserGrowth()
  await fetchRoleDistribution()
  await fetchMenuDistribution()
  await fetchRecentLogs()
  await fetchSystemInfo()
  await fetchOnlineUsers()
  initCharts()
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
  padding: 20px;
}

.stat-card-wrapper {
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card-wrapper:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stat-card {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #333;
}

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.quick-actions .el-button {
  width: 100%;
  height: 50px;
  font-size: 14px;
  transition: all 0.3s;
}

.quick-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.system-info {
  padding: 10px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: #666;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.online-users {
  max-height: 350px;
  overflow-y: auto;
}

.empty-text {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

.online-user-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.online-user-item:hover {
  background-color: #f9f9f9;
}

.online-user-item:last-child {
  border-bottom: none;
}

.user-info {
  flex: 1;
  margin-left: 12px;
}

.user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.user-ip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .stat-value {
    font-size: 28px;
  }
}

@media (max-width: 768px) {
  .quick-actions {
    grid-template-columns: 1fr;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
  }

  .stat-icon {
    margin-right: 0;
    margin-bottom: 12px;
  }
}

/* 滚动条样式 */
.online-users::-webkit-scrollbar {
  width: 6px;
}

.online-users::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.online-users::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}
</style>