<template>
  <template
    v-for="item in items"
    :key="item.id"
  >
    <!-- 有子菜单：递归渲染 -->
    <el-sub-menu
      v-if="item.children && item.children.length > 0"
      :index="item.path || item.id.toString()"
    >
      <template #title>
        <el-icon v-if="item.icon">
          <component :is="getIconComponent(item.icon)" />
        </el-icon>
        <span v-show="!collapsed">{{ item.name }}</span>
      </template>
      <SidebarMenu
        :items="item.children"
        :collapsed="collapsed"
      />
    </el-sub-menu>
    <!-- 无子菜单：菜单项 -->
    <el-menu-item
      v-else
      :index="item.path"
      @click="handleMenuClick(item)"
    >
      <el-tooltip
        v-if="collapsed"
        :content="item.name"
        placement="right"
        :show-after="300"
      >
        <el-icon v-if="item.icon">
          <component :is="getIconComponent(item.icon)" />
        </el-icon>
      </el-tooltip>
      <template v-else>
        <el-icon v-if="item.icon">
          <component :is="getIconComponent(item.icon)" />
        </el-icon>
        <span>{{ item.name }}</span>
      </template>
    </el-menu-item>
  </template>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router';
import { getIconComponent } from '@/constants/icons';

defineOptions({
  name: 'SidebarMenu',
});

defineProps({
  items: {
    type: Array,
    required: true,
    default: () => [],
  },
  collapsed: {
    type: Boolean,
    default: false,
  },
});

const router = useRouter();
const route = useRoute();

// 处理菜单点击
const handleMenuClick = (item) => {
  console.log('[SidebarMenu] 点击菜单:', item);

  if (item.path) {
    // 确保路径以 / 开头
    const targetPath = item.path.startsWith('/') ? item.path : `/${item.path}`;
    console.log('[SidebarMenu] 跳转到路径:', targetPath);

    router.push(targetPath).then(() => {
      console.log('[SidebarMenu] 路由跳转成功，当前路由:', route.path);
    }).catch((err) => {
      console.error('[SidebarMenu] 路由跳转失败:', err);
    });
  }
};
</script>

<style scoped>
/* 菜单项样式由父组件统一控制 */
</style>
