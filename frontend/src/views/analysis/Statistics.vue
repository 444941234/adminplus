<template>
  <div class="statistics-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据统计</span>
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
                <component :is="stat.icon" />
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
import * as echarts from 'echarts'

const statsData = ref([
  {
    title: '总用户数',
    value: '1,234',
    icon: 'User',
    color: '#409EFF'
  },
  {
    title: '今日访问',
    value: '567',
    icon: 'View',
    color: '#67C23A'
  },
  {
    title: '活跃用户',
    value: '890',
    icon: 'TrendCharts',
    color: '#E6A23C'
  },
  {
    title: '新增注册',
    value: '45',
    icon: 'Plus',
    color: '#F56C6C'
  }
])

const userChartRef = ref()
const visitChartRef = ref()
let userChart = null
let visitChart = null

// 初始化用户增长图表
const initUserChart = () => {
  if (!userChartRef.value) return
  
  userChart = echarts.init(userChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['新增用户', '活跃用户']
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
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        data: [120, 132, 101, 134, 90, 230, 210],
        smooth: true,
        lineStyle: {
          color: '#5470C6'
        }
      },
      {
        name: '活跃用户',
        type: 'line',
        data: [220, 182, 191, 234, 290, 330, 310],
        smooth: true,
        lineStyle: {
          color: '#91CC75'
        }
      }
    ]
  }
  
  userChart.setOption(option)
}

// 初始化访问量图表
const initVisitChart = () => {
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
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '访问量',
        type: 'bar',
        data: [320, 332, 301, 334, 390, 330, 320],
        itemStyle: {
          color: '#91CC75'
        }
      }
    ]
  }
  
  visitChart.setOption(option)
}

// 响应式调整图表大小
const resizeCharts = () => {
  userChart?.resize()
  visitChart?.resize()
}

onMounted(() => {
  initUserChart()
  initVisitChart()
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
  padding: 20px;
}

.card-header {
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