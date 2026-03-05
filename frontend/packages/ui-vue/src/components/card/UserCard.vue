<template>
  <div class="user-card" @click="$emit('click')">
    <!-- 头部信息 -->
    <div class="user-header">
      <el-avatar :src="user.avatar" :size="80" />
      <div class="user-info">
        <div class="user-name">{{ user.nickname || user.name }}</div>
        <div v-if="user.motto" class="user-motto">"{{ user.motto }}"</div>
      </div>
    </div>

    <!-- 详细信息列表 -->
    <ul v-if="showDetails" class="user-info-list">
      <li v-if="user.email" class="info-item">
        <el-icon><Message /></el-icon>
        <span>{{ user.email }}</span>
      </li>
      <li v-if="user.phone" class="info-item">
        <el-icon><Phone /></el-icon>
        <span>{{ user.phone }}</span>
      </li>
      <li v-if="user.department" class="info-item">
        <el-icon><OfficeBuilding /></el-icon>
        <span>{{ user.department }}</span>
      </li>
      <li v-if="user.role" class="info-item">
        <el-icon><UserFilled /></el-icon>
        <span>{{ user.role }}</span>
      </li>
    </ul>

    <!-- 标签 -->
    <div v-if="user.tags && user.tags.length" class="user-tags">
      <el-tag
        v-for="tag in user.tags"
        :key="tag"
        :type="getTagType(tag)"
        size="small"
      >
        {{ tag }}
      </el-tag>
    </div>

    <!-- 操作按钮 -->
    <div v-if="showActions" class="user-actions">
      <el-button type="primary" size="small" @click.stop="$emit('edit')">
        编辑
      </el-button>
      <el-button size="small" @click.stop="$emit('message')">
        私信
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Message, Phone, OfficeBuilding, UserFilled } from '@element-plus/icons-vue';

defineOptions({
  name: 'UserCard'
});

export interface UserInfo {
  name: string;
  nickname?: string;
  avatar: string;
  email?: string;
  phone?: string;
  motto?: string;
  department?: string;
  role?: string;
  tags?: string[];
}

interface Props {
  user: UserInfo;
  showDetails?: boolean;
  showActions?: boolean;
}

withDefaults(defineProps<Props>(), {
  showDetails: true,
  showActions: false
});

defineEmits<{
  click: [];
  edit: [];
  message: [];
}>();

const getTagType = (tag: string) => {
  const types: Record<string, string> = {
    '管理员': 'danger',
    '开发者': 'primary',
    '设计师': 'warning',
    '产品': 'success'
  };
  return types[tag] || 'info';
};
</script>

<style scoped>
.user-card {
  @include card-style;
  padding: var(--space-xl);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

/* 顶部装饰条 */
.user-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--primary-gradient);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.user-card:hover::before {
  opacity: 1;
}

.user-header {
  display: flex;
  gap: var(--space-lg);
  align-items: center;
  position: relative;
}

/* 头像容器 */
.user-header :deep(.el-avatar) {
  border: 3px solid var(--bg-primary);
  box-shadow: var(--shadow-md);
  transition: all var(--transition-normal);
}

.user-card:hover .user-header :deep(.el-avatar) {
  box-shadow: var(--shadow-lg);
  transform: scale(1.05);
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
  line-height: 1.3;
}

.user-motto {
  font-size: 13px;
  color: var(--text-secondary);
  font-style: italic;
  line-height: 1.4;
  opacity: 0.8;
}

.user-info-list {
  list-style: none;
  padding: 0;
  margin: var(--space-lg) 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-md);
  color: var(--text-secondary);
  font-size: 14px;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.info-item:hover {
  background: var(--bg-secondary);
  color: var(--primary-color);
}

.info-item .el-icon {
  font-size: 16px;
  color: var(--primary-color);
  opacity: 0.7;
  transition: opacity var(--transition-fast);
}

.info-item:hover .el-icon {
  opacity: 1;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
  margin-top: var(--space-lg);
}

.user-tags :deep(.el-tag) {
  border-radius: var(--radius-full);
  padding: 4px 12px;
  font-weight: 500;
  border: none;
}

.user-actions {
  display: flex;
  gap: var(--space-sm);
  margin-top: var(--space-lg);
  padding-top: var(--space-lg);
  border-top: 1px solid var(--border-light);
}

.user-actions :deep(.el-button) {
  border-radius: var(--radius-md);
  font-weight: 500;
}

.user-actions :deep(.el-button--primary) {
  background: var(--primary-gradient);
  border: none;
}

.user-actions :deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

/* 响应式 */
@media (max-width: 480px) {
  .user-card {
    padding: var(--space-lg);
  }

  .user-header {
    flex-direction: column;
    text-align: center;
  }

  .user-info {
    width: 100%;
  }

  .user-name {
    font-size: 18px;
  }
}
</style>
