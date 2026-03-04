<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">日志管理</span>
          <div class="header-actions">
            <el-button
              v-if="selectedRows.length > 0"
              type="danger"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>
      </template>

      <div class="page-card-body">
        <!-- 日志类型选项卡 -->
        <el-tabs v-model="activeLogType" @tab-change="handleLogTypeChange">
          <el-tab-pane label="全部" name=""></el-tab-pane>
          <el-tab-pane label="操作日志" name="1"></el-tab-pane>
          <el-tab-pane label="登录日志" name="2"></el-tab-pane>
          <el-tab-pane label="系统日志" name="3"></el-tab-pane>
        </el-tabs>

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

        <!-- 日志列表容器 -->
        <div class="table-container">
          <el-table
            v-loading="loading"
            :data="logList"
            stripe
            border
            style="width: 100%"
            height="100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column
              type="selection"
              width="55"
            />
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
        </div>

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
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志类型">
          <el-tag :type="getLogTypeTag(currentLog.logType)">
            {{ getLogTypeDesc(currentLog.logType) }}
          </el-tag>
        </el-descriptions-item>
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
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { getLogList, getLogById, deleteLog, deleteLogsBatch } from '@/api/log'

// 当前激活的日志类型
const activeLogType = ref('')

// 查询表单
const queryForm = reactive({
  page: 1,
  size: 10,
  logType: null,
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

// 选中的行
const selectedRows = ref([])

// 详情对话框
const detailDialogVisible = ref(false)
const currentLog = ref({})

// 日志类型映射
const logTypeMap = {
  1: '操作日志',
  2: '登录日志',
  3: '系统日志'
}

const getLogTypeDesc = (type) => {
  return logTypeMap[type] || '未知'
}

const getLogTypeTag = (type) => {
  const tagMap = {
    1: 'primary',
    2: 'success',
    3: 'warning'
  }
  return tagMap[type] || ''
}

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
    // 响应拦截器已经返回 data 字段，res 就是 PageResultResp
    logList.value = res.records || []
    total.value = res.total || 0
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
  activeLogType.value = ''
  queryForm.page = 1
  queryForm.logType = null
  queryForm.username = ''
  queryForm.module = ''
  queryForm.operationType = null
  queryForm.status = null
  queryForm.startTime = ''
  queryForm.endTime = ''
  dateRange.value = []
  loadLogList()
}

// 日志类型切换
const handleLogTypeChange = (tabName) => {
  queryForm.logType = tabName ? parseInt(tabName) : null
  queryForm.page = 1
  loadLogList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条日志')
    return
  }
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条日志吗？`,
    '批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = selectedRows.value.map(row => row.id)
      await deleteLogsBatch(ids)
      ElMessage.success('删除成功')
      selectedRows.value = []
      loadLogList()
    } catch (error) {
      console.error('批量删除日志失败:', error)
    }
  }).catch(() => {})
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
    const log = await getLogById(row.id)
    currentLog.value = log
    detailDialogVisible.value = true
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
      await deleteLog(row.id)
      ElMessage.success('删除成功')
      loadLogList()
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
  margin-bottom: 16px;
  flex-shrink: 0; /* 防止搜索表单被压缩 */
}

.table-container {
  flex: 1;
  min-height: 0; /* 重要：允许缩小 */
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.pagination-container {
  flex-shrink: 0; /* 防止分页被压缩 */
  display: flex;
  justify-content: flex-end;
  padding: 12px 0;
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
