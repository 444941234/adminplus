# AGENTS.md - AdminPlus Frontend Project Guide

## Project Overview

Vue 3.5 + Element Plus 2.8 admin frontend with RBAC (Role-Based Access Control) system.

**Tech Stack:** Vue 3.5, Vue Router 4, Pinia 2.2, Axios 1.7, Element Plus 2.8, Vite 6.0, Vitest 2.1, SCSS

## Build/Lint/Test Commands

```bash
# Development (port 5173)
npm run dev

# Production build
npm run build

# Lint (ESLint with auto-fix)
npm run lint

# Format (Prettier)
npm run format

# Test - watch mode
npm run test

# Test - single run (CI)
npm run test:run

# Test - UI mode
npm run test:ui

# Run single test file
npx vitest run test/components/LoginForm.test.js

# Run tests matching pattern
npx vitest run -t "renders login form"
```

## Project Structure

```
frontend/src/
├── api/              # API interfaces (one file per domain)
├── stores/           # Pinia stores (Composition API style)
├── router/           # Vue Router config with dynamic routes
├── views/            # Page components (auth/, system/, analysis/)
├── components/       # Reusable components
├── composables/      # Vue composables (useTable, useForm, useConfirm)
├── directives/       # Custom directives (v-auth)
├── utils/            # Utility functions (request.js, errorHandler.js)
├── styles/           # Global SCSS (variables.scss, index.scss)
├── constants/        # Application constants
├── layout/           # Layout components
├── App.vue           # Root component
└── main.js           # Entry point
```

## Code Style Guidelines

### Imports

```javascript
// 1. Vue/core imports first
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'

// 2. Third-party libraries
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'

// 3. Local imports (use @ alias)
import { getUserList, createUser } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { useConfirm } from '@/composables/useConfirm'
```

### Vue Components

- Use `<script setup>` with Composition API
- PascalCase for component files: `User.vue`, `RoleManagement.vue`
- Use Chinese comments for JSDoc (project convention)

```vue
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, deleteUser } from '@/api/user'

// Reactive state
const loading = ref(false)
const tableData = ref([])
const queryForm = reactive({
  page: 1,
  size: 10,
  keyword: '',
})

// Methods
const getData = async () => {
  loading.value = true
  try {
    const data = await getUserList(queryForm)
    tableData.value = data.records
  } catch {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  getData()
})
</script>

<template>
  <!-- Use scoped styles -->
</template>

<style scoped>
/* Scoped styles here */
</style>
```

### API Layer

```javascript
// src/api/user.js
import request from '@/utils/request'

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @returns {Promise<Object>} 用户列表数据
 */
export const getUserList = (params) => {
  return request({
    url: '/v1/sys/users',
    method: 'get',
    params,
  })
}
```

### Pinia Stores

```javascript
// src/stores/user.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const user = ref(null)
  const permissions = ref([])

  const login = async (username, password) => {
    const data = await loginApi({ username, password })
    token.value = data.token
    user.value = data.user
    return data
  }

  const hasPermission = (permission) => {
    if (!permission) return true
    return permissions.value.includes(permission)
  }

  return { token, user, permissions, login, hasPermission }
})
```

### Error Handling

```javascript
// Use try/catch with ElMessage for user feedback
try {
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  getData()
} catch {
  // Error already handled by request interceptor
}
```

### Formatting (Prettier)

```json
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "useTabs": false,
  "trailingComma": "es5",
  "printWidth": 100,
  "arrowParens": "always",
  "endOfLine": "lf"
}
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Components | PascalCase | `UserManagement.vue` |
| Composables | camelCase with `use` prefix | `useTable.js` |
| Stores | camelCase with `use` prefix | `useUserStore` |
| API functions | camelCase | `getUserList` |
| Constants | SCREAMING_SNAKE_CASE | `ROUTE_PATH` |
| CSS classes | kebab-case | `.card-header` |
| SCSS variables | kebab-case with `$` | `$primary-color` |

## Styling

### SCSS Variables

Located in `src/styles/variables.scss`. Use these variables instead of hardcoded values:

```scss
@import '@/styles/variables.scss';

.my-component {
  color: $text-primary;
  background-color: $bg-white;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  padding: $spacing-lg;
}
```

### Color Palette (智谱AI Style)

- Primary: `#0066FF` (科技蓝)
- Success: `#10B981`
- Warning: `#F59E0B`
- Danger: `#EF4444`
- Info: `#3B82F6`

### Utility Classes

```html
<div class="flex flex-between">
<div class="text-center">
<span class="text-primary">Primary text</span>
<div class="mt-lg mb-md">
```

## Permission System

### v-auth Directive

```vue
<!-- Single permission -->
<el-button v-auth="'user:add'">新增用户</el-button>

<!-- Any of permissions -->
<el-button v-auth="['user:add', 'user:edit']">操作</el-button>

<!-- All permissions required -->
<el-button v-auth.all="['user:add', 'user:edit']">批量操作</el-button>
```

### Programmatic Check

```javascript
const userStore = useUserStore()
if (userStore.hasPermission('user:delete')) {
  // Has permission
}
```

## Testing

Test files in `frontend/test/` directory:

```javascript
// test/components/Example.test.js
import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import Component from '@/views/Example.vue'

vi.mock('@/api/example', () => ({
  getData: vi.fn()
}))

describe('Component', () => {
  it('renders correctly', () => {
    const wrapper = mount(Component)
    expect(wrapper.find('.container').exists()).toBe(true)
  })
})
```

## API Response Format

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

The `request.js` interceptor returns `data` directly on success (code 200).

## Environment Variables

```
# .env.development
VITE_API_BASE_URL=http://localhost:8081

# .env.production
VITE_API_BASE_URL=/api
```

## Important Notes

1. **Never use `as any` or `@ts-ignore`** - maintain type safety
2. **Always use scoped styles** in Vue components
3. **Use SCSS variables** from `variables.scss`, not hardcoded colors
4. **Handle errors** with try/catch and ElMessage for user feedback
5. **Follow existing patterns** in api/, stores/, views/ directories
6. **Use Element Plus components** instead of creating custom ones when possible
