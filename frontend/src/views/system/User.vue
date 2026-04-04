<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import {
  Button,
  Card,
  CardContent,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui'
import { Edit, KeyRound, LockKeyhole, Plus, Trash2 } from 'lucide-vue-next'
import { ConfirmDialog, ListSearchBar, Pagination, StatusBadge } from '@/components/common'
import { getDeptTree, getRoleList, getUserList, updateUserStatus, deleteUser } from '@/api'
import type { Dept, Role, User } from '@/types'
import { getUserPagePermissionState } from '@/lib/page-permissions'
import { useUserStore } from '@/stores/user'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { usePageList } from '@/composables/usePageList'
import UserFormDialog from '@/components/user/UserFormDialog.vue'
import PasswordResetDialog from '@/components/user/PasswordResetDialog.vue'
import AssignRoleDialog from '@/components/user/AssignRoleDialog.vue'

const roleList = ref<Role[]>([])
const deptTree = ref<Dept[]>([])
const userStore = useUserStore()

const filters = reactive({
  keyword: '',
  deptId: 'all'
})

const { loading, tableData, fetchData: fetchUsers, goToPage } = usePageList<User>(
  (params) => getUserList(params),
  {
    page: 1,
    size: 10,
    getParams: () => ({
      keyword: filters.keyword.trim() || undefined,
      deptId: filters.deptId === 'all' || filters.deptId === '0' ? undefined : filters.deptId
    })
  }
)

// 对话框状态
const formDialogOpen = ref(false)
const editUserId = ref('')
const deleteDialogOpen = ref(false)
const deleteUserId = ref('')

const { loading: deleteLoading, run: runDelete } = useAsyncAction('删除用户失败')
const { run: runMeta } = useAsyncAction('获取用户关联数据失败')

const passwordDialogOpen = ref(false)
const resetUserId = ref('')
const resetUsername = ref('')
const assignDialogOpen = ref(false)
const assignUser = ref<User | null>(null)

// 状态切换确认
const statusConfirmOpen = ref(false)
const statusChangeUser = ref<User | null>(null)

import { formatDateTime as formatTime } from '@/utils/format'

const permissionState = computed(() => getUserPagePermissionState(userStore.hasPermission))
const canAddUser = computed(() => permissionState.value.canAddUser)
const canEditUser = computed(() => permissionState.value.canEditUser)
const canDeleteUser = computed(() => permissionState.value.canDeleteUser)
const canAssignUser = computed(() => permissionState.value.canAssignUser)

const deptOptions = computed(() => {
  const options: Array<{ id: string; label: string }> = [{ id: '0', label: '无部门' }]
  const walk = (items: Dept[], level = 0) => {
    items.forEach((item) => {
      options.push({
        id: item.id,
        label: `${'　'.repeat(level)}${item.name}`
      })
      if (item.children?.length) walk(item.children, level + 1)
    })
  }
  walk(deptTree.value)
  return options
})

// 防抖搜索（300ms）
const debouncedSearch = useDebounceFn(() => {
  tableData.page = 1
  fetchUsers()
}, 300)

const fetchMeta = () => runMeta(async () => {
  const [rolesRes, deptRes] = await Promise.all([getRoleList(), getDeptTree()])
  roleList.value = rolesRes.data.records || []
  deptTree.value = deptRes.data
})

const handleSearch = () => {
  debouncedSearch()
}

const handleResetSearch = async () => {
  filters.keyword = ''
  filters.deptId = 'all'
  tableData.page = 1
  await fetchUsers()
}

const handleAdd = () => {
  editUserId.value = ''
  formDialogOpen.value = true
}

const handleEdit = (id: string) => {
  editUserId.value = id
  formDialogOpen.value = true
}

const handleStatusClick = (user: User) => {
  statusChangeUser.value = user
  statusConfirmOpen.value = true
}

const handleStatusConfirm = () => {
  if (!statusChangeUser.value) return
  runDelete(async () => {
    const newStatus = statusChangeUser.value!.status === 1 ? 0 : 1
    await updateUserStatus(statusChangeUser.value!.id, newStatus)
  }, {
    successMessage: '状态更新成功',
    errorMessage: '更新状态失败',
    onSuccess: () => fetchUsers()
  }).finally(() => {
    statusConfirmOpen.value = false
    statusChangeUser.value = null
  })
}

const handleDeleteConfirm = (id: string) => {
  deleteUserId.value = id
  deleteDialogOpen.value = true
}

const handleDelete = () => {
  runDelete(async () => {
    await deleteUser(deleteUserId.value)
  }, {
    successMessage: '用户删除成功',
    onSuccess: () => fetchUsers()
  }).finally(() => {
    deleteDialogOpen.value = false
  })
}

const openPasswordDialog = (user: User) => {
  resetUserId.value = user.id
  resetUsername.value = user.username
  passwordDialogOpen.value = true
}

const openAssignDialog = (user: User) => {
  assignUser.value = user
  assignDialogOpen.value = true
}

onMounted(async () => {
  await Promise.all([fetchUsers(), fetchMeta()])
})
</script>

<template>
  <div class="space-y-4">
    <ListSearchBar
      :loading="loading"
      @search="handleSearch"
      @reset="handleResetSearch"
    >
      <template #filters>
        <Input
          v-model="filters.keyword"
          placeholder="搜索用户名、昵称、邮箱、电话"
          class="w-60"
          @keyup.enter="handleSearch"
        />
        <Select
          v-model="filters.deptId"
          @update:model-value="handleSearch"
        >
          <SelectTrigger class="w-40">
            <SelectValue placeholder="全部部门" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">
              全部部门
            </SelectItem>
            <SelectItem
              v-for="dept in deptOptions"
              :key="dept.id"
              :value="dept.id"
            >
              {{ dept.label }}
            </SelectItem>
          </SelectContent>
        </Select>
      </template>
      <template #actions>
        <Button
          v-if="canAddUser"
          @click="handleAdd"
        >
          <Plus class="mr-2 h-4 w-4" />
          新增用户
        </Button>
      </template>
    </ListSearchBar>

    <Card>
      <CardContent class="p-0">
        <table class="w-full">
          <thead class="border-b bg-muted/50">
            <tr>
              <th class="p-4 text-left font-medium">
                用户名
              </th>
              <th class="p-4 text-left font-medium">
                昵称
              </th>
              <th class="p-4 text-left font-medium">
                邮箱
              </th>
              <th class="p-4 text-left font-medium">
                电话
              </th>
              <th class="p-4 text-left font-medium">
                部门
              </th>
              <th class="p-4 text-left font-medium">
                角色
              </th>
              <th class="p-4 text-left font-medium">
                状态
              </th>
              <th class="p-4 text-left font-medium">
                创建时间
              </th>
              <th class="p-4 text-left font-medium">
                操作
              </th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-if="loading">
              <td
                colspan="9"
                class="h-32 text-center text-muted-foreground"
              >
                加载中...
              </td>
            </tr>
            <tr v-else-if="tableData.records.length === 0">
              <td
                colspan="9"
                class="h-32 text-center text-muted-foreground"
              >
                暂无数据
              </td>
            </tr>
            <tr
              v-for="user in tableData.records"
              :key="user.id"
              class="hover:bg-muted/30"
            >
              <td class="p-4 font-medium">
                {{ user.username }}
              </td>
              <td class="p-4">
                {{ user.nickname || '-' }}
              </td>
              <td class="p-4">
                {{ user.email || '-' }}
              </td>
              <td class="p-4">
                {{ user.phone || '-' }}
              </td>
              <td class="p-4">
                {{ user.deptName || '-' }}
              </td>
              <td class="p-4 text-sm text-muted-foreground">
                {{ user.roles?.join('、') || '-' }}
              </td>
              <td class="p-4">
                <StatusBadge
                  :status="user.status"
                  :clickable="canEditUser"
                  @toggle="handleStatusClick(user)"
                />
              </td>
              <td class="p-4 text-sm text-muted-foreground">
                {{ formatTime(user.createTime) }}
              </td>              <td class="p-4">
                <div class="flex gap-2">
                  <Button
                    v-if="canEditUser"
                    size="sm"
                    variant="ghost"
                    @click="handleEdit(user.id)"
                  >
                    <Edit class="h-4 w-4" />
                  </Button>
                  <Button
                    v-if="canAssignUser"
                    size="sm"
                    variant="ghost"
                    @click="openAssignDialog(user)"
                  >
                    <KeyRound class="h-4 w-4" />
                  </Button>
                  <Button
                    v-if="canEditUser"
                    size="sm"
                    variant="ghost"
                    @click="openPasswordDialog(user)"
                  >
                    <LockKeyhole class="h-4 w-4" />
                  </Button>
                  <Button
                    v-if="canDeleteUser"
                    size="sm"
                    variant="ghost"
                    class="text-destructive"
                    @click="handleDeleteConfirm(user.id)"
                  >
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <Pagination
          :current="tableData.page"
          :total="tableData.total"
          :page-size="tableData.size"
          @change="goToPage"
        />
      </CardContent>
    </Card>

    <!-- 新增/编辑用户对话框 -->
    <UserFormDialog
      v-if="canAddUser || canEditUser"
      v-model:open="formDialogOpen"
      :edit-id="editUserId"
      :dept-options="deptOptions"
      @success="fetchUsers"
    />

    <!-- 重置密码对话框 -->
    <PasswordResetDialog
      v-if="canEditUser"
      v-model:open="passwordDialogOpen"
      :user-id="resetUserId"
      :username="resetUsername"
      @success="fetchUsers"
    />

    <!-- 分配角色对话框 -->
    <AssignRoleDialog
      v-if="canAssignUser"
      v-model:open="assignDialogOpen"
      :user="assignUser"
      @success="fetchUsers"
    />

    <!-- 删除确认对话框 -->
    <ConfirmDialog
      v-if="canDeleteUser"
      v-model:open="deleteDialogOpen"
      title="确认删除用户"
      description="删除后不可恢复，如果用户仍有关联数据，后端可能会拒绝删除。"
      confirm-text="确认删除"
      :loading="deleteLoading"
      @confirm="handleDelete"
    />

    <!-- 状态切换确认对话框 -->
    <ConfirmDialog
      v-if="canEditUser"
      v-model:open="statusConfirmOpen"
      :title="`确认${statusChangeUser?.status === 1 ? '禁用' : '启用'}用户`"
      :description="`确定要${statusChangeUser?.status === 1 ? '禁用' : '启用'}用户「${statusChangeUser?.username}」吗？`"
      :confirm-text="statusChangeUser?.status === 1 ? '确认禁用' : '确认启用'"
      @confirm="handleStatusConfirm"
    />
  </div>
</template>