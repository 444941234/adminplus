<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Badge,
  Button,
  Card,
  CardContent,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui'
import { ChevronLeft, ChevronRight, Edit, KeyRound, Plus, Search, Shield, Trash2 } from 'lucide-vue-next'
import { getDeptTree, getRoleList, getUserList, updateUserStatus, deleteUser } from '@/api'
import type { Dept, PageResult, Role, User } from '@/types'
import { getUserPagePermissionState } from '@/lib/page-permissions'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'
import UserFormDialog from '@/components/user/UserFormDialog.vue'
import PasswordResetDialog from '@/components/user/PasswordResetDialog.vue'
import AssignRoleDialog from '@/components/user/AssignRoleDialog.vue'

const loading = ref(false)
const tableData = ref<PageResult<User>>({ records: [], total: 0, page: 1, size: 10 })
const roleList = ref<Role[]>([])
const deptTree = ref<Dept[]>([])
const userStore = useUserStore()

const filters = reactive({
  keyword: '',
  deptId: 'all'
})

// 对话框状态
const formDialogOpen = ref(false)
const editUserId = ref('')
const deleteDialogOpen = ref(false)
const deleteUserId = ref('')
const deleteLoading = ref(false)

const passwordDialogOpen = ref(false)
const resetUserId = ref('')
const resetUsername = ref('')
const assignDialogOpen = ref(false)
const assignUser = ref<User | null>(null)

// 状态切换确认
const statusConfirmOpen = ref(false)
const statusChangeUser = ref<User | null>(null)

const formatTime = (date: string) => {
  if (!date) return '-'
  const d = new Date(date)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const permissionState = computed(() => getUserPagePermissionState(userStore.hasPermission))
const canAddUser = computed(() => permissionState.value.canAddUser)
const canEditUser = computed(() => permissionState.value.canEditUser)
const canDeleteUser = computed(() => permissionState.value.canDeleteUser)
const canAssignUser = computed(() => permissionState.value.canAssignUser)

const totalPages = computed(() => Math.ceil(tableData.value.total / tableData.value.size) || 1)

const visiblePages = computed(() => {
  const current = tableData.value.page
  const total = totalPages.value
  const pages: Array<number | string> = []
  if (total <= 7) {
    for (let i = 1; i <= total; i += 1) pages.push(i)
    return pages
  }
  pages.push(1)
  if (current > 3) pages.push('...')
  const start = Math.max(2, current - 1)
  const end = Math.min(total - 1, current + 1)
  for (let i = start; i <= end; i += 1) pages.push(i)
  if (current < total - 2) pages.push('...')
  pages.push(total)
  return pages
})

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

const queryParams = computed(() => ({
  page: tableData.value.page,
  size: tableData.value.size,
  keyword: filters.keyword.trim() || undefined,
  deptId: filters.deptId === 'all' || filters.deptId === '0' ? undefined : filters.deptId
}))

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams.value)
    tableData.value = res.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取用户列表失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

// 防抖搜索（300ms）
const debouncedSearch = useDebounceFn(() => {
  tableData.value.page = 1
  fetchUsers()
}, 300)

const fetchMeta = async () => {
  try {
    const [rolesRes, deptRes] = await Promise.all([getRoleList(), getDeptTree()])
    roleList.value = rolesRes.data.records || []
    deptTree.value = deptRes.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取用户关联数据失败'
    toast.error(message)
  }
}

const handleSearch = () => {
  debouncedSearch()
}

const handleResetSearch = async () => {
  filters.keyword = ''
  filters.deptId = 'all'
  tableData.value.page = 1
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

const handleStatusConfirm = async () => {
  if (!statusChangeUser.value) return
  try {
    const newStatus = statusChangeUser.value.status === 1 ? 0 : 1
    await updateUserStatus(statusChangeUser.value.id, newStatus)
    toast.success('状态更新成功')
    await fetchUsers()
  } catch (error) {
    const message = error instanceof Error ? error.message : '更新状态失败'
    toast.error(message)
  } finally {
    statusConfirmOpen.value = false
    statusChangeUser.value = null
  }
}

const handleDeleteConfirm = (id: string) => {
  deleteUserId.value = id
  deleteDialogOpen.value = true
}

const handleDelete = async () => {
  deleteLoading.value = true
  try {
    await deleteUser(deleteUserId.value)
    toast.success('用户删除成功')
    await fetchUsers()
  } catch (error) {
    const message = error instanceof Error ? error.message : '删除用户失败'
    toast.error(message)
  } finally {
    deleteLoading.value = false
    deleteDialogOpen.value = false
  }
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

const goToPage = async (page: number) => {
  if (page < 1 || page > totalPages.value || page === tableData.value.page) return
  tableData.value.page = page
  await fetchUsers()
}

onMounted(async () => {
  await Promise.all([fetchUsers(), fetchMeta()])
})
</script>

<template>
  <div class="space-y-4">
    <Card>
      <CardContent class="p-4">
        <div class="grid items-end gap-4 md:grid-cols-4">
          <div class="space-y-2 md:col-span-2">
            <Label>关键词</Label>
            <Input v-model="filters.keyword" placeholder="搜索用户名、昵称、邮箱、电话" @keyup.enter="handleSearch" @input="handleSearch" />
          </div>
          <div class="space-y-2">
            <Label>部门</Label>
            <Select v-model="filters.deptId" @update:model-value="handleSearch">
              <SelectTrigger>
                <SelectValue placeholder="全部部门" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">全部部门</SelectItem>
                <SelectItem v-for="dept in deptOptions" :key="dept.id" :value="dept.id">
                  {{ dept.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="flex gap-2">
            <Button @click="handleSearch">
              <Search class="mr-2 h-4 w-4" />
              搜索
            </Button>
            <Button variant="outline" @click="handleResetSearch">重置</Button>
          </div>
        </div>
        <div class="mt-4 flex justify-end">
          <Button v-if="canAddUser" @click="handleAdd">
            <Plus class="mr-2 h-4 w-4" />
            新增用户
          </Button>
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardContent class="p-0">
        <table class="w-full">
          <thead class="border-b bg-muted/50">
            <tr>
              <th class="p-4 text-left font-medium">用户名</th>
              <th class="p-4 text-left font-medium">昵称</th>
              <th class="p-4 text-left font-medium">邮箱</th>
              <th class="p-4 text-left font-medium">电话</th>
              <th class="p-4 text-left font-medium">部门</th>
              <th class="p-4 text-left font-medium">角色</th>
              <th class="p-4 text-left font-medium">状态</th>
              <th class="p-4 text-left font-medium">创建时间</th>
              <th class="p-4 text-left font-medium">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-if="loading">
              <td colspan="9" class="h-32 text-center text-muted-foreground">加载中...</td>
            </tr>
            <tr v-else-if="tableData.records.length === 0">
              <td colspan="9" class="h-32 text-center text-muted-foreground">暂无数据</td>
            </tr>
            <tr v-for="user in tableData.records" :key="user.id" class="hover:bg-muted/30">
              <td class="p-4 font-medium">{{ user.username }}</td>
              <td class="p-4">{{ user.nickname || '-' }}</td>
              <td class="p-4">{{ user.email || '-' }}</td>
              <td class="p-4">{{ user.phone || '-' }}</td>
              <td class="p-4">{{ user.deptName || '-' }}</td>
              <td class="p-4 text-sm text-muted-foreground">{{ user.roles?.join('、') || '-' }}</td>
              <td class="p-4">
                <Badge
                  :variant="user.status === 1 ? 'default' : 'destructive'"
                  :class="canEditUser ? 'cursor-pointer hover:opacity-80 transition-opacity' : ''"
                  @click="canEditUser && handleStatusClick(user)"
                >
                  {{ user.status === 1 ? '正常' : '禁用' }}
                </Badge>
              </td>
              <td class="p-4 text-sm text-muted-foreground">{{ formatTime(user.createTime) }}
            </td>              <td class="p-4">
                <div class="flex gap-2">
                  <Button v-if="canEditUser" size="sm" variant="ghost" @click="handleEdit(user.id)">
                    <Edit class="h-4 w-4" />
                  </Button>
                  <Button v-if="canAssignUser" size="sm" variant="ghost" @click="openAssignDialog(user)">
                    <Shield class="h-4 w-4" />
                  </Button>
                  <Button v-if="canEditUser" size="sm" variant="ghost" @click="openPasswordDialog(user)">
                    <KeyRound class="h-4 w-4" />
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

        <div class="flex items-center justify-between border-t px-4 py-4">
          <p class="text-sm text-muted-foreground">
            共 <span class="font-medium">{{ tableData.total }}</span> 条记录，
            第 <span class="font-medium">{{ tableData.page }}</span> / <span class="font-medium">{{ totalPages }}</span> 页
          </p>
          <div class="flex items-center gap-1">
            <Button variant="outline" size="icon" :disabled="tableData.page === 1" @click="goToPage(tableData.page - 1)">
              <ChevronLeft class="h-4 w-4" />
            </Button>
            <template v-for="(page, index) in visiblePages" :key="index">
              <span v-if="page === '...'" class="px-2 text-muted-foreground">...</span>
              <Button
                v-else
                size="icon"
                :variant="page === tableData.page ? 'default' : 'outline'"
                @click="goToPage(page as number)"
              >
                {{ page }}
              </Button>
            </template>
            <Button
              variant="outline"
              size="icon"
              :disabled="tableData.page >= totalPages"
              @click="goToPage(tableData.page + 1)"
            >
              <ChevronRight class="h-4 w-4" />
            </Button>
          </div>
        </div>
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
    <AlertDialog v-if="canDeleteUser" v-model:open="deleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认删除用户</AlertDialogTitle>
          <AlertDialogDescription>删除后不可恢复，如果用户仍有关联数据，后端可能会拒绝删除。</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction :disabled="deleteLoading" @click="handleDelete">确认删除</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>

    <!-- 状态切换确认对话框 -->
    <AlertDialog v-if="canEditUser" v-model:open="statusConfirmOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认{{ statusChangeUser?.status === 1 ? '禁用' : '启用' }}用户</AlertDialogTitle>
          <AlertDialogDescription>
            确定要{{ statusChangeUser?.status === 1 ? '禁用' : '启用' }}用户「{{ statusChangeUser?.username }}」吗？
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction @click="handleStatusConfirm">确认</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>