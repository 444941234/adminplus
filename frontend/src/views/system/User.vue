<template>
  <div class="user-page">
    <el-row :gutter="24">
      <!-- 左侧部门树 -->
      <el-col :xs="24" :sm="24" :md="7" :lg="6" :xl="6">
        <BmCard class="dept-tree-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">部门列表</span>
              <BmButton type="text" @click="handleResetDept">
                重置
              </BmButton>
            </div>
          </template>
          <BmInput
            v-model="deptFilterText"
            class="dept-filter"
            clearable
            placeholder="搜索部门"
            prefix-icon="🔍"
          />
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
                <span class="dept-icon">🏢</span>
                <span class="dept-name">{{ node.label }}</span>
                <span v-if="data.children && data.children.length > 0" class="dept-count">
                  ({{ data.children.length }})
                </span>
              </span>
            </template>
          </el-tree>
        </BmCard>
      </el-col>

      <!-- 右侧用户列表 -->
      <el-col :xs="24" :sm="24" :md="17" :lg="18" :xl="18">
        <BmCard class="user-list-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">用户管理</span>
              <BmButton type="primary" @click="handleAdd">
                <span class="btn-icon">➕</span>
                新增用户
              </BmButton>
            </div>
          </template>

          <!-- 搜索表单 -->
          <el-form :inline="true" :model="queryForm" class="search-form">
            <el-form-item label="用户名">
              <BmInput
                v-model="queryForm.keyword"
                clearable
                placeholder="请输入用户名"
              />
            </el-form-item>
            <el-form-item>
              <BmButton type="primary" @click="handleSearch">
                <span class="btn-icon">🔍</span>
                搜索
              </BmButton>
              <BmButton @click="handleReset">
                <span class="btn-icon">🔄</span>
                重置
              </BmButton>
            </el-form-item>
          </el-form>

          <!-- 当前选中的部门提示 -->
          <div v-if="selectedDeptName" class="dept-alert">
            <span class="alert-icon">ℹ️</span>
            <span>当前筛选部门: {{ selectedDeptName }}</span>
          </div>

          <div class="table-container">
            <BmTable
              v-if="!loading && tableData.length > 0"
              :columns="tableColumns"
              :data="tableData"
              :border="true"
              :stripe="true"
            >
              <template #deptName="{ row }">
                <BmBadge v-if="row.deptName" type="info">
                  {{ row.deptName }}
                </BmBadge>
                <span v-else class="empty-text">-</span>
              </template>
              <template #roles="{ row }">
                <span v-if="row.roles && row.roles.length > 0">
                  <BmBadge
                    v-for="role in row.roles"
                    :key="role"
                    type="primary"
                    class="role-badge"
                  >
                    {{ role }}
                  </BmBadge>
                </span>
                <span v-else class="empty-text">无</span>
              </template>
              <template #status="{ row }">
                <BmBadge :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </BmBadge>
              </template>
              <template #operations="{ row }">
                <div class="operation-buttons">
                  <BmButton size="small" type="primary" @click="handleEdit(row)">
                    编辑
                  </BmButton>
                  <BmButton size="small" type="default" @click="handleAssignRole(row)">
                    分配角色
                  </BmButton>
                  <BmButton size="small" type="warning" @click="handleStatus(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </BmButton>
                  <BmButton size="small" type="danger" @click="handleDelete(row)">
                    删除
                  </BmButton>
                </div>
              </template>
            </BmTable>

            <!-- Loading state -->
            <div v-if="loading" class="loading-state">
              <span class="loading-spinner"></span>
              <span>加载中...</span>
            </div>

            <!-- Empty state -->
            <div v-if="!loading && tableData.length === 0" class="empty-state">
              <span class="empty-icon">📭</span>
              <span class="empty-text">暂无数据</span>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination-container">
            <BmPagination
              v-model:current="queryForm.page"
              v-model:page-size="queryForm.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              :show-total="true"
              :show-jumper="true"
              @current-change="getData"
              @size-change="getData"
            />
          </div>
        </BmCard>
      </el-col>
    </el-row>

    <!-- 新增/编辑对话框 -->
    <BmModal
      v-model:visible="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="确定"
      @confirm="handleSubmit"
      @cancel="dialogVisible = false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <BmInput
            v-model="form.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <BmInput
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <BmInput
            v-model="form.nickname"
            placeholder="请输入昵称"
          />
        </el-form-item>
        <el-form-item label="所属部门" prop="deptId">
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
        <el-form-item label="邮箱" prop="email">
          <BmInput
            v-model="form.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <BmInput
            v-model="form.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>
      </el-form>
    </BmModal>

    <!-- 分配角色对话框 -->
    <BmModal
      v-model:visible="roleDialogVisible"
      title="分配角色"
      width="400px"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="确定"
      :confirm-button-loading="roleSubmitLoading"
      @confirm="handleRoleSubmit"
      @cancel="roleDialogVisible = false"
    >
      <div class="role-checkbox-group">
        <BmCheckbox
          v-for="role in allRoles"
          :key="role.id"
          :model-value="selectedRoles.includes(role.id)"
          :label="role.id"
          @update:model-value="toggleRole(role.id)"
        >
          {{ role.name }}
        </BmCheckbox>
      </div>
    </BmModal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElTree as ElTreeType } from 'element-plus';
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
import { BmCard, BmButton, BmInput, BmTable, BmPagination, BmModal, BmBadge, BmCheckbox } from '@adminplus/ui-vue';
import type { Column } from '@adminplus/ui-vue';

defineOptions({
  name: 'User'
});

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
const deptTreeRef = ref<InstanceType<typeof ElTreeType>>();
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

// 表格列定义
const tableColumns = ref<Column[]>([
  { label: 'ID', prop: 'id', width: '80px', align: 'center' },
  { label: '用户名', prop: 'username', width: '120px' },
  { label: '昵称', prop: 'nickname', width: '120px' },
  { label: '所属部门', prop: 'deptName', width: '150px' },
  { label: '邮箱', prop: 'email', width: '180px' },
  { label: '手机号', prop: 'phone', width: '130px' },
  { label: '角色', prop: 'roles', width: '180px' },
  { label: '状态', prop: 'status', width: '100px', align: 'center' },
  { label: '操作', prop: 'operations', width: '320px', align: 'center' },
]);

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

const filterDeptNode = (value: string, data: any) => {
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

const handleDeptClick = (data: any) => {
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

const handleEdit = (row: any) => {
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

const handleStatus = async (row: any) => {
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

const handleDelete = async (row: any) => {
  try {
    await confirmDelete();
    await deleteUser(row.id);
    ElMessage.success('删除成功');
    getData();
  } catch {
    // 取消操作
  }
};

const handleAssignRole = async (row: any) => {
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

const toggleRole = (roleId: number) => {
  const index = selectedRoles.value.indexOf(roleId);
  if (index > -1) {
    selectedRoles.value.splice(index, 1);
  } else {
    selectedRoles.value.push(roleId);
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

<style scoped lang="scss">
.user-page {
  padding: 0;
}

.dept-tree-card,
.user-list-card {
  @include card-style;

  :deep(.bm-card__header) {
    border-bottom: 1px solid var(--border-color);
    padding: var(--space-md) var(--space-lg);
  }

  :deep(.bm-card__body) {
    padding: var(--space-lg);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
  }
}

.btn-icon {
  font-size: 16px;
}

.dept-filter {
  margin-bottom: var(--space-md);
}

.dept-tree-node {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  flex: 1;

  .dept-icon {
    font-size: 16px;
  }

  .dept-name {
    flex: 1;
  }

  .dept-count {
    color: var(--text-secondary);
    font-size: 12px;
  }
}

.search-form {
  margin-bottom: var(--space-md);
}

.dept-alert {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  margin-bottom: var(--space-md);
  background: var(--primary-light, rgba(22, 93, 255, 0.1));
  border: 1px solid var(--primary-color);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 14px;

  .alert-icon {
    font-size: 16px;
  }
}

.table-container {
  margin-bottom: var(--space-md);
  min-height: 300px;

  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: var(--space-2xl);
    gap: var(--space-md);
    color: var(--text-secondary);

    .loading-spinner {
      width: 40px;
      height: 40px;
      border: 3px solid var(--border-color);
      border-top-color: var(--primary-color);
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: var(--space-2xl);
    gap: var(--space-md);
    color: var(--text-tertiary);

    .empty-icon {
      font-size: 48px;
      opacity: 0.5;
    }

    .empty-text {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-text {
  color: var(--text-placeholder);
}

.role-badge {
  margin-right: var(--space-xs);
}

.operation-buttons {
  display: flex;
  gap: var(--space-xs);
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
}

.role-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

@media (max-width: 767px) {
  .dept-tree-card,
  .user-list-card {
    margin-bottom: var(--space-md);
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-sm);
  }

  .pagination-container {
    justify-content: center;

    :deep(.bm-pagination) {
      flex-wrap: wrap;
      justify-content: center;
    }
  }

  .operation-buttons {
    flex-direction: column;

    .bm-button {
      width: 100%;
    }
  }
}
</style>
