# AdminPlus Frontend

当前前端是一个基于 `Vue 3.5 + TypeScript + Vite 6` 的管理后台，已经围绕后端 RBAC、分析、工作流和日志治理能力补齐了主要页面与权限链路。

## 技术栈

- Vue 3.5
- TypeScript 5.6
- Vite 6
- Pinia 2
- Vue Router 4
- Axios
- Tailwind CSS 3
- shadcn-vue / reka-ui
- vee-validate + zod
- ECharts / Unovis
- vue-sonner

## 当前页面范围

当前前端已接入的主要页面和模块：

- `/login`
- `/dashboard`
- `/profile`
- `/analysis/statistics`
- `/analysis/report`
- `/workflow/definitions`
- `/workflow/my`
- `/workflow/pending`
- `/workflow/detail/:id`
- `/system/user`
- `/system/role`
- `/system/menu`
- `/system/dept`
- `/system/dict`
- `/system/log`
- `/system/file`
- `/system/config`

## 当前能力

### 动态路由与导航

- 登录后根据后端 `/sys/menus/user/tree` 动态注册业务路由
- 左侧菜单按后端菜单树渲染，不再依赖前端静态菜单
- 切换账号或退出登录时会清理已注册动态路由

### RBAC 前端表现

- 页面入口跟随后端菜单树
- 关键按钮按权限标识控制显隐
- 已接通用户、角色、菜单、部门、字典、日志、工作流等页面的按钮级权限

### 系统管理

- 用户管理
  - 分页、关键词搜索、部门筛选
  - 新增、编辑、删除、状态切换、密码重置
  - 角色分配
- 角色管理
  - 新增、编辑、删除
  - 角色菜单授权
- 菜单管理
  - 树形展示
  - 新增、编辑、删除
  - 批量启用、批量禁用、批量删除
- 部门管理
  - 树形展示、搜索
  - 新增、编辑、删除
- 字典管理
  - 字典主表 CRUD
  - 字典项 CRUD、状态切换、父子层级管理
- 日志管理
  - 多条件筛选、分页、详情查看
  - 单条删除、批量删除
  - 按条件清理、清理过期日志
  - Excel / CSV 导出
- 文件管理
  - 我的文件列表
  - 按目录查看文件
  - 文件上传
  - 打开 / 下载文件
  - 删除文件
- 系统监控
  - 系统名称、版本、操作系统、JDK、数据库信息
  - 总内存、已用 / 空闲内存、内存占用率
  - 在线用户列表
  - 手动刷新监控数据

### 分析与工作流

- 仪表盘快捷入口已接入分析和工作流模块
- 仪表盘快捷入口已接入文件管理与系统监控入口
- 工作流页面已支持定义列表、我的流程、待审批、详情页
- 工作流操作按钮按 `workflow:create`、`workflow:approve` 等权限控制

## 项目结构

```text
src/
├── api/                  # 按业务域拆分的接口层
├── components/ui/        # 基于 shadcn-vue/reka-ui 的基础 UI 组件
├── composables/          # 可复用组合式逻辑
├── layout/               # 主布局与导航
├── lib/                  # 通用工具与校验器
├── router/               # 路由配置、动态路由与守卫
├── stores/               # Pinia 状态管理
├── styles/               # 全局样式
├── types/                # 类型定义
├── views/                # 页面视图
├── App.vue               # 根组件
└── main.ts               # 应用入口
```

## 运行

### 安装依赖

```bash
npm install
```

### 开发环境

```bash
npm run dev
```

默认访问地址：

- `http://localhost:5173`

### 构建

```bash
npm run build
```

### 预览

```bash
npm run preview
```

## 当前脚本

`package.json` 当前提供以下脚本：

```bash
npm run dev
npm run build
npm run preview
npm run lint
npm run format
npm run test
npm run test:run
npm run test:ui
```

说明：

- 当前已补基础 Vitest 配置，首批单测覆盖：
  - 动态菜单树转路由
  - 权限通配与精确匹配判断
  - 路由守卫跳转决策
  - 侧边栏菜单树生成、折叠态叶子菜单和分组展开状态
- 后续若继续补测试，优先应覆盖按钮权限显隐和授权弹窗提交流程
- 仓库历史里存在旧版 JS 前端结构说明，但不属于这套当前 TypeScript 前端说明主体

## 与后端联调

前端请求基于 [request.ts](/D:/IdeaProjects/adminplus/frontend/src/api/request.ts)：

- `baseURL`: `/api/v1`
- token 来源: `localStorage`
- 鉴权方式: `Authorization: Bearer <token>`
- 401 响应会清理 token 并跳转 `/login`
- `DELETE` 请求已支持 request body，用于菜单/日志批量删除等接口

Vite 代理配置位于 [vite.config.ts](/D:/IdeaProjects/adminplus/frontend/vite.config.ts)：

```ts
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8081',
      changeOrigin: true
    }
  }
}
```

因此本地开发时需要后端运行在：

- `http://localhost:8081`

## 登录与状态

用户状态由 [user.ts](/D:/IdeaProjects/adminplus/frontend/src/stores/user.ts) 管理，当前包含：

- `token`
- `refreshToken`
- `userInfo`
- `permissions`
- `menus`
- `captcha`

登录成功后会拉取：

- 当前用户信息
- 权限列表
- 菜单树

## 默认账号

- 用户名: `admin`
- 密码: `admin123`

## 现状说明

- 动态路由依赖后端菜单 `path/component` 配置正确
- 前端已补较多表单前置校验，但仍以服务端校验为最终准则
- 当前 README 只描述现有 TypeScript 前端，不覆盖仓库历史中的旧版 JS 实现
- `docker-compose.yml` 引用了 `frontend/Dockerfile`，但当前仓库中没有这个文件，Docker 前端镜像链路暂不完整
