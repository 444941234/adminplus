<template>
  <div class="report-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表管理</span>
          <div class="header-actions">
            <el-button type="primary">
              <el-icon><Plus /></el-icon>
              新建报表
            </el-button>
          </div>
        </div>
      </template>

      <!-- 报表列表 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
      >
        <el-table-column
          prop="name"
          label="报表名称"
          width="200"
        />
        <el-table-column
          prop="type"
          label="报表类型"
          width="120"
        >
          <template #default="{ row }">
            <el-tag>{{ getReportTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="描述"
        />
        <el-table-column
          prop="createdTime"
          label="创建时间"
          width="180"
        />
        <el-table-column
          prop="status"
          label="状态"
          width="100"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
            >
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="250"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getData"
          @current-change="getData"
        />
      </div>
    </el-card>

    <!-- 报表查看对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="报表详情"
      width="90%"
      fullscreen
    >
      <div
        v-if="currentReport"
        class="report-detail"
      >
        <div class="report-header">
          <h2>{{ currentReport.name }}</h2>
          <div class="report-meta">
            <span>类型：{{ getReportTypeText(currentReport.type) }}</span>
            <span>创建时间：{{ currentReport.createdTime }}</span>
          </div>
        </div>
        
        <div class="report-content">
          <!-- 这里可以放置具体的报表内容 -->
          <el-card header="数据概览">
            <div
              ref="reportChartRef"
              class="chart-container"
            />
          </el-card>
          
          <el-card
            header="详细数据"
            class="mt-20"
          >
            <el-table
              :data="reportTableData"
              border
            >
              <el-table-column
                prop="date"
                label="日期"
                width="120"
              />
              <el-table-column
                prop="userCount"
                label="用户数"
                width="100"
              />
              <el-table-column
                prop="visitCount"
                label="访问量"
                width="100"
              />
              <el-table-column
                prop="orderCount"
                label="订单数"
                width="100"
              />
              <el-table-column
                prop="revenue"
                label="收入"
                width="120"
              />
            </el-table>
          </el-card>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="viewDialogVisible = false">
          关闭
        </el-button>
        <el-button type="primary">
          导出报表
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const viewDialogVisible = ref(false)
const currentReport = ref(null)
const reportChartRef = ref()

// 报表类型映射
const reportTypes = {
  1: '用户统计',
  2: '访问统计',
  3: '业务统计',
  4: '财务统计'
}

// 模拟报表表格数据
const reportTableData = ref([
  { date: '2026-02-01', userCount: 120, visitCount: 450, orderCount: 80, revenue: '¥12,500' },
  { date: '2026-02-02', userCount: 135, visitCount: 520, orderCount: 95, revenue: '¥14,800' },
  { date: '2026-02-03', userCount: 110, visitCount: 380, orderCount: 70, revenue: '¥10,200' },
  { date: '2026-02-04', userCount: 150, visitCount: 600, orderCount: 110, revenue: '¥18,500' },
  { date: '2026-02-05', userCount: 125, visitCount: 480, orderCount: 85, revenue: '¥13,200' }
])

// 获取报表类型文本
const getReportTypeText = (type) => {
  return reportTypes[type] || '未知类型'
}

// 获取数据
const getData = async () => {
  loading.value = true
  try {
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        name: '用户增长报表',
        type: 1,
        description: '统计用户注册和活跃情况',
        createdTime: '2026-02-01 10:00:00',
        status: 1
      },
      {
        id: 2,
        name: '访问量统计报表',
        type: 2,
        description: '统计系统访问量和页面浏览情况',
        createdTime: '2026-02-02 14:30:00',
        status: 1
      },
      {
        id: 3,
        name: '业务数据报表',
        type: 3,
        description: '统计业务订单和转化情况',
        createdTime: '2026-02-03 09:15:00',
        status: 1
      }
    ]
    total.value = 3
  } catch {
    ElMessage.error('获取报表列表失败')
  } finally {
    loading.value = false
  }
}

// 查看报表
const handleView = (row) => {
  currentReport.value = row
  viewDialogVisible.value = true
  
  // 初始化图表
  nextTick(() => {
    initReportChart()
  })
}

// 编辑报表
const handleEdit = (row) => {
  ElMessage.info(`编辑报表: ${row.name}`)
}

// 删除报表
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除报表"${row.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('删除成功')
    getData()
  } catch {
    // 用户取消操作
  }
}

// 初始化报表图表
const initReportChart = () => {
  if (!reportChartRef.value) return
  
  const chart = echarts.init(reportChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['用户数', '访问量', '订单数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: reportTableData.value.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '用户数',
        type: 'line',
        data: reportTableData.value.map(item => item.userCount),
        smooth: true,
        lineStyle: {
          color: '#5470C6'
        }
      },
      {
        name: '访问量',
        type: 'line',
        data: reportTableData.value.map(item => item.visitCount),
        smooth: true,
        lineStyle: {
          color: '#91CC75'
        }
      },
      {
        name: '订单数',
        type: 'line',
        data: reportTableData.value.map(item => item.orderCount),
        smooth: true,
        lineStyle: {
          color: '#FAC858'
        }
      }
    ]
  }
  
  chart.setOption(option)
}

onMounted(() => {
  getData()
})
</script>

<style scoped>
.report-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.report-detail {
  height: 100%;
}

.report-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.report-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.report-meta {
  display: flex;
  gap: 20px;
  color: #909399;
}

.report-content {
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.chart-container {
  height: 400px;
  width: 100%;
}

.mt-20 {
  margin-top: 20px;
}
</style>