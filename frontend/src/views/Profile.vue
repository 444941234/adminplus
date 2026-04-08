<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ProfileInfo from '@/components/profile/ProfileInfo.vue'
import ProfileSecurity from '@/components/profile/ProfileSecurity.vue'
import ActivityDashboard from '@/components/profile/ActivityDashboard.vue'
import QuickSettings from '@/components/profile/QuickSettings.vue'
import { getProfile, updateProfile, getActivityStats } from '@/api'
import type { Profile, ActivityStats } from '@/types'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'

// State management
const profile = ref<Profile | null>(null)
const activityStats = ref<ActivityStats | null>(null)
const { loading: profileLoading, run: runProfile } = useAsyncAction('获取个人资料失败')
const { loading: activityLoading, run: runActivity } = useAsyncAction()
const { loading: updating, run: runUpdate } = useAsyncAction('更新失败')

// Compute user initials
const initials = computed(() => {
  if (!profile.value) return '?'
  const name = profile.value.nickname || profile.value.username
  return name.charAt(0).toUpperCase()
})

// Data fetching
const fetchProfile = () => runProfile(async () => {
  const res = await getProfile()
  profile.value = res.data
})

const fetchActivityStats = () => runActivity(async () => {
  const res = await getActivityStats()
  activityStats.value = res.data
}, { onError: () => {} })

// Profile update handlers
const handleUpdateField = (field: keyof Profile, value: string) => {
  if (!profile.value) return

  runUpdate(async () => {
    await updateProfile({ [field]: value })
  }, {
    successMessage: '更新成功',
    onSuccess: () => {
      profile.value = { ...profile.value!, [field]: value } as Profile
    },
    onError: () => fetchProfile()
  })
}

// Initialize data
onMounted(() => {
  fetchProfile()
  fetchActivityStats()
})
</script>

<template>
  <div class="profile-page">
    <!-- Loading State -->
    <div
      v-if="profileLoading"
      class="profile-page__loading"
    >
      <p>加载个人资料中...</p>
    </div>

    <!-- Main Content -->
    <div
      v-else-if="profile"
      class="profile-page__content"
    >
      <!-- User Card - Compact header with avatar and key info -->
      <div class="user-card">
        <Avatar class="user-card__avatar">
          <AvatarFallback>{{ initials }}</AvatarFallback>
        </Avatar>
        <div class="user-card__info">
          <h1 class="user-card__name">
            {{ profile.nickname || profile.username }}
          </h1>
          <p class="user-card__username">
            @{{ profile.username }}
          </p>
          <div class="user-card__meta">
            <span
              v-if="profile.email"
              class="user-card__meta-item"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="14"
                height="14"
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
              {{ profile.email }}
            </span>
            <span
              v-if="profile.phone"
              class="user-card__meta-item"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" />
              </svg>
              {{ profile.phone }}
            </span>
            <span
              v-if="profile.deptName"
              class="user-card__meta-item"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                <circle
                  cx="9"
                  cy="7"
                  r="4"
                />
                <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
              {{ profile.deptName }}
            </span>
          </div>
        </div>
        <div class="user-card__roles">
          <span
            v-for="role in profile.roles"
            :key="role"
            class="user-card__role"
          >
            {{ role }}
          </span>
        </div>
      </div>

      <!-- Two-column layout -->
      <div class="profile-page__grid">
        <!-- Left Column: Profile & Security -->
        <div class="profile-page__left">
          <ProfileInfo
            :profile="profile"
            :loading="updating"
            @update-field="handleUpdateField"
          />
          <ProfileSecurity />
        </div>

        <!-- Right Column: Activity & Settings -->
        <div class="profile-page__right">
          <ActivityDashboard
            v-if="activityStats"
            :activity="activityStats"
            :loading="activityLoading"
          />
          <QuickSettings />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.profile-page__loading {
  padding: 60px 0;
  text-align: center;
  font-size: 14px;
  color: rgb(148 163 184);
  border-radius: 16px;
  background: rgb(248 250 252);
}

/* User Card - Compact header */
.user-card {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 24px;
  background: white;
  border: 1px solid rgb(226 232 240);
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.user-card__avatar {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  border-radius: 12px;
  font-size: 24px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.user-card__info {
  flex: 1;
  min-width: 0;
}

.user-card__name {
  font-size: 20px;
  font-weight: 600;
  color: rgb(15 23 42);
  margin: 0 0 4px 0;
}

.user-card__username {
  font-size: 14px;
  color: rgb(100 116 139);
  margin: 0 0 16px 0;
}

.user-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 12px;
}

.user-card__meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgb(100 116 139);
}

.user-card__roles {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.user-card__role {
  padding: 6px 12px;
  background: rgb(241 245 249);
  border: 1px solid rgb(226 232 240);
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: rgb(71 85 105);
}

/* Two-column grid */
.profile-page__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: start;
}

.profile-page__left,
.profile-page__right {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Tablet - Single column */
@media (max-width: 900px) {
  .profile-page__grid {
    grid-template-columns: 1fr;
  }
}

/* Mobile - Compact layout */
@media (max-width: 640px) {
  .profile-page {
    padding: 16px;
  }

  .user-card {
    flex-direction: column;
    text-align: center;
    gap: 16px;
    padding: 20px;
  }

  .user-card__avatar {
    width: 56px;
    height: 56px;
    font-size: 20px;
  }

  .user-card__name {
    font-size: 18px;
  }

  .user-card__meta {
    justify-content: center;
  }

  .user-card__roles {
    justify-content: center;
  }
}
</style>