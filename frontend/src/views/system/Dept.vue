<template>
  <div class="dept-page">
    <BmCard>
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <div class="header-actions">
            <BmButton
              type="primary"
              @click="handleAdd"
            >
              <span class="icon-plus">+</span>
              新增部门
            </BmButton>
          </div>
        </div>
      </template>

      <!-- 部门树 -->
      <el-tree
        ref="treeRef"
        v-loading="loading"
        :data="treeData"
        :props="treeProps"
        node-key="id"
        :indent="40"
        :expand-on-click-node="false"
        :default-expand-all="true"
        highlight-current
        class="dept-tree"
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <div class="tree-node" :class="`level-${node.level}`">
            <div class="level-indicator">
              <span class="level-number">L{{ node.level }}</span>
            </div>
            <div class="node-info">
              <div class="node-name-row">
                <span class="node-name">{{ node.label }}</span>
                <span
                  class="status-tag"
                  :class="data.status === 1 ? 'status-enabled' : 'status-disabled'"
                >
                  {{ data.status === 1 ? '启用' : '禁用' }}
                </span>
              </div>
              <div class="node-details">
                <div class="detail-row">
                  <span class="detail-label">编码</span>
                  <span class="detail-value">{{ data.code || '-' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">负责人</span>
                  <span class="detail-value">{{ data.leader || '-' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">电话</span>
                  <span class="detail-value">{{ data.phone || '-' }}</span>
                </div>
              </div>
            </div>
            <div class="node-actions">
              <BmButton
                type="primary"
                size="small"
                @click="handleAddChild(data)"
              >
                添加子部门
              </BmButton>
              <BmButton
                type="warning"
                size="small"
                @click="handleEdit(data)"
              >
                编辑
              </BmButton>
              <BmButton
                type="danger"
                size="small"
                @click="handleDelete(data)"
              >
                删除
              </BmButton>
            </div>
          </div>
        </template>
      </el-tree>
    </BmCard>

    <!-- 部门编辑对话框 -->
    <BmModal
      v-model:visible="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item
          label="部门名称"
          prop="name"
        >
          <BmInput
            v-model="form.name"
            placeholder="请输入部门名称"
          />
        </el-form-item>
        <el-form-item
          label="部门编码"
          prop="code"
        >
          <BmInput
            v-model="form.code"
            placeholder="请输入部门编码"
          />
        </el-form-item>
        <el-form-item
          label="负责人"
          prop="leader"
        >
          <BmInput
            v-model="form.leader"
            placeholder="请输入负责人"
          />
        </el-form-item>
        <el-form-item
          label="联系电话"
          prop="phone"
        >
          <BmInput
            v-model="form.phone"
            placeholder="请输入联系电话"
          />
        </el-form-item>
        <el-form-item
          label="状态"
          prop="status"
        >
          <BmSwitch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
        <el-form-item
          label="排序"
          prop="sortOrder"
        >
          <el-input-number
            v-model="form.sortOrder"
            :min="0"
            :max="999"
          />
        </el-form-item>
        <el-form-item
          label="备注"
          prop="remark"
        >
          <BmInput
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <BmButton @click="dialogVisible = false">
          取消
        </BmButton>
        <BmButton
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </BmButton>
      </template>
    </BmModal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from '@/utils/elementCompat';
import { BmCard, BmButton, BmModal, BmInput, BmSwitch } from '@adminplus/ui-vue';
import { createDept, deleteDept, getDeptTree, updateDept } from '@/api/dept';

const loading = ref(false);
const treeData = ref([]);
const treeRef = ref();
const dialogVisible = ref(false);
const dialogTitle = ref('');
const submitLoading = ref(false);
const isEdit = ref(false);

const formRef = ref();
const form = reactive({
  id: null,
  parentId: null,
  name: '',
  code: '',
  leader: '',
  phone: '',
  status: 1,
  sortOrder: 0,
  remark: '',
});

const treeProps = {
  label: 'name',
  children: 'children',
};

// 手机号格式校验（只在有值时校验）
const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'));
  } else {
    callback();
  }
};

const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
};

// 获取部门树数据
const getData = async () => {
  loading.value = true;
  try {
    const data = await getDeptTree();

    // 后端现在直接返回树形结构
    if (Array.isArray(data)) {
      treeData.value = data;
    } else if (data && Array.isArray(data.data)) {
      treeData.value = data.data;
    } else {
      console.warn('部门树数据格式异常:', data);
      treeData.value = [];
    }
  } catch (error) {
    console.error('获取部门数据失败:', error);
    ElMessage.error('获取部门数据失败');
    treeData.value = [];
  } finally {
    loading.value = false;
  }
};

// 新增部门
const handleAdd = () => {
  isEdit.value = false;
  dialogTitle.value = '新增部门';
  resetForm();
  dialogVisible.value = true;
};

// 新增子部门
const handleAddChild = (data) => {
  isEdit.value = false;
  dialogTitle.value = '新增子部门';
  resetForm();
  form.parentId = data.id;
  dialogVisible.value = true;
};

// 编辑部门
const handleEdit = (data) => {
  isEdit.value = true;
  dialogTitle.value = '编辑部门';
  Object.assign(form, {
    id: data.id,
    parentId: data.parentId,
    name: data.name,
    code: data.code,
    leader: data.leader,
    phone: data.phone,
    status: data.status,
    sortOrder: data.sortOrder,
    remark: data.remark,
  });
  dialogVisible.value = true;
};

// 删除部门
const handleDelete = async (data) => {
  try {
    await ElMessageBox.confirm(`确定要删除部门"${data.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await deleteDept(data.id);
    ElMessage.success('删除成功');
    getData();
  } catch {
    // 用户取消操作
  }
};

// 节点点击
const handleNodeClick = (data) => {
  console.log('选中部门:', data);
};

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    parentId: null,
    name: '',
    code: '',
    leader: '',
    phone: '',
    status: 1,
    sortOrder: 0,
    remark: '',
  });
  formRef.value?.resetFields();  // 清除验证状态（Element Plus 需要）
};

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate();

  submitLoading.value = true;
  try {
    if (isEdit.value) {
      await updateDept(form.id, form);
    } else {
      await createDept(form);
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
    dialogVisible.value = false;
    getData();
  } catch {
    ElMessage.error('操作失败');
  } finally {
    submitLoading.value = false;
  }
};

onMounted(() => {
  getData();
});
</script>

<style scoped>
.dept-page {
}

/* 树形连接线样式 */
.dept-tree {
  background-color: #fafbfc;
  border-radius: 8px;
  padding: 16px;
}

:deep(.el-tree-node) {
  position: relative;
}

:deep(.el-tree-node__expand-icon) {
  font-size: 14px;
  color: #909399;
}

:deep(.el-tree-node__expand-icon.is-leaf) {
  color: transparent;
}

:deep(.el-tree-node__children) {
  position: relative;
}

/* 树形连接线 - 水平线 */
:deep(.el-tree-node__children .el-tree-node) {
  position: relative;
}

:deep(.el-tree-node__children .el-tree-node::before) {
  content: '';
  position: absolute;
  left: -18px;
  top: 0;
  height: 100%;
  width: 1px;
  background-color: #e4e7ed;
}

:deep(.el-tree-node__children .el-tree-node:last-child::before) {
  height: 24px;
}

/* 树形连接线 - 垂直线连接到父节点 */
:deep(.el-tree-node__children .el-tree-node::after) {
  content: '';
  position: absolute;
  left: -18px;
  top: 24px;
  width: 18px;
  height: 1px;
  background-color: #e4e7ed;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
  padding: 12px 16px;
  gap: 16px;
  border-radius: 6px;
  transition: all 0.2s;
  background-color: #ffffff;
  border-left: 3px solid transparent;
}

.tree-node:hover {
  background-color: #ecf5ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

/* 层级指示器 */
.level-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.level-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 24px;
  padding: 0 8px;
  font-size: 12px;
  font-weight: 600;
  color: #409eff;
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  border-radius: 12px;
  border: 1px solid #b3d8ff;
}

/* 不同层级的视觉效果 */
.tree-node.level-1 {
  border-left-color: #67c23a;
}

.tree-node.level-1 .level-number {
  color: #67c23a;
  background: linear-gradient(135deg, #f0f9ff 0%, #d4f4dd 100%);
  border-color: #b3e19d;
}

.tree-node.level-2 {
  border-left-color: #409eff;
}

.tree-node.level-2 .level-number {
  color: #409eff;
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  border-color: #b3d8ff;
}

.tree-node.level-3 {
  border-left-color: #e6a23c;
}

.tree-node.level-3 .level-number {
  color: #e6a23c;
  background: linear-gradient(135deg, #fef9f0 0%, #fdf0d4 100%);
  border-color: #f5dab1;
}

.tree-node.level-4 {
  border-left-color: #f56c6c;
}

.tree-node.level-4 .level-number {
  color: #f56c6c;
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  border-color: #fab6b6;
}

.tree-node.level-5,
.tree-node.level-6,
.tree-node.level-7,
.tree-node.level-8 {
  border-left-color: #909399;
}

.tree-node.level-5 .level-number,
.tree-node.level-6 .level-number,
.tree-node.level-7 .level-number,
.tree-node.level-8 .level-number {
  color: #909399;
  background: linear-gradient(135deg, #f4f4f5 0%, #e9e9eb 100%);
  border-color: #d3d4d6;
}

.node-info {
  flex: 1;
  min-width: 0;
}

.node-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.node-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

/* 根节点字体稍大 */
.tree-node.level-1 .node-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;

  &.status-enabled {
    background-color: #f0f9ff;
    color: #67c23a;
  }

  &.status-disabled {
    background-color: #f4f4f5;
    color: #909399;
  }
}

.icon-plus {
  font-size: 14px;
  font-weight: bold;
}

.node-details {
  display: flex;
  flex-wrap: wrap;
  gap: 20px 28px;
}

.detail-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.detail-label {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}

.detail-value {
  font-size: 14px;
  color: #606266;
  word-break: break-all;
}

.node-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  align-items: flex-start;
}

/* 树形组件内容区域的样式优化 */
:deep(.el-tree-node__content) {
  height: auto !important;
  padding: 6px 0 !important;
  margin-bottom: 4px;
}

:deep(.el-tree-node__content:hover) {
  background-color: transparent !important;
}

/* 当前选中节点高亮 */
:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: transparent !important;
}

:deep(.el-tree-node.is-current > .el-tree-node__content .tree-node) {
  background-color: #ecf5ff !important;
  border-left-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

/* 响应式：小屏幕时优化布局 */
@media (max-width: 768px) {
  .dept-tree {
    padding: 12px;
  }

  .tree-node {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    padding: 10px 12px;
  }

  .node-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .node-details {
    gap: 12px 16px;
  }

  .level-indicator {
    align-self: flex-start;
  }
}
</style>
