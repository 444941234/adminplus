<template>
  <div class="config-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>参数配置</span>
          <div class="header-actions">
            <el-button
              type="primary"
              @click="handleAdd"
            >
              <el-icon><Plus /></el-icon>
              新增配置
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
      >
        <el-table-column
          prop="key"
          label="配置键"
          width="200"
        />
        <el-table-column
          prop="value"
          label="配置值"
        />
        <el-table-column
          prop="description"
          label="描述"
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
          width="200"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 配置编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="配置键"
          prop="key"
        >
          <el-input
            v-model="form.key"
            placeholder="请输入配置键"
          />
        </el-form-item>
        <el-form-item
          label="配置值"
          prop="value"
        >
          <el-input
            v-model="form.value"
            placeholder="请输入配置值"
          />
        </el-form-item>
        <el-form-item
          label="描述"
          prop="description"
        >
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入配置描述"
          />
        </el-form-item>
        <el-form-item
          label="状态"
          prop="status"
        >
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const isEdit = ref(false)

const formRef = ref()
const form = reactive({
  key: '',
  value: '',
  description: '',
  status: 1
})

const rules = {
  key: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  value: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
}

// 获取数据
const getData = async () => {
  loading.value = true
  try {
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        key: 'system.name',
        value: 'AdminPlus',
        description: '系统名称',
        status: 1
      },
      {
        id: 2,
        key: 'system.version',
        value: '1.0.0',
        description: '系统版本',
        status: 1
      }
    ]
  } catch {
    ElMessage.error('获取配置列表失败')
  } finally {
    loading.value = false
  }
}

// 新增配置
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增配置'
  form.key = ''
  form.value = ''
  form.description = ''
  form.status = 1
  dialogVisible.value = true
}

// 编辑配置
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑配置'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除配置
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该配置吗？',
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

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()

  submitLoading.value = true
  try {
    // 模拟保存操作
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    getData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getData()
})
</script>

<style scoped>
.config-page {
  min-height: calc(100vh - 84px);
}

/* 卡片样式 */
.config-page :deep(.el-card) {
  border-radius: $radius-md;
}

.config-page :deep(.el-card__header) {
  padding: $spacing-md $spacing-lg;
  border-bottom: 1px solid $border-light;
}

.config-page :deep(.el-card__body) {
  padding: $spacing-lg;
}

/* 表格容器 */
.table-container {
  margin-bottom: $spacing-lg;
}

.table-container :deep(.el-table) {
  border-radius: $radius-md;
  overflow: hidden;
}

.table-container :deep(.el-table th) {
  background-color: $bg-light;
  font-weight: 600;
  color: $text-regular;
}

.table-container :deep(.el-table__body-wrapper) {
  border-radius: 0 0 $radius-md $radius-md;
}

/* 配置键样式 */
.config-key {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: $font-size-sm;
  color: $primary-color;
  background-color: rgba($primary-color, 0.1);
  padding: 2px 8px;
  border-radius: $radius-sm;
}

/* 配置值样式 */
.config-value {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: $font-size-sm;
  color: $text-regular;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 表单样式 */
.config-form {
  padding: $spacing-sm 0;
}

/* 响应式样式 */
@media (max-width: 992px) {
  .config-page {
    padding: $spacing-md;
  }
}

@media (max-width: 768px) {
  .config-page {
    padding: $spacing-base;
  }

  .table-container {
    margin-bottom: $spacing-md;
  }

  /* 在小屏幕上让配置值显示完整 */
  .config-value {
    max-width: 150px;
  }
}

@media (max-width: 576px) {
  .config-page {
    padding: $spacing-base $spacing-sm;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-base;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

/* 滚动条样式优化 */
:deep(.el-table__body-wrapper)::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb {
  background-color: $border-dark;
  border-radius: 3px;

  &:hover {
    background-color: color.scale($border-dark, $lightness: -10%);
  }
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar-track {
  background-color: transparent;
}

/* 对话框样式 */
:deep(.el-dialog__body) {
  padding: $spacing-lg;
}

/* 按钮组间距 */
.header-actions {
  display: flex;
  gap: $spacing-sm;
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>