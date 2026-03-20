# AdminPlus Backend

AdminPlus 后端是一个基于 `Spring Boot 3.5 + JDK 21` 的管理系统 API，当前覆盖 RBAC、仪表盘统计、日志、文件、验证码和工作流等模块。

## 技术栈

- Spring Boot 3.5
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis + Caffeine
- Spring AOP
- SpringDoc OpenAPI
- MinIO
- Apache POI
- JUnit 5 / Mockito / H2

## 目录结构

```text
src/main/java/com/adminplus/
├── common/            # 通用配置、安全、异常、切面、响应体
├── constants/         # 枚举和常量
├── controller/        # REST 接口
├── health/            # 自定义健康检查
├── pojo/              # entity / dto
├── repository/        # JPA Repository
├── runner/            # 启动初始化逻辑
├── scheduler/         # 定时清理任务
├── service/           # 业务接口
├── service/impl/      # 业务实现
└── utils/             # 工具类
```

## 核心能力

### RBAC

- 认证登录、登出、刷新 token
- 当前用户信息、权限、菜单
- 用户、角色、菜单、部门、字典管理

### 平台能力

- 验证码生成
- 仪表盘统计与图表数据
- 操作日志与日志清理
- 文件上传、本地或 MinIO 存储
- 健康检查与 Actuator

### 工作流

- 工作流定义 CRUD
- 工作流节点管理
- 工作流草稿、提交流程、审批、驳回、撤回、取消

## 关键接口前缀

- `/api/v1/auth`
- `/api/v1/captcha`
- `/api/v1/sys/users`
- `/api/v1/sys/roles`
- `/api/v1/sys/menus`
- `/api/v1/sys/depts`
- `/api/v1/sys/dicts`
- `/api/v1/sys/logs`
- `/api/v1/sys/dashboard`
- `/api/v1/profile`
- `/api/v1/files`
- `/api/v1/workflow/definitions`
- `/api/v1/workflow/instances`

Swagger 文档地址：

- `http://localhost:8081/api/swagger-ui.html`

## 本地运行

### 环境要求

- JDK 21
- Maven 3.9+
- PostgreSQL 16+
- Redis 7+

### 启动

```bash
mvn spring-boot:run
```

应用默认运行在：

- `http://localhost:8081/api`

## 配置说明

主配置文件在 [application.yml](/D:/IdeaProjects/adminplus/backend/src/main/resources/application.yml)。

默认配置要点：

- 端口: `8081`
- 上下文路径: `/api`
- 数据库: `jdbc:postgresql://127.0.0.1:5432/adminplus`
- Redis: `127.0.0.1:6379`
- CORS 默认允许: `http://localhost:5173`
- JWT 默认使用 Bearer Token 模式
- 文件存储默认使用本地目录 `uploads`

环境配置：

- 开发环境: [application-dev.yml](/D:/IdeaProjects/adminplus/backend/src/main/resources/application-dev.yml)
- 生产环境: [application-prod.yml](/D:/IdeaProjects/adminplus/backend/src/main/resources/application-prod.yml)

重要环境变量：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_DEV_SECRET`
- `CORS_ALLOWED_ORIGINS`
- `FILE_STORAGE_TYPE`
- `FILE_STORAGE_LOCAL_PATH`
- `MINIO_ENDPOINT`
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`
- `LOG_STORAGE_MODE`

## 安全与运行机制

- 基于 Spring Security 的 JWT 鉴权
- 生产环境强制要求有效 RSA JWK 格式的 `JWT_SECRET`
- 支持 Token 黑名单
- 可根据配置切换 Cookie/Bearer Token 的 CSRF 策略
- 默认启用严格安全响应头和受限的静态资源暴露

## 初始化数据

启动时 [DataInitializationRunner.java](/D:/IdeaProjects/adminplus/backend/src/main/java/com/adminplus/runner/DataInitializationRunner.java) 会自动初始化：

- 部门
- 角色
- 菜单与按钮权限
- 用户
- 字典

默认管理员账号：

- 用户名: `admin`
- 密码: `admin123`

## 测试

当前测试代码主要集中在：

- 服务层
- 工作流
- 安全相关
- 工具类

可执行：

```bash
mvn test
```
