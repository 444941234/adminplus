<template>
  <div class="role-page">
    <BmCard class="role-table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">角色管理</span>
          <BmButton type="primary" @click="handleAdd">
            <span class="icon-plus">+</span>
            新增角色
          </BmButton>
        </div>
      </template>

      <!-- 数据表格 -->
      <BmTable
        :data="tableData"
        :columns="tableColumns"
        :loading="loading"
        border
      >
        <template #dataScope="{ row }">
          <span class="data-scope-tag" :class="`scope-${row.dataScope}`">
            {{ getDataScopeText(row.dataScope) }}
          </span>
        </template>
        <template #status="{ row }">
          <span class="status-tag" :class="row.status === 1 ? 'status-enabled' : 'status-disabled'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </span>
        </template>
        <template #actions="{ row }">
          <div class="action-buttons">
            <BmButton type="primary" size="small" @click="handleEdit(row)">
              编辑
            </BmButton>
            <BmButton type="warning" size="small" @click="handleAssignMenu(row)">
              分配权限
            </BmButton>
            <BmButton type="danger" size="small" @click="handleDelete(row)">
              删除
            </BmButton>
          </div>
        </template>
      </BmTable>
    </BmCard>

    <!-- 新增/编辑对话框 -->
    <BmModal
      v-model:visible="dialogVisible"
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
        <el-form-item label="角色编码" prop="code">
          <BmInput
            v-model="form.code"
            placeholder="请输入角色编码（如：ROLE_ADMIN）"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <BmInput
            v-model="form.name"
            placeholder="请输入角色名称"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <BmInput
            v-model="form.description"
            type="textarea"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="数据权限" prop="dataScope">
          <BmSelect
            v-model="form.dataScope"
            placeholder="请选择数据权限"
          >
            <option label="全部数据" :value="1" />
            <option label="本部门" :value="2" />
            <option label="本部门及以下" :value="3" />
            <option label="仅本人" :value="4" />
          </BmSelect>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <BmRadioGroup v-model="form.status">
            <BmRadio :value="1">正常</BmRadio>
            <BmRadio :value="0">禁用</BmRadio>
          </BmRadioGroup>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="form.sortOrder"
            :min="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <BmButton @click="dialogVisible = false">取消</BmButton>
        <BmButton
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </BmButton>
      </template>
    </BmModal>

    <!-- 分配权限对话框 -->
    <BmModal
      v-model:visible="menuDialogVisible"
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
        <BmButton @click="menuDialogVisible = false">取消</BmButton>
        <BmButton
          type="primary"
          :loading="menuSubmitLoading"
          @click="handleMenuSubmit"
        >
          确定
        </BmButton>
      </template>
    </BmModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from '@/utils/elementCompat';
import { BmCard, BmButton, BmTable, BmModal, BmInput, BmSelect, BmRadio, BmRadioGroup } from '@adminplus/ui-vue';
import { getRoleList, createRole, updateRole, deleteRole, assignMenus, getRoleMenuIds } from '@/api/role';
import { getMenuTree } from '@/api/menu';
import { useConfirm } from '@/composables/useConfirm';

defineOptions({
  name: 'Role'
});

const loading = ref(false);
const submitLoading = ref(false);
const menuSubmitLoading = ref(false);
const dialogVisible = ref(false);
const menuDialogVisible = ref(false);
const dialogTitle = ref('新增角色');
const isEdit = ref(false);
const tableData = ref([]);
const menuTreeData = ref([]);
const currentRoleId = ref(null);

const formRef = ref();
const menuTreeRef = ref();
const form = reactive({
  id: null,
  code: '',
  name: '',
  description: '',
  dataScope: 1,
  status: 1,
  sortOrder: 0
});

const rules = {
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
};

// 表格列定义
const tableColumns = [
  { prop: 'id', label: 'ID', width: '80px', align: 'center' as const },
  { prop: 'code', label: '角色编码', width: '150px' },
  { prop: 'name', label: '角色名称', width: '150px' },
  { prop: 'description', label: '描述' },
  { prop: 'dataScope', label: '数据权限', width: '120px', align: 'center' as const },
  { prop: 'status', label: '状态', width: '100px', align: 'center' as const },
  { prop: 'sortOrder', label: '排序', width: '80px', align: 'center' as const },
  { prop: 'actions', label: '操作', width: '280px', align: 'center' as const }
];

// 确认操作
const confirmDelete = useConfirm({
  message: '确定要删除该角色吗？',
  type: 'warning'
});

const getDataScopeText = (scope: number) => {
  const texts: Record<number, string> = { 1: '全部数据', 2: '本部门', 3: '本部门及以下', 4: '仅本人' };
  return texts[scope] || '未知';
};

const getData = async () => {
  loading.value = true;
  try {
    const data = await getRoleList();
    tableData.value = data.records;
  } catch {
    ElMessage.error('获取角色列表失败');
  } finally {
    loading.value = false;
  }
};

const getMenuData = async () => {
  try {
    menuTreeData.value = await getMenuTree();
  } catch {
    ElMessage.error('获取菜单树失败');
  }
};

const handleAdd = () => {
  isEdit.value = false;
  dialogTitle.value = '新增角色';
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  isEdit.value = true;
  dialogTitle.value = '编辑角色';
  Object.assign(form, row);
  dialogVisible.value = true;
};

const handleDialogClose = () => {
  formRef.value?.resetFields();
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    description: '',
    dataScope: 1,
    status: 1,
    sortOrder: 0
  });
};

const handleSubmit = async () => {
  await formRef.value.validate();

  submitLoading.value = true;
  try {
    if (isEdit.value) {
      await updateRole(form.id, form);
      ElMessage.success('更新成功');
    } else {
      await createRole(form);
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

const handleAssignMenu = async (row: any) => {
  currentRoleId.value = row.id;
  menuDialogVisible.value = true;

  // 加载菜单树
  await getMenuData();

  // 加载角色已有的菜单
  try {
    const menuIds = await getRoleMenuIds(row.id);
    menuTreeRef.value?.setCheckedKeys(menuIds);
  } catch {
    ElMessage.error('获取角色菜单失败');
  }
};

const handleMenuDialogClose = () => {
  menuTreeRef.value?.setCheckedKeys([]);
};

const handleMenuSubmit = async () => {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || [];
  const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || [];
  const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys];

  menuSubmitLoading.value = true;
  try {
    await assignMenus(currentRoleId.value, allCheckedKeys);
    ElMessage.success('分配权限成功');
    menuDialogVisible.value = false;
  } catch {
    ElMessage.error('分配权限失败');
  } finally {
    menuSubmitLoading.value = false;
  }
};

const handleDelete = async (row: any) => {
  try {
    await confirmDelete();
    await deleteRole(row.id);
    ElMessage.success('删除成功');
    getData();
  } catch {
    // 取消操作
  }
};

onMounted(() => {
  getData();
});
</script>

<style scoped lang="scss">
.role-page {
  padding: 0;
}

.role-table-card {
  @include card-style;

  :deep(.bm-card__header) {
    border-bottom: 1px solid var(--border-color);
    padding: var(--space-md) var(--space-lg);
  }

  :deep(.bm-card__body) {
    padding: var(--space-lg);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .icon-plus {
    font-size: 14px;
    font-weight: bold;
  }
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.data-scope-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.scope-1 {
    background-color: #ecf5ff;
    color: #409eff;
  }

  &.scope-2 {
    background-color: #fdf6ec;
    color: #e6a23c;
  }

  &.scope-3 {
    background-color: #f0f9ff;
    color: #67c23a;
  }

  &.scope-4 {
    background-color: #fef0f0;
    color: #f56c6c;
  }
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.status-enabled {
    background-color: #f0f9ff;
    color: #67c23a;
  }

  &.status-disabled {
    background-color: #fef0f0;
    color: #f56c6c;
  }
}

@media (max-width: 767px) {
  :deep(.bm-card__header) {
    .card-header {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--space-sm);
    }
  }

  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
