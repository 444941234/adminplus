# 测试覆盖情况

## 测试统计概览

| 项目 | 测试文件数 | 测试用例数 | 状态 |
|------|-----------|-----------|------|
| 后端 | 78 | 857 | ✅ 全部通过 |
| 前端 | 52 | 938 | ✅ 全部通过 |

## 后端测试覆盖

### 核心业务模块

| 模块 | 测试类 | 覆盖功能 |
|------|--------|----------|
| 工作流实例 | `WorkflowInstanceServiceTest.java` | 草稿创建、提交、审批、回退、取消、撤回 |
| 工作流定义 | `WorkflowDefinitionServiceTest.java` | CRUD、状态管理、缓存 |
| 工作流钩子 | `WorkflowHookServiceTest.java` | 钩子执行、校验、日志记录 |
| 钩子执行器 | `SpELHookExecutorTest.java`, `BeanHookExecutorTest.java`, `HttpHookExecutorTest.java` | SpEL表达式、Bean调用、HTTP请求 |
| 工作流抄送 | `WorkflowCcServiceTest.java` | 抄送记录CRUD、标记已读 |
| 工作流催办 | `WorkflowUrgeServiceTest.java` | 催办发送、已读标记 |

### 用户权限模块

| 模块 | 测试类 | 覆盖功能 |
|------|--------|----------|
| 用户管理 | `UserServiceTest.java` | CRUD、角色分配、密码管理 |
| 角色管理 | `RoleServiceTest.java` | CRUD、菜单授权 |
| 菜单管理 | `MenuServiceTest.java` | CRUD、树形结构 |
| 部门管理 | `DeptServiceTest.java` | CRUD、树形结构 |
| 权限校验 | `PermissionServiceTest.java` | 权限检查、缓存 |

### 安全测试

| 测试类 | 覆盖功能 |
|--------|----------|
| `WorkflowSecurityTest.java` | 工作流操作权限校验、数据隔离 |
| `AuthControllerTest.java` | 登录、登出、Token刷新 |

### 定时任务测试

| 测试类 | 覆盖功能 |
|--------|----------|
| `LogCleanupSchedulerTest.java` | 日志清理、钩子日志清理 |

### 集成测试

| 测试类 | 覆盖功能 |
|--------|----------|
| `WorkflowIntegrationTest.java` | 工作流完整流程集成测试 |
| `WorkflowStateMachineIntegrationTest.java` | 状态机集成测试 |
| `UserIntegrationTest.java` | 用户模块集成测试 |
| `AuthIntegrationTest.java` | 认证授权集成测试 |

## 前端测试覆盖

### 视图组件测试

| 测试文件 | 覆盖功能 |
|----------|----------|
| `WorkflowDetail.test.ts` | 流程详情、审批操作、回退、抄送、加签 |
| `MyWorkflow.test.ts` | 我的流程列表、草稿管理、催办 |
| `WorkflowDesigner.test.ts` | 流程设计器、节点配置 |
| `WorkflowCenter.test.ts` | 流程中心、待办列表 |
| `WorkflowVisualizer.test.ts` | 流程图可视化、节点状态、边缘连接 |
| `FormManager.test.ts` | 表单模板管理、分类过滤、权限校验 |
| `UrgeCenter.test.ts` | 催办中心 |
| `MyCc.test.ts` | 抄送列表 |

### 组件测试

| 测试文件 | 覆盖功能 |
|----------|----------|
| `WorkflowNodeProperties.test.ts` | 节点属性编辑、钩子配置 |
| `WorkflowActionButtons.test.ts` | 操作按钮权限 |
| `WorkflowFormRenderer.test.ts` | 表单渲染 |
| `WorkflowBusinessCard.test.ts` | 业务卡片展示 |

### 组合式函数测试

| 测试文件 | 覆盖功能 |
|----------|----------|
| `useWorkflowForm.test.ts` | 表单数据管理、验证、提交 |

### API测试

| 测试文件 | 覆盖功能 |
|----------|----------|
| `user.test.ts` | 用户API调用 |

### 系统管理页面测试

| 测试文件 | 覆盖功能 |
|----------|----------|
| `User.test.ts` | 用户管理CRUD、角色分配 |
| `Role.test.ts` | 角色管理CRUD、菜单授权 |
| `Menu.test.ts` | 菜单管理CRUD、树形结构 |
| `Dept.test.ts` | 部门管理CRUD、树形结构 |
| `Dict.test.ts` | 字典管理CRUD |
| `Config.test.ts` | 参数配置管理、分组切换、搜索过滤、权限控制 |
| `File.test.ts` | 文件管理、上传下载 |
| `Log.test.ts` | 日志查询、导出 |
| `Profile.test.ts` | 个人资料编辑、密码修改 |

## 运行测试

```bash
# 运行所有后端测试
cd backend && mvn test

# 运行特定测试类
cd backend && mvn test -Dtest=WorkflowHookServiceTest

# 运行所有前端测试
cd frontend && npm run test

# 运行前端测试（无监视模式）
cd frontend && npm run test:run
```

## 测试原则

1. **单元测试优先**: 核心业务逻辑必须有单元测试覆盖
2. **Mock外部依赖**: 数据库、缓存、HTTP请求使用Mock
3. **集成测试补充**: 关键流程需要集成测试验证
4. **前端组件测试**: 测试组件行为和事件，而非实现细节