<script setup lang="ts">
import { ref } from 'vue'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { toast } from 'vue-sonner'
import { changePassword } from '@/api'
import { isStrongPassword } from '@/lib/validators'

const isPasswordDialogOpen = ref(false)
const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const isChangingPassword = ref(false)

const validationErrors = ref<{
  currentPassword?: string
  newPassword?: string
  confirmPassword?: string
}>({})

const PASSWORD_REQUIREMENT_MSG = '密码需 12 位以上，且包含大小写字母、数字和特殊字符'

const validateForm = (): boolean => {
  const errors: typeof validationErrors.value = {}

  if (!currentPassword.value.trim()) {
    errors.currentPassword = '请输入当前密码'
  }

  if (!newPassword.value.trim()) {
    errors.newPassword = '请输入新密码'
  } else if (!isStrongPassword(newPassword.value)) {
    errors.newPassword = PASSWORD_REQUIREMENT_MSG
  }

  if (!confirmPassword.value.trim()) {
    errors.confirmPassword = '请确认新密码'
  } else if (confirmPassword.value !== newPassword.value) {
    errors.confirmPassword = '两次输入的密码不一致'
  }

  validationErrors.value = errors
  return Object.keys(errors).length === 0
}

const handlePasswordChange = async () => {
  if (!validateForm()) return

  try {
    isChangingPassword.value = true
    await changePassword(currentPassword.value, newPassword.value)
    toast.success('密码修改成功')
    isPasswordDialogOpen.value = false
    resetPasswordForm()
  } catch (error) {
    const err = error as { response?: { data?: { message?: string } } }
    toast.error(err.response?.data?.message || '密码修改失败，请重试')
  } finally {
    isChangingPassword.value = false
  }
}

const resetPasswordForm = () => {
  currentPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  validationErrors.value = {}
}

const handleDialogOpenChange = (open: boolean) => {
  isPasswordDialogOpen.value = open
  if (!open) resetPasswordForm()
}
</script>

<template>
  <div class="profile-security">
    <div class="profile-security__header">
      <h3 class="profile-security__title">
        安全设置
      </h3>
    </div>

    <div class="profile-security__content">
      <div class="security-item">
        <div class="security-item__left">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <rect
              width="18"
              height="11"
              x="3"
              y="11"
              rx="2"
              ry="2"
            />
            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
          </svg>
          <div class="security-item__info">
            <span class="security-item__label">登录密码</span>
            <span class="security-item__status">已设置</span>
          </div>
        </div>
        <Dialog
          :open="isPasswordDialogOpen"
          @update:open="handleDialogOpenChange"
        >
          <DialogTrigger as-child>
            <Button
              variant="outline"
              size="sm"
            >
              修改
            </Button>
          </DialogTrigger>
          <DialogContent class="sm:max-w-md">
            <DialogHeader>
              <DialogTitle>修改密码</DialogTitle>
              <DialogDescription>
                输入当前密码和新密码
              </DialogDescription>
            </DialogHeader>

            <div class="password-form">
              <div class="password-field">
                <Label for="current-password">当前密码</Label>
                <Input
                  id="current-password"
                  v-model="currentPassword"
                  type="password"
                  placeholder="请输入当前密码"
                  :class="{ 'input--error': validationErrors.currentPassword }"
                  @keyup.enter="handlePasswordChange"
                />
                <span
                  v-if="validationErrors.currentPassword"
                  class="field-error"
                >
                  {{ validationErrors.currentPassword }}
                </span>
              </div>

              <div class="password-field">
                <Label for="new-password">新密码</Label>
                <Input
                  id="new-password"
                  v-model="newPassword"
                  type="password"
                  placeholder="12位以上，含大小写字母、数字、特殊字符"
                  :class="{ 'input--error': validationErrors.newPassword }"
                  @keyup.enter="handlePasswordChange"
                />
                <span
                  v-if="validationErrors.newPassword"
                  class="field-error"
                >
                  {{ validationErrors.newPassword }}
                </span>
              </div>

              <div class="password-field">
                <Label for="confirm-password">确认新密码</Label>
                <Input
                  id="confirm-password"
                  v-model="confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  :class="{ 'input--error': validationErrors.confirmPassword }"
                  @keyup.enter="handlePasswordChange"
                />
                <span
                  v-if="validationErrors.confirmPassword"
                  class="field-error"
                >
                  {{ validationErrors.confirmPassword }}
                </span>
              </div>
            </div>

            <DialogFooter>
              <Button
                variant="outline"
                @click="handleDialogOpenChange(false)"
              >
                取消
              </Button>
              <Button
                :disabled="isChangingPassword"
                @click="handlePasswordChange"
              >
                {{ isChangingPassword ? '修改中...' : '确认修改' }}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-security {
  border: 1px solid rgb(226 232 240);
  border-radius: 16px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.profile-security__header {
  padding: 20px;
  border-bottom: 1px solid rgb(226 232 240);
}

.profile-security__title {
  font-size: 16px;
  font-weight: 600;
  color: rgb(15 23 42);
  margin: 0;
}

.profile-security__content {
  padding: 20px;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: rgb(248 250 252);
  border-radius: 8px;
}

.security-item__left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.security-item__left svg {
  color: rgb(100 116 139);
}

.security-item__info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.security-item__label {
  font-size: 13px;
  font-weight: 500;
  color: rgb(15 23 42);
}

.security-item__status {
  font-size: 12px;
  color: rgb(100 116 139);
}

/* Password Dialog Form */
.password-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 0;
}

.password-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input--error {
  border-color: rgb(239 68 68) !important;
}

.field-error {
  font-size: 12px;
  color: rgb(239 68 68);
}

@media (max-width: 640px) {
  .profile-security__header,
  .profile-security__content {
    padding: 16px;
  }
}
</style>
