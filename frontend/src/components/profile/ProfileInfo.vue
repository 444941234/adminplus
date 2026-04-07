<script setup lang="ts">
import { computed } from 'vue'
import InlineEditField from './InlineEditField.vue'
import type { Profile } from '@/types'

interface Props {
  profile: Profile
  loading?: boolean
}

interface Emits {
  (_e: 'updateField', _field: keyof Profile, _value: string): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<Emits>()

const roleList = computed(() => {
  return props.profile.roles?.filter(Boolean) ?? []
})

const handleSave = (field: keyof Profile, value: string) => {
  emit('updateField', field, value)
}

const handleSaveNickname = (value: string) => handleSave('nickname', value)
const handleSaveEmail = (value: string) => handleSave('email', value)
const handleSavePhone = (value: string) => handleSave('phone', value)
</script>

<template>
  <div class="profile-info">
    <div class="profile-info__header">
      <h3 class="profile-info__title">
        个人资料
      </h3>
    </div>

    <div class="profile-info__content">
      <!-- Nickname -->
      <InlineEditField
        :model-value="profile.nickname || ''"
        label="显示名称"
        placeholder="请输入显示名称"
        :loading="loading"
        @save="handleSaveNickname"
      />

      <!-- Email -->
      <InlineEditField
        :model-value="profile.email || ''"
        label="邮箱"
        type="email"
        placeholder="请输入邮箱"
        :loading="loading"
        @save="handleSaveEmail"
      />

      <!-- Phone -->
      <InlineEditField
        :model-value="profile.phone || ''"
        label="手机号"
        type="tel"
        placeholder="请输入手机号"
        :loading="loading"
        @save="handleSavePhone"
      />

      <!-- Department -->
      <InlineEditField
        :model-value="profile.deptName || ''"
        label="部门"
        placeholder="未分配"
        readonly
      />

      <!-- Roles -->
      <div class="profile-info__field">
        <label class="profile-info__label">角色</label>
        <div class="profile-info__roles">
          <span
            v-for="role in roleList"
            :key="role"
            class="profile-info__role"
          >
            {{ role }}
          </span>
          <span
            v-if="roleList.length === 0"
            class="profile-info__role--empty"
          >
            未分配
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-info {
  border: 1px solid rgb(226 232 240);
  border-radius: 16px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.profile-info__header {
  padding: 20px;
  border-bottom: 1px solid rgb(226 232 240);
}

.profile-info__title {
  font-size: 16px;
  font-weight: 600;
  color: rgb(15 23 42);
  margin: 0;
}

.profile-info__content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-info__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-info__label {
  font-size: 13px;
  font-weight: 500;
  color: rgb(100 116 139);
}

.profile-info__roles {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.profile-info__role {
  padding: 6px 12px;
  background: rgb(241 245 249);
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: rgb(71 85 105);
}

.profile-info__role--empty {
  font-size: 13px;
  color: rgb(148 163 184);
}

@media (max-width: 640px) {
  .profile-info__header,
  .profile-info__content {
    padding: 16px;
  }
}
</style>
