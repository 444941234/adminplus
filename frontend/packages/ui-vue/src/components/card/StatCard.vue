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
import { ArrowUp, ArrowDown } from '../../utils/icons';

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
  position: relative;
  overflow: hidden;
  transition: all var(--transition-normal);
}

/* 背景装饰 */
.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, var(--stat-bg-color, rgba(0,0,0,0.05)) 0%, transparent 70%);
  border-radius: 50%;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card-primary {
  --stat-bg-color: rgba(24, 144, 255, 0.08);
}

.stat-card-success {
  --stat-bg-color: rgba(82, 196, 26, 0.08);
}

.stat-card-warning {
  --stat-bg-color: rgba(250, 140, 22, 0.08);
}

.stat-card-danger {
  --stat-bg-color: rgba(255, 77, 79, 0.08);
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
  position: relative;
  z-index: 1;
  transition: all var(--transition-normal);
}

/* 图标光泽效果 */
.stat-icon::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 255, 255, 0.1) 50%,
    transparent 70%
  );
  transform: rotate(45deg);
  transition: all 0.6s;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
  box-shadow: var(--shadow-lg);
}

.stat-card:hover .stat-icon::after {
  animation: shine 1.5s ease-in-out;
}

@keyframes shine {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  100% { transform: translateX(100%) translateY(100%) rotate(45deg); }
}

.stat-card-primary .stat-icon {
  background: linear-gradient(135deg, #1890ff 0%, #6366F1 100%);
  box-shadow: 0 8px 16px rgba(24, 144, 255, 0.3);
}

.stat-card-success .stat-icon {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  box-shadow: 0 8px 16px rgba(82, 196, 26, 0.3);
}

.stat-card-warning .stat-icon {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
  box-shadow: 0 8px 16px rgba(250, 140, 22, 0.3);
}

.stat-card-danger .stat-icon {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  box-shadow: 0 8px 16px rgba(255, 77, 79, 0.3);
}

.stat-content {
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  letter-spacing: -0.5px;
  transition: all var(--transition-normal);
}

.stat-card:hover .stat-value {
  transform: translateX(4px);
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: var(--space-sm);
  font-weight: 500;
}

.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  margin-top: var(--space-sm);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
}

.stat-trend.trend-up {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.1);
}

.stat-trend.trend-down {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
}

.stat-card:hover .stat-trend {
  transform: scale(1.05);
}

/* 响应式 */
@media (max-width: 480px) {
  .stat-card {
    padding: var(--space-md);
  }

  .stat-icon {
    width: 56px;
    height: 56px;
  }

  .stat-icon :deep(.el-icon) {
    font-size: 28px !important;
  }

  .stat-value {
    font-size: 24px;
  }
}
</style>
