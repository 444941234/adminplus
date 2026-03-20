# AdminPlus

AdminPlus 是一个前后端分离的管理系统仓库，当前由两个子项目组成：

- `frontend/`: Vue 3.5 + TypeScript + Vite 6 管理后台前端
- `backend/`: Spring Boot 3.5 + JDK 21 + PostgreSQL 的 RBAC 后端

当前仓库以 RBAC 管理能力为主，当前前端已经接入了动态路由、菜单树导航、工作流、分析、日志治理和批量管理操作；后端还扩展了文件上传、验证码、健康检查等能力。

## 当前代码现状

- 前端已经切换到 `Vue 3 + TypeScript`，UI 基础为 `Tailwind CSS + shadcn-vue/reka-ui`
- 当前前端页面已覆盖：登录、仪表盘、个人资料、用户/角色/菜单/部门/字典/日志/文件/系统监控、分析统计、工作流
- 当前前端已接入：
  - 基于后端菜单树的动态路由
  - 基于权限标识的按钮显隐
  - 用户角色分配、角色菜单授权
  - 菜单与日志的批量操作
  - 日志导出、按条件清理、过期清理
  - 文件上传、我的文件与按目录查看
  - 系统监控与在线用户概览
- 后端仍有额外能力尚未形成完整前端模块：
  - 验证码之外的更多安全运维配置页
- `docker-compose.yml` 当前引用了 `frontend/Dockerfile`，但仓库中没有这个文件，因此 Docker 一键拉起前端这部分目前不完整

## 目录结构

```text
.
├── frontend/                # Vue 3 管理后台
├── backend/                 # Spring Boot API 服务
├── docs/                    # 项目补充文档
├── @docs/                   # 中文说明文档
├── docker-compose.yml       # PostgreSQL / Redis / Backend / Frontend 编排
└── AGENTS.md                # 代理协作说明
```

## 技术栈

### 前端

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

### 后端

- Spring Boot 3.5
- Spring Security OAuth2 Resource Server
- Spring Data JPA
- PostgreSQL
- Redis + Caffeine Cache
- Spring AOP / Actuator / Validation
- SpringDoc OpenAPI
- MinIO
- Apache POI

## 本地启动

### 1. 启动依赖

后端默认依赖以下基础设施：

- PostgreSQL: `127.0.0.1:5432`
- Redis: `127.0.0.1:6379`

可先使用根目录的 `docker-compose.yml` 启动数据库与 Redis，但如果直接启动完整 compose，需要先补齐前端镜像构建文件。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认地址：

- API 基础路径: `http://localhost:8081/api`
- Swagger UI: `http://localhost:8081/api/swagger-ui.html`
- 健康检查: `http://localhost:8081/api/actuator/health`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认地址：

- 前端页面: `http://localhost:5173`
- Vite 代理后端: `http://localhost:8081`

## 默认初始化数据

后端启动时会执行基础数据初始化，包含部门、角色、菜单、用户、权限与字典数据。

默认管理员账号：

- 用户名: `admin`
- 密码: `admin123`

还会初始化 `manager`、`user1`、`dev1`、`operator1`、`cs1` 等测试账号，初始密码同为 `admin123`。

## 功能概览

### 已有前后端联调页面

- 登录与验证码
- 仪表盘统计
- 个人资料
- 分析统计与报表
- 工作流定义、我的流程、待审批、流程详情
- 用户管理
- 角色管理
- 菜单管理
- 部门管理
- 字典管理
- 日志管理
- 文件管理
- 系统监控页面

### 后端已提供、前端当前仍未完整页面化的能力

- 验证码之外的更多安全运维配置页

## 接口约定

后端统一返回结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1710000000000
}
```

前端 Axios 实例的 `baseURL` 为 `/api/v1`，并在请求头中附带本地存储的 Bearer Token。

## 更多文档

- [frontend/README.md](/D:/IdeaProjects/adminplus/frontend/README.md)
- [backend/README.md](/D:/IdeaProjects/adminplus/backend/README.md)
