<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { Button, Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, Input, Label, Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui'
import { createUser, getUserById, updateUser } from '@/api'
import { isValidChinaPhone, isValidEmail, isStrongPassword } from '@/lib/validators'
import { toast } from 'vue-sonner'

interface Props {
  open: boolean
  editId?: string
  deptOptions: Array<{ id: string; label: string }>
}

const props = withDefaults(defineProps<Props>(), {
  editId: ''
})

const emit = defineEmits<{
  (_e: 'update:open', _value: boolean): void
  (_e: 'success'): void
}>()

const isEdit = computed(() => !!props.editId)
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: '',
  deptId: '0',
  status: '1'
})

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

// 监听 open 变化，加载编辑数据
watch(
  () => props.open,
  async (isOpen) => {
    if (isOpen) {
      if (props.editId) {
        // 编辑模式：加载数据
        loading.value = true
        try {
          const res = await getUserById(props.editId)
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
          emit('update:open', false)
        } finally {
          loading.value = false
        }
      } else {
        // 新增模式：重置表单
        resetForm()
      }
    }
  }
)

const handleSubmit = async () => {
  // 表单验证
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

  loading.value = true
  try {
    if (isEdit.value) {
      await updateUser(props.editId, {
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
    emit('update:open', false)
    emit('success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '保存用户失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

const handleOpenChange = (value: boolean) => {
  emit('update:open', value)
}
</script>

<template>
  <Dialog
    :open="open"
    @update:open="handleOpenChange"
  >
    <DialogContent class="sm:max-w-[560px]">
      <DialogHeader>
        <DialogTitle>{{ isEdit ? '编辑用户' : '新增用户' }}</DialogTitle>
        <DialogDescription>{{ isEdit ? '修改用户基础信息' : '创建新的系统用户' }}</DialogDescription>
      </DialogHeader>
      <div
        v-if="loading"
        class="py-8 text-center text-muted-foreground"
      >
        加载中...
      </div>
      <div
        v-else
        class="space-y-4 py-2"
      >
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label for="username">用户名</Label>
            <Input
              id="username"
              v-model="form.username"
              :disabled="isEdit"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </div>
          <div class="space-y-2">
            <Label for="nickname">昵称</Label>
            <Input
              id="nickname"
              v-model="form.nickname"
              placeholder="请输入昵称"
            />
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label for="email">邮箱</Label>
            <Input
              id="email"
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱"
              autocomplete="email"
            />
          </div>
          <div class="space-y-2">
            <Label for="phone">手机号</Label>
            <Input
              id="phone"
              v-model="form.phone"
              placeholder="请输入手机号"
              autocomplete="tel"
            />
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label for="deptId">部门</Label>
            <Select v-model="form.deptId">
              <SelectTrigger id="deptId">
                <SelectValue placeholder="请选择部门" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="dept in deptOptions"
                  :key="dept.id"
                  :value="dept.id"
                >
                  {{ dept.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-2">
            <Label for="status">状态</Label>
            <Select v-model="form.status">
              <SelectTrigger id="status">
                <SelectValue placeholder="请选择状态" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="1">
                  正常
                </SelectItem>
                <SelectItem value="0">
                  禁用
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
        <div
          v-if="!isEdit"
          class="space-y-2"
        >
          <Label for="password">初始密码</Label>
          <Input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="12 位以上，需包含大小写字母、数字和特殊字符"
            autocomplete="new-password"
          />
        </div>
      </div>
      <DialogFooter>
        <Button
          variant="outline"
          @click="handleOpenChange(false)"
        >
          取消
        </Button>
        <Button
          :disabled="loading"
          @click="handleSubmit"
        >
          {{ isEdit ? '保存' : '创建' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>