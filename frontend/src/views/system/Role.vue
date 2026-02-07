<template>
  <div class="role-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button
            type="primary"
            @click="handleAdd"
          >
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

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
          prop="code"
          label="角色编码"
          width="150"
        />
        <el-table-column
          prop="name"
          label="角色名称"
          width="150"
        />
        <el-table-column
          prop="description"
          label="描述"
        />
        <el-table-column
          label="数据权限"
          width="120"
        >
          <template #default="{ row }">
            <el-tag :type="getDataScopeType(row.dataScope)">
              {{ getDataScopeText(row.dataScope) }}
            </el-tag>
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
          prop="sortOrder"
          label="排序"
          width="80"
        />
        <el-table-column
          label="操作"
          width="280"
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
              type="warning"
              size="small"
              @click="handleAssignMenu(row)"
            >
              分配权限
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
        label-width="100px"
      >
        <el-form-item
          label="角色编码"
          prop="code"
        >
          <el-input
            v-model="form.code"
            placeholder="请输入角色编码（如 ROLE_ADMIN）"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item
          label="角色名称"
          prop="name"
        >
          <el-input
            v-model="form.name"
            placeholder="请输入角色名称"
          />
        </el-form-item>
        <el-form-item
          label="描述"
          prop="description"
        >
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item
          label="数据权限"
          prop="dataScope"
        >
          <el-select
            v-model="form.dataScope"
            placeholder="请选择数据权限"
          >
            <el-option
              label="全部数据"
              :value="1"
            />
            <el-option
              label="本部门"
              :value="2"
            />
            <el-option
              label="本部门及以下"
              :value="3"
            />
            <el-option
              label="仅本人"
              :value="4"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="状态"
          prop="status"
        >
          <el-radio-group v-model="form.status">
            <el-radio :value="1">
              正常
            </el-radio>
            <el-radio :value="0">
              禁用
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="排序"
          prop="sortOrder"
        >
          <el-input-number
            v-model="form.sortOrder"
            :min="0"
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

    <!-- 分配权限对话框 -->
    <el-dialog
      v-model="menuDialogVisible"
      title="分配菜单权限"
      width="500px"
      @close="handleMenuDialogClose"
    >
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        show-checkbox
        default-expand-all
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="menuSubmitLoading"
          @click="handleMenuSubmit"
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
import { Plus } from '@element-plus/icons-vue'
import { getRoleList, createRole, updateRole, deleteRole, assignMenus, getRoleMenuIds } from '@/api/role'
import { getMenuTree } from '@/api/menu'
import { useConfirm } from '@/composables/useConfirm'

const loading = ref(false)
const submitLoading = ref(false)
const menuSubmitLoading = ref(false)
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const isEdit = ref(false)
const tableData = ref([])
const menuTreeData = ref([])
const currentRoleId = ref(null)

const formRef = ref()
const menuTreeRef = ref()
const form = reactive({
  id: null,
  code: '',
  name: '',
  description: '',
  dataScope: 1,
  status: 1,
  sortOrder: 0
})

const rules = {
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

// 确认操作
const confirmDelete = useConfirm({
  message: '确定要删除该角色吗？',
  type: 'warning'
})

const getDataScopeType = (scope) => {
  const types = { 1: '', 2: 'warning', 3: 'info', 4: 'danger' }
  return types[scope] || ''
}

const getDataScopeText = (scope) => {
  const texts = { 1: '全部数据', 2: '本部门', 3: '本部门及以下', 4: '仅本人' }
  return texts[scope] || '未知'
}

const getData = async () => {
  loading.value = true
  try {
    const data = await getRoleList()
    tableData.value = data.records
  } catch {
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const getMenuData = async () => {
  try {
    menuTreeData.value = await getMenuTree()
  } catch {
    ElMessage.error('获取菜单树失败')
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    description: '',
    dataScope: 1,
    status: 1,
    sortOrder: 0
  })
}

const handleSubmit = async () => {
  await formRef.value.validate()

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateRole(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
      ElMessage.success('创建��功')
    }
    dialogVisible.value = false
    getData()
  } catch {
    // 错误已在验证或 API 中处理
  } finally {
    submitLoading.value = false
  }
}

const handleAssignMenu = async (row) => {
  currentRoleId.value = row.id
  menuDialogVisible.value = true

  // 加载菜单树
  await getMenuData()

  // 加载角色已有的菜单
  try {
    const menuIds = await getRoleMenuIds(row.id)
    menuTreeRef.value?.setCheckedKeys(menuIds)
  } catch {
    ElMessage.error('获取角色菜单失败')
  }
}

const handleMenuDialogClose = () => {
  menuTreeRef.value?.setCheckedKeys([])
}

const handleMenuSubmit = async () => {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
  const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]

  menuSubmitLoading.value = true
  try {
    await assignMenus(currentRoleId.value, allCheckedKeys)
    ElMessage.success('分配权限成功')
    menuDialogVisible.value = false
  } catch {
    ElMessage.error('分配权限失败')
  } finally {
    menuSubmitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await confirmDelete()
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    getData()
  } catch {
    // 取消操作
  }
}

onMounted(() => {
  getData()
})
</script>

<style scoped>
.role-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>