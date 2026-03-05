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
import { ChatDotRound } from '@element-plus/icons-vue';

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
  background: var(--primary-gradient);
  min-height: 200px;
}

.welcome-banner.compact {
  min-height: 150px;
  padding: var(--space-xl);
}

.banner-bg {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner-title {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 var(--space-sm) 0;
  line-height: 1.3;
}

.banner-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 var(--space-lg) 0;
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
}

.banner-action {
  margin-top: var(--space-lg);
}

:deep(.banner-action .el-button) {
  background: rgba(255, 255, 255, 0.9);
  color: var(--primary-color);
  border: none;

  &:hover {
    background: white;
  }
}

@media (max-width: 767px) {
  .banner-title {
    font-size: 24px;
  }

  .banner-subtitle {
    font-size: 14px;
  }
}
</style>
