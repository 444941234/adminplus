<template>
  <div class="floating-panel" :class="{ expanded }">
    <!-- 触发按钮 -->
    <div class="panel-trigger" @click="toggle">
      <el-icon :size="20">
        <Close v-if="expanded" />
        <Operation v-else />
      </el-icon>
    </div>

    <!-- 面板内容 -->
    <transition name="slide-up">
      <div v-show="expanded" class="panel-content">
        <div class="panel-item" @click="handleTheme">
          <el-icon><Brush /></el-icon>
          <span>随机换肤</span>
        </div>
        <div class="panel-item" @click="handleConfig">
          <el-icon><Setting /></el-icon>
          <span>主题配置</span>
        </div>
        <div class="panel-item" @click="handleClearCache">
          <el-icon><Delete /></el-icon>
          <span>清理缓存</span>
        </div>
        <div class="panel-divider"></div>
        <div class="panel-item collapse" @click="toggle">
          <el-icon><ArrowDown /></el-icon>
          <span>收起浮窗</span>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import {
  Close,
  Operation,
  Brush,
  Setting,
  Delete,
  ArrowDown
} from '@element-plus/icons-vue';

defineOptions({
  name: 'FloatingPanel'
});

const expanded = ref(false);

const emit = defineEmits<{
  toggle: [];
  themeChange: [];
  config: [];
  clearCache: [];
}>();

const toggle = () => {
  expanded.value = !expanded.value;
  emit('toggle');
};

const handleTheme = () => {
  emit('themeChange');
  toggle();
};

const handleConfig = () => {
  emit('config');
  toggle();
};

const handleClearCache = () => {
  emit('clearCache');
  toggle();
};
</script>

<style scoped>
.floating-panel {
  position: fixed;
  right: var(--space-lg);
  bottom: var(--space-lg);
  z-index: var(--z-fixed);
}

.panel-trigger {
  width: 56px;
  height: 56px;
  background: var(--primary-gradient);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(74, 144, 226, 0.4);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.panel-trigger::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.panel-trigger:hover::before {
  opacity: 1;
}

.panel-trigger:hover {
  transform: scale(1.1) rotate(90deg);
  box-shadow: 0 12px 32px rgba(74, 144, 226, 0.5);
}

.panel-trigger:active {
  transform: scale(1.05) rotate(90deg);
}

.panel-trigger .el-icon {
  font-size: 24px;
  transition: transform var(--transition-normal);
}

.expanded .panel-trigger .el-icon {
  transform: rotate(45deg);
}

.panel-content {
  position: absolute;
  bottom: 70px;
  right: 0;
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-2xl);
  padding: var(--space-sm);
  min-width: 200px;
  border: 1px solid var(--border-light);
  backdrop-filter: blur(20px);
}

.panel-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.panel-item:hover {
  background: var(--bg-secondary);
  color: var(--primary-color);
  transform: translateX(-4px);
}

.panel-item:active {
  transform: translateX(-2px);
}

.panel-item .el-icon {
  font-size: 18px;
  transition: transform var(--transition-fast);
}

.panel-item:hover .el-icon {
  transform: scale(1.2);
}

.panel-divider {
  height: 1px;
  background: var(--border-light);
  margin: var(--space-sm) var(--space-md);
}

.panel-item.collapse {
  color: var(--text-secondary);
}

.panel-item.collapse:hover {
  background: transparent;
  color: var(--text-tertiary);
  transform: none;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all var(--transition-normal);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}

/* 脉冲动画 */
@keyframes pulse {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(74, 144, 226, 0.4);
  }
  50% {
    box-shadow: 0 8px 32px rgba(74, 144, 226, 0.6);
  }
}

.panel-trigger:not(:hover) {
  animation: pulse 2s ease-in-out infinite;
}
</style>
