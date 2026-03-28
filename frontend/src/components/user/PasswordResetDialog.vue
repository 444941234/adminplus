<script setup lang="ts">
import { ref, watch } from 'vue'
import { Button, Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, Input, Label } from '@/components/ui'
import { Eye, EyeOff } from 'lucide-vue-next'
import { resetPassword } from '@/api'
import { isStrongPassword } from '@/lib/validators'
import { toast } from 'vue-sonner'

interface Props {
  open: boolean
  userId?: string
  username?: string
}

const props = withDefaults(defineProps<Props>(), {
  userId: '',
  username: ''
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const loading = ref(false)
const newPassword = ref('')
const showPassword = ref(false)

// 重置状态
watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      newPassword.value = ''
      showPassword.value = false
    }
  }
)

const handleSubmit = async () => {
  if (!newPassword.value.trim()) {
    toast.warning('请输入新密码')
    return
  }
  if (!isStrongPassword(newPassword.value.trim())) {
    toast.warning('密码需 12 位以上，且包含大小写字母、数字和特殊字符')
    return
  }

  loading.value = true
  try {
    await resetPassword(props.userId, newPassword.value.trim())
    toast.success('密码重置成功')
    emit('update:open', false)
    emit('success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '重置密码失败'
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
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent class="sm:max-w-[420px]">
      <DialogHeader>
        <DialogTitle>重置密码</DialogTitle>
        <DialogDescription>为用户 {{ username }} 设置新的登录密码</DialogDescription>
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
        <Button variant="outline" @click="handleOpenChange(false)">取消</Button>
        <Button :disabled="loading" @click="handleSubmit">
          {{ loading ? '处理中...' : '确认重置' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>