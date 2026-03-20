<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Switch } from '@/components/ui/switch'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui/select'
import { Button } from '@/components/ui/button'
import { toast } from 'vue-sonner'
import { getUserSettings, updateUserSettings } from '@/api'
import type { UserSettings } from '@/types'
import { LANGUAGE_OPTIONS } from '@/constants/languages'

const loading = ref(false)
const updating = ref<Record<string, boolean>>({})
const settings = ref<UserSettings>({
  notifications: true,
  darkMode: false,
  emailUpdates: true,
  language: 'zh-CN'
})

const fetchSettings = async () => {
  loading.value = true
  try {
    const res = await getUserSettings()
    settings.value = res.data
  } catch (error) {
    console.error('Failed to fetch settings:', error)
    toast.error('获取设置失败')
  } finally {
    loading.value = false
  }
}

const updateSetting = async <K extends keyof UserSettings>(
  key: K,
  value: UserSettings[K]
) => {
  const settingKey = String(key)
  updating.value[settingKey] = true
  try {
    await updateUserSettings({ [key]: value })
    settings.value[key] = value
    toast.success('设置更新成功')
  } catch (error) {
    console.error('Failed to update setting:', error)
    toast.error('设置更新失败')
    // Revert on error
    await fetchSettings()
  } finally {
    updating.value[settingKey] = false
  }
}

const handleNotificationChange = (checked: boolean) => {
  updateSetting('notifications', checked)
}

const handleDarkModeChange = (checked: boolean) => {
  updateSetting('darkMode', checked)
}

const handleEmailUpdatesChange = (checked: boolean) => {
  updateSetting('emailUpdates', checked)
}

const handleLanguageChange = (value: unknown) => {
  if (typeof value === 'string') {
    updateSetting('language', value)
  }
}

const handleViewAllSettings = () => {
  // Navigate to full settings page
  toast.info('跳转到完整设置页面')
}

onMounted(fetchSettings)
</script>

<template>
  <div class="quick-settings">
    <div class="quick-settings__header">
      <h3 class="quick-settings__title">快捷设置</h3>
      <p class="quick-settings__subtitle">快速管理您的偏好设置</p>
    </div>

    <div v-if="loading" class="quick-settings__loading">
      <p>加载设置中...</p>
    </div>

    <div v-else class="quick-settings__content">
      <!-- Notification Settings -->
      <div class="setting-item">
        <div class="setting-item__info">
          <div class="setting-item__icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/>
              <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>
            </svg>
          </div>
          <div class="setting-item__details">
            <p class="setting-item__label">通知提醒</p>
            <p class="setting-item__description">接收系统通知和提醒</p>
          </div>
        </div>
        <Switch
          :checked="settings.notifications"
          :disabled="updating['notifications']"
          @update:checked="handleNotificationChange"
        />
      </div>

      <!-- Dark Mode -->
      <div class="setting-item">
        <div class="setting-item__info">
          <div class="setting-item__icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>
            </svg>
          </div>
          <div class="setting-item__details">
            <p class="setting-item__label">深色模式</p>
            <p class="setting-item__description">切换深色主题外观</p>
          </div>
        </div>
        <Switch
          :checked="settings.darkMode"
          :disabled="updating['darkMode']"
          @update:checked="handleDarkModeChange"
        />
      </div>

      <!-- Email Updates -->
      <div class="setting-item">
        <div class="setting-item__info">
          <div class="setting-item__icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect width="20" height="16" x="2" y="4" rx="2"/>
              <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
            </svg>
          </div>
          <div class="setting-item__details">
            <p class="setting-item__label">邮件更新</p>
            <p class="setting-item__description">通过邮件接收更新通知</p>
          </div>
        </div>
        <Switch
          :checked="settings.emailUpdates"
          :disabled="updating['emailUpdates']"
          @update:checked="handleEmailUpdatesChange"
        />
      </div>

      <!-- Language Selection -->
      <div class="setting-item">
        <div class="setting-item__info">
          <div class="setting-item__icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="2" x2="22" y1="12" y2="12"/>
              <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
            </svg>
          </div>
          <div class="setting-item__details">
            <p class="setting-item__label">语言设置</p>
            <p class="setting-item__description">选择界面显示语言</p>
          </div>
        </div>
        <Select
          :model-value="settings.language"
          @update:model-value="handleLanguageChange"
          :disabled="updating['language']"
        >
          <SelectTrigger class="w-40">
            <SelectValue placeholder="选择语言" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem
              v-for="option in LANGUAGE_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </SelectItem>
          </SelectContent>
        </Select>
      </div>

      <!-- View All Button -->
      <div class="quick-settings__footer">
        <Button
          variant="outline"
          class="w-full"
          @click="handleViewAllSettings"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2">
            <circle cx="12" cy="12" r="3"/>
            <path d="M12 1v6m0 6v6"/>
            <path d="m1 12h6m6 0h6"/>
          </svg>
          查看所有设置
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.quick-settings {
  border: 1px solid rgb(226 232 240);
  border-radius: 16px;
  background: white;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.quick-settings__header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgb(226 232 240);
}

.quick-settings__title {
  font-size: 18px;
  font-weight: 600;
  color: rgb(15 23 42);
  margin: 0 0 4px 0;
}

.quick-settings__subtitle {
  font-size: 13px;
  color: rgb(100 116 139);
  margin: 0;
}

.quick-settings__loading {
  padding: 40px 0;
  text-align: center;
  font-size: 14px;
  color: rgb(148 163 184);
}

.quick-settings__content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-radius: 12px;
  background: rgb(248 250 252);
  border: 1px solid rgb(226 232 240);
  transition: all 0.2s ease;
}

.setting-item:hover {
  background: rgb(241 245 249);
  border-color: rgb(203 213 225);
}

.setting-item__info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.setting-item__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: white;
  color: rgb(59 130 246);
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.setting-item__details {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.setting-item__label {
  font-size: 14px;
  font-weight: 500;
  color: rgb(15 23 42);
  margin: 0;
}

.setting-item__description {
  font-size: 12px;
  color: rgb(100 116 139);
  margin: 0;
  line-height: 1.4;
}

.quick-settings__footer {
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid rgb(226 232 240);
}

/* Mobile Responsive */
@media (max-width: 640px) {
  .quick-settings {
    padding: 16px;
  }

  .quick-settings__header {
    margin-bottom: 16px;
  }

  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .setting-item__info {
    width: 100%;
  }

  .setting-item > :deep(.switch-wrapper),
  .setting-item > :deep([role="combobox"]) {
    align-self: flex-end;
  }
}
</style>
