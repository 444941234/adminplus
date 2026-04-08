<script setup lang="ts">
/**
 * ActivityDashboard Component
 *
 * Displays user activity statistics and recent activity timeline.
 */
import { computed } from 'vue'
import { formatTime } from '@/utils/activityUtils'
import type { ActivityStats } from '@/types'

interface Props {
  activity: ActivityStats
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

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
    <div class="activity-dashboard__header">
      <h3 class="activity-dashboard__title">
        活动统计
      </h3>
    </div>

    <!-- Stats Grid -->
    <div class="activity-stats">
      <div class="activity-stat">
        <div class="activity-stat__icon">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
          </svg>
        </div>
        <div class="activity-stat__content">
          <span class="activity-stat__value">{{ activity.daysActive }}</span>
          <span class="activity-stat__label">活跃天数</span>
        </div>
      </div>
      <div class="activity-stat">
        <div class="activity-stat__icon">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
          </svg>
        </div>
        <div class="activity-stat__content">
          <span class="activity-stat__value">{{ activity.totalActions }}</span>
          <span class="activity-stat__label">总操作数</span>
        </div>
      </div>
    </div>

    <!-- Recent Activity -->
    <div class="activity-list">
      <div
        v-if="loading"
        class="activity-list__loading"
      >
        加载中...
      </div>
      <div
        v-else-if="displayActivities.length === 0"
        class="activity-list__empty"
      >
        暂无活动记录
      </div>
      <div
        v-else
        class="activity-list__items"
      >
        <div
          v-for="item in displayActivities"
          :key="item.id"
          class="activity-item"
        >
          <span class="activity-item__icon">{{ getActivityIcon(item.type) }}</span>
          <div class="activity-item__content">
            <span class="activity-item__action">{{ item.action }}</span>
            <span class="activity-item__time">{{ formatTime(item.timestamp) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Last Login -->
    <div class="last-login">
      <span class="last-login__label">上次登录</span>
      <span class="last-login__value">{{ formatTime(activity.lastLogin) }}</span>
    </div>
  </div>
</template>

<style scoped>
.activity-dashboard {
  border: 1px solid rgb(226 232 240);
  border-radius: 16px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.activity-dashboard__header {
  padding: 20px;
  border-bottom: 1px solid rgb(226 232 240);
}

.activity-dashboard__title {
  font-size: 16px;
  font-weight: 600;
  color: rgb(15 23 42);
  margin: 0;
}

/* Stats */
.activity-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 20px;
  border-bottom: 1px solid rgb(241 245 249);
}

.activity-stat {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, rgb(248 250 252) 0%, rgb(241 245 249) 100%);
  border-radius: 12px;
  border: 1px solid rgb(226 232 240);
}

.activity-stat__icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 8px;
  color: rgb(59 130 246);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.activity-stat__content {
  display: flex;
  flex-direction: column;
}

.activity-stat__value {
  font-size: 20px;
  font-weight: 700;
  color: rgb(15 23 42);
  line-height: 1;
}

.activity-stat__label {
  font-size: 12px;
  color: rgb(100 116 139);
  margin-top: 4px;
}

/* Activity List */
.activity-list {
  padding: 20px;
  border-bottom: 1px solid rgb(241 245 249);
}

.activity-list__loading,
.activity-list__empty {
  text-align: center;
  font-size: 14px;
  color: rgb(148 163 184);
  padding: 20px 0;
}

.activity-list__items {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  transition: background-color 150ms ease-out;
}

.activity-item:hover {
  background: rgb(248 250 252);
}

.activity-item__icon {
  font-size: 16px;
  flex-shrink: 0;
}

.activity-item__content {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.activity-item__action {
  font-size: 13px;
  font-weight: 500;
  color: rgb(15 23 42);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-item__time {
  font-size: 12px;
  color: rgb(148 163 184);
}

/* Last Login */
.last-login {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-login__label {
  font-size: 13px;
  color: rgb(100 116 139);
}

.last-login__value {
  font-size: 13px;
  font-weight: 500;
  color: rgb(15 23 42);
}

/* Mobile */
@media (max-width: 640px) {
  .activity-dashboard__header,
  .activity-stats,
  .activity-list,
  .last-login {
    padding: 16px;
  }

  .activity-stats {
    grid-template-columns: 1fr;
  }
}
</style>
