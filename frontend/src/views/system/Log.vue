<template>
  <div class="log-page">
    <el-card class="log-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">日志管理</span>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form
        :inline="true"
        :model="queryForm"
        class="search-form"
      >
        <el-form-item label="用户名">
          <el-input
            v-model="queryForm.username"
            clearable
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item label="操作模块">
          <el-input
            v-model="queryForm.module"
            clearable
            placeholder="请输入模块名称"
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select
            v-model="queryForm.operationType"
            clearable
            placeholder="请选择操作类型"
          >
            <el-option label="查询" :value="1" />
            <el-option label="新增" :value="2" />
            <el-option label="修改" :value="3" />
            <el-option label="删除" :value="4" />
            <el-option label="导出" :value="5" />
            <el-option label="导入" :value="6" />
            <el-option label="其他" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryForm.status"
            clearable
            placeholder="请选择状态"
          >
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="handleSearch"
          >
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 日志列表 -->
      <el-table
        v-loading="loading"
        :data="logList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column
          prop="username"
          label="操作人"
          width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="module"
          label="操作模块"
          width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="operationType"
          label="操作类型"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)">
              {{ getOperationTypeDesc(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="操作描述"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="ip"
          label="IP地址"
          width="140"
          show-overflow-tooltip
        />
        <el-table-column
          prop="costTime"
          label="执行时长"
          width="100"
        >
          <template #default="{ row }">
            {{ row.costTime }}ms
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
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
          prop="createTime"
          label="操作时间"
          width="180"
        />
        <el-table-column
          label="操作"
          width="150"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作人">
          {{ currentLog.username }}
        </el-descriptions-item>
        <el-descriptions-item label="操作模块">
          {{ currentLog.module }}
        </el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTag(currentLog.operationType)">
            {{ getOperationTypeDesc(currentLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'">
            {{ currentLog.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址" :span="2">
          {{ currentLog.ip }}
        </el-descriptions-item>
        <el-descriptions-item label="操作地点" :span="2">
          {{ currentLog.location || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">
          {{ currentLog.method || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="执行时长">
          {{ currentLog.costTime }}ms
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">
          {{ currentLog.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">
          {{ currentLog.description }}
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="params-content">{{ currentLog.params || '-' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog.errorMsg" label="异常信息" :span="2">
          <pre class="error-content">{{ currentLog.errorMsg }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getLogList, getLogById, deleteLog } from '@/api/log'

// 查询表单
const queryForm = reactive({
  page: 1,
  size: 10,
  username: '',
  module: '',
  operationType: null,
  status: null,
  startTime: '',
  endTime: ''
})

// 日期范围
const dateRange = ref([])

// 列表数据
const loading = ref(false)
const logList = ref([])
const total = ref(0)

// 详情对话框
const detailDialogVisible = ref(false)
const currentLog = ref({})

// 操作类型映射
const operationTypeMap = {
  1: '查询',
  2: '新增',
  3: '修改',
  4: '删除',
  5: '导出',
  6: '导入',
  7: '其他'
}

const getOperationTypeDesc = (type) => {
  return operationTypeMap[type] || '未知'
}

const getOperationTypeTag = (type) => {
  const tagMap = {
    1: 'info',
    2: 'success',
    3: 'warning',
    4: 'danger',
    5: 'info',
    6: 'success',
    7: ''
  }
  return tagMap[type] || ''
}

// 加载日志列表
const loadLogList = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    // 移除空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })

    const res = await getLogList(params)
    if (res.code === 200) {
      logList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载日志列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryForm.page = 1
  loadLogList()
}

// 重置
const handleReset = () => {
  queryForm.page = 1
  queryForm.username = ''
  queryForm.module = ''
  queryForm.operationType = null
  queryForm.status = null
  queryForm.startTime = ''
  queryForm.endTime = ''
  dateRange.value = []
  loadLogList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  queryForm.size = size
  loadLogList()
}

// 页码变化
const handleCurrentChange = (page) => {
  queryForm.page = page
  loadLogList()
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await getLogById(row.id)
    if (res.code === 200) {
      currentLog.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取日志详情失败:', error)
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该日志吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteLog(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadLogList()
      }
    } catch (error) {
      console.error('删除日志失败:', error)
    }
  }).catch(() => {})
}

// 页面加载
onMounted(() => {
  loadLogList()
})
</script>

<style scoped>
.log-page {
  padding: 20px;
}

.log-card {
  min-height: calc(100vh - 120px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.params-content,
.error-content {
  margin: 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  max-height: 200px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.error-content {
  background-color: #fef0f0;
  color: #f56c6c;
}
</style>