<template>
  <BmLayout
    :collapsed="collapsed"
    @toggle="handleToggle"
  >
    <template #sidebar>
      <AppSidebar
        :menus="menus"
        :collapsed="collapsed"
      />
    </template>

    <template #header>
      <AppHeader
        :user="userInfo"
        :collapsed="collapsed"
        @toggle="handleToggle"
        @command="handleCommand"
      />
    </template>

    <router-view />
  </BmLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { BmLayout, AppSidebar, AppHeader } from '@adminplus/ui-vue';
import type { MenuItem, UserInfo } from '@adminplus/ui-vue';
import { useUserStore } from '@/stores/user';

defineOptions({
  name: 'Layout'
});

const router = useRouter();
const userStore = useUserStore();
const collapsed = ref(false);

// 用户信息
const userInfo = computed<UserInfo>(() => ({
  nickname: userStore.user?.nickname || 'Admin',
  avatar: userStore.user?.avatar || ''
}));

// 处理侧边栏折叠
const handleToggle = () => {
  collapsed.value = !collapsed.value;
};

// 处理头部命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      await router.push('/profile');
      break;
    case 'settings':
      await router.push('/system/config');
      break;
    case 'logout':
      userStore.logout();
      await router.push('/login');
      break;
  }
};

// 从路由配置中获取菜单数据
const menus = computed<MenuItem[]>(() => {
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
const filterUniqueRoutes = (routes: any[]) => {
  const seen = new Set();
  return routes.filter((r) => {
    // 过滤掉隐藏的路由
    if (r.meta?.hidden) {
      return false;
    }
    // 过滤掉没有 component 的路由（目录类型除外）
    // 目录 type=0 不需要 component，但需要有子路由
    const isDirectory = r.meta?.type === 0;
    if (!isDirectory && !r.components?.default && !r.component) {
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
const convertRoutesToMenus = (routes: any[], parentPath = ''): MenuItem[] => {
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
/* 布局样式由 AdminLayout 组件内部处理 */
</style>
