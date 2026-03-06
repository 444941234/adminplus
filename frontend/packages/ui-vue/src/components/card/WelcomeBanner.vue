<template>
  <div class="welcome-banner" :class="{ compact }">
    <!-- 装饰背景 -->
    <div class="banner-bg"></div>

    <!-- 内容 -->
    <div class="banner-content">
      <h1 class="banner-title">{{ title }}</h1>
      <p v-if="subtitle" class="banner-subtitle">{{ subtitle }}</p>

      <!-- 个性化问候 -->
      <div v-if="greeting" class="banner-greeting">
        <el-icon><ChatDotRound /></el-icon>
        <span>{{ greeting }}</span>
      </div>

      <!-- 操作按钮 -->
      <div v-if="actionText" class="banner-action">
        <el-button type="primary" @click="$emit('action')">
          {{ actionText }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ChatDotRound } from '../../utils/icons';

defineOptions({
  name: 'WelcomeBanner'
});

interface Props {
  username: string;
  greeting?: string;
  title?: string;
  subtitle?: string;
  actionText?: string;
  compact?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  compact: false
});

defineEmits<{
  action: [];
}>();

const title = computed(() => {
  if (props.title) return props.title;
  return `欢迎回来，${props.username}！`;
});

const subtitle = computed(() => {
  if (props.subtitle) return props.subtitle;
  const hour = new Date().getHours();
  if (hour < 6) return '夜深了，注意休息';
  if (hour < 12) return '美好的一天从现在开始';
  if (hour < 18) return '下午好，继续加油';
  return '晚上好，辛苦了';
});
</script>

<style scoped>
.welcome-banner {
  @include card-style;
  padding: var(--space-2xl);
  color: white;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1890ff 0%, #6366F1 100%);
  min-height: 200px;
  border: none;
  box-shadow: 0 10px 40px rgba(24, 144, 255, 0.25);
}

.welcome-banner.compact {
  min-height: 150px;
  padding: var(--space-xl);
}

/* 背景装饰 - 多层渐变圆 */
.banner-bg {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  animation: float 20s ease-in-out infinite;
}

.banner-bg::before {
  content: '';
  position: absolute;
  top: 20%;
  right: 30%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  animation: float 15s ease-in-out infinite reverse;
}

.banner-bg::after {
  content: '';
  position: absolute;
  bottom: 10%;
  left: 10%;
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
  border-radius: 50%;
  animation: float 12s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 var(--space-sm) 0;
  line-height: 1.3;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.banner-subtitle {
  font-size: 16px;
  opacity: 0.95;
  margin: 0 0 var(--space-lg) 0;
  font-weight: 400;
}

.banner-greeting {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  margin-top: var(--space-md);
  padding: var(--space-sm) var(--space-md);
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-md);
  backdrop-filter: blur(10px);
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all var(--transition-normal);
}

.banner-greeting:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.banner-greeting .el-icon {
  font-size: 16px;
}

.banner-action {
  margin-top: var(--space-lg);
}

:deep(.banner-action .el-button) {
  background: rgba(255, 255, 255, 0.95);
  color: var(--primary-color);
  border: none;
  font-weight: 600;
  padding: 12px 28px;
  border-radius: var(--radius-md);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all var(--transition-normal);
}

:deep(.banner-action .el-button:hover) {
  background: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

:deep(.banner-action .el-button:active) {
  transform: translateY(0);
}

/* 响应式 */
@media (max-width: 767px) {
  .welcome-banner {
    padding: var(--space-xl);
    min-height: 180px;
  }

  .banner-title {
    font-size: 24px;
  }

  .banner-subtitle {
    font-size: 14px;
  }

  .banner-greeting {
    font-size: 13px;
  }

  :deep(.banner-action .el-button) {
    width: 100%;
  }
}
</style>
