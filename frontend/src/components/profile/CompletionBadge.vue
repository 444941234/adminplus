<script setup lang="ts">
import { computed } from 'vue'
import { Card } from '@/components/ui/card'
import { Progress } from '@/components/ui/progress'
import { Trophy } from 'lucide-vue-next'

interface Props {
  nickname?: string
  email?: string
  phone?: string
  hasAvatar?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  nickname: '',
  email: '',
  phone: '',
  hasAvatar: false
})

const completionPercentage = computed(() => {
  const fields = [
    props.nickname,
    props.email,
    props.phone,
    props.hasAvatar ? 'avatar' : ''
  ]
  const filled = fields.filter(Boolean).length
  const percent = Math.round((filled / fields.length) * 100)
  return Number.isNaN(percent) ? 0 : percent
})

const completionMessage = computed(() => {
  const percentage = completionPercentage.value
  if (percentage === 100) {
    return 'Congratulations! Your profile is complete.'
  } else if (percentage >= 75) {
    return "Almost there! Just a few more details to complete your profile."
  } else if (percentage >= 50) {
    return "You're halfway there! Keep adding more information to your profile."
  } else if (percentage >= 25) {
    return 'Good start! Add more details to help others recognize you.'
  } else {
    return 'Get started by adding your profile information to build your presence.'
  }
})

const badgeTitle = computed(() => {
  return completionPercentage.value === 100 ? 'Profile Complete!' : 'Profile Progress'
})

const badgeColor = computed(() => {
  const percentage = completionPercentage.value
  if (percentage === 100) return 'text-green-600'
  if (percentage >= 75) return 'text-blue-600'
  if (percentage >= 50) return 'text-yellow-600'
  return 'text-slate-600'
})
</script>

<template>
  <Card class="completion-badge overflow-hidden border-0 shadow-md">
    <div class="completion-badge__content">
      <div class="completion-badge__header">
        <div class="completion-badge__icon" :class="badgeColor">
          <Trophy :size="32" />
        </div>
        <div class="completion-badge__title">
          <h3 class="completion-badge__title-text">{{ badgeTitle }}</h3>
          <p class="completion-badge__percentage">{{ completionPercentage }}%</p>
        </div>
      </div>

      <div class="completion-badge__progress">
        <Progress :model-value="completionPercentage" class="completion-badge__progress-bar" />
      </div>

      <p class="completion-badge__message">{{ completionMessage }}</p>
    </div>
  </Card>
</template>

<style scoped>
.completion-badge {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16px;
}

.completion-badge__content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.completion-badge__header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.completion-badge__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.completion-badge__title {
  flex: 1;
  min-width: 0;
}

.completion-badge__title-text {
  font-size: 16px;
  font-weight: 600;
  color: rgb(15 23 42);
  margin: 0 0 4px 0;
  line-height: 1.3;
}

.completion-badge__percentage {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.completion-badge__progress {
  display: flex;
  align-items: center;
}

.completion-badge__progress-bar {
  height: 10px;
  width: 100%;
}

.completion-badge__progress-bar :deep(.progress-root) {
  background: rgba(0, 0, 0, 0.06);
}

.completion-badge__progress-bar :deep(.progress-indicator) {
  background: linear-gradient(90deg, #3b82f6 0%, #1d4ed8 100%);
}

.completion-badge__message {
  font-size: 13px;
  line-height: 1.6;
  color: rgb(71 85 105);
  margin: 0;
  padding-top: 4px;
}

@media (max-width: 640px) {
  .completion-badge__content {
    padding: 16px;
  }

  .completion-badge__icon {
    width: 44px;
    height: 44px;
  }

  .completion-badge__icon svg {
    width: 24px;
    height: 24px;
  }

  .completion-badge__title-text {
    font-size: 14px;
  }

  .completion-badge__percentage {
    font-size: 20px;
  }

  .completion-badge__message {
    font-size: 12px;
  }
}
</style>
