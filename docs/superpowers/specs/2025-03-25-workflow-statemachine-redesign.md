# 工作流审批系统重构设计文档

## 1. 概述

### 1.1 目标
使用 Spring State Machine 重构现有工作流审批系统，实现：
- 集中化的状态管理
- 支持复杂流程（并行审批、条件分支、退回、转办等）
- 可视化流程图展示
- 完整的操作审计

### 1.2 范围
- **MVP**: 基础状态机 + 串行审批 + 条件分支 + 退回 + 流程图可视化
- **P2**: 并行审批与会签（或签/会签、加签/转办）
- **P3**: 可视化流程设计器

### 1.3 兼容性
- 直接重构，不考虑历史数据迁移
- 现有表结构扩展，保留核心字段

---

## 2. 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端层 (Vue 3 + Vue Flow)                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ 流程列表     │  │ 流程设计器   │  │ 我的审批     │          │
│  │ (Vue Flow)   │  │ (Vue Flow)   │  │ (状态图)     │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │ HTTP/REST
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Controller 层                             │
│  WorkflowDefinitionController  |  WorkflowInstanceController   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Service 层                               │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  WorkflowStateMachineService (状态机编排)                │   │
│  │  - sendEvent(submit, approve, reject, rollback)          │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  WorkflowDefinitionService (流程定义管理)                │   │
│  │  WorkflowNodeService (节点配置)                          │   │
│  │  WorkflowInstanceService (实例管理)                      │   │
│  │  ApprovalService (审批逻辑)                              │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Spring State Machine                          │
│  States: DRAFT, RUNNING, APPROVED, REJECTED, CANCELLED          │
│  Events: SUBMIT, APPROVE, REJECT, CANCEL, ROLLBACK              │
│  Transitions: 配置在 StateMachineConfigurer                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Repository 层                             │
│  WorkflowDefinitionRepository, WorkflowInstanceRepository      │
│  StateMachineRepository (状态持久化)                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Database (PostgreSQL)                       │
│  sys_workflow_definition, sys_workflow_node                   │
│  sys_workflow_instance, sys_workflow_approval                  │
│  sys_workflow_log, spring_state_machine_context               │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 状态机设计

### 3.1 状态枚举
```java
public enum WorkflowState {
    DRAFT,       // 草稿
    RUNNING,     // 运行中
    APPROVED,    // 已通过
    REJECTED,    // 已拒绝
    CANCELLED     // 已取消
}
```

### 3.2 事件枚举
```java
public enum WorkflowEvent {
    SUBMIT,      // 提交
    APPROVE,     // 同意
    REJECT,      // 拒绝
    CANCEL,      // 取消
    ROLLBACK     // 退回
}
```

### 3.3 状态转换规则
```
DRAFT ──SUBMIT──→ RUNNING ──APPROVE──→ RUNNING(下一节点) ──...──→ APPROVED
  │                  │                                              │
  │                  └──REJECT──→ REJECTED                          │
  │                  │                                              │
  └──CANCEL────→ CANCELLED ←──────────────────────────────────────┘

RUNNING ──ROLLBACK──→ RUNNING(上一节点) / DRAFT(首节点)
```

### 3.4 关键点
- 每个审批节点是一个 RUNNING 子状态
- APPROVE 后判断是否还有下一节点，有则继续 RUNNING，无则 APPROVED
- REJECT 直接结束，不可恢复（需重新发起）
- ROLLBACK 返回上一 RUNNING 节点，首节点则返回 DRAFT
- CANCEL 只能在 DRAFT 或 RUNNING 时执行

---

## 4. 条件分支与 Guard/Action

### 4.1 条件守卫 (Guard)
```java
@Component
public class AmountGuard implements Guard<WorkflowState, WorkflowEvent> {
    @Override
    public boolean evaluate(StateContext<WorkflowState, WorkflowEvent> context) {
        Map<String, Object> businessData = context.getExtendedState()
            .get("businessData", Map.class);
        double amount = (double) businessData.get("amount");
        String targetNode = (String) context.getMessageHeader("targetNode");

        if (amount > 10000 && "senior_approval".equals(targetNode)) {
            return true;
        }
        if (amount <= 10000 && "normal_approval".equals(targetNode)) {
            return true;
        }
        return false;
    }
}
```

### 4.2 状态动作 (Action)
```java
// 创建审批记录
@Component
public class CreateApprovalAction implements Action<WorkflowState, WorkflowEvent> {
    @Override
    public void execute(StateContext<WorkflowState, WorkflowEvent> context) {
        String instanceId = context.getExtendedState().get("instanceId", String.class);
        String nodeId = context.getExtendedState().get("nodeId", String.class);
        // 创建审批记录...
    }
}

// 发送通知
@Component
public class NotifyAction implements Action<WorkflowState, WorkflowEvent> {
    // 发送通知给下一节点审批人
}

// 记录日志
@Component
public class LogAction implements Action<WorkflowState, WorkflowEvent> {
    // 记录状态变更日志
}
```

---

## 5. 数据库设计

### 5.1 工作流定义表 (sys_workflow_definition)
```sql
CREATE TABLE sys_workflow_definition (
    id VARCHAR(36) PRIMARY KEY,
    definition_name VARCHAR(100) NOT NULL,
    definition_key VARCHAR(50) NOT NULL,
    category VARCHAR(50),
    description TEXT,
    version INT DEFAULT 1,

    -- 状态机配置
    state_machine_config JSONB,

    -- 表单定义
    form_schema JSONB,

    -- 基础字段
    status INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    remark VARCHAR(500)
);
```

### 5.2 工作流节点表 (sys_workflow_node)
```sql
CREATE TABLE sys_workflow_node (
    id VARCHAR(36) PRIMARY KEY,
    definition_id VARCHAR(36) NOT NULL,
    node_name VARCHAR(100) NOT NULL,
    node_code VARCHAR(50) NOT NULL,
    node_order INT NOT NULL,

    -- 审批人配置
    approver_type VARCHAR(20),
    approver_id VARCHAR(36),
    is_counter_sign BOOLEAN DEFAULT FALSE,
    auto_pass_same_user BOOLEAN DEFAULT FALSE,

    -- 条件配置
    condition_expression TEXT,

    -- 节点类型: approval, condition, fork, join
    node_type VARCHAR(20) DEFAULT 'approval',

    -- 下一节点配置
    next_nodes JSONB,

    -- 基础字段
    description TEXT,
    status INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50),
    remark VARCHAR(500)
);
```

### 5.3 工作流实例表 (sys_workflow_instance)
```sql
CREATE TABLE sys_workflow_instance (
    id VARCHAR(36) PRIMARY KEY,
    definition_id VARCHAR(36) NOT NULL,
    definition_name VARCHAR(100),
    definition_version INT,

    -- 发起人信息
    user_id VARCHAR(36) NOT NULL,
    user_name VARCHAR(50),
    dept_id VARCHAR(36),
    dept_name VARCHAR(100),

    -- 流程信息
    title VARCHAR(200) NOT NULL,
    business_data JSONB,
    business_key VARCHAR(50),

    -- 状态
    status VARCHAR(20) NOT NULL DEFAULT 'draft',

    -- 当前节点
    current_node_id VARCHAR(36),
    current_node_name VARCHAR(100),

    -- 状态机上下文
    state_machine_context JSONB,

    -- 节点路径历史
    node_path JSONB,

    -- 时间字段
    submit_time TIMESTAMP,
    finish_time TIMESTAMP,

    -- 基础字段
    remark VARCHAR(500),
    status INT DEFAULT 1,
    deleted BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);
```

### 5.4 审批记录表 (sys_workflow_approval)
```sql
CREATE TABLE sys_workflow_approval (
    id VARCHAR(36) PRIMARY KEY,
    instance_id VARCHAR(36) NOT NULL,
    node_id VARCHAR(36) NOT NULL,
    node_name VARCHAR(100) NOT NULL,

    -- 审批人信息
    approver_id VARCHAR(36) NOT NULL,
    approver_name VARCHAR(50),

    -- 审批结果
    approval_status VARCHAR(20) DEFAULT 'pending',
    comment TEXT,
    attachments JSONB,
    approval_time TIMESTAMP,

    -- 退回相关
    is_rollback BOOLEAN DEFAULT FALSE,
    rollback_from_node_id VARCHAR(36),
    rollback_from_node_name VARCHAR(100),

    -- 基础字段
    deleted BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);
```

### 5.5 状态机持久化表 (spring_state_machine_context)
```sql
CREATE TABLE spring_state_machine_context (
    machine_id VARCHAR(100) PRIMARY KEY,
    state VARCHAR(50) NOT NULL,
    extended_state JSONB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 5.6 流程操作日志表 (sys_workflow_log)
```sql
CREATE TABLE sys_workflow_log (
    id VARCHAR(36) PRIMARY KEY,
    instance_id VARCHAR(36) NOT NULL,
    node_id VARCHAR(36),
    node_name VARCHAR(100),

    -- 操作信息
    action VARCHAR(50) NOT NULL,
    action_desc VARCHAR(200),

    -- 操作人
    operator_id VARCHAR(36) NOT NULL,
    operator_name VARCHAR(50),

    -- 状态变化
    from_status VARCHAR(20),
    to_status VARCHAR(20),

    -- 业务数据快照
    business_snapshot JSONB,

    -- 基础字段
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    deleted BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50)
);
```

---

## 6. API 接口

### 6.1 流程定义管理
```
POST   /api/v1/workflow/definitions           创建流程定义
GET    /api/v1/workflow/definitions           查询流程定义列表
GET    /api/v1/workflow/definitions/{id}      获取流程定义详情
PUT    /api/v1/workflow/definitions/{id}      更新流程定义
DELETE /api/v1/workflow/definitions/{id}      删除流程定义
```

### 6.2 节点管理
```
POST   /api/v1/workflow/definitions/{id}/nodes 添加节点
PUT    /api/v1/workflow/nodes/{id}             更新节点
DELETE /api/v1/workflow/nodes/{id}             删除节点
GET    /api/v1/workflow/definitions/{id}/graph 获取流程图数据
```

### 6.3 流程实例管理
```
POST   /api/v1/workflow/instances             创建草稿
POST   /api/v1/workflow/instances/{id}/submit 提交流程
POST   /api/v1/workflow/instances/start       直接发起流程
GET    /api/v1/workflow/instances             查询我的流程
GET    /api/v1/workflow/instances/pending      查询待我审批
GET    /api/v1/workflow/instances/{id}         获取流程详情
POST   /api/v1/workflow/instances/{id}/approve 同意审批
POST   /api/v1/workflow/instances/{id}/reject  拒绝审批
POST   /api/v1/workflow/instances/{id}/cancel  取消流程
POST   /api/v1/workflow/instances/{id}/rollback 退回流程
POST   /api/v1/workflow/instances/{id}/withdraw 撤回流程
```

### 6.4 查询与监控
```
GET    /api/v1/workflow/instances/{id}/approvals  获取审批记录
GET    /api/v1/workflow/instances/{id}/logs       获取操作日志
GET    /api/v1/workflow/state-machine/{id}/status  获取状态机状态
```

---

## 7. 技术栈

### 7.1 后端依赖
```xml
<!-- Spring State Machine -->
<dependency>
    <groupId>org.springframework.statemachine</groupId>
    <artifactId>spring-statemachine-starter</artifactId>
    <version>4.0.0</version>
</dependency>

<!-- Spring State Machine 持久化 -->
<dependency>
    <groupId>org.springframework.statemachine</groupId>
    <artifactId>spring-statemachine-data-jpa</artifactId>
    <version>4.0.0</version>
</dependency>
```

### 7.2 前端依赖
```json
{
  "dependencies": {
    "@vue-flow/core": "^1.33.0",
    "@vue-flow/background": "^1.3.0",
    "@vue-flow/controls": "^1.1.1",
    "@vue-flow/minimap": "^1.4.0",
    "@vue-flow/node-resizer": "^1.2.0"
  }
}
```

---

## 8. 阶段划分

### 8.1 MVP 阶段（2-3周）
**目标：端到端跑通一个简单流程**

| 模块 | 交付物 |
|------|--------|
| 后端 | Spring State Machine 配置、状态转换、条件分支、退回功能 |
| 前端 | 流程列表、我的审批、流程状态图展示 |
| 数据库 | 迁移现有表结构，添加新表和字段 |
| 测试 | 单元测试 + 集成测试覆盖核心场景 |

**MVP 场景：**
1. 用户发起请假申请（金额 ≤ 5000 走普通审批）
2. 用户发起报销申请（金额 > 5000 走高级审批）
3. 审批人同意/拒绝
4. 审批人退回到上一节点或发起人
5. 发起人取消流程
6. 前端展示流程状态图

### 8.2 P2 阶段（1-2周）
**目标：并行审批与会签**

| 功能 | 描述 |
|------|------|
| 并行节点 | 多人同时审批，配置通过策略（全部通过/任一通过） |
| 或签/会签 | 支持或签（一人通过即可）、会签（全部通过） |
| 加签/转办 | 审批人添加他人共同审批或转给他人 |

### 8.3 P3 阶段（2-3周）
**目标：可视化设计器**

| 功能 | 描述 |
|------|------|
| 流程设计器 | 拖拽式创建流程定义 |
| 节点配置 | 可视化配置节点属性、审批人、条件 |
| 流程预览 | 实时预览流程图 |
| 版本管理 | 流程定义版本控制 |

---

## 9. 关键类设计

### 9.1 状态机配置类
```java
@Configuration
@EnableStateMachineFactory
public class WorkflowStateMachineConfig
    extends StateMachineConfigurerAdapter<WorkflowState, WorkflowEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<WorkflowState, WorkflowEvent> states) {
        // 状态配置
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<WorkflowState, WorkflowEvent> transitions) {
        // 转换配置
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<WorkflowState, WorkflowEvent> config) {
        // 持久化配置
    }
}
```

### 9.2 状态机服务类
```java
@Service
public class WorkflowStateMachineService {

    public WorkflowInstanceResp submit(String instanceId);

    public WorkflowInstanceResp approve(String instanceId, ApprovalActionReq req);

    public WorkflowInstanceResp reject(String instanceId, ApprovalActionReq req);

    public void cancel(String instanceId);

    public void rollback(String instanceId, String toNodeId, String comment);
}
```

---

## 10. 测试策略

### 10.1 单元测试
- StateMachine 配置测试
- Guard 条件判断测试
- Action 执行测试
- Service 层业务逻辑测试

### 10.2 集成测试
- 完整流程审批测试
- 条件分支测试
- 退回功能测试
- 并发审批测试（P2）

---

## 11. 风险与缓解

| 风险 | 缓解措施 |
|------|---------|
| Spring State Machine 学习曲线 | 参考官方文档，先实现核心功能 |
| 状态持久化复杂性 | 使用 Spring State Machine JPA 模块 |
| 前端流程图性能 | 使用虚拟滚动，大数据量时分页 |
| 并发审批一致性 | 使用数据库锁 + 乐观锁 |
