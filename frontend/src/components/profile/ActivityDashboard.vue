<script setup lang="ts">
import { computed } from 'vue'
import { Badge } from '@/components/ui'
import { formatTime, getActivityColor } from '@/utils/activityUtils'
import type { ActivityStats } from '@/types'

interface Props {
  activity: ActivityStats
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

// Get activity icon based on type
const getActivityIcon = (type: string): string => {
  const icons: Record<string, string> = {
    create: '✨',
    update: '📝',
    delete: '🗑️',
    login: '🔐'
  }
  return icons[type] || '📌'
}

const displayActivities = computed(() => {
  return props.activity.recentActivity.slice(0, 5)
})
</script>

<template>
  <div class="activity-dashboard">
    <!-- Stats Cards -->
    <div class="activity-dashboard__stats">
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--active">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
          </svg>
        </div>
        <div class="stat-card__content">
          <p class="stat-card__value">{{ activity.daysActive }}</p>
          <p class="stat-card__label">Days Active</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--actions">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
        </div>
        <div class="stat-card__content">
          <p class="stat-card__value">{{ activity.totalActions }}</p>
          <p class="stat-card__label">Total Actions</p>
        </div>
      </div>
    </div>

    <!-- Recent Activity Timeline -->
    <div class="activity-dashboard__timeline">
      <div class="activity-dashboard__header">
        <h3 class="activity-dashboard__title">Recent Activity</h3>
        <Badge variant="outline">Last 5 activities</Badge>
      </div>

      <div v-if="loading" class="activity-dashboard__loading">
        Loading activity data...
      </div>

      <div v-else-if="displayActivities.length === 0" class="activity-dashboard__empty">
        <p>No recent activity</p>
      </div>

      <div v-else class="activity-list">
        <div
          v-for="item in displayActivities"
          :key="item.id"
          class="activity-item"
        >
          <div class="activity-item__icon">
            {{ getActivityIcon(item.type) }}
          </div>
          <div class="activity-item__content">
            <div class="activity-item__header">
              <span class="activity-item__action">{{ item.action }}</span>
              <span
                class="activity-item__badge"
                :style="{
                  backgroundColor: getActivityColor(item.type).bg,
                  color: getActivityColor(item.type).text,
                  borderColor: getActivityColor(item.type).border
                }"
              >
                {{ item.type }}
              </span>
            </div>
            <p class="activity-item__time">{{ formatTime(item.timestamp) }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Last Login Info -->
    <div class="activity-dashboard__login">
      <div class="activity-dashboard__header">
        <h3 class="activity-dashboard__title">Last Login</h3>
      </div>

      <div v-if="loading" class="activity-dashboard__loading">
        Loading login information...
      </div>

      <div v-else class="login-info">
        <div class="login-info__item">
          <span class="login-info__label">Time</span>
          <span class="login-info__value">{{ formatTime(activity.lastLogin) }}</span>
        </div>
        <div class="login-info__item">
          <span class="login-info__label">IP Address</span>
          <span class="login-info__value">{{ activity.lastLoginIp }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.activity-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Stats Cards */
.activity-dashboard__stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  background: white;
  border: 1px solid rgb(226 232 240);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.stat-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  flex-shrink: 0;
}

.stat-card__icon--active {
  background: rgb(220 252 231);
  color: rgb(22 101 52);
}

.stat-card__icon--actions {
  background: rgb(219 234 254);
  color: rgb(30 64 175);
}

.stat-card__content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-card__value {
  font-size: 28px;
  font-weight: 700;
  color: rgb(15 23 42);
  line-height: 1;
  margin: 0;
}

.stat-card__label {
  font-size: 13px;
  font-weight: 500;
  color: rgb(100 116 139);
  margin: 0;
}

/* Timeline Section */
.activity-dashboard__timeline,
.activity-dashboard__login {
  border: 1px solid rgb(226 232 240);
  border-radius: 16px;
  background: white;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.activity-dashboard__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgb(226 232 240);
}

.activity-dashboard__title {
  font-size: 16px;
  font-weight: 600;
  color: rgb(15 23 42);
  margin: 0;
}

.activity-dashboard__loading,
.activity-dashboard__empty {
  padding: 32px 0;
  text-align: center;
  font-size: 14px;
  color: rgb(148 163 184);
}

/* Activity List */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  background: rgb(248 250 252);
  border: 1px solid rgb(226 232 240);
  transition: all 0.2s ease;
}

.activity-item:hover {
  background: rgb(241 245 249);
  border-color: rgb(203 213 225);
}

.activity-item__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: white;
  font-size: 18px;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.activity-item__content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  min-width: 0;
}

.activity-item__header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.activity-item__action {
  font-size: 14px;
  font-weight: 500;
  color: rgb(15 23 42);
}

.activity-item__badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border: 1px solid;
}

.activity-item__time {
  font-size: 12px;
  color: rgb(100 116 139);
  margin: 0;
}

/* Login Info */
.login-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.login-info__item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.login-info__label {
  font-size: 12px;
  font-weight: 600;
  color: rgb(100 116 139);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.login-info__value {
  font-size: 14px;
  font-weight: 500;
  color: rgb(15 23 42);
  word-break: break-all;
}

/* Mobile Responsive */
@media (max-width: 640px) {
  .activity-dashboard__stats {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-card__icon {
    width: 40px;
    height: 40px;
  }

  .stat-card__value {
    font-size: 24px;
  }

  .activity-dashboard__timeline,
  .activity-dashboard__login {
    padding: 16px;
  }

  .activity-item {
    padding: 12px;
  }

  .activity-item__icon {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }
}
</style>
