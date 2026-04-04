<script setup lang="ts">
import { ref } from 'vue'
import { Card } from '@/components/ui/card'
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

// Password change dialog state
const isPasswordDialogOpen = ref(false)
const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const isChangingPassword = ref(false)

// Validation errors
const validationErrors = ref<{
  currentPassword?: string
  newPassword?: string
  confirmPassword?: string
}>({})

// Password requirement message (matches isStrongPassword validator)
const PASSWORD_REQUIREMENT_MSG = '密码需 12 位以上，且包含大小写字母、数字和特殊字符'

// Validate password form
const validateForm = (): boolean => {
  const errors: typeof validationErrors.value = {}

  // Check all fields are filled
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

// Handle password change
const handlePasswordChange = async () => {
  // Validate form
  if (!validateForm()) {
    return
  }

  try {
    isChangingPassword.value = true
    await changePassword(currentPassword.value, newPassword.value)

    // Show success message
    toast.success('密码修改成功')

    // Close dialog and reset form on success
    isPasswordDialogOpen.value = false
    resetPasswordForm()
  } catch (error) {
    const err = error as { response?: { data?: { message?: string } } }
    toast.error(err.response?.data?.message || '密码修改失败，请重试')
  } finally {
    isChangingPassword.value = false
  }
}

// Reset password form
const resetPasswordForm = () => {
  currentPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  validationErrors.value = {}
}

// Handle dialog close
const handleDialogOpenChange = (open: boolean) => {
  isPasswordDialogOpen.value = open
  if (!open) {
    resetPasswordForm()
  }
}
</script>

<template>
  <div class="profile-security">
    <!-- Password Section -->
    <Card class="profile-security__card">
      <div class="profile-security__header">
        <h3 class="profile-security__title">
          密码安全
        </h3>
        <p class="profile-security__description">
          定期修改密码以保护账户安全
        </p>
      </div>
      <div class="profile-security__content">
        <Dialog
          :open="isPasswordDialogOpen"
          @update:open="handleDialogOpenChange"
        >
          <DialogTrigger as-child>
            <Button variant="outline">
              修改密码
            </Button>
          </DialogTrigger>
          <DialogContent class="sm:max-w-md">
            <DialogHeader>
              <DialogTitle>修改密码</DialogTitle>
              <DialogDescription>
                输入当前密码和新密码以更新您的凭据
              </DialogDescription>
            </DialogHeader>

            <div class="password-form space-y-4 py-4">
              <!-- Current Password -->
              <div class="password-form__field space-y-2">
                <Label for="current-password">当前密码</Label>
                <Input
                  id="current-password"
                  v-model="currentPassword"
                  type="password"
                  placeholder="请输入当前密码"
                  autocomplete="current-password"
                  :class="{ 'password-form__input--error': validationErrors.currentPassword }"
                  @keyup.enter="handlePasswordChange"
                />
                <div
                  v-if="validationErrors.currentPassword"
                  class="password-form__field-error"
                >
                  {{ validationErrors.currentPassword }}
                </div>
              </div>

              <!-- New Password -->
              <div class="password-form__field space-y-2">
                <Label for="new-password">新密码</Label>
                <Input
                  id="new-password"
                  v-model="newPassword"
                  type="password"
                  placeholder="12 位以上，需包含大小写字母、数字和特殊字符"
                  autocomplete="new-password"
                  :class="{ 'password-form__input--error': validationErrors.newPassword }"
                  @keyup.enter="handlePasswordChange"
                />
                <div
                  v-if="validationErrors.newPassword"
                  class="password-form__field-error"
                >
                  {{ validationErrors.newPassword }}
                </div>
              </div>

              <!-- Confirm Password -->
              <div class="password-form__field space-y-2">
                <Label for="confirm-password">确认新密码</Label>
                <Input
                  id="confirm-password"
                  v-model="confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  autocomplete="new-password"
                  :class="{ 'password-form__input--error': validationErrors.confirmPassword }"
                  @keyup.enter="handlePasswordChange"
                />
                <div
                  v-if="validationErrors.confirmPassword"
                  class="password-form__field-error"
                >
                  {{ validationErrors.confirmPassword }}
                </div>
              </div>
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                @click="handleDialogOpenChange(false)"
              >
                取消
              </Button>
              <Button
                type="button"
                :disabled="isChangingPassword"
                @click="handlePasswordChange"
              >
                {{ isChangingPassword ? '修改中...' : '确认修改' }}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </Card>

    <!-- Two-Factor Authentication Section -->
    <Card class="profile-security__card">
      <div class="profile-security__header">
        <h3 class="profile-security__title">
          双因素认证
        </h3>
        <p class="profile-security__description">
          为您的账户添加额外的安全保护层
        </p>
      </div>
      <div class="profile-security__content">
        <div class="security-card__2fa">
          <div class="security-card__2fa-info">
            <p class="security-card__2fa-status">
              未启用
            </p>
            <p class="security-card__2fa-description">
              通过短信或认证器应用使用 2FA 保护您的账户
            </p>
          </div>
          <Button
            variant="outline"
            disabled
          >
            即将推出
          </Button>
        </div>
      </div>
    </Card>
  </div>
</template>

<style scoped>
.profile-security {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Consistent card styling to match other components */
.profile-security__card :deep(.rounded-lg) {
  border-radius: 16px;
}

.profile-security__card :deep(.shadow-sm) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* Consistent header styling */
.profile-security__header {
  padding: 24px 24px 16px 24px;
  border-bottom: 1px solid rgb(226 232 240);
}

.profile-security__title {
  font-size: 18px;
  font-weight: 600;
  color: rgb(15 23 42);
  margin: 0;
  line-height: 1.4;
}

.profile-security__description {
  font-size: 13px;
  color: rgb(100 116 139);
  margin: 4px 0 0 0;
  line-height: 1.5;
}

.profile-security__content {
  padding: 24px;
}

.password-form {
  display: flex;
  flex-direction: column;
}

.password-form__field {
  display: flex;
  flex-direction: column;
}

.password-form__input--error {
  border-color: rgb(239 68 68) !important;
}

.password-form__field-error {
  font-size: 13px;
  color: rgb(239 68 68);
  margin-top: 4px;
  line-height: 1.4;
}

.security-card__2fa {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.security-card__2fa-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.security-card__2fa-status {
  font-size: 14px;
  font-weight: 500;
  color: rgb(100 116 139);
  margin: 0;
  line-height: 1.5;
}

.security-card__2fa-description {
  font-size: 13px;
  color: rgb(148 163 184);
  margin: 0;
  line-height: 1.5;
}

/* Mobile Responsive */
@media (max-width: 640px) {
  .profile-security {
    gap: 20px;
  }

  .profile-security__header {
    padding: 16px 16px 12px 16px;
  }

  .profile-security__content {
    padding: 16px;
  }

  .security-card__2fa {
    flex-direction: column;
    align-items: flex-start;
  }

  .security-card__2fa-info {
    width: 100%;
  }

  .security-card__2fa button {
    width: 100%;
  }
}
</style>
