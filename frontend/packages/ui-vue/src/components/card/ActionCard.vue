<template>
  <div :class="['action-card', { column }]">
    <!-- 标题 -->
    <div v-if="title" class="action-card-title">
      <h3>{{ title }}</h3>
    </div>

    <!-- 操作按钮列表 -->
    <div :class="['action-list', { column }]">
      <div
        v-for="action in actions"
        :key="action.id"
        :class="['action-button', `action-${action.type || 'default'}`]"
        @click="handleAction(action.id)"
      >
        <el-icon :size="24">
          <component :is="getIcon(action.icon)" />
        </el-icon>
        <span class="action-label">{{ action.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

defineOptions({
  name: 'ActionCard'
});

export interface ActionItem {
  id: string;
  label: string;
  icon: string;
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'default';
  disabled?: boolean;
}

interface Props {
  title?: string;
  actions: ActionItem[];
  column?: boolean;
}

defineProps<Props>();

const emit = defineEmits<{
  action: [id: string];
}>();

const handleAction = (id: string) => {
  emit('action', id);
};

const getIcon = (iconName: string) => {
  try {
    return defineAsyncComponent(() =>
      import(`@element-plus/icons-vue`).then(m => (m as any)[iconName])
    );
  } catch {
    return null;
  }
};
</script>

<style scoped>
.action-card {
  @include card-style;
  padding: var(--space-lg);
}

.action-card-title {
  margin-bottom: var(--space-lg);
}

.action-card-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.action-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-md);
}

.action-list.column {
  flex-direction: column;
}

.action-button {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  background: var(--bg-secondary);
  color: var(--text-primary);
  flex: 1;
  min-width: 120px;
}

.action-button:hover {
  background: var(--bg-tertiary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.action-button:active {
  transform: translateY(0);
}

.action-primary {
  background: linear-gradient(135deg, rgba(78, 136, 243, 0.1) 0%, rgba(99, 102, 241, 0.1) 100%);
  color: var(--primary-color);
}

.action-primary:hover {
  background: linear-gradient(135deg, rgba(78, 136, 243, 0.2) 0%, rgba(99, 102, 241, 0.2) 100%);
}

.action-success {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(5, 150, 105, 0.1) 100%);
  color: var(--success-color);
}

.action-success:hover {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(5, 150, 105, 0.2) 100%);
}

.action-warning {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(217, 119, 6, 0.1) 100%);
  color: var(--warning-color);
}

.action-warning:hover {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2) 0%, rgba(217, 119, 6, 0.2) 100%);
}

.action-danger {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(220, 38, 38, 0.1) 100%);
  color: var(--danger-color);
}

.action-danger:hover {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.2) 0%, rgba(220, 38, 38, 0.2) 100%);
}

.action-label {
  font-size: 14px;
  font-weight: 500;
}

@media (max-width: 767px) {
  .action-list.column .action-button {
    min-width: 100%;
  }
}
</style>
