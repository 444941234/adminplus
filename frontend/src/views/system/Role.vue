<template>
  <div class="role-page">
    <el-card>
      <template #header>
        <el-row justify="end">
          <el-col :xs="24" :sm="24" :md="20" :lg="20" :xl="20">
            <span>角色管理</span>
          </el-col>
          <el-col :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
            <el-button
              type="primary"
              @click="handleAdd"
            >
              <el-icon><Plus /></el-icon>
              新增角色
            </el-button>
          </el-col>
        </el-row>
      </template>

      <!-- 鏁版嵁琛ㄦ牸 -->
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
          label="瑙掕壊缂栫爜"
          width="150"
        />
        <el-table-column
          prop="name"
          label="瑙掕壊鍚嶇О"
          width="150"
        />
        <el-table-column
          prop="description"
          label="鎻忚堪"
        />
        <el-table-column
          label="鏁版嵁鏉冮檺"
          width="120"
        >
          <template #default="{ row }">
            <el-tag v-bind="getDataScopeType(row.dataScope) ? { type: getDataScopeType(row.dataScope) } : {}">
              {{ getDataScopeText(row.dataScope) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="鐘舵€?
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '姝ｅ父' : '绂佺敤' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="sortOrder"
          label="鎺掑簭"
          width="80"
        />
        <el-table-column
          label="鎿嶄綔"
          width="280"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              缂栬緫
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="handleAssignMenu(row)"
            >
              鍒嗛厤鏉冮檺
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              鍒犻櫎
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 鏂板/缂栬緫瀵硅瘽妗?-->
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
          label="瑙掕壊缂栫爜"
          prop="code"
        >
          <el-input
            v-model="form.code"
            placeholder="璇疯緭鍏ヨ鑹茬紪鐮侊紙濡?ROLE_ADMIN锛?
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item
          label="瑙掕壊鍚嶇О"
          prop="name"
        >
          <el-input
            v-model="form.name"
            placeholder="璇疯緭鍏ヨ鑹插悕绉?
          />
        </el-form-item>
        <el-form-item
          label="鎻忚堪"
          prop="description"
        >
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="璇疯緭鍏ユ弿杩?
          />
        </el-form-item>
        <el-form-item
          label="鏁版嵁鏉冮檺"
          prop="dataScope"
        >
          <el-select
            v-model="form.dataScope"
            placeholder="璇烽€夋嫨鏁版嵁鏉冮檺"
          >
            <el-option
              label="鍏ㄩ儴鏁版嵁"
              :value="1"
            />
            <el-option
              label="鏈儴闂?
              :value="2"
            />
            <el-option
              label="鏈儴闂ㄥ強浠ヤ笅"
              :value="3"
            />
            <el-option
              label="浠呮湰浜?
              :value="4"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="鐘舵€?
          prop="status"
        >
          <el-radio-group v-model="form.status">
            <el-radio :value="1">
              姝ｅ父
            </el-radio>
            <el-radio :value="0">
              绂佺敤
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="鎺掑簭"
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
          鍙栨秷
        </el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          纭畾
        </el-button>
      </template>
    </el-dialog>

    <!-- 鍒嗛厤鏉冮檺瀵硅瘽妗?-->
    <el-dialog
      v-model="menuDialogVisible"
      title="鍒嗛厤鑿滃崟鏉冮檺"
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
          鍙栨秷
        </el-button>
        <el-button
          type="primary"
          :loading="menuSubmitLoading"
          @click="handleMenuSubmit"
        >
          纭畾
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
const dialogTitle = ref('鏂板瑙掕壊')
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
  code: [{ required: true, message: '璇疯緭鍏ヨ鑹茬紪鐮?, trigger: 'blur' }],
  name: [{ required: true, message: '璇疯緭鍏ヨ鑹插悕绉?, trigger: 'blur' }]
}

// 纭鎿嶄綔
const confirmDelete = useConfirm({
  message: '纭畾瑕佸垹闄よ瑙掕壊鍚楋紵',
  type: 'warning'
})

const getDataScopeType = (scope) => {
  const types = { 1: '', 2: 'warning', 3: 'info', 4: 'danger' }
  return types[scope] || ''
}

const getDataScopeText = (scope) => {
  const texts = { 1: '鍏ㄩ儴鏁版嵁', 2: '鏈儴闂?, 3: '鏈儴闂ㄥ強浠ヤ笅', 4: '浠呮湰浜? }
  return texts[scope] || '鏈煡'
}

const getData = async () => {
  loading.value = true
  try {
    const data = await getRoleList()
    tableData.value = data.records
  } catch {
    ElMessage.error('鑾峰彇瑙掕壊鍒楄〃澶辫触')
  } finally {
    loading.value = false
  }
}

const getMenuData = async () => {
  try {
    menuTreeData.value = await getMenuTree()
  } catch {
    ElMessage.error('鑾峰彇鑿滃崟鏍戝け璐?)
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '鏂板瑙掕壊'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '缂栬緫瑙掕壊'
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
      ElMessage.success('鏇存柊鎴愬姛')
    } else {
      await createRole(form)
      ElMessage.success('鍒涘缓锟斤拷鍔?)
    }
    dialogVisible.value = false
    getData()
  } catch {
    // 閿欒宸插湪楠岃瘉鎴?API 涓鐞?
  } finally {
    submitLoading.value = false
  }
}

const handleAssignMenu = async (row) => {
  currentRoleId.value = row.id
  menuDialogVisible.value = true

  // 鍔犺浇鑿滃崟鏍?
  await getMenuData()

  // 鍔犺浇瑙掕壊宸叉湁鐨勮彍鍗?
  try {
    const menuIds = await getRoleMenuIds(row.id)
    menuTreeRef.value?.setCheckedKeys(menuIds)
  } catch {
    ElMessage.error('鑾峰彇瑙掕壊鑿滃崟澶辫触')
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
    ElMessage.success('鍒嗛厤鏉冮檺鎴愬姛')
    menuDialogVisible.value = false
  } catch {
    ElMessage.error('鍒嗛厤鏉冮檺澶辫触')
  } finally {
    menuSubmitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await confirmDelete()
    await deleteRole(row.id)
    ElMessage.success('鍒犻櫎鎴愬姛')
    getData()
  } catch {
    // 鍙栨秷鎿嶄綔
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

</style>
