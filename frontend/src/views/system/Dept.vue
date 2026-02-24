<template>
  <div class="dept-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增部门
            </el-button>
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
        default-expand-all
        highlight-current
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <div class="node-info">
              <div class="node-name-row">
                <span class="node-name">{{ node.label }}</span>
                <el-tag
                  :type="data.status === 1 ? 'success' : 'info'"
                  class="status-tag"
                  size="small"
                >
                  {{ data.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </div>
              <div class="node-details">
                <span class="detail-item">编码: {{ data.code }}</span>
                <span class="detail-item">负责人: {{ data.leader || '-' }}</span>
                <span class="detail-item">电话: {{ data.phone || '-' }}</span>
              </div>
            </div>
            <div class="node-actions">
              <el-button type="primary" size="small" @click="handleAddChild(data)">
                添加子部门
              </el-button>
              <el-button type="warning" size="small" @click="handleEdit(data)"> 编辑 </el-button>
              <el-button type="danger" size="small" @click="handleDelete(data)"> 删除 </el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 部门编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit"> 确定 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
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
  id: undefined,
  parentId: undefined,
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

const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
};

// 获取部门树数据
const getData = async () => {
  loading.value = true;
  try {
    treeData.value = await getDeptTree();
  } catch {
    ElMessage.error('获取部门数据失败');
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
    id: undefined,
    parentId: undefined,
    name: '',
    code: '',
    leader: '',
    phone: '',
    status: 1,
    sortOrder: 0,
    remark: '',
  });
  formRef.value?.resetFields();
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
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 12px 8px;
  flex-wrap: wrap;
  gap: 12px;
}

.node-info {
  flex: 1;
  min-width: 0;
}

.node-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.node-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.status-tag {
  flex-shrink: 0;
}

.node-details {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 13px;
  color: #606266;
}

.detail-item {
  white-space: nowrap;
  display: flex;
  align-items: center;
}

.detail-item::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 4px;
  background-color: #dcdfe6;
  border-radius: 50%;
  margin-right: 8px;
}

.detail-item:first-child::before {
  display: none;
}

.node-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 响应式：小屏幕时优化布局 */
@media (max-width: 768px) {
  .tree-node {
    flex-direction: column;
    align-items: flex-start;
  }

  .node-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>