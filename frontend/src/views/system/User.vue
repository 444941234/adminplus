<template>
  <div class="user-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button
            type="primary"
            @click="handleAdd"
          >
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
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
            v-model="queryForm.keyword"
            placeholder="请输入用户名"
            clearable
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

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
      >
        <el-table-column
          prop="id"
          label="ID"
          width="80"
        />
        <el-table-column
          prop="username"
          label="用户名"
          width="150"
        />
        <el-table-column
          prop="nickname"
          label="昵称"
          width="150"
        />
        <el-table-column
          prop="email"
          label="邮箱"
          width="200"
        />
        <el-table-column
          prop="phone"
          label="手机号"
          width="150"
        />
        <el-table-column
          label="角色"
          width="200"
        >
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roles"
              :key="role"
              size="small"
              style="margin-right: 5px"
            >
              {{ role }}
            </el-tag>
            <span
              v-if="!row.roles || row.roles.length === 0"
              style="color: #909399"
            >无</span>
          </template>
        </el-table-column>
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
          label="操作"
          width="320"
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
              type="info"
              size="small"
              @click="handleAssignRole(row)"
            >
              分配角色
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="handleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
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
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="getData"
        @current-change="getData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item
          label="用户名"
          prop="username"
        >
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item
          v-if="!isEdit"
          label="密码"
          prop="password"
        >
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-form-item
          label="昵称"
          prop="nickname"
        >
          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
          />
        </el-form-item>
        <el-form-item
          label="邮箱"
          prop="email"
        >
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>
        <el-form-item
          label="手机号"
          prop="phone"
        >
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
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

    <!-- 分配角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="400px"
      @close="handleRoleDialogClose"
    >
      <el-checkbox-group v-model="selectedRoles">
        <el-checkbox
          v-for="role in allRoles"
          :key="role.id"
          :label="role.id"
        >
          {{ role.name }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="roleSubmitLoading"
          @click="handleRoleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, updateUserStatus, assignRoles, getUserRoleIds } from '@/api/user'
import { getRoleList } from '@/api/role'
import { useConfirm } from '@/composables/useConfirm'

const loading = ref(false)
const submitLoading = ref(false)
const roleSubmitLoading = ref(false)
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const tableData = ref([])
const total = ref(0)
const allRoles = ref([])
const selectedRoles = ref([])
const currentUserId = ref(null)

const queryForm = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const formRef = ref()
const form = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

// 确认操作
const confirmDelete = useConfirm({
  message: '确定要删除该用户吗？',
  type: 'warning'
})

const confirmStatus = useConfirm({
  message: '确定要执行此操作吗？',
  type: 'warning'
})

const getData = async () => {
  loading.value = true
  try {
    const data = await getUserList(queryForm)
    tableData.value = data.records
    total.value = data.total
  } catch {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const getRoles = async () => {
  try {
    const data = await getRoleList()
    allRoles.value = data.records
  } catch {
    ElMessage.error('获取角色列表失败')
  }
}

const handleSearch = () => {
  queryForm.page = 1
  getData()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.page = 1
  getData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: ''
  })
}

const handleSubmit = async () => {
  await formRef.value.validate()

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateUser(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    getData()
  } catch {
    // 错误已在验证或 API 中处理
  } finally {
    submitLoading.value = false
  }
}

const handleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  try {
    await confirmStatus(`确定要${action}该用户吗？`)
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    getData()
  } catch {
    // 取消操作
  }
}

const handleDelete = async (row) => {
  try {
    await confirmDelete()
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    getData()
  } catch {
    // 取消操作
  }
}

const handleAssignRole = async (row) => {
  currentUserId.value = row.id
  roleDialogVisible.value = true

  // 加载用户已有的角色
  try {
    const roleIds = await getUserRoleIds(row.id)
    selectedRoles.value = roleIds
  } catch {
    ElMessage.error('获取用户角色失败')
  }
}

const handleRoleDialogClose = () => {
  selectedRoles.value = []
}

const handleRoleSubmit = async () => {
  roleSubmitLoading.value = true
  try {
    await assignRoles(currentUserId.value, selectedRoles.value)
    ElMessage.success('分配角色成功')
    roleDialogVisible.value = false
    getData()
  } catch {
    ElMessage.error('分配角色失败')
  } finally {
    roleSubmitLoading.value = false
  }
}

onMounted(() => {
  getData()
  getRoles()
})
</script>

<style scoped>
.user-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>