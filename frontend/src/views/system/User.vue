<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
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
  Checkbox,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  ScrollArea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui'
import { ChevronLeft, ChevronRight, Edit, Eye, EyeOff, KeyRound, Plus, Search, Shield, Trash2 } from 'lucide-vue-next'
import {
  assignRoles,
  createUser,
  deleteUser,
  getDeptTree,
  getRoleList,
  getUserById,
  getUserList,
  getUserRoles,
  resetPassword,
  updateUser,
  updateUserStatus
} from '@/api'
import type { Dept, PageResult, Role, User } from '@/types'
import { getUserPagePermissionState } from '@/lib/page-permissions'
import { isStrongPassword, isValidChinaPhone, isValidEmail } from '@/lib/validators'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'

interface UserFormState {
  username: string
  nickname: string
  email: string
  phone: string
  password: string
  deptId: string
  status: string
}

const loading = ref(false)
const rolesLoading = ref(false)
const tableData = ref<PageResult<User>>({ records: [], total: 0, page: 1, size: 10 })
const roleList = ref<Role[]>([])
const deptTree = ref<Dept[]>([])
const userStore = useUserStore()

const filters = reactive({
  keyword: '',
  deptId: 'all'
})

const dialogOpen = ref(false)
const dialogLoading = ref(false)
const isEdit = ref(false)
const editId = ref('')

const deleteDialogOpen = ref(false)
const deleteUserId = ref('')

const passwordDialogOpen = ref(false)
const passwordDialogLoading = ref(false)
const resetUserId = ref('')
const resetUsername = ref('')
const newPassword = ref('')
const showPassword = ref(false)

const assignDialogOpen = ref(false)
const assignDialogLoading = ref(false)
const assignUser = ref<User | null>(null)
const selectedRoleIds = ref<string[]>([])

const form = reactive<UserFormState>({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: '',
  deptId: '0',
  status: '1'
})

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
        label: `${'　'.repeat(level)}${item.deptName ?? item.name ?? ''}`
      })
      if (item.children?.length) walk(item.children, level + 1)
    })
  }
  walk(deptTree.value)
  return options
})

const getRoleName = (role: Role) => role.roleName ?? role.name ?? ''
const getRoleCode = (role: Role) => role.roleCode ?? role.code ?? ''

const resetForm = () => {
  Object.assign(form, {
    username: '',
    nickname: '',
    email: '',
    phone: '',
    password: '',
    deptId: '0',
    status: '1'
  })
}

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

const fetchMeta = async () => {
  rolesLoading.value = true
  try {
    const [rolesRes, deptRes] = await Promise.all([getRoleList(), getDeptTree()])
    roleList.value = rolesRes.data.records || []
    deptTree.value = deptRes.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取用户关联数据失败'
    toast.error(message)
  } finally {
    rolesLoading.value = false
  }
}

const handleSearch = async () => {
  tableData.value.page = 1
  await fetchUsers()
}

const handleResetSearch = async () => {
  filters.keyword = ''
  filters.deptId = 'all'
  tableData.value.page = 1
  await fetchUsers()
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  editId.value = ''
  dialogOpen.value = true
}

const handleEdit = async (id: string) => {
  isEdit.value = true
  editId.value = id
  dialogLoading.value = true
  dialogOpen.value = true
  try {
    const res = await getUserById(id)
    const user = res.data
    Object.assign(form, {
      username: user.username,
      nickname: user.nickname || '',
      email: user.email || '',
      phone: user.phone || '',
      password: '',
      deptId: user.deptId || '0',
      status: String(user.status ?? 1)
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取用户详情失败'
    toast.error(message)
    dialogOpen.value = false
  } finally {
    dialogLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.username.trim()) {
    toast.warning('请输入用户名')
    return
  }
  if (!form.nickname.trim()) {
    toast.warning('请输入昵称')
    return
  }
  if (!isEdit.value && !form.password.trim()) {
    toast.warning('请输入密码')
    return
  }
  if (form.email.trim() && !isValidEmail(form.email.trim())) {
    toast.warning('邮箱格式不正确')
    return
  }
  if (form.phone.trim() && !isValidChinaPhone(form.phone.trim())) {
    toast.warning('手机号格式不正确')
    return
  }
  if (!isEdit.value && !isStrongPassword(form.password.trim())) {
    toast.warning('密码需 12 位以上，且包含大小写字母、数字和特殊字符')
    return
  }

  dialogLoading.value = true
  try {
    if (isEdit.value) {
      await updateUser(editId.value, {
        nickname: form.nickname.trim(),
        email: form.email.trim() || undefined,
        phone: form.phone.trim() || undefined,
        deptId: form.deptId === '0' ? undefined : form.deptId,
        status: Number(form.status)
      })
      toast.success('用户更新成功')
    } else {
      await createUser({
        username: form.username.trim(),
        password: form.password.trim(),
        nickname: form.nickname.trim(),
        email: form.email.trim() || undefined,
        phone: form.phone.trim() || undefined,
        deptId: form.deptId === '0' ? undefined : form.deptId
      })
      toast.success('用户创建成功')
    }
    dialogOpen.value = false
    await fetchUsers()
  } catch (error) {
    const message = error instanceof Error ? error.message : '保存用户失败'
    toast.error(message)
  } finally {
    dialogLoading.value = false
  }
}

const handleStatusChange = async (id: string, status: number) => {
  try {
    await updateUserStatus(id, status === 1 ? 0 : 1)
    toast.success('状态更新成功')
    await fetchUsers()
  } catch (error) {
    const message = error instanceof Error ? error.message : '更新状态失败'
    toast.error(message)
  }
}

const handleDeleteConfirm = (id: string) => {
  deleteUserId.value = id
  deleteDialogOpen.value = true
}

const handleDelete = async () => {
  try {
    await deleteUser(deleteUserId.value)
    toast.success('用户删除成功')
    await fetchUsers()
  } catch (error) {
    const message = error instanceof Error ? error.message : '删除用户失败'
    toast.error(message)
  } finally {
    deleteDialogOpen.value = false
  }
}

const openPasswordDialog = (user: User) => {
  resetUserId.value = user.id
  resetUsername.value = user.username
  newPassword.value = ''
  showPassword.value = false
  passwordDialogOpen.value = true
}

const handleResetPassword = async () => {
  if (!newPassword.value.trim()) {
    toast.warning('请输入新密码')
    return
  }
  if (!isStrongPassword(newPassword.value.trim())) {
    toast.warning('密码需 12 位以上，且包含大小写字母、数字和特殊字符')
    return
  }

  passwordDialogLoading.value = true
  try {
    await resetPassword(resetUserId.value, newPassword.value.trim())
    toast.success('密码重置成功')
    passwordDialogOpen.value = false
  } catch (error) {
    const message = error instanceof Error ? error.message : '重置密码失败'
    toast.error(message)
  } finally {
    passwordDialogLoading.value = false
  }
}

const toggleRoleSelection = (roleId: string, checked: boolean) => {
  const next = new Set(selectedRoleIds.value)
  if (checked) {
    next.add(roleId)
  } else {
    next.delete(roleId)
  }
  selectedRoleIds.value = Array.from(next)
}

const openAssignDialog = async (user: User) => {
  assignUser.value = user
  assignDialogOpen.value = true
  assignDialogLoading.value = true
  try {
    const [rolesRes, selectedRes] = await Promise.all([getRoleList(), getUserRoles(user.id)])
    roleList.value = rolesRes.data.records || []
    selectedRoleIds.value = selectedRes.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取用户角色失败'
    toast.error(message)
    assignDialogOpen.value = false
  } finally {
    assignDialogLoading.value = false
  }
}

const handleAssignSubmit = async () => {
  if (!assignUser.value) return
  assignDialogLoading.value = true
  try {
    await assignRoles(assignUser.value.id, selectedRoleIds.value)
    toast.success('角色分配成功')
    assignDialogOpen.value = false
    await fetchUsers()
  } catch (error) {
    const message = error instanceof Error ? error.message : '角色分配失败'
    toast.error(message)
  } finally {
    assignDialogLoading.value = false
  }
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
            <Input v-model="filters.keyword" placeholder="搜索用户名、昵称、邮箱、电话" @keyup.enter="handleSearch" />
          </div>
          <div class="space-y-2">
            <Label>部门</Label>
            <Select v-model="filters.deptId">
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
                  @click="canEditUser && handleStatusChange(user.id, user.status)"
                >
                  {{ user.status === 1 ? '正常' : '禁用' }}
                </Badge>
              </td>
              <td class="p-4 text-sm text-muted-foreground">{{ user.createTime }}</td>
              <td class="p-4">
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

    <Dialog v-if="canAddUser || canEditUser" v-model:open="dialogOpen">
      <DialogContent class="sm:max-w-[560px]">
        <DialogHeader>
          <DialogTitle>{{ isEdit ? '编辑用户' : '新增用户' }}</DialogTitle>
          <DialogDescription>{{ isEdit ? '修改用户基础信息' : '创建新的系统用户' }}</DialogDescription>
        </DialogHeader>
        <div v-if="dialogLoading" class="py-8 text-center text-muted-foreground">加载中...</div>
        <div v-else class="space-y-4 py-2">
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>用户名</Label>
              <Input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
            </div>
            <div class="space-y-2">
              <Label>昵称</Label>
              <Input v-model="form.nickname" placeholder="请输入昵称" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>邮箱</Label>
              <Input v-model="form.email" type="email" placeholder="请输入邮箱" />
            </div>
            <div class="space-y-2">
              <Label>手机号</Label>
              <Input v-model="form.phone" placeholder="请输入手机号" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>部门</Label>
              <Select v-model="form.deptId">
                <SelectTrigger>
                  <SelectValue placeholder="请选择部门" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem v-for="dept in deptOptions" :key="dept.id" :value="dept.id">
                    {{ dept.label }}
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="space-y-2">
              <Label>状态</Label>
              <Select v-model="form.status">
                <SelectTrigger>
                  <SelectValue placeholder="请选择状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">正常</SelectItem>
                  <SelectItem value="0">禁用</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <div v-if="!isEdit" class="space-y-2">
            <Label>初始密码</Label>
            <Input v-model="form.password" type="password" placeholder="12 位以上，需包含大小写字母、数字和特殊字符" />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="dialogOpen = false">取消</Button>
          <Button :disabled="dialogLoading || rolesLoading" @click="handleSubmit">{{ isEdit ? '保存' : '创建' }}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <Dialog v-if="canEditUser" v-model:open="passwordDialogOpen">
      <DialogContent class="sm:max-w-[420px]">
        <DialogHeader>
          <DialogTitle>重置密码</DialogTitle>
          <DialogDescription>为用户 {{ resetUsername }} 设置新的登录密码</DialogDescription>
        </DialogHeader>
        <div class="space-y-2 py-2">
          <Label>新密码</Label>
          <div class="relative">
            <Input
              v-model="newPassword"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入新密码"
              class="pr-10"
            />
            <Button
              type="button"
              variant="ghost"
              size="icon"
              class="absolute right-0 top-0 h-full px-3 hover:bg-transparent"
              @click="showPassword = !showPassword"
            >
              <Eye v-if="!showPassword" class="h-4 w-4 text-muted-foreground" />
              <EyeOff v-else class="h-4 w-4 text-muted-foreground" />
            </Button>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="passwordDialogOpen = false">取消</Button>
          <Button :disabled="passwordDialogLoading" @click="handleResetPassword">
            {{ passwordDialogLoading ? '处理中...' : '确认重置' }}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <Dialog v-if="canAssignUser" v-model:open="assignDialogOpen">
      <DialogContent class="sm:max-w-[560px]">
        <DialogHeader>
          <DialogTitle>分配角色{{ assignUser ? ` - ${assignUser.username}` : '' }}</DialogTitle>
          <DialogDescription>为当前用户分配系统角色</DialogDescription>
        </DialogHeader>
        <div v-if="assignDialogLoading" class="py-8 text-center text-muted-foreground">加载中...</div>
        <ScrollArea v-else class="max-h-[360px] rounded-md border">
          <div class="space-y-1 p-4">
            <label
              v-for="role in roleList"
              :key="role.id"
              class="flex cursor-pointer items-center gap-3 rounded px-3 py-2 transition-colors hover:bg-muted/50"
            >
              <Checkbox
                :checked="selectedRoleIds.includes(role.id)"
                @update:checked="toggleRoleSelection(role.id, Boolean($event))"
              />
              <div>
                <p class="text-sm font-medium">{{ getRoleName(role) }}</p>
                <p class="text-xs text-muted-foreground">{{ getRoleCode(role) }}</p>
              </div>
            </label>
          </div>
        </ScrollArea>
        <DialogFooter>
          <Button variant="outline" @click="assignDialogOpen = false">取消</Button>
          <Button :disabled="assignDialogLoading" @click="handleAssignSubmit">保存角色</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <AlertDialog v-if="canDeleteUser" v-model:open="deleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认删除用户</AlertDialogTitle>
          <AlertDialogDescription>删除后不可恢复，如果用户仍有关联数据，后端可能会拒绝删除。</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction @click="handleDelete">确认删除</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>
