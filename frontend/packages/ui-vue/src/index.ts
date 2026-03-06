// 导出 composables
export * from './composables/useTheme';
export * from './composables/useBreakpoint';

// 导出布局组件
export { default as FloatingPanel } from './components/layout/FloatingPanel.vue';
export { default as AppHeader } from './components/layout/AppHeader.vue';
export { default as AppSidebar } from './components/layout/AppSidebar.vue';
export { default as AdminLayout } from './components/layout/AdminLayout.vue';
export { default as SidebarMenuItem } from './components/layout/SidebarMenuItem.vue';

// 导出卡片组件
export { default as StatCard } from './components/card/StatCard.vue';
export { default as UserCard } from './components/card/UserCard.vue';
export { default as WelcomeBanner } from './components/card/WelcomeBanner.vue';
export { default as ActionCard } from './components/card/ActionCard.vue';

// 导出 BigModel 组件
export * from './components/bigmodel';

// 导出常用反馈 composables（用于替换 Element Plus）
export { useToast, toast } from './components/bigmodel/feedback/useToast';
export { useConfirm, confirmDialog } from './components/bigmodel/feedback/useConfirm';

// 导出类型
export type { MenuItem } from './components/layout/SidebarMenuItem.vue';
export type { UserInfo, BreadcrumbItem } from './components/layout/AppHeader.vue';
export type { UserInfo as UserCardInfo } from './components/card/UserCard.vue';
export type { ActionItem } from './components/card/ActionCard.vue';
