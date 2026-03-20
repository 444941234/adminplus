<script setup lang="ts">
import { ref } from 'vue'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
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
import { changePassword } from '@/api'

// Password change dialog state
const isPasswordDialogOpen = ref(false)
const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const isChangingPassword = ref(false)
const passwordError = ref('')

// Validation errors
const validationErrors = ref<{
  currentPassword?: string
  newPassword?: string
  confirmPassword?: string
}>({})

// Validate password form
const validateForm = (): boolean => {
  const errors: typeof validationErrors.value = {}

  // Check all fields are filled
  if (!currentPassword.value.trim()) {
    errors.currentPassword = 'Current password is required'
  }

  if (!newPassword.value.trim()) {
    errors.newPassword = 'New password is required'
  } else if (newPassword.value.length < 6) {
    errors.newPassword = 'Password must be at least 6 characters'
  }

  if (!confirmPassword.value.trim()) {
    errors.confirmPassword = 'Please confirm your password'
  } else if (confirmPassword.value !== newPassword.value) {
    errors.confirmPassword = 'Passwords do not match'
  }

  validationErrors.value = errors
  return Object.keys(errors).length === 0
}

// Handle password change
const handlePasswordChange = async () => {
  // Clear previous errors
  passwordError.value = ''

  // Validate form
  if (!validateForm()) {
    return
  }

  try {
    isChangingPassword.value = true
    await changePassword(currentPassword.value, newPassword.value)

    // Close dialog and reset form on success
    isPasswordDialogOpen.value = false
    resetPasswordForm()
  } catch (error: any) {
    passwordError.value = error.response?.data?.message || 'Failed to change password. Please try again.'
  } finally {
    isChangingPassword.value = false
  }
}

// Reset password form
const resetPasswordForm = () => {
  currentPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  passwordError.value = ''
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
    <Card>
      <CardHeader>
        <CardTitle>Password</CardTitle>
        <CardDescription>
          Change your password to keep your account secure
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Dialog :open="isPasswordDialogOpen" @update:open="handleDialogOpenChange">
          <DialogTrigger as-child>
            <Button variant="outline">Change Password</Button>
          </DialogTrigger>
          <DialogContent class="sm:max-w-md">
            <DialogHeader>
              <DialogTitle>Change Password</DialogTitle>
              <DialogDescription>
                Enter your current password and a new password to update your credentials
              </DialogDescription>
            </DialogHeader>

            <div class="password-form space-y-4 py-4">
              <!-- Error message -->
              <div v-if="passwordError" class="password-form__error">
                {{ passwordError }}
              </div>

              <!-- Current Password -->
              <div class="password-form__field space-y-2">
                <Label for="current-password">Current Password</Label>
                <Input
                  id="current-password"
                  v-model="currentPassword"
                  type="password"
                  placeholder="Enter current password"
                  :class="{ 'password-form__input--error': validationErrors.currentPassword }"
                  @keyup.enter="handlePasswordChange"
                />
                <div v-if="validationErrors.currentPassword" class="password-form__field-error">
                  {{ validationErrors.currentPassword }}
                </div>
              </div>

              <!-- New Password -->
              <div class="password-form__field space-y-2">
                <Label for="new-password">New Password</Label>
                <Input
                  id="new-password"
                  v-model="newPassword"
                  type="password"
                  placeholder="Enter new password (min 6 characters)"
                  :class="{ 'password-form__input--error': validationErrors.newPassword }"
                  @keyup.enter="handlePasswordChange"
                />
                <div v-if="validationErrors.newPassword" class="password-form__field-error">
                  {{ validationErrors.newPassword }}
                </div>
              </div>

              <!-- Confirm Password -->
              <div class="password-form__field space-y-2">
                <Label for="confirm-password">Confirm New Password</Label>
                <Input
                  id="confirm-password"
                  v-model="confirmPassword"
                  type="password"
                  placeholder="Confirm new password"
                  :class="{ 'password-form__input--error': validationErrors.confirmPassword }"
                  @keyup.enter="handlePasswordChange"
                />
                <div v-if="validationErrors.confirmPassword" class="password-form__field-error">
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
                Cancel
              </Button>
              <Button
                type="button"
                :disabled="isChangingPassword"
                @click="handlePasswordChange"
              >
                {{ isChangingPassword ? 'Changing...' : 'Change Password' }}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </CardContent>
    </Card>

    <!-- Two-Factor Authentication Section -->
    <Card>
      <CardHeader>
        <CardTitle>Two-Factor Authentication</CardTitle>
        <CardDescription>
          Add an extra layer of security to your account
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div class="security-card__2fa">
          <div class="security-card__2fa-info">
            <p class="security-card__2fa-status">Not enabled</p>
            <p class="security-card__2fa-description">
              Protect your account with 2FA via SMS or authenticator app
            </p>
          </div>
          <Button variant="outline" disabled>
            Coming Soon
          </Button>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<style scoped>
.profile-security {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.password-form {
  display: flex;
  flex-direction: column;
}

.password-form__error {
  padding: 12px;
  background-color: rgb(254 242 242);
  border: 1px solid rgb(254 226 226);
  border-radius: 6px;
  color: rgb(185 28 28);
  font-size: 14px;
  line-height: 1.5;
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

/* Mobile responsive */
@media (max-width: 640px) {
  .profile-security {
    gap: 20px;
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
