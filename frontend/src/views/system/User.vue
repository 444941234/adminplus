<template>
  <div class="user-page">
    <el-row :gutter="20">
      <!-- 左侧部门树 -->
      <el-col :span="5">
        <el-card class="dept-tree-card">
          <template #header>
            <div class="card-header">
              <span>部门列表</span>
              <el-button
                link
                type="primary"
                @click="handleResetDept"
              >
                重置
              </el-button>
            </div>
          </template>
          <el-input
            v-model="deptFilterText"
            class="dept-filter"
            clearable
            placeholder="搜索部门"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-tree
            ref="deptTreeRef"
            :data="deptTreeData"
            :filter-node-method="filterDeptNode"
            :props="deptTreeProps"
            default-expand-all
            highlight-current
            node-key="id"
            @node-click="handleDeptClick"
          >
            <template #default="{ node, data }">
              <span class="dept-tree-node">
                <el-icon><OfficeBuilding /></el-icon>
                <span class="dept-name">{{ node.label }}</span>
                <span
                  v-if="data.children && data.children.length > 0"
                  class="dept-count"
                >
                  ({{ data.children.length }})
                </span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧用户列表 -->
      <el-col :span="19">
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
                clearable
                placeholder="请输入用户名"
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

          <!-- 当前选中的部门提示 -->
          <el-alert
            v-if="selectedDeptName"
            :closable="false"
            :title="`当前筛选部门: ${selectedDeptName}`"
            class="dept-alert"
            type="info"
          />

          <!-- 数据表格 -->
          <el-table
            v-loading="loading"
            :data="tableData"
            border
          >
            <el-table-column
              label="ID"
              prop="id"
              width="80"
            />
            <el-table-column
              label="用户名"
              prop="username"
              width="120"
            />
            <el-table-column
              label="昵称"
              prop="nickname"
              width="120"
            />
            <el-table-column
              label="所属部门"
              prop="deptName"
              width="150"
            >
              <template #default="{ row }">
                <el-tag
                  v-if="row.deptName"
                  size="small"
                  type="info"
                >
                  {{ row.deptName }}
                </el-tag>
                <span
                  v-else
                  style="color: #909399"
                >-</span>
              </template>
            </el-table-column>
            <el-table-column
              label="邮箱"
              prop="email"
              width="180"
            />
            <el-table-column
              label="手机号"
              prop="phone"
              width="130"
            />
            <el-table-column
              label="角色"
              width="180"
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
              fixed="right"
              label="操作"
              width="320"
            >
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="primary"
                  @click="handleEdit(row)"
                >
                  编辑
                </el-button>
                <el-button
                  size="small"
                  type="info"
                  @click="handleAssignRole(row)"
                >
                  分配角色
                </el-button>
                <el-button
                  size="small"
                  type="warning"
                  @click="handleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button
                  size="small"
                  type="danger"
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
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            style="margin-top: 20px; justify-content: flex-end"
            @size-change="getData"
            @current-change="getData"
          />
        </el-card>
      </el-col>
    </el-row>

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
          label="所属部门"
          prop="deptId"
        >
          <el-tree-select
            v-model="form.deptId"
            :data="deptTreeData"
            :props="deptTreeProps"
            check-strictly
            clearable
            placeholder="请选择所属部门"
            style="width: 100%"
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
import { onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { OfficeBuilding, Plus, Refresh, Search } from '@element-plus/icons-vue';
import {
  assignRoles,
  createUser,
  deleteUser,
  getUserList,
  getUserRoleIds,
  updateUser,
  updateUserStatus,
} from '@/api/user';
import { getRoleList } from '@/api/role';
import { getDeptTree } from '@/api/dept';
import { useConfirm } from '@/composables/useConfirm';

const loading = ref(false);
const submitLoading = ref(false);
const roleSubmitLoading = ref(false);
const dialogVisible = ref(false);
const roleDialogVisible = ref(false);
const dialogTitle = ref('新增用户');
const isEdit = ref(false);
const tableData = ref([]);
const total = ref(0);
const allRoles = ref([]);
const selectedRoles = ref([]);
const currentUserId = ref(null);

// 部门树相关
const deptTreeData = ref([]);
const deptTreeRef = ref();
const deptFilterText = ref('');
const selectedDeptName = ref('');

const deptTreeProps = {
  children: 'children',
  label: 'name',
  value: 'id',
};

const queryForm = reactive({
  page: 1,
  size: 10,
  keyword: '',
  deptId: '',
});

const formRef = ref();
const form = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  deptId: '',
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
};

// 确认操作
const confirmDelete = useConfirm({
  message: '确定要删除该用户吗？',
  type: 'warning',
});

const confirmStatus = useConfirm({
  message: '确定要执行此操作吗？',
  type: 'warning',
});

// 监听部门搜索
watch(deptFilterText, (val) => {
  deptTreeRef.value?.filter(val);
});

const filterDeptNode = (value, data) => {
  if (!value) return true;
  return data.name.includes(value);
};

const getDeptTreeData = async () => {
  try {
    const data = await getDeptTree();
    deptTreeData.value = data;
  } catch {
    ElMessage.error('获取部门列表失败');
  }
};

const handleDeptClick = (data) => {
  queryForm.deptId = data.id;
  selectedDeptName.value = data.name;
  queryForm.page = 1;
  getData();
};

const handleResetDept = () => {
  queryForm.deptId = '';
  selectedDeptName.value = '';
  deptTreeRef.value?.setCurrentKey(null);
  queryForm.page = 1;
  getData();
};

const getData = async () => {
  loading.value = true;
  try {
    const data = await getUserList(queryForm);
    tableData.value = data.records;
    total.value = data.total;
  } catch {
    ElMessage.error('获取用户列表失败');
  } finally {
    loading.value = false;
  }
};

const getRoles = async () => {
  try {
    const data = await getRoleList();
    allRoles.value = data.records;
  } catch {
    ElMessage.error('获取角色列表失败');
  }
};

const handleSearch = () => {
  queryForm.page = 1;
  getData();
};

const handleReset = () => {
  queryForm.keyword = '';
  queryForm.page = 1;
  getData();
};

const handleAdd = () => {
  isEdit.value = false;
  dialogTitle.value = '新增用户';
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  isEdit.value = true;
  dialogTitle.value = '编辑用户';
  Object.assign(form, row);
  dialogVisible.value = true;
};

const handleDialogClose = () => {
  formRef.value?.resetFields();
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    deptId: '',
  });
};

const handleSubmit = async () => {
  await formRef.value.validate();

  submitLoading.value = true;
  try {
    if (isEdit.value) {
      await updateUser(form.id, form);
      ElMessage.success('更新成功');
    } else {
      await createUser(form);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    getData();
  } catch {
    // 错误已在验证或 API 中处理
  } finally {
    submitLoading.value = false;
  }
};

const handleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1;
  const action = newStatus === 1 ? '启用' : '禁用';

  try {
    await confirmStatus(`确定要${action}该用户吗？`);
    await updateUserStatus(row.id, newStatus);
    ElMessage.success(`${action}成功`);
    getData();
  } catch {
    // 取消操作
  }
};

const handleDelete = async (row) => {
  try {
    await confirmDelete();
    await deleteUser(row.id);
    ElMessage.success('删除成功');
    getData();
  } catch {
    // 取消操作
  }
};

const handleAssignRole = async (row) => {
  currentUserId.value = row.id;
  roleDialogVisible.value = true;

  // 加载用户已有的角色
  try {
    const roleIds = await getUserRoleIds(row.id);
    selectedRoles.value = roleIds;
  } catch {
    ElMessage.error('获取用户角色失败');
  }
};

const handleRoleDialogClose = () => {
  selectedRoles.value = [];
};

const handleRoleSubmit = async () => {
  roleSubmitLoading.value = true;
  try {
    await assignRoles(currentUserId.value, selectedRoles.value);
    ElMessage.success('分配角色成功');
    roleDialogVisible.value = false;
    getData();
  } catch {
    ElMessage.error('分配角色失败');
  } finally {
    roleSubmitLoading.value = false;
  }
};

onMounted(() => {
  getData();
  getRoles();
  getDeptTreeData();
});
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

.dept-tree-card {
  height: calc(100vh - 140px);
}

.dept-tree-card :deep(.el-card__body) {
  height: calc(100% - 50px);
  overflow: auto;
}

.dept-filter {
  margin-bottom: 15px;
}

.dept-tree-node {
  display: flex;
  align-items: center;
  gap: 5px;
}

.dept-name {
  flex: 1;
}

.dept-count {
  color: #909399;
  font-size: 12px;
}

.dept-alert {
  margin-bottom: 15px;
}
</style>
