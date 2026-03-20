<script setup lang="ts">
import { computed } from 'vue'
import { Badge } from '@/components/ui/badge'
import InlineEditField from './InlineEditField.vue'
import type { Profile } from '@/types'

interface Props {
  profile: Profile
  loading?: boolean
}

interface Emits {
  (e: 'updateField', field: keyof Profile, value: string): void
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
      <div>
        <h3 class="profile-info__title">Profile Information</h3>
        <p class="profile-info__subtitle">Click the edit icon to update your information</p>
      </div>
    </div>

    <div v-if="loading" class="profile-info__loading">
      Loading profile information...
    </div>

    <div v-else class="profile-info__content">
      <!-- Display Name (Editable) -->
      <div class="profile-info__field">
        <InlineEditField
          :model-value="profile.nickname || ''"
          label="Display Name"
          placeholder="Enter your display name"
          :loading="loading"
          @save="handleSaveNickname"
        />
      </div>

      <!-- Email (Editable) -->
      <div class="profile-info__field">
        <InlineEditField
          :model-value="profile.email || ''"
          label="Email"
          type="email"
          placeholder="Enter your email address"
          :loading="loading"
          @save="handleSaveEmail"
        />
      </div>

      <!-- Phone (Editable) -->
      <div class="profile-info__field">
        <InlineEditField
          :model-value="profile.phone || ''"
          label="Phone"
          type="tel"
          placeholder="Enter your phone number"
          :loading="loading"
          @save="handleSavePhone"
        />
      </div>

      <!-- Department (Readonly) -->
      <div class="profile-info__field">
        <InlineEditField
          :model-value="profile.deptName || ''"
          label="Department"
          placeholder="No department assigned"
          readonly
        />
      </div>

      <!-- Roles (Display only) -->
      <div class="profile-info__field profile-info__field--roles">
        <div class="profile-info__roles">
          <label class="profile-info__roles-label">Roles</label>
          <div v-if="roleList.length === 0" class="profile-info__roles-empty">
            <Badge variant="outline">No roles assigned</Badge>
          </div>
          <div v-else class="profile-info__roles-list">
            <Badge v-for="role in roleList" :key="role" variant="secondary" class="role-badge">
              {{ role }}
            </Badge>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-info__header {
  border-bottom: 1px solid rgb(226 232 240);
  padding-bottom: 16px;
}

.profile-info__title {
  font-size: 18px;
  font-weight: 600;
  color: rgb(15 23 42);
  margin: 0;
  line-height: 1.4;
}

.profile-info__subtitle {
  font-size: 14px;
  color: rgb(100 116 139);
  margin: 4px 0 0 0;
  line-height: 1.5;
}

.profile-info__loading {
  padding: 40px 0;
  text-align: center;
  font-size: 14px;
  color: rgb(148 163 184);
}

.profile-info__content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-info__field {
  display: flex;
  flex-direction: column;
}

.profile-info__field--roles {
  margin-top: 8px;
}

.profile-info__roles {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-info__roles-label {
  font-size: 13px;
  font-weight: 500;
  color: rgb(100 116 139);
  letter-spacing: 0.01em;
}

.profile-info__roles-empty {
  display: flex;
}

.profile-info__roles-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.role-badge {
  font-size: 13px;
  padding: 4px 12px;
  font-weight: 500;
}

/* Mobile responsive */
@media (max-width: 640px) {
  .profile-info__header {
    padding-bottom: 12px;
  }

  .profile-info__title {
    font-size: 16px;
  }

  .profile-info__subtitle {
    font-size: 13px;
  }

  .profile-info__content {
    gap: 16px;
  }

  .profile-info__roles-list {
    gap: 6px;
  }

  .role-badge {
    font-size: 12px;
    padding: 3px 10px;
  }
}
</style>
