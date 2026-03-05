<template>
  <div :class="['stat-card', `stat-card-${type}`]">
    <!-- 图标 -->
    <div class="stat-icon">
      <el-icon :size="32">
        <component :is="displayIcon" />
      </el-icon>
    </div>

    <!-- 内容 -->
    <div class="stat-content">
      <div class="stat-value">{{ displayValue }}</div>
      <div class="stat-label">{{ label }}</div>
      <div v-if="trend" :class="['stat-trend', trendUp ? 'trend-up' : 'trend-down']">
        <el-icon>
          <ArrowUp v-if="trendUp" />
          <ArrowDown v-else />
        </el-icon>
        <span>{{ trend }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue';

defineOptions({
  name: 'StatCard'
});

type CardType = 'primary' | 'success' | 'warning' | 'danger';

interface Props {
  type: CardType;
  icon: string;
  value: number | string;
  label: string;
  trend?: string;
  trendUp?: boolean;
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  trendUp: true,
  loading: false
});

// 导入图标
const displayIcon = computed(() => {
  try {
    return require(`@element-plus/icons-vue/dist/es/${props.icon}.mjs`).default;
  } catch {
    return null;
  }
});

const displayValue = computed(() => {
  if (props.loading) return '--';
  return typeof props.value === 'number' ? props.value.toLocaleString() : props.value;
});
</script>

<style scoped>
.stat-card {
  @include card-style;
  padding: var(--space-lg);
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-card-primary .stat-icon {
  background: var(--primary-gradient);
}

.stat-card-success .stat-icon {
  background: var(--success-gradient);
}

.stat-card-warning .stat-icon {
  background: var(--warning-gradient);
}

.stat-card-danger .stat-icon {
  background: var(--danger-gradient);
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: var(--space-sm);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-top: var(--space-sm);
}

.stat-trend.trend-up {
  color: var(--success-color);
}

.stat-trend.trend-down {
  color: var(--danger-color);
}
</style>
