<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { toast } from 'vue-sonner'
import ProfileHero from '@/components/profile/ProfileHero.vue'
import ProfileInfo from '@/components/profile/ProfileInfo.vue'
import ProfileSecurity from '@/components/profile/ProfileSecurity.vue'
import ActivityDashboard from '@/components/profile/ActivityDashboard.vue'
import QuickSettings from '@/components/profile/QuickSettings.vue'
import CompletionBadge from '@/components/profile/CompletionBadge.vue'
import { getProfile, updateProfile, getActivityStats } from '@/api'
import type { Profile, ActivityStats } from '@/types'

// State management
const profile = ref<Profile | null>(null)
const activityStats = ref<ActivityStats | null>(null)
const profileLoading = ref(false)
const activityLoading = ref(false)
const updating = ref(false)

// Computed properties
const hasAvatar = computed(() => !!profile.value?.avatar)

// Data fetching
const fetchProfile = async () => {
  profileLoading.value = true
  try {
    const res = await getProfile()
    profile.value = res.data
  } catch (error) {
    console.error('Failed to fetch profile:', error)
    toast.error('获取个人资料失败')
  } finally {
    profileLoading.value = false
  }
}

const fetchActivityStats = async () => {
  activityLoading.value = true
  try {
    const res = await getActivityStats()
    activityStats.value = res.data
  } catch (error) {
    console.error('Failed to fetch activity stats:', error)
    console.warn('Activity stats could not be loaded. Some dashboard features may be unavailable.')
    // Don't show toast for activity stats as it's not critical
  } finally {
    activityLoading.value = false
  }
}

// Profile update handlers
const handleUpdateField = async (field: keyof Profile, value: string) => {
  if (!profile.value) return

  updating.value = true
  try {
    await updateProfile({ [field]: value })
    // Update local state with type-safe approach using spread operator
    profile.value = {
      ...profile.value,
      [field]: value
    } as Profile
    toast.success('更新成功')
  } catch (error) {
    console.error('Failed to update profile:', error)
    toast.error('更新失败')
    // Revert on error by refetching
    await fetchProfile()
  } finally {
    updating.value = false
  }
}

// Event handlers for ProfileHero component
const handleEdit = () => {
  console.log('Edit mode requested - feature coming soon')
  toast.info('编辑模式即将推出')
}

const handleChangeAvatar = () => {
  console.log('Avatar change requested - feature coming soon')
  toast.info('头像上传功能即将推出')
}

// Initialize data
onMounted(() => {
  fetchProfile()
  fetchActivityStats()
})
</script>

<template>
  <div class="profile-page">
    <!-- Page Header -->
    <div class="profile-page__header">
      <h1 class="profile-page__title">个人中心</h1>
      <p class="profile-page__subtitle">管理您的个人资料、安全设置和偏好</p>
    </div>

    <!-- Hero Section -->
    <div v-if="profileLoading" class="profile-page__loading">
      <p>加载个人资料中...</p>
    </div>

    <div v-else-if="profile" class="profile-page__hero">
      <ProfileHero
        :profile="profile"
        @edit="handleEdit"
        @change-avatar="handleChangeAvatar"
      />
    </div>

    <!-- Main Content Grid -->
    <div v-if="profile" class="profile-page__grid">
      <!-- Left Column: Profile Info (2fr) -->
      <div class="profile-page__main">
        <ProfileInfo
          :profile="profile"
          :loading="updating"
          @update-field="handleUpdateField"
        />

        <!-- Security Section -->
        <div class="profile-page__security">
          <ProfileSecurity />
        </div>
      </div>

      <!-- Middle Column: Activity Dashboard (1fr) -->
      <div class="profile-page__activity">
        <ActivityDashboard
          v-if="activityStats"
          :activity="activityStats"
          :loading="activityLoading"
        />
        <div v-else class="profile-page__activity-empty">
          <p>暂无活动数据</p>
        </div>
      </div>

      <!-- Right Column: Quick Settings (1fr) -->
      <div class="profile-page__settings">
        <QuickSettings />

        <!-- Completion Badge -->
        <div class="profile-page__completion">
          <CompletionBadge
            :nickname="profile.nickname"
            :email="profile.email"
            :phone="profile.phone"
            :has-avatar="hasAvatar"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* Page Header */
.profile-page__header {
  text-align: center;
}

.profile-page__title {
  font-size: 32px;
  font-weight: 700;
  color: rgb(15 23 42);
  margin: 0 0 8px 0;
  letter-spacing: -0.02em;
}

.profile-page__subtitle {
  font-size: 16px;
  color: rgb(100 116 139);
  margin: 0;
}

/* Loading State */
.profile-page__loading {
  padding: 60px 0;
  text-align: center;
  font-size: 14px;
  color: rgb(148 163 184);
  border-radius: 16px;
  background: rgb(248 250 252);
}

/* Hero Section */
.profile-page__hero {
  width: 100%;
}

/* Main Grid Layout */
.profile-page__grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 24px;
  align-items: start;
}

/* Column Containers */
.profile-page__main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-page__activity {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-page__settings {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Empty Activity State */
.profile-page__activity-empty {
  padding: 60px 0;
  text-align: center;
  font-size: 14px;
  color: rgb(148 163 184);
  border: 1px dashed rgb(226 232 240);
  border-radius: 16px;
  background: rgb(248 250 252);
}

/* Desktop (>1024px) - Default 3-column layout */
@media (min-width: 1024px) {
  .profile-page__grid {
    grid-template-columns: 2fr 1fr 1fr;
  }
}

/* Tablet (768px - 1023px) - 2-column layout */
@media (max-width: 1023px) and (min-width: 768px) {
  .profile-page__grid {
    grid-template-columns: 1fr 1fr;
  }

  .profile-page__activity {
    grid-column: 1 / 2;
  }

  .profile-page__settings {
    grid-column: 2 / 3;
  }
}

/* Mobile (<768px) - Single column layout */
@media (max-width: 767px) {
  .profile-page {
    padding: 16px;
    gap: 24px;
  }

  .profile-page__header {
    text-align: center;
  }

  .profile-page__title {
    font-size: 24px;
  }

  .profile-page__subtitle {
    font-size: 14px;
  }

  .profile-page__grid {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .profile-page__main,
  .profile-page__activity,
  .profile-page__settings {
    width: 100%;
  }
}

/* Large Desktop (>1400px) - Optimized spacing */
@media (min-width: 1400px) {
  .profile-page {
    padding: 32px;
  }

  .profile-page__grid {
    gap: 32px;
  }
}
</style>
