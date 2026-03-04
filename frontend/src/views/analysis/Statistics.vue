<template>
  <div class="statistics-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据统计</span>
          <el-button type="primary" :icon="Refresh" circle @click="loadData" :loading="loading" />
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-card
          v-for="stat in statsData"
          :key="stat.title"
          shadow="hover"
          class="stat-card"
        >
          <div class="stat-content">
            <div
              class="stat-icon"
              :style="{ background: stat.color }"
            >
              <el-icon :size="24">
                <component :is="getIconComponent(stat.icon)" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stat.value }}
              </div>
              <div class="stat-title">
                {{ stat.title }}
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 图表区域 -->
      <div class="charts-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card header="用户增长趋势">
              <div
                ref="userChartRef"
                class="chart-container"
              />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card header="访问量统计">
              <div
                ref="visitChartRef"
                class="chart-container"
              />
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getIconComponent } from '@/constants/icons'
import * as echarts from 'echarts'
import { getStatistics, getVisitTrend } from '@/api/dashboard'

const loading = ref(false)

const statsData = ref([
  {
    title: '总用户数',
    value: '0',
    icon: 'User',
    color: '#409EFF'
  },
  {
    title: '今日访问',
    value: '0',
    icon: 'View',
    color: '#67C23A'
  },
  {
    title: '活跃用户',
    value: '0',
    icon: 'TrendCharts',
    color: '#E6A23C'
  },
  {
    title: '今日新增',
    value: '0',
    icon: 'Plus',
    color: '#F56C6C'
  }
])

const userChartRef = ref()
const visitChartRef = ref()
let userChart = null
let visitChart = null

// 加载统计数据
const loadData = async () => {
  loading.value = true
  try {
    const [statsResp, visitResp] = await Promise.all([
      getStatistics(),
      getVisitTrend()
    ])

    console.log('统计数据响应:', statsResp)
    console.log('访问量数据响应:', visitResp)

    // 响应拦截器已返回 data 部分，直接使用
    if (statsResp) {
      // 使用模块中的 ref 变量，而非 API 返回的普通对象
      statsData.value = [
        {
          title: '总用户数',
          value: formatNumber(statsResp.totalUsers || 0),
          icon: 'User',
          color: '#409EFF'
        },
        {
          title: '今日访问',
          value: formatNumber(statsResp.todayVisits || 0),
          icon: 'View',
          color: '#67C23A'
        },
        {
          title: '活跃用户',
          value: formatNumber(statsResp.activeUsers || 0),
          icon: 'TrendCharts',
          color: '#E6A23C'
        },
        {
          title: '今日新增',
          value: formatNumber(statsResp.todayNewUsers || 0),
          icon: 'Plus',
          color: '#F56C6C'
        }
      ]

      // 更新用户增长图表
      if (statsResp.userGrowthData) {
        updateUserChart(statsResp.userGrowthData)
      }
    }

    if (visitResp) {
      // 更新访问量图表
      updateVisitChart(visitResp)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 格式化数字
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num.toLocaleString()
}

// 初始化用户增长图表
const initUserChart = (data) => {
  if (!userChartRef.value) return

  userChart = echarts.init(userChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['新增用户']
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
      data: data?.labels || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        data: data?.values || [],
        smooth: true,
        lineStyle: {
          color: '#5470C6'
        },
        areaStyle: {
          color: 'rgba(84, 112, 198, 0.1)'
        }
      }
    ]
  }

  userChart.setOption(option)
}

// 更新用户增长图表
const updateUserChart = (data) => {
  if (userChart) {
    userChart.setOption({
      xAxis: {
        data: data.labels || []
      },
      series: [
        {
          data: data.values || []
        }
      ]
    })
  } else {
    initUserChart(data)
  }
}

// 初始化访问量图表
const initVisitChart = (data) => {
  if (!visitChartRef.value) return

  visitChart = echarts.init(visitChartRef.value)
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
      data: data?.labels || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '访问量',
        type: 'bar',
        data: data?.values || [],
        itemStyle: {
          color: '#91CC75'
        }
      }
    ]
  }

  visitChart.setOption(option)
}

// 更新访问量图表
const updateVisitChart = (data) => {
  if (visitChart) {
    visitChart.setOption({
      xAxis: {
        data: data.labels || []
      },
      series: [
        {
          data: data.values || []
        }
      ]
    })
  } else {
    initVisitChart(data)
  }
}

// 响应式调整图表大小
const resizeCharts = () => {
  userChart?.resize()
  visitChart?.resize()
}

onMounted(async () => {
  // 等待数据加载后初始化图表
  await loadData()

  // 如果图表还未初始化，则初始化
  if (!userChart && userChartRef.value) {
    initUserChart({ labels: [], values: [] })
  }
  if (!visitChart && visitChartRef.value) {
    initVisitChart({ labels: [], values: [] })
  }

  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  userChart?.dispose()
  visitChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
})
</script>

<style scoped>
.statistics-page {
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 10px;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
}

.charts-section {
  margin-top: 30px;
}

.chart-container {
  height: 300px;
  width: 100%;
}
</style>