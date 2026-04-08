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
  } catch {
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
    toast.success('设置已更新')
  } catch {
    toast.error('设置更新失败')
    await fetchSettings()
  } finally {
    updating.value[settingKey] = false
  }
}

onMounted(fetchSettings)
</script>

<template>
  <div class="quick-settings">
    <div class="quick-settings__header">
      <h3 class="quick-settings__title">
        快捷设置
      </h3>
    </div>

    <div
      v-if="loading"
      class="quick-settings__loading"
    >
      加载中...
    </div>

    <div
      v-else
      class="quick-settings__content"
    >
      <!-- Notifications -->
      <div class="setting-item">
        <div class="setting-item__left">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
            <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />
          </svg>
          <span>通知提醒</span>
        </div>
        <Switch
          :model-value="settings.notifications"
          :disabled="updating['notifications']"
          @update:model-value="(v: boolean) => updateSetting('notifications', v)"
        />
      </div>

      <!-- Dark Mode -->
      <div class="setting-item">
        <div class="setting-item__left">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z" />
          </svg>
          <span>深色模式</span>
        </div>
        <Switch
          :model-value="settings.darkMode"
          :disabled="updating['darkMode']"
          @update:model-value="(v: boolean) => updateSetting('darkMode', v)"
        />
      </div>

      <!-- Email Updates -->
      <div class="setting-item">
        <div class="setting-item__left">
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
              width="20"
              height="16"
              x="2"
              y="4"
              rx="2"
            />
            <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
          </svg>
          <span>邮件更新</span>
        </div>
        <Switch
          :model-value="settings.emailUpdates"
          :disabled="updating['emailUpdates']"
          @update:model-value="(v: boolean) => updateSetting('emailUpdates', v)"
        />
      </div>

      <!-- Language -->
      <div class="setting-item">
        <div class="setting-item__left">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <circle
              cx="12"
              cy="12"
              r="10"
            />
            <line
              x1="2"
              x2="22"
              y1="12"
              y2="12"
            />
            <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
          </svg>
          <span>语言</span>
        </div>
        <Select
          :model-value="settings.language"
          :disabled="updating['language']"
          @update:model-value="(v) => updateSetting('language', v as string)"
        >
          <SelectTrigger class="setting-item__select">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem
              v-for="opt in LANGUAGE_OPTIONS"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </SelectItem>
          </SelectContent>
        </Select>
      </div>
    </div>
  </div>
</template>

<style scoped>
.quick-settings {
  border: 1px solid rgb(226 232 240);
  border-radius: 16px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.quick-settings__header {
  padding: 20px;
  border-bottom: 1px solid rgb(226 232 240);
}

.quick-settings__title {
  font-size: 16px;
  font-weight: 600;
  color: rgb(15 23 42);
  margin: 0;
}

.quick-settings__loading {
  padding: 40px 20px;
  text-align: center;
  font-size: 14px;
  color: rgb(148 163 184);
}

.quick-settings__content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid rgb(241 245 249);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item__left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 500;
  color: rgb(15 23 42);
}

.setting-item__left svg {
  color: rgb(100 116 139);
  flex-shrink: 0;
}

.setting-item__select {
  width: 120px;
  font-size: 13px;
}

@media (max-width: 640px) {
  .quick-settings__header,
  .quick-settings__content {
    padding: 16px;
  }
}
</style>
