<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ProfileHero from '@/components/profile/ProfileHero.vue'
import ProfileInfo from '@/components/profile/ProfileInfo.vue'
import ProfileSecurity from '@/components/profile/ProfileSecurity.vue'
import ActivityDashboard from '@/components/profile/ActivityDashboard.vue'
import QuickSettings from '@/components/profile/QuickSettings.vue'
import { getProfile, updateProfile, getActivityStats } from '@/api'
import type { Profile, ActivityStats } from '@/types'
import { useAsyncAction } from '@/composables/useAsyncAction'

// State management
const profile = ref<Profile | null>(null)
const activityStats = ref<ActivityStats | null>(null)
const { loading: profileLoading, run: runProfile } = useAsyncAction('获取个人资料失败')
const { loading: activityLoading, run: runActivity } = useAsyncAction()
const { loading: updating, run: runUpdate } = useAsyncAction('更新失败')

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
    <!-- Hero Section -->
    <div
      v-if="profileLoading"
      class="profile-page__loading"
    >
      <p>加载个人资料中...</p>
    </div>

    <div
      v-else-if="profile"
      class="profile-page__hero"
    >
      <ProfileHero
        :profile="profile"
      />
    </div>

    <!-- Main Content Grid -->
    <div
      v-if="profile"
      class="profile-page__grid"
    >
      <!-- Left Column: Profile Info (2fr) -->
      <div class="profile-page__main">
        <ProfileInfo
          :profile="profile"
          :loading="updating"
          @update-field="handleUpdateField"
        />

        <!-- Security Section -->
        <ProfileSecurity />
      </div>

      <!-- Middle Column: Activity Dashboard (1fr) -->
      <div class="profile-page__activity">
        <ActivityDashboard
          v-if="activityStats"
          :activity="activityStats"
          :loading="activityLoading"
        />
        <div
          v-else
          class="profile-page__activity-empty"
        >
          <p>暂无活动数据</p>
        </div>
      </div>

      <!-- Right Column: Quick Settings (1fr) -->
      <div class="profile-page__settings">
        <QuickSettings />
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

/* Tablet (641px - 1023px) - 2-column layout */
@media (max-width: 1023px) and (min-width: 641px) {
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

/* Mobile (<640px) - Single column layout */
@media (max-width: 640px) {
  .profile-page {
    padding: 16px;
    gap: 24px;
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
