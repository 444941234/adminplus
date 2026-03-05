# AdminPlus 前端改版实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 创建独立的 @adminplus/ui-vue 组件库，实现渐变鲜艳风格的全站改版

**Architecture:**
- 创建 packages/ui-vue 作为独立 UI 组件库
- 使用 CSS 变量 + Sass mixins 构建样式系统
- Pinia 管理主题状态，支持运行时切换
- 组件采用 TDD 开发，每个组件都有对应测试

**Tech Stack:**
- Vue 3.5 + Element Plus 2.8
- Vite 6 + Sass
- Pinia 2.2
- Vitest 2.1

---

## 前置检查

### Task 0: 环境准备

**Files:**
- Check: `frontend/package.json`
- Check: `backend/pom.xml`

**Step 1: 验证前端依赖已安装**

Run: `cd frontend && npm list vue element-plus pinia`

Expected Output:
```
vue@3.5.x
element-plus@2.8.x
pinia@2.2.x
```

**Step 2: 验证后端服务可访问**

Run: `curl http://localhost:8081/api/actuator/health`

Expected: `{"status":"UP"}`

**Step 3: 创建工作目录**

Run: `mkdir -p frontend/packages/ui-vue/src/{styles,components,composables}`

---

## 阶段一：基础架构搭建

### Task 1: 创建 ui-vue 包配置

**Files:**
- Create: `frontend/packages/ui-vue/package.json`

**Step 1: 创建 package.json**

```json
{
  "name": "@adminplus/ui-vue",
  "version": "1.0.0",
  "type": "module",
  "main": "src/index.ts",
  "exports": {
    ".": "./src/index.ts",
    "./styles": "./src/styles/index.scss"
  },
  "dependencies": {
    "vue": "^3.5.0",
    "element-plus": "^2.8.0",
    "@element-plus/icons-vue": "^2.3.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "sass-embedded": "^1.97.3"
  }
}
```

**Step 2: 创建 Vite 配置**

Create: `frontend/packages/ui-vue/vite.config.ts`

```typescript
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/styles/variables.scss" as *;`
      }
    }
  }
});
```

**Step 3: 创建 TypeScript 配置**

Create: `frontend/packages/ui-vue/tsconfig.json`

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "jsx": "preserve",
    "esModuleInterop": true,
    "skipLibCheck": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules"]
}
```

**Step 4: 提交**

Run:
```bash
cd frontend
git add packages/ui-vue/
git commit -m "feat: 创建 ui-vue 组件库基础结构"
```

---

### Task 2: 创建 CSS 变量系统

**Files:**
- Create: `frontend/packages/ui-vue/src/styles/variables.scss`
- Create: `frontend/packages/ui-vue/src/styles/mixins.scss`
- Create: `frontend/packages/ui-vue/src/styles/index.scss`

**Step 1: 创建 CSS 变量文件**

Create: `frontend/packages/ui-vue/src/styles/variables.scss`

```scss
// ========== 颜色系统 ==========
:root {
  // 主色调 - 渐变鲜艳风
  --primary-color: #4e88f3;
  --primary-light: #7aa9f8;
  --primary-dark: #2563eb;
  --primary-gradient: linear-gradient(135deg, #4e88f3 0%, #6366f1 100%);

  // 辅助色
  --success-color: #10b981;
  --success-light: #34d399;
  --success-dark: #059669;
  --success-gradient: linear-gradient(135deg, #10b981 0%, #059669 100%);

  --warning-color: #f59e0b;
  --warning-light: #fbbf24;
  --warning-dark: #d97706;
  --warning-gradient: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);

  --danger-color: #ef4444;
  --danger-light: #f87171;
  --danger-dark: #dc2626;
  --danger-gradient: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);

  --info-color: #06b6d4;
  --info-light: #22d3ee;
  --info-dark: #0891b2;

  // 背景色
  --bg-primary: #ffffff;
  --bg-secondary: #f8fafc;
  --bg-tertiary: #f1f5f9;
  --bg-dark: #0f172a;
  --bg-page: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);

  // 文字色
  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --text-tertiary: #94a3b8;
  --text-inverse: #ffffff;
  --text-disabled: #cbd5e1;

  // 边框色
  --border-color: #e2e8f0;
  --border-light: #f1f5f9;
  --border-dark: #cbd5e1;

  // 圆角
  --radius-xs: 4px;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 24px;
  --radius-2xl: 32px;
  --radius-full: 9999px;

  // 阴影
  --shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.1);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 30px rgba(0, 0, 0, 0.15);
  --shadow-xl: 0 20px 50px rgba(0, 0, 0, 0.2);
  --shadow-2xl: 0 25px 60px rgba(0, 0, 0, 0.25);

  // 内阴影
  --shadow-inner: inset 0 2px 4px rgba(0, 0, 0, 0.06);

  // 间距
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 16px;
  --space-lg: 24px;
  --space-xl: 32px;
  --space-2xl: 48px;
  --space-3xl: 64px;

  // 布局尺寸
  --sidebar-width: 260px;
  --sidebar-collapsed-width: 70px;
  --header-height: 64px;
  --footer-height: 50px;

  // 过渡
  --transition-fast: 150ms ease;
  --transition-normal: 250ms ease;
  --transition-slow: 350ms ease;
  --transition-slower: 500ms ease;

  // Z-index
  --z-dropdown: 1000;
  --z-sticky: 1020;
  --z-fixed: 1030;
  --z-modal-backdrop: 1040;
  --z-modal: 1050;
  --z-popover: 1060;
  --z-tooltip: 1070;
}

// 暗黑主题变量
.theme-dark {
  --bg-primary: #1e293b;
  --bg-secondary: #0f172a;
  --bg-tertiary: #334155;
  --bg-page: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);

  --text-primary: #f1f5f9;
  --text-secondary: #cbd5e1;
  --text-tertiary: #94a3b8;

  --border-color: #334155;
  --border-light: #1e293b;
  --border-dark: #475569;
}

// 渐变主题变量
.theme-gradient {
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --success-gradient: linear-gradient(135deg, #56ab2f 0%, #a8e063 100%);
  --card-gradient: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}
```

**Step 2: 创建 Sass Mixins 文件**

Create: `frontend/packages/ui-vue/src/styles/mixins.scss`

```scss
// 卡片基础样式
@mixin card-style {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  transition: all var(--transition-normal);
  overflow: hidden;

  &:hover {
    box-shadow: var(--shadow-lg);
    transform: translateY(-2px);
  }
}

// 渐变按钮
@mixin gradient-button($gradient: var(--primary-gradient)) {
  background: $gradient;
  color: var(--text-inverse);
  border: none;
  padding: 12px 24px;
  border-radius: var(--radius-md);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-normal);

  &:hover {
    filter: brightness(1.1);
    transform: translateY(-1px);
    box-shadow: var(--shadow-lg);
  }

  &:active {
    transform: translateY(0);
  }
}

// 玻璃态效果
@mixin glass-effect($opacity: 0.8) {
  background: rgba(255, 255, 255, $opacity);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

// Flex 居中
@mixin flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

// Flex 列居中
@mixin flex-column-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

// 文本省略
@mixin text-ellipsis($lines: 1) {
  @if $lines == 1 {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  } @else {
    display: -webkit-box;
    -webkit-line-clamp: $lines;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

// 响应式断点
@mixin mobile {
  @media (max-width: 767px) {
    @content;
  }
}

@mixin tablet {
  @media (min-width: 768px) and (max-width: 1023px) {
    @content;
  }
}

@mixin desktop {
  @media (min-width: 1024px) {
    @content;
  }
}

// 滚动条样式
@mixin custom-scrollbar($width: 6px, $thumb-color: var(--text-tertiary)) {
  &::-webkit-scrollbar {
    width: $width;
    height: $width;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: $thumb-color;
    border-radius: var(--radius-full);

    &:hover {
      background: var(--text-secondary);
    }
  }
}

// 动画
@mixin fade-in($duration: var(--transition-normal)) {
  animation: fadeIn $duration ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@mixin slide-up($duration: var(--transition-normal)) {
  animation: slideUp $duration ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

**Step 3: 创建样式入口文件**

Create: `frontend/packages/ui-vue/src/styles/index.scss`

```scss
// 导入变量
@use './variables.scss';

// 导入 mixins
@use './mixins.scss';

// 导入主题
@use './themes/default.scss';
@use './themes/gradient.scss';
@use './themes/dark.scss';

// 导入组件样式
@use './components/layout.scss';
@use './components/card.scss';

// 全局样式
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  width: 100%;
  height: 100%;
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-normal);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all var(--transition-normal);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
```

**Step 4: 创建主题文件**

Create: `frontend/packages/ui-vue/src/styles/themes/default.scss`

```scss
// 默认主题
.theme-default {
  // 继承 root 变量
}
```

Create: `frontend/packages/ui-vue/src/styles/themes/gradient.scss`

```scss
// 渐变鲜艳主题
.theme-gradient {
  --primary-color: #6366f1;
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --bg-page: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);
}
```

Create: `frontend/packages/ui-vue/src/styles/themes/dark.scss`

```scss
// 暗黑主题
.theme-dark {
  --bg-primary: #1e293b;
  --bg-secondary: #0f172a;
  --bg-tertiary: #334155;
  --text-primary: #f1f5f9;
  --text-secondary: #cbd5e1;
  --border-color: #334155;
}
```

**Step 5: 创建组件样式文件**

Create: `frontend/packages/ui-vue/src/styles/components/layout.scss`

```scss
// 布局组件样式
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page);
}

.app-sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background: var(--bg-dark);
  position: fixed;
  left: 0;
  top: 0;
  transition: width var(--transition-normal);
  z-index: var(--z-fixed);

  &.collapsed {
    width: var(--sidebar-collapsed-width);
  }
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: var(--sidebar-width);
  transition: margin-left var(--transition-normal);

  &.sidebar-collapsed {
    margin-left: var(--sidebar-collapsed-width);
  }
}

.app-header {
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  padding: 0 var(--space-lg);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
}

.content-area {
  flex: 1;
  padding: var(--space-lg);
  overflow-y: auto;
  @include custom-scrollbar;
}

@include mobile {
  .app-sidebar {
    transform: translateX(-100%);

    &.mobile-open {
      transform: translateX(0);
    }
  }

  .main-wrapper {
    margin-left: 0;
  }
}
```

Create: `frontend/packages/ui-vue/src/styles/components/card.scss`

```scss
// 卡片组件样式
.stat-card {
  @include card-style;
  padding: var(--space-lg);
  display: flex;
  align-items: center;
  gap: var(--space-lg);

  &-primary .stat-icon {
    background: var(--primary-gradient);
  }

  &-success .stat-icon {
    background: var(--success-gradient);
  }

  &-warning .stat-icon {
    background: var(--warning-gradient);
  }

  &-danger .stat-icon {
    background: var(--danger-gradient);
  }
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: var(--space-sm);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-top: var(--space-sm);

  &.trend-up {
    color: var(--success-color);
  }

  &.trend-down {
    color: var(--danger-color);
  }
}

// 用户卡片
.user-card {
  @include card-style;
  padding: var(--space-xl);
}

.user-header {
  display: flex;
  gap: var(--space-lg);
  align-items: center;
}

.user-name {
  font-size: 20px;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
}

.user-motto {
  font-size: 14px;
  color: var(--text-secondary);
  font-style: italic;
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
  margin-bottom: var(--space-sm);
  color: var(--text-secondary);
  font-size: 14px;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
  margin-top: var(--space-lg);
}

// 欢迎横幅
.welcome-banner {
  @include card-style;
  padding: var(--space-2xl);
  color: white;
  position: relative;
  overflow: hidden;
  background: var(--primary-gradient);

  .banner-title {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: var(--space-sm);
  }

  .banner-subtitle {
    font-size: 16px;
    opacity: 0.9;
  }

  .banner-greeting {
    margin-top: var(--space-md);
    padding: var(--space-sm) var(--space-md);
    background: rgba(255, 255, 255, 0.2);
    border-radius: var(--radius-md);
    backdrop-filter: blur(10px);
    font-size: 14px;
    display: inline-block;
  }
}

// 浮动面板
.floating-panel {
  position: fixed;
  right: var(--space-lg);
  bottom: var(--space-lg);
  z-index: var(--z-fixed);
}

.panel-trigger {
  width: 48px;
  height: 48px;
  background: var(--primary-gradient);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  transition: all var(--transition-normal);

  &:hover {
    transform: scale(1.1);
  }
}

.panel-content {
  position: absolute;
  bottom: 60px;
  right: 0;
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xl);
  padding: var(--space-md);
  min-width: 180px;
}

.panel-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
  color: var(--text-primary);

  &:hover {
    background: var(--bg-secondary);
  }
}

.panel-divider {
  height: 1px;
  background: var(--border-color);
  margin: var(--space-sm) 0;
}
```

**Step 6: 提交**

Run:
```bash
cd frontend
git add packages/ui-vue/src/styles/
git commit -m "feat: 添加样式系统（CSS 变量、mixins、主题）"
```

---

### Task 3: 创建 Composables

**Files:**
- Create: `frontend/packages/ui-vue/src/composables/useTheme.ts`
- Create: `frontend/packages/ui-vue/src/composables/useBreakpoint.ts`

**Step 1: 创建 useTheme composable**

Create: `frontend/packages/ui-vue/src/composables/useTheme.ts`

```typescript
import { computed, watch } from 'vue';

export type ThemeType = 'default' | 'gradient' | 'dark';

export interface UseThemeOptions {
  storageKey?: string;
  defaultTheme?: ThemeType;
}

export function useTheme(options: UseThemeOptions = {}) {
  const {
    storageKey = 'adminplus-theme',
    defaultTheme = 'gradient'
  } = options;

  // 从 localStorage 读取保存的主题
  const savedTheme = localStorage.getItem(storageKey) as ThemeType;
  const currentTheme = computed(() => savedTheme || defaultTheme);

  // 应用主题到 document
  const applyTheme = (theme: ThemeType) => {
    const html = document.documentElement;
    // 移除所有主题类
    html.classList.remove('theme-default', 'theme-gradient', 'theme-dark');
    // 添加当前主题类
    html.classList.add(`theme-${theme}`);
    // 保存到 localStorage
    localStorage.setItem(storageKey, theme);
  };

  // 设置主题
  const setTheme = (theme: ThemeType) => {
    applyTheme(theme);
  };

  // 切换到下一个主题
  const toggleTheme = () => {
    const themes: ThemeType[] = ['default', 'gradient', 'dark'];
    const currentIndex = themes.indexOf(currentTheme.value);
    const nextTheme = themes[(currentIndex + 1) % themes.length];
    setTheme(nextTheme);
  };

  // 计算属性
  const isDark = computed(() => currentTheme.value === 'dark');
  const isGradient = computed(() => currentTheme.value === 'gradient');

  // 初始化应用主题
  applyTheme(currentTheme.value);

  return {
    currentTheme,
    isDark,
    isGradient,
    setTheme,
    toggleTheme
  };
}
```

**Step 2: 创建 useBreakpoint composable**

Create: `frontend/packages/ui-vue/src/composables/useBreakpoint.ts`

```typescript
import { ref, computed, onMounted, onUnmounted } from 'vue';

export type Breakpoint = 'mobile' | 'tablet' | 'desktop';

export interface BreakpointValues {
  mobile: number;
  tablet: number;
  desktop: number;
}

export interface UseBreakpointOptions {
  mobile?: number;
  tablet?: number;
  desktop?: number;
}

export function useBreakpoint(options: UseBreakpointOptions = {}) {
  const {
    mobile = 768,
    tablet = 1024,
    desktop = 1024
  } = options;

  const width = ref(window.innerWidth);

  // 计算当前断点
  const breakpoint = computed<Breakpoint>(() => {
    if (width.value < mobile) return 'mobile';
    if (width.value < tablet) return 'tablet';
    return 'desktop';
  });

  // 便捷计算属性
  const isMobile = computed(() => breakpoint.value === 'mobile');
  const isTablet = computed(() => breakpoint.value === 'tablet');
  const isDesktop = computed(() => breakpoint.value === 'desktop');
  const isMobileOrTablet = computed(() => isMobile.value || isTablet.value);

  // 处理窗口大小变化
  const handleResize = () => {
    width.value = window.innerWidth;
  };

  // 添加事件监听
  onMounted(() => {
    window.addEventListener('resize', handleResize);
  });

  // 移除事件监听
  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
  });

  return {
    width,
    breakpoint,
    isMobile,
    isTablet,
    isDesktop,
    isMobileOrTablet
  };
}
```

**Step 3: 提交**

Run:
```bash
cd frontend
git add packages/ui-vue/src/composables/
git commit -m "feat: 添加 useTheme 和 useBreakpoint composables"
```

---

### Task 4: 创建组件导出入口

**Files:**
- Create: `frontend/packages/ui-vue/src/index.ts`

**Step 1: 创建导出文件**

Create: `frontend/packages/ui-vue/src/index.ts`

```typescript
// 导出 composables
export * from './composables/useTheme';
export * from './composables/useBreakpoint';

// 导出样式
export * from './styles/variables.scss';
export * from './styles/mixins.scss';

// 组件导出将在后续添加
// export * from './components/layout';
// export * from './components/card';
```

**Step 2: 更新主项目依赖**

Modify: `frontend/package.json`

在 `dependencies` 中添加：
```json
"@adminplus/ui-vue": "workspace:*"
```

**Step 3: 提交**

Run:
```bash
cd frontend
git add packages/ui-vue/src/index.ts package.json
git commit -m "feat: 添加 ui-vue 组件库导出入口"
```

---

## 阶段二：核心布局组件

### Task 5: 创建 FloatingPanel 组件

**Files:**
- Create: `frontend/packages/ui-vue/src/components/layout/FloatingPanel.vue`
- Test: `frontend/packages/ui-vue/src/components/layout/FloatingPanel.test.ts`

**Step 1: 编写测试**

Create: `frontend/packages/ui-vue/src/components/layout/FloatingPanel.test.ts`

```typescript
import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import { h } from 'vue';
import FloatingPanel from './FloatingPanel.vue';

describe('FloatingPanel', () => {
  it('should render trigger button', () => {
    const wrapper = mount(FloatingPanel);
    expect(wrapper.find('.panel-trigger').exists()).toBe(true);
  });

  it('should not show panel content by default', () => {
    const wrapper = mount(FloatingPanel);
    expect(wrapper.find('.panel-content').exists()).toBe(false);
  });

  it('should show panel content when expanded', async () => {
    const wrapper = mount(FloatingPanel);
    await wrapper.find('.panel-trigger').trigger('click');
    expect(wrapper.find('.panel-content').exists()).toBe(true);
  });

  it('should emit toggle event', async () => {
    const wrapper = mount(FloatingPanel);
    await wrapper.find('.panel-trigger').trigger('click');
    expect(wrapper.emitted('toggle')).toBeTruthy();
  });
});
```

**Step 2: 运行测试验证失败**

Run: `cd frontend && npm test FloatingPanel.test.ts`

Expected: FAIL with "component not found"

**Step 3: 实现组件**

Create: `frontend/packages/ui-vue/src/components/layout/FloatingPanel.vue`

```vue
<template>
  <div class="floating-panel" :class="{ expanded }">
    <!-- 触发按钮 -->
    <div class="panel-trigger" @click="toggle">
      <el-icon :size="20">
        <Close v-if="expanded" />
        <Operation v-else />
      </el-icon>
    </div>

    <!-- 面板内容 -->
    <transition name="slide-up">
      <div v-show="expanded" class="panel-content">
        <div class="panel-item" @click="handleTheme">
          <el-icon><Brush /></el-icon>
          <span>随机换肤</span>
        </div>
        <div class="panel-item" @click="handleConfig">
          <el-icon><Setting /></el-icon>
          <span>主题配置</span>
        </div>
        <div class="panel-item" @click="handleClearCache">
          <el-icon><Delete /></el-icon>
          <span>清理缓存</span>
        </div>
        <div class="panel-divider"></div>
        <div class="panel-item collapse" @click="toggle">
          <el-icon><ArrowDown /></el-icon>
          <span>收起浮窗</span>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import {
  Close,
  Operation,
  Brush,
  Setting,
  Delete,
  ArrowDown
} from '@element-plus/icons-vue';

defineOptions({
  name: 'FloatingPanel'
});

const expanded = ref(false);

const emit = defineEmits<{
  toggle: [];
  themeChange: [];
  config: [];
  clearCache: [];
}>();

const toggle = () => {
  expanded.value = !expanded.value;
  emit('toggle');
};

const handleTheme = () => {
  emit('themeChange');
  toggle();
};

const handleConfig = () => {
  emit('config');
  toggle();
};

const handleClearCache = () => {
  emit('clearCache');
  toggle();
};
</script>

<style scoped>
.floating-panel {
  position: fixed;
  right: var(--space-lg);
  bottom: var(--space-lg);
  z-index: var(--z-fixed);
}

.panel-trigger {
  width: 48px;
  height: 48px;
  background: var(--primary-gradient);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  transition: all var(--transition-normal);
}

.panel-trigger:hover {
  transform: scale(1.1);
}

.panel-content {
  position: absolute;
  bottom: 60px;
  right: 0;
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xl);
  padding: var(--space-md);
  min-width: 180px;
}

.panel-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
  color: var(--text-primary);
  font-size: 14px;
}

.panel-item:hover {
  background: var(--bg-secondary);
}

.panel-divider {
  height: 1px;
  background: var(--border-color);
  margin: var(--space-sm) 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all var(--transition-normal);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
```

**Step 4: 运行测试验证通过**

Run: `cd frontend && npm test FloatingPanel.test.ts`

Expected: PASS

**Step 5: 提交**

Run:
```bash
cd frontend
git add packages/ui-vue/src/components/layout/FloatingPanel.vue
git add packages/ui-vue/src/components/layout/FloatingPanel.test.ts
git commit -m "feat: 添加 FloatingPanel 组件"
```

---

### Task 6: 创建 AppHeader 组件

**Files:**
- Create: `frontend/packages/ui-vue/src/components/layout/AppHeader.vue`
- Test: `frontend/packages/ui-vue/src/components/layout/AppHeader.test.ts`

**Step 1: 编写测试**

Create: `frontend/packages/ui-vue/src/components/layout/AppHeader.test.ts`

```typescript
import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import AppHeader from './AppHeader.vue';

describe('AppHeader', () => {
  const mockUser = {
    nickname: '测试用户',
    avatar: 'https://example.com/avatar.jpg'
  };

  const mockBreadcrumbs = [
    { title: '首页', path: '/' },
    { title: '用户管理', path: '/users' }
  ];

  it('should render user info', () => {
    const wrapper = mount(AppHeader, {
      props: {
        user: mockUser,
        breadcrumbs: mockBreadcrumbs
      }
    });
    expect(wrapper.text()).toContain('测试用户');
  });

  it('should render breadcrumbs', () => {
    const wrapper = mount(AppHeader, {
      props: {
        user: mockUser,
        breadcrumbs: mockBreadcrumbs
      }
    });
    expect(wrapper.find('.el-breadcrumb').exists()).toBe(true);
  });

  it('should emit toggle event when collapse button clicked', async () => {
    const wrapper = mount(AppHeader, {
      props: {
        user: mockUser,
        breadcrumbs: [],
        collapsed: false
      }
    });
    await wrapper.find('.header-left button').trigger('click');
    expect(wrapper.emitted('toggle')).toBeTruthy();
  });
});
```

**Step 2: 运行测试验证失败**

Run: `cd frontend && npm test AppHeader.test.ts`

Expected: FAIL

**Step 3: 实现组件**

Create: `frontend/packages/ui-vue/src/components/layout/AppHeader.vue`

```vue
<template>
  <header class="app-header">
    <!-- 左侧：折叠按钮 + 面包屑 -->
    <div class="header-left">
      <el-button
        :icon="collapsed ? Expand : Fold"
        circle
        @click="$emit('toggle')"
      />
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

    <!-- 中间：搜索框 -->
    <div class="header-center">
      <el-input
        v-model="searchText"
        placeholder="AI 智能搜索..."
        :prefix-icon="Search"
        class="search-input"
        clearable
        @input="handleSearch"
      />
    </div>

    <!-- 右侧：用户信息 -->
    <div class="header-right">
      <!-- 通知 -->
      <el-badge :value="notificationCount" :max="99">
        <el-button :icon="Bell" circle />
      </el-badge>

      <!-- 用户下拉 -->
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :src="user.avatar" :size="36" />
          <span class="user-name">{{ user.nickname }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import {
  Expand,
  Fold,
  Search,
  Bell,
  User,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue';

export interface BreadcrumbItem {
  title: string;
  path: string;
}

export interface UserInfo {
  nickname: string;
  avatar: string;
}

defineOptions({
  name: 'AppHeader'
});

interface Props {
  collapsed?: boolean;
  user: UserInfo;
  breadcrumbs?: BreadcrumbItem[];
  notificationCount?: number;
}

withDefaults(defineProps<Props>(), {
  collapsed: false,
  breadcrumbs: () => [],
  notificationCount: 0
});

const emit = defineEmits<{
  toggle: [];
  search: [value: string];
  command: [command: string];
}>();

const searchText = ref('');

const handleSearch = (value: string) => {
  emit('search', value);
};

const handleCommand = (command: string) => {
  emit('command', command);
};
</script>

<style scoped>
.app-header {
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  padding: 0 var(--space-lg);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.header-center {
  flex: 1;
  max-width: 500px;
  margin: 0 var(--space-lg);
}

.search-input {
  width: 100%;
}

:deep(.search-input .el-input__wrapper) {
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-sm);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.user-info:hover {
  background: var(--bg-secondary);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

@include mobile {
  .header-center {
    display: none;
  }

  .user-name {
    display: none;
  }
}
</style>
```

**Step 4: 运行测试验证通过**

Run: `cd frontend && npm test AppHeader.test.ts`

Expected: PASS

**Step 5: 提交**

Run:
```bash
cd frontend
git add packages/ui-vue/src/components/layout/AppHeader.vue
git add packages/ui-vue/src/components/layout/AppHeader.test.ts
git commit -m "feat: 添加 AppHeader 组件"
```

---

### Task 7-9: 创建其他布局组件

由于篇幅限制，以下组件按相同模式实现：

- **Task 7**: AppSidebar.vue
- **Task 8**: AdminLayout.vue
- **Task 9**: SidebarMenuItem.vue

每个组件遵循 TDD 流程：测试 → 实现 → 提交

---

## 阶段三：卡片组件

### Task 10: 创建 StatCard 组件

**Files:**
- Create: `frontend/packages/ui-vue/src/components/card/StatCard.vue`
- Test: `frontend/packages/ui-vue/src/components/card/StatCard.test.ts`

**Step 1: 编写测试**

Create: `frontend/packages/ui-vue/src/components/card/StatCard.test.ts`

```typescript
import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import StatCard from './StatCard.vue';

describe('StatCard', () => {
  it('should render with correct props', () => {
    const wrapper = mount(StatCard, {
      props: {
        type: 'primary',
        icon: 'User',
        value: 1234,
        label: '用户总数'
      }
    });
    expect(wrapper.find('.stat-value').text()).toBe('1234');
    expect(wrapper.find('.stat-label').text()).toBe('用户总数');
  });

  it('should show trend when provided', () => {
    const wrapper = mount(StatCard, {
      props: {
        type: 'primary',
        icon: 'User',
        value: 1234,
        label: '用户总数',
        trend: '+12%',
        trendUp: true
      }
    });
    expect(wrapper.find('.stat-trend').exists()).toBe(true);
    expect(wrapper.find('.stat-trend').text()).toContain('+12%');
    expect(wrapper.find('.stat-trend').classes()).toContain('trend-up');
  });

  it('should apply correct class based on type', () => {
    const wrapper = mount(StatCard, {
      props: {
        type: 'success',
        icon: 'Check',
        value: 100,
        label: '完成率'
      }
    });
    expect(wrapper.find('.stat-card').classes()).toContain('stat-card-success');
  });
});
```

**Step 2-5**: 实现并测试（略）

---

### Task 11-13: 创建其他卡片组件

- **Task 11**: UserCard.vue
- **Task 12**: WelcomeBanner.vue
- **Task 13**: ActionCard.vue

---

## 阶段四：状态管理

### Task 14: 创建主题状态管理

**Files:**
- Create: `frontend/src/stores/theme.ts`

**Step 1: 创建主题 store**

Create: `frontend/src/stores/theme.ts`

```typescript
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { ThemeType } from '@adminplus/ui-vue';

export const useThemeStore = defineStore('theme', () => {
  // 状态
  const currentTheme = ref<ThemeType>('gradient');
  const sidebarCollapsed = ref(false);
  const primaryColor = ref('#6366f1');

  // 计算属性
  const isDark = computed(() => currentTheme.value === 'dark');
  const isGradient = computed(() => currentTheme.value === 'gradient');
  const themeClass = computed(() => `theme-${currentTheme.value}`);

  // 方法
  function setTheme(theme: ThemeType) {
    currentTheme.value = theme;
    document.documentElement.className = themeClass.value;
    localStorage.setItem('adminplus-theme', theme);
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value;
    localStorage.setItem(
      'adminplus-sidebar-collapsed',
      String(sidebarCollapsed.value)
    );
  }

  function setPrimaryColor(color: string) {
    primaryColor.value = color;
    document.documentElement.style.setProperty('--primary-color', color);
    localStorage.setItem('adminplus-primary-color', color);
  }

  // 初始化
  function init() {
    const savedTheme = localStorage.getItem('adminplus-theme') as ThemeType;
    const savedCollapsed = localStorage.getItem('adminplus-sidebar-collapsed');
    const savedColor = localStorage.getItem('adminplus-primary-color');

    if (savedTheme) setTheme(savedTheme);
    if (savedCollapsed) sidebarCollapsed.value = savedCollapsed === 'true';
    if (savedColor) setPrimaryColor(savedColor);
  }

  return {
    currentTheme,
    sidebarCollapsed,
    primaryColor,
    isDark,
    isGradient,
    themeClass,
    setTheme,
    toggleSidebar,
    setPrimaryColor,
    init
  };
});
```

**Step 2: 在 main.ts 中初始化**

Modify: `frontend/src/main.ts`

```typescript
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import '@adminplus/ui-vue/styles'; // 导入 UI 组件库样式

import App from './App.vue';
import router from './router';
import { useThemeStore } from './stores/theme';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(ElementPlus);

// 初始化主题
const themeStore = useThemeStore();
themeStore.init();

app.mount('#app');
```

**Step 3: 提交**

Run:
```bash
cd frontend
git add src/stores/theme.ts src/main.ts
git commit -m "feat: 添加主题状态管理"
```

---

## 阶段五：页面迁移

### Task 15: 改造 Dashboard 页面

**Files:**
- Modify: `frontend/src/views/Dashboard.vue`

**Step 1: 使用新组件重写 Dashboard**

Modify: `frontend/src/views/Dashboard.vue`

```vue
<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <WelcomeBanner
      :username="userStore.username || 'Admin'"
      :greeting="greeting"
    />

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="stat in stats" :key="stat.key">
        <StatCard
          :type="stat.type"
          :icon="stat.icon"
          :value="stat.value"
          :label="stat.label"
          :trend="stat.trend"
          :trend-up="stat.trendUp"
          :loading="loading"
        />
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <span>数据趋势</span>
          </template>
          <div ref="lineChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <span>数据分布</span>
          </template>
          <div ref="pieChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useUserStore } from '@/stores/user';
import { useThemeStore } from '@/stores/theme';
import { WelcomeBanner, StatCard } from '@adminplus/ui-vue';
import * as echarts from 'echarts';

defineOptions({
  name: 'Dashboard'
});

const userStore = useUserStore();
const themeStore = useThemeStore();

const loading = ref(false);

// 统计数据
const stats = ref([
  {
    key: 'users',
    type: 'primary' as const,
    icon: 'User',
    value: 1234,
    label: '用户总数',
    trend: '+12%',
    trendUp: true
  },
  {
    key: 'roles',
    type: 'success' as const,
    icon: 'UserFilled',
    value: 56,
    label: '角色数量',
    trend: '+5%',
    trendUp: true
  },
  {
    key: 'depts',
    type: 'warning' as const,
    icon: 'OfficeBuilding',
    value: 23,
    label: '部门数量',
    trend: '0%',
    trendUp: true
  },
  {
    key: 'logs',
    type: 'danger' as const,
    icon: 'Document',
    value: 856,
    label: '今日日志',
    trend: '-3%',
    trendUp: false
  }
]);

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 12) return '上午好，开始新的一天！';
  if (hour < 18) return '下午好，继续加油！';
  return '晚上好，注意休息！';
});

// 图表
const lineChartRef = ref<HTMLElement>();
const pieChartRef = ref<HTMLElement>();
let lineChart: echarts.ECharts | null = null;
let pieChart: echarts.ECharts | null = null;

const initCharts = () => {
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value);
    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
      },
      yAxis: { type: 'value' },
      series: [{
        data: [120, 200, 150, 80, 70, 110, 130],
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(78, 136, 243, 0.3)' },
            { offset: 1, color: 'rgba(78, 136, 243, 0)' }
          ])
        }
      }]
    });
  }

  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value);
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 1048, name: '用户管理' },
          { value: 735, name: '角色管理' },
          { value: 580, name: '部门管理' },
          { value: 484, name: '菜单管理' }
        ]
      }]
    });
  }
};

onMounted(() => {
  initCharts();
});
</script>

<style scoped lang="scss">
.dashboard {
  padding: 0;
}

.stats-row {
  margin-top: var(--space-lg);
}

.charts-row {
  margin-top: var(--space-lg);
}

.chart-card {
  @include card-style;
}
</style>
```

**Step 2: 提交**

Run:
```bash
cd frontend
git add src/views/Dashboard.vue
git commit -m "feat: 使用新 UI 组件重构 Dashboard 页面"
```

---

### Task 16-19: 迁移其他页面

- **Task 16**: Profile.vue 个人中心
- **Task 17**: views/system/User.vue 用户管理
- **Task 18**: views/system/Role.vue 角色管理
- **Task 19**: layout/Layout.vue 主布局

---

## 阶段六：测试和优化

### Task 20: 编写集成测试

**Files:**
- Create: `frontend/tests/integration/layout.test.ts`

**Step 1: 创建布局集成测试**

Create: `frontend/tests/integration/layout.test.ts`

```typescript
import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia } from 'pinia';
import AdminLayout from '@adminplus/ui-vue/src/components/layout/AdminLayout.vue';

describe('Layout Integration', () => {
  const mockMenus = [
    { id: '1', name: '首页', path: '/', icon: 'House' },
    { id: '2', name: '用户管理', path: '/users', icon: 'User' }
  ];

  it('should render complete layout', () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [createPinia()]
      },
      props: {
        menus: mockMenus,
        theme: 'gradient'
      }
    });

    expect(wrapper.find('.admin-layout').exists()).toBe(true);
    expect(wrapper.find('.app-sidebar').exists()).toBe(true);
    expect(wrapper.find('.app-header').exists()).toBe(true);
    expect(wrapper.find('.content-area').exists()).toBe(true);
    expect(wrapper.find('.floating-panel').exists()).toBe(true);
  });

  it('should handle sidebar collapse', async () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [createPinia()]
      },
      props: {
        menus: mockMenus
      }
    });

    const mainWrapper = wrapper.find('.main-wrapper');
    expect(mainWrapper.classes()).not.toContain('sidebar-collapsed');

    await wrapper.vm.handleToggle();
    expect(mainWrapper.classes()).toContain('sidebar-collapsed');
  });
});
```

---

### Task 21: 响应式适配测试

**Files:**
- Create: `frontend/tests/unit/breakpoint.test.ts`

---

### Task 22: 性能优化

**Step 1: 配置组件按需加载**

Modify: `frontend/vite.config.ts`

```typescript
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@adminplus/ui-vue': resolve(__dirname, 'packages/ui-vue/src')
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'adminplus-ui': ['@adminplus/ui-vue']
        }
      }
    }
  }
});
```

---

## 验收检查清单

在完成所有任务后，运行以下检查：

```bash
# 1. 所有测试通过
cd frontend
npm test

# 2. 构建成功
npm run build

# 3. 检查构建产物大小
ls -lh dist/assets/*.js

# 4. 启动开发服务器
npm run dev

# 5. 手动测试清单
# [ ] 侧边栏可以正常折叠/展开
# [ ] 主题可以正常切换
# [ ] 浮动面板功能正常
# [ ] 所有页面样式正确
# [ ] 移动端适配正常
# [ ] 无控制台错误
```

---

## 完成后清理

```bash
# 关闭浏览器自动化
# 清理临时文件
# 更新文档
```

---

**计划完成条件：**
- 所有测试通过
- 页面样式符合设计
- 无控制台错误
- 性能可接受（首屏 < 2s）
