<template>
  <el-container class="layout-container">
    <el-aside width="240px">
      <div class="logo">
        <h2>AdminPlus</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#FFFFFF"
        text-color="#333333"
        active-text-color="#0066FF"
      >
        <!-- 动态菜单 -->
        <template
          v-for="menu in menus"
          :key="menu.id"
        >
          <!-- 目录类型（有子菜单） -->
          <el-sub-menu
            v-if="menu.children && menu.children.length > 0"
            :index="menu.path || menu.id.toString()"
          >
            <template #title>
              <el-icon v-if="menu.icon">
                <component :is="getIcon(menu.icon)" />
              </el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <!-- 子菜单 -->
            <template
              v-for="child in menu.children"
              :key="child.id"
            >
              <!-- 如果子菜单还有子菜单（多层嵌套） -->
              <el-sub-menu
                v-if="child.children && child.children.length > 0"
                :index="child.path || child.id.toString()"
              >
                <template #title>
                  <el-icon v-if="child.icon">
                    <component :is="getIcon(child.icon)" />
                  </el-icon>
                  <span>{{ child.name }}</span>
                </template>
                <template
                  v-for="grandchild in child.children"
                  :key="grandchild.id"
                >
                  <el-menu-item
                    v-if="grandchild.type === 1"
                    :index="grandchild.path"
                  >
                    <el-icon v-if="grandchild.icon">
                      <component :is="getIcon(grandchild.icon)" />
                    </el-icon>
                    <span>{{ grandchild.name }}</span>
                  </el-menu-item>
                </template>
              </el-sub-menu>
              <!-- 普通菜单项 -->
              <el-menu-item
                v-else-if="child.type === 1"
                :index="child.path"
              >
                <el-icon v-if="child.icon">
                  <component :is="getIcon(child.icon)" />
                </el-icon>
                <span>{{ child.name }}</span>
              </el-menu-item>
            </template>
          </el-sub-menu>
          <!-- 菜单类型（无子菜单） -->
          <el-menu-item
            v-else-if="menu.type === 1"
            :index="menu.path"
          >
            <el-icon v-if="menu.icon">
              <component :is="getIcon(menu.icon)" />
            </el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-left">
          <span class="welcome-text">欢迎，{{ userStore.user?.nickname || userStore.user?.username }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-icon><Avatar /></el-icon>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item
                  command="logout"
                  divided
                >
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  ArrowDown,
  Avatar,
  DataAnalysis,
  Document,
  HomeFilled,
  Menu,
  Monitor,
  Setting,
  Tools,
  User,
  UserFilled,
} from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { useConfirm } from '@/composables/useConfirm';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const menus = ref([]);

// 图标映射表
const iconMap = {
  HomeFilled: HomeFilled,
  Setting: Setting,
  User: User,
  UserFilled: UserFilled,
  Menu: Menu,
  Document: Document,
  Tools: Tools,
  DataAnalysis: DataAnalysis,
  Monitor: Monitor,
};

// 获取图标组件
const getIcon = (iconName) => {
  return iconMap[iconName] || Menu;
};

const activeMenu = computed(() => route.path);

// 从路由配置中获取菜单数据
const loadMenusFromRoutes = () => {
  const layoutRoute = router.getRoutes().find((r) => r.name === 'Layout');
  if (layoutRoute && layoutRoute.children) {
    // 将路由数据转换为菜单格式
    menus.value = convertRoutesToMenus(layoutRoute.children);
    console.log('[Layout] 从路由加载菜单:', menus.value);
  }
};

// 将路由配置转换为菜单格式
const convertRoutesToMenus = (routes) => {
  return routes
    .filter((route) => !route.meta?.hidden)
    .map((route) => ({
      id: route.meta?.id || route.name,
      name: route.meta?.title || route.name,
      path: route.path,
      icon: route.meta?.icon,
      type: route.meta?.type,
      visible: route.meta?.hidden === false ? 1 : 0,
      children: route.children ? convertRoutesToMenus(route.children) : [],
    }));
};

// 确认操作
const confirmLogout = useConfirm({
  message: '确定要退出登录吗？',
  type: 'warning',
});

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
  loadMenusFromRoutes();
});
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #ffffff;
  border-right: 1px solid #e5e7eb;
  overflow-x: hidden;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #0066ff;
  font-size: 20px;
  font-weight: bold;
  background: linear-gradient(135deg, #0066ff 0%, #7b5fd6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  border-bottom: 1px solid #e5e7eb;
}

.el-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  padding: 0 24px;
  border-bottom: 1px solid #e5e7eb;
}

.header-left {
  font-size: 16px;
}

.welcome-text {
  color: #1a1a1a;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 20px;
  color: #0066ff;
  transition: color 0.3s;
}

.el-dropdown-link:hover {
  color: #3385ff;
}

.el-main {
  background-color: #f7f8fa;
  padding: 20px;
}

/* 菜单样式 */
:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item) {
  border-right: 3px solid transparent;
  transition: all 0.3s;
  margin: 4px 8px;
  border-radius: 8px;
}

:deep(.el-menu-item.is-active) {
  background-color: #e8f0fe !important;
  color: #0066ff !important;
  font-weight: 600;
}

:deep(.el-menu-item:hover) {
  background-color: #f5f7fa;
  color: #0066ff;
}

:deep(.el-sub-menu__title) {
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #f5f7fa;
  color: #0066ff;
}

:deep(.el-sub-menu .el-menu-item) {
  margin: 4px 8px 4px 24px;
  border-radius: 6px;
}
</style>