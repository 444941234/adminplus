<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getDictList, createDict, updateDict, deleteDict, updateDictStatus } from '@/api/dict'
import { debounce } from '@/utils/debounce'
import { useConfirm } from '@/composables/useConfirm'

const router = useRouter()

// 数据
const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formData = reactive({
  id: null,
  dictType: '',
  dictName: '',
  status: 1,
  remark: ''
})

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

// 分页信息
const total = ref(0)

// 表单验证规则
const rules = {
  dictType: [
    { required: true, message: '请输入字典类型', trigger: 'blur' }
  ],
  dictName: [
    { required: true, message: '请输入字典名称', trigger: 'blur' }
  ]
}

const formRef = ref(null)

// 确认操作
const confirmDelete = useConfirm({
  message: '确定要删除该字典吗？',
  type: 'warning'
})

const confirmStatus = useConfirm({
  message: '确定要执行此操作吗？',
  type: 'warning'
})

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const data = await getDictList(queryParams)
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索（使用防抖）
const searchDebounced = debounce(() => {
  queryParams.page = 1
  getList()
}, 300)

const handleSearch = () => {
  searchDebounced()
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.page = 1
  getList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增字典'
  Object.assign(formData, {
    id: null,
    dictType: '',
    dictName: '',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑字典'
  Object.assign(formData, {
    id: row.id,
    dictType: row.dictType,
    dictName: row.dictName,
    status: row.status,
    remark: row.remark
  })
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (formData.id) {
      await updateDict(formData.id, {
        dictName: formData.dictName,
        status: formData.status,
        remark: formData.remark
      })
      ElMessage.success('更新成功')
    } else {
      await createDict(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await confirmDelete()
    await deleteDict(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  try {
    await confirmStatus(`确定要${action}该字典吗？`)
    await updateDictStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    getList()
  } catch {
    // 取消操作
  }
}

// 分页
const handlePageChange = (page) => {
  queryParams.page = page
  getList()
}

const handleSizeChange = (size) => {
  queryParams.size = size
  queryParams.page = 1
  getList()
}

// 查看字典项
const handleViewItems = (row) => {
  router.push({
    name: 'DictItem',
    params: { dictId: row.id },
    query: { dictType: row.dictType, dictName: row.dictName }
  })
}

onMounted(() => {
  getList()
})
</script>

<template>
  <div class="dict-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>字典管理</span>
          <el-button
            type="primary"
            @click="handleAdd"
          >
            <el-icon><Plus /></el-icon>
            新增字典
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form
        :inline="true"
        :model="queryParams"
      >
        <el-form-item label="关键字">
          <el-input
            v-model="queryParams.keyword"
            placeholder="字典类型/名称"
            clearable
            @input="handleSearch"
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

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
      >
        <el-table-column
          prop="id"
          label="ID"
          width="80"
        />
        <el-table-column
          prop="dictType"
          label="字典类型"
          width="150"
        />
        <el-table-column
          prop="dictName"
          label="字典名称"
          width="150"
        />
        <el-table-column
          prop="remark"
          label="备注"
        />
        <el-table-column
          label="状态"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
        />
        <el-table-column
          label="操作"
          width="320"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewItems(row)"
            >
              字典项
            </el-button>
            <el-button
              v-auth="'dict:edit'"
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-auth="'dict:edit'"
              type="warning"
              size="small"
              @click="handleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              v-auth="'dict:delete'"
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
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </el-card>

    <!-- 对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="字典类型"
          prop="dictType"
        >
          <el-input
            v-model="formData.dictType"
            placeholder="请输入字典类型"
            :disabled="!!formData.id"
          />
        </el-form-item>
        <el-form-item
          label="字典名称"
          prop="dictName"
        >
          <el-input
            v-model="formData.dictName"
            placeholder="请输入字典名称"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">
              正常
            </el-radio>
            <el-radio :label="0">
              禁用
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dict-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>