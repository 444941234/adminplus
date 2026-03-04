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
        <el-icon v-if="item.icon && isValidIcon(item.icon)">
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
    >
      <el-tooltip
        v-if="collapsed"
        :content="item.name"
        placement="right"
        :show-after="300"
      >
        <el-icon v-if="item.icon && isValidIcon(item.icon)">
          <component :is="getIconComponent(item.icon)" />
        </el-icon>
      </el-tooltip>
      <template v-else>
        <el-icon v-if="item.icon && isValidIcon(item.icon)">
          <component :is="getIconComponent(item.icon)" />
        </el-icon>
        <span>{{ item.name }}</span>
      </template>
    </el-menu-item>
  </template>
</template>

<script setup>
import { getIconComponent, ICON_MAP } from '@/constants/icons';

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

// 检查图标是否有效
const isValidIcon = (iconName) => {
  return iconName && typeof iconName === 'string' && ICON_MAP[iconName];
};
</script>

<style scoped>
/* 菜单项样式由父组件统一控制 */
</style>
