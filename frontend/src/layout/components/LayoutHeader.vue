<template>
  <el-header>
    <div class="header-left">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/">
        <el-breadcrumb-item
          v-for="item in breadcrumbs"
          :key="item.path"
          :to="item.path"
        >
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <!-- 全屏切换 -->
      <el-tooltip
        :content="isFullscreen ? '退出全屏' : '全屏'"
        placement="bottom"
      >
        <el-button
          text
          class="header-btn"
          @click="toggleFullscreen"
        >
          <el-icon :size="18">
            <component :is="isFullscreen ? CloseBold : FullScreen" />
          </el-icon>
        </el-button>
      </el-tooltip>

      <!-- 用户下拉菜单 -->
      <el-dropdown @command="handleCommand">
        <span class="el-dropdown-link">
          <el-icon><Avatar /></el-icon>
          <span class="username">{{ username }}</span>
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item
              command="logout"
              divided
            >
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Avatar, ArrowDown, User, SwitchButton, CloseBold, FullScreen } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { useConfirm } from '@/composables/useConfirm';

defineOptions({
  name: 'LayoutHeader',
});

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const isFullscreen = ref(false);

// 用户名显示
const username = computed(() => {
  return userStore.user?.nickname || userStore.user?.username || '用户';
});

// 面包屑数据
const breadcrumbs = computed(() => {
  const matched = route.matched.filter((item) => item.meta?.title);
  const result = matched.map((item) => ({
    path: item.path,
    title: item.meta?.title || item.name,
  }));

  // 确保首页在面包屑中
  if (result.length === 0 || result[0].path !== '/dashboard') {
    result.unshift({
      path: '/dashboard',
      title: '首页',
    });
  }

  return result;
});

// 全屏切换
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch((err) => {
      ElMessage.error(`无法进入全屏模式: ${err.message}`);
    });
  } else {
    document.exitFullscreen().catch((err) => {
      ElMessage.error(`无法退出全屏模式: ${err.message}`);
    });
  }
};

// 监听全屏状态变化
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement;
};

// 确认退出
const confirmLogout = useConfirm({
  message: '确定要退出登录吗？',
  type: 'warning',
});

// 下拉菜单命令处理
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await confirmLogout();
      userStore.logout();
      ElMessage.success('退出成功');
      await router.push('/login');
    } catch {
      // 取消操作
    }
  } else if (command === 'profile') {
    await router.push('/profile');
  }
};

onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange);
});

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange);
});
</script>

<style scoped lang="scss">
.el-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  padding: 0 24px;
  border-bottom: 1px solid #e5e7eb;
  height: 60px;
  width: 100%;
  box-sizing: border-box;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-btn {
  padding: 8px;
  border-radius: 8px;

  &:hover {
    background-color: #f5f7fa;
  }
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.3s;

  &:hover {
    background-color: #f5f7fa;
  }

  .username {
    color: #1a1a1a;
    font-weight: 500;
    font-size: 14px;
  }

  .el-icon {
    color: #0066ff;
    font-size: 16px;
  }
}

:deep(.el-breadcrumb__item) {
  .el-breadcrumb__inner {
    color: #666666;
    font-weight: 400;

    &.is-link:hover {
      color: #0066ff;
    }
  }

  &:last-child .el-breadcrumb__inner {
    color: #1a1a1a;
    font-weight: 500;
  }
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    font-size: 16px;
  }
}
</style>
