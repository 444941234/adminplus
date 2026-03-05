# AdminPlus 前端改版设计文档

**日期**: 2026-03-05
**版本**: 1.0
**状态**: 已批准

## 概述

对 AdminPlus 前端进行全站改版，采用 Vue Shop Vite 的渐变鲜艳设计风格，创建独立的 UI 组件库。

### 目标

- 创建可复用的 `@adminplus/ui-vue` 组件库
- 实现渐变鲜艳的视觉风格
- 支持多主题切换
- 提升用户体验和视觉吸引力

## 技术栈

- Vue 3.5 + Element Plus 2.8
- Vite 6 + Pinia
- Sass/SCSS
- Vitest

## 目录结构

```
frontend/
├── packages/
│   └── ui-vue/              # 新的 UI 组件库
│       ├── src/
│       │   ├── styles/      # 样式系统
│       │   │   ├── variables.scss
│       │   │   ├── mixins.scss
│       │   │   └── themes/
│       │   │       ├── default.scss
│       │   │       ├── dark.scss
│       │   │       └── gradient.scss
│       │   ├── components/
│       │   │   ├── layout/
│       │   │   │   ├── AdminLayout.vue
│       │   │   │   ├── AppSidebar.vue
│       │   │   │   ├── AppHeader.vue
│       │   │   │   └── FloatingPanel.vue
│       │   │   └── card/
│       │   │       ├── StatCard.vue
│       │   │       ├── UserCard.vue
│       │   │       └── WelcomeBanner.vue
│       │   └── composables/
│       │       ├── useTheme.ts
│       │       └── useBreakpoint.ts
├── src/
│   ├── stores/
│   │   └── theme.ts        # 主题状态管理
│   └── utils/
│       └── errorHandler.ts # 错误处理
```

## 样式系统

### CSS 变量

```scss
:root {
  --primary-color: #4e88f3;
  --primary-gradient: linear-gradient(135deg, #4e88f3 0%, #6366f1 100%);
  --bg-primary: #ffffff;
  --bg-secondary: #f8fafc;
  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --radius-lg: 16px;
  --shadow-lg: 0 10px 30px rgba(0, 0, 0, 0.15);
  --sidebar-width: 260px;
  --header-height: 64px;
}
```

### 主题

| 主题名 | 描述 |
|--------|------|
| default | 默认商务风格 |
| gradient | 渐变鲜艳风格（默认） |
| dark | 暗黑模式 |

## 核心组件

### 1. AdminLayout

主布局组件，包含侧边栏、顶部导航、内容区和浮动面板。

### 2. AppSidebar

可折叠侧边栏，支持菜单导航、主题切换。

### 3. AppHeader

顶部导航，包含折叠按钮、面包屑、搜索框、用户信息。

### 4. FloatingPanel

右下角浮动快捷面板，支持主题配置、缓存清理等。

### 5. StatCard

统计卡片，带图标、数值、标签、趋势。

### 6. UserCard

用户信息卡片，展示头像、昵称、签名、标签等。

### 7. WelcomeBanner

欢迎横幅，个性化问候语。

## 状态管理

```typescript
// stores/theme.ts
interface ThemeState {
  currentTheme: 'default' | 'gradient' | 'dark';
  sidebarCollapsed: boolean;
  primaryColor: string;
}
```

## 实施计划

| 阶段 | 任务 | 工期 |
|------|------|------|
| 1 | 基础架构搭建 | 1周 |
| 2 | 核心组件开发 | 1周 |
| 3 | 卡片组件开发 | 3天 |
| 4 | 页面迁移改造 | 1周 |
| 5 | 测试优化 | 3天 |

**总工期**: 约 4 周

## 风险与缓解

| 风险 | 缓解措施 |
|------|---------|
| Element Plus 样式冲突 | 使用 CSS 变量覆盖 + scoped 样式 |
| 响应式适配问题 | 使用 useBreakpoint composable 统一处理 |
| 性能影响 | 按需加载组件，优化 CSS 体积 |

## 验收标准

- [ ] 所有新组件通过单元测试
- [ ] 支持移动端、平板、桌面端适配
- [ ] 主题切换流畅无闪烁
- [ ] 无控制台错误
- [ ] 页面加载时间 < 2s

## 后续优化

- 添加更多主题选项
- 支持自定义主题配置
- 组件 Storybook 文档
- 性能监控和优化
