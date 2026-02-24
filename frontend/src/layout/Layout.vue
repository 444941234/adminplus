<template>
  <el-container class="layout-container">
    <Sidebar
      v-model:collapsed="isCollapsed"
      :menus="menus"
    />

    <el-container class="content-container">
      <LayoutHeader />

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import Sidebar from './components/Sidebar.vue';
import LayoutHeader from './components/LayoutHeader.vue';

defineOptions({
  name: 'Layout',
});

const router = useRouter();

// 侧边栏折叠状态
const isCollapsed = ref(false);

// 从路由配置中获取菜单数据
const menus = computed(() => {
  const allRoutes = router.getRoutes();
  const layoutRoute = allRoutes.find((r) => r.name === 'Layout');

  if (!layoutRoute?.children) {
    return [];
  }

  // 去重并过滤，只保留有 component 的路由
  const uniqueRoutes = filterUniqueRoutes(layoutRoute.children);
  return convertRoutesToMenus(uniqueRoutes);
});

// 过滤重复路由和无效路由（没有 component 的不显示为菜单项）
const filterUniqueRoutes = (routes) => {
  const seen = new Set();
  return routes.filter((r) => {
    // 过滤掉隐藏的路由
    if (r.meta?.hidden) {
      return false;
    }
    // 过滤掉没有 component 的路由（如只有 redirect 的路由）
    if (!r.components?.default && !r.component) {
      return false;
    }
    // 去重：根据 path 判断
    if (seen.has(r.path)) {
      return false;
    }
    seen.add(r.path);
    return true;
  });
};

// 将路由配置转换为菜单格式
const convertRoutesToMenus = (routes, parentPath = '') => {
  return routes.map((r) => {
    // 构建完整路径
    let fullPath = r.path;
    if (parentPath && !fullPath.startsWith('/')) {
      fullPath = `${parentPath}/${fullPath}`;
    }
    // 如果路径不是以 / 开头，添加前导 /
    if (!fullPath.startsWith('/')) {
      fullPath = `/${fullPath}`;
    }

    return {
      id: r.meta?.id || r.name,
      name: r.meta?.title || r.name,
      path: fullPath,
      icon: r.meta?.icon,
      children: r.children ? convertRoutesToMenus(r.children, fullPath) : [],
    };
  });
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100%;
}

/* 内容区域容器 - 占据剩余空间 */
:deep(.content-container) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0; /* 防止内容溢出 */
}

:deep(.el-main) {
  background-color: #f7f8fa;
  padding: 20px;
  flex: 1; /* 主内容区域占据剩余空间 */
  overflow: auto; /* 内容溢出时滚动 */
}
</style>
