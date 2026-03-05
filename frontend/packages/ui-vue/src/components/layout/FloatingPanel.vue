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
  width: 48px;
  height: 48px;
  background: var(--primary-gradient);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  transition: all var(--transition-normal);
}

.panel-trigger:hover {
  transform: scale(1.1);
}

.panel-content {
  position: absolute;
  bottom: 60px;
  right: 0;
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xl);
  padding: var(--space-md);
  min-width: 180px;
}

.panel-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
  color: var(--text-primary);
  font-size: 14px;
}

.panel-item:hover {
  background: var(--bg-secondary);
}

.panel-divider {
  height: 1px;
  background: var(--border-color);
  margin: var(--space-sm) 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all var(--transition-normal);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
