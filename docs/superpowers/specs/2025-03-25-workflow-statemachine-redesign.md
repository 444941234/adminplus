# 工作流审批系统重构设计文档 v2

## 1. 概述

### 1.1 目标
使用 Spring State Machine 重构现有工作流审批系统，实现：
- 集中化的状态管理
- 支持复杂流程（并行审批、条件分支、退回、转办等）
- 可视化流程图展示
- 完整的操作审计

### 1.2 范围
- **MVP 1**: 基础状态机 + 串行审批 + 流程图可视化
- **MVP 2**: 条件分支 + 退回功能
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
│  │  WorkflowDefinitionService, WorkflowNodeService         │   │
│  │  WorkflowInstanceService, ApprovalService                │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Spring State Machine                          │
│  States: DRAFT, NODE_1, NODE_2, NODE_3, ..., APPROVED, REJECTED  │
│  Events: SUBMIT, APPROVE, REJECT, CANCEL, ROLLBACK              │
│  ExtendedState: currentNodeId, nodePath, businessData         │
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

## 3. 状态机设计（修正版）

### 3.1 设计原则

Spring State Machine 的状态转换必须明确，不支持"运行中再回到运行中"的自转换模式。

### 3.2 状态设计（扁平化方案 - 推荐）

**状态枚举:**
```java
public enum WorkflowState {
    DRAFT,         // 草稿
    NODE_1,        // 审批节点1 (动态生成，实际使用枚举 + 扩展状态)
    NODE_2,        // 审批节点2
    NODE_3,        // 审批节点3
    ...
    APPROVED,      // 已通过
    REJECTED,      // 已拒绝
    CANCELLED,     // 已取消
    ERROR          // 异常状态
}
```

**改进方案：使用扩展状态跟踪节点**
```java
// 核心状态保持简单
public enum WorkflowState {
    DRAFT, RUNNING, APPROVED, REJECTED, CANCELLED
}

// 节点信息存储在 ExtendedState 中
public class WorkflowExtendedState {
    private String currentNodeId;      // 当前节点ID
    private List<String> nodePath;     // 已走节点路径
    private Map<String, Object> businessData;
}
```

### 3.3 状态转换规则（修正版）

```
DRAFT ──SUBMIT──→ RUNNING
RUNNING ──APPROVE──→ RUNNING (下一节点)
RUNNING ──APPROVE──→ APPROVED (无下一节点时)
RUNNING ──REJECT──→ REJECTED
RUNNING ──CANCEL──→ CANCELLED
RUNNING ──ROLLBACK──→ RUNNING (上一节点) / DRAFT (首节点)
```

**关键点：**
- RUNNING 状态保持不变，通过 `currentNodeId` 字段跟踪当前节点
- APPROVE 后检查是否有下一节点，有则更新 `currentNodeId`，无则状态变为 APPROVED
- REJECT/CANCEL 直接结束，不可恢复
- ROLLBACK 从 `nodePath` 获取上一节点，更新 `currentNodeId`

### 3.4 扩展状态设计

```java
@Component
public class WorkflowExtendedState {

    public void updateCurrentNode(StateMachine<WorkflowState, WorkflowEvent> sm,
                                     String nodeId) {
        sm.getExtendedState().getVariables().put("currentNodeId", nodeId);

        // 更新节点路径
        List<String> path = (List<String>) sm.getExtendedState()
            .getVariables().get("nodePath");
        if (path == null) {
            path = new ArrayList<>();
            sm.getExtendedState().getVariables().put("nodePath", path);
        }
        if (!path.contains(nodeId)) {
            path.add(nodeId);
        }
    }

    public String getCurrentNodeId(StateMachine<WorkflowState, WorkflowEvent> sm) {
        return (String) sm.getExtendedState().getVariables().get("currentNodeId");
    }

    public String getPreviousNodeId(StateMachine<WorkflowState, WorkflowEvent> sm) {
        List<String> path = (List<String>) sm.getExtendedState()
            .getVariables().get("nodePath");
        if (path != null && path.size() > 1) {
            return path.get(path.size() - 2);
        }
        return null;  // 首节点，无上一节点
    }
}
```

---

## 4. 条件分支与 Guard/Action

### 4.1 SpEL 表达式条件守卫
```java
@Component
public class SpELGuard implements Guard<WorkflowState, WorkflowEvent> {

    @Autowired
    private SpelExpressionParser parser;

    @Override
    public boolean evaluate(StateContext<WorkflowState, WorkflowEvent> context) {
        String expression = (String) context.getMessageHeader("conditionExpression");
        Map<String, Object> businessData = (Map<String, Object>)
            context.getExtendedState().getVariables().get("businessData");

        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        evalContext.setVariable("businessData", businessData);
        evalContext.setVariable("instance", context.getExtendedState().getVariables().get("instance"));

        Boolean result = parser.parseExpression(expression)
            .getValue(evalContext, Boolean.class);
        return result != null && result;
    }
}
```

**使用示例（数据库存储）：**
```sql
-- sys_workflow_node.condition_expression
-- 条件1: 金额 > 10000 且类型为报销
"#businessData.amount > 10000 and #businessData.type == 'expense'"

-- 条件2: 部门为财务部
"#instance.deptId == 'finance'"
```

### 4.2 状态动作链
```java
// 审批动作链
@Component
public class ApproveActionChain implements Action<WorkflowState, WorkflowEvent> {

    @Autowired
    private CreateApprovalAction createApprovalAction;

    @Autowired
    private NotifyAction notifyAction;

    @Autowired
    private LogAction logAction;

    @Override
    public void execute(StateContext<WorkflowState, WorkflowEvent> context) {
        createApprovalAction.execute(context);
        notifyAction.execute(context);
        logAction.execute(context);
    }
}
```

---

## 5. 数据库设计（修正版）

### 5.1 工作流定义表
```sql
CREATE TABLE sys_workflow_definition (
    id VARCHAR(36) PRIMARY KEY,
    definition_name VARCHAR(100) NOT NULL,
    definition_key VARCHAR(50) NOT NULL UNIQUE,
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

CREATE UNIQUE INDEX uk_workflow_def_key ON sys_workflow_definition(definition_key) WHERE deleted = FALSE;
CREATE INDEX idx_workflow_def_category ON sys_workflow_definition(category);
```

### 5.2 工作流节点表
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

    -- 条件配置（SpEL 表达式）
    condition_expression TEXT,

    -- 节点类型
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

-- 外键约束
ALTER TABLE sys_workflow_node
ADD CONSTRAINT fk_wf_node_definition
FOREIGN KEY (definition_id) REFERENCES sys_workflow_definition(id);

CREATE INDEX idx_workflow_node_def ON sys_workflow_node(definition_id);
CREATE INDEX idx_workflow_node_order ON sys_workflow_node(node_order);
```

### 5.3 工作流实例表
```sql
CREATE TABLE sys_workflow_instance (
    id VARCHAR(36) PRIMARY KEY,
    definition_id VARCHAR(36) NOT NULL,
    definition_name VARCHAR(100),
    definition_version INT,

    -- 乐观锁（并发控制）
    version BIGINT DEFAULT 0,

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

-- 外键约束
ALTER TABLE sys_workflow_instance
ADD CONSTRAINT fk_wf_instance_definition
FOREIGN KEY (definition_id) REFERENCES sys_workflow_definition(id);

-- 业务键唯一约束（防止重复发起）
CREATE UNIQUE INDEX uk_wf_instance_business_key
ON sys_workflow_instance(business_key) WHERE deleted = FALSE;

CREATE INDEX idx_wf_inst_def ON sys_workflow_instance(definition_id);
CREATE INDEX idx_wf_inst_user ON sys_workflow_instance(user_id);
CREATE INDEX idx_wf_inst_status ON sys_workflow_instance(status);
CREATE INDEX idx_wf_inst_current_node ON sys_workflow_instance(current_node_id);
CREATE INDEX idx_wf_inst_deleted ON sys_workflow_instance(deleted);
```

### 5.4 审批记录表
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

-- 外键约束
ALTER TABLE sys_workflow_approval
ADD CONSTRAINT fk_wf_approval_instance
FOREIGN KEY (instance_id) REFERENCES sys_workflow_instance(id);

CREATE INDEX idx_wf_approval_inst ON sys_workflow_approval(instance_id);
CREATE INDEX idx_wf_approval_node ON sys_workflow_approval(node_id);
CREATE INDEX idx_wf_approval_approver ON sys_workflow_approval(approver_id);
CREATE INDEX idx_wf_approval_status ON sys_workflow_approval(approval_status);
CREATE INDEX idx_wf_approval_deleted ON sys_workflow_approval(deleted);
CREATE INDEX idx_wf_approval_rollback ON sys_workflow_approval(instance_id, is_rollback);
```

### 5.5 状态机持久化表
```sql
CREATE TABLE spring_state_machine_context (
    machine_id VARCHAR(100) PRIMARY KEY,
    state VARCHAR(50) NOT NULL,
    extended_state JSONB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_state_machine_update ON spring_state_machine_context(update_time);
```

### 5.6 流程操作日志表
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

CREATE INDEX idx_wf_log_inst ON sys_workflow_log(instance_id);
CREATE INDEX idx_wf_log_action ON sys_workflow_log(action);
CREATE INDEX idx_wf_log_inst_time ON sys_workflow_log(instance_id, create_time DESC);
CREATE INDEX idx_wf_log_deleted ON sys_workflow_log(deleted);
```

---

## 6. 状态机持久化策略

### 6.1 StateMachinePersister 实现
```java
@Component
public class WorkflowStateMachinePersister
    implements StateMachinePersister<WorkflowState, WorkflowEvent, String> {

    @Autowired
    private StateMachineRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void persist(StateMachine<WorkflowState, WorkflowEvent> stateMachine,
                       String contextId) throws Exception {
        StateMachineContext<WorkflowState, WorkflowEvent> context =
            stateMachine.getStateMachineContext();

        SpringStateMachineContext persistableContext = new SpringStateMachineContext();
        persistableContext.setState(context.getState().toString());

        // 序列化 extended state
        Map<String, Object> extendedState = new HashMap<>();
        context.getExtendedState().getVariables().forEach((k, v) -> {
            try {
                // 序列化复杂对象
                String json = objectMapper.writeValueAsString(v);
                extendedState.put(k, json);
            } catch (Exception e) {
                extendedState.put(k, v);
            }
        });
        persistableContext.setExtendedState(extendedState);

        repository.save(persistableContext.toEntity(contextId));
    }

    @Override
    public StateMachine<WorkflowState, WorkflowEvent> restore(String contextId)
        throws Exception {
        StateMachineEntity entity = repository.findById(contextId)
            .orElseThrow(() -> new IllegalArgumentException("State machine not found"));

        // 重建状态机...
    }
}
```

### 6.2 持久化时机
- 每次 sendEvent 后自动持久化
- 应用启动时恢复所有 RUNNING 状态的实例

---

## 7. 事务边界与并发控制

### 7.1 事务配置
```java
@Service
public class WorkflowStateMachineService {

    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        rollbackFor = Exception.class
    )
    public WorkflowInstanceResp approve(String instanceId, ApprovalActionReq req) {
        // 1. 加锁并获取实例
        // 2. 发送事件到状态机
        // 3. 更新审批记录
        // 4. 记录日志
        // 全部在一个事务中
    }
}
```

### 7.2 并发控制
```java
@Entity
public class WorkflowInstanceEntity extends BaseEntity {

    @Version
    private Long version;  // JPA 乐观锁

    // OR 使用悲观锁查询
    @Query("SELECT w FROM WorkflowInstanceEntity w WHERE w.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    WorkflowInstanceEntity findByIdForUpdate(@Param("id") String id);
}
```

---

## 8. 退回功能实现

### 8.1 退回逻辑
```java
@Service
public class WorkflowStateMachineService {

    @Transactional
    public void rollback(String instanceId, String toNodeId, String comment) {
        WorkflowInstanceEntity instance = repository.findById(instanceId)
            .orElseThrow(() -> new BizException("流程不存在"));

        if (!instance.isRunning()) {
            throw new BizException("只有运行中的流程可以退回");
        }

        // 获取节点路径
        List<String> nodePath = instance.getNodePath();
        String currentNodeId = instance.getCurrentNodeId();

        int currentIndex = nodePath.indexOf(currentNodeId);
        int targetIndex = nodePath.indexOf(toNodeId);

        // 验证退回目标
        if (targetIndex >= currentIndex) {
            throw new BizException("不能退回到当前或之后的节点");
        }

        if (targetIndex < 0) {
            throw new BizException("目标节点不在路径中");
        }

        // 更新当前节点
        instance.setCurrentNodeId(toNodeId);
        instance.setVersion(instance.getVersion() + 1);

        // 创建退回审批记录
        WorkflowApprovalEntity rollbackApproval = new WorkflowApprovalEntity();
        rollbackApproval.setInstanceId(instanceId);
        rollbackApproval.setNodeId(toNodeId);
        rollbackApproval.setApprovalStatus("pending");
        rollbackApproval.setIsRollback(true);
        rollbackApproval.setRollbackFromNodeId(currentNodeId);
        rollbackApproval.setComment(comment);
        approvalRepository.save(rollbackApproval);

        // 记录日志
        logService.logAction(instanceId, "ROLLBACK", currentNodeId + " → " + toNodeId);

        // 持久化状态机
        stateMachinePersister.persist(stateMachine, instanceId);
    }
}
```

---

## 9. API 接口设计（修正版）

### 9.1 流程定义管理
```
POST   /api/v1/workflow/definitions           创建流程定义
GET    /api/v1/workflow/definitions           查询流程定义列表
GET    /api/v1/workflow/definitions/{id}      获取流程定义详情
PUT    /api/v1/workflow/definitions/{id}      更新流程定义
DELETE /api/v1/workflow/definitions/{id}      删除流程定义
```

### 9.2 节点管理
```
POST   /api/v1/workflow/definitions/{id}/nodes 添加节点
PUT    /api/v1/workflow/nodes/{id}             更新节点
DELETE /api/v1/workflow/nodes/{id}             删除节点
GET    /api/v1/workflow/definitions/{id}/graph 获取流程图数据
```

### 9.3 流程实例管理
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

### 9.4 查询与监控
```
GET    /api/v1/workflow/instances/{id}/approvals  获取审批记录
GET    /api/v1/workflow/instances/{id}/logs       获取操作日志
GET    /api/v1/workflow/instances/{id}/available-actions 获取可用操作
GET    /api/v1/workflow/instances/{id}/state-diagram 获取状态图数据
```

**可用操作响应示例：**
```json
GET /api/v1/workflow/instances/{id}/available-actions
{
  "canApprove": true,
  "canReject": true,
  "canCancel": false,
  "canRollback": true,
  "canWithdraw": false,
  "availableRollbackNodes": [
    {"nodeId": "node-001", "nodeName": "部门审批"},
    {"nodeId": "node-000", "nodeName": "草稿"}
  ]
}
```

---

## 10. 错误处理

### 10.1 状态机错误处理
```java
@Component
public class WorkflowStateMachineErrorHandler {

    public void handleStateChangeError(
        StateContext<WorkflowState, WorkflowEvent> context,
        Exception exception) {

        String instanceId = (String) context.getExtendedState()
            .getVariables().get("instanceId");

        // 记录错误日志
        log.error("State machine error for instance: {}", instanceId, exception);

        // 发送告警
        alertService.sendAlert("Workflow State Machine Error", instanceId);

        // 更新流程状态为错误
        WorkflowInstanceEntity instance = repository.findById(instanceId).orElse(null);
        if (instance != null) {
            instance.setStatus("error");
            repository.save(instance);
        }
    }
}
```

### 10.2 状态转换监听器
```java
@Component
public class WorkflowStateMachineListener
    extends StateMachineListenerAdapter<WorkflowState, WorkflowEvent> {

    @Override
    public void stateChanged(State<WorkflowState> from, State<WorkflowState> to) {
        log.info("State changed: {} -> {}", from.getId(), to.getId());

        // 发布事件
        applicationEventPublisher.publishEvent(
            new WorkflowStateChangedEvent(from, to)
        );
    }

    @Override
    public void transition(
        Transition<WorkflowState, WorkflowEvent> transition) {
        log.info("Transition: {} --[{}]--> {}",
            transition.getSource().getId(),
            transition.getEvent().getId(),
            transition.getTarget().getId());
    }
}
```

---

## 11. 技术栈

### 11.1 后端依赖
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

### 11.2 前端依赖
```json
{
  "dependencies": {
    "@vue-flow/core": "^1.33.0",
    "@vue-flow/background": "^1.3.0",
    "@vue-flow/controls": "^1.1.1",
    "@vue-flow/minimap": "^1.4.0"
  }
}
```

---

## 12. 阶段划分（修订版）

### 12.1 MVP 1 阶段（1-2周）
**目标：端到端跑通基础串行审批**

| 模块 | 交付物 |
|------|--------|
| 后端 | Spring State Machine 基础配置、状态转换、状态持久化 |
| 前端 | 流程列表、我的审批、基础状态图展示 |
| 数据库 | 创建新表结构 |
| 测试 | 单元测试 + 集成测试 |

### 12.2 MVP 2 阶段（1周）
**目标：添加条件分支和退回**

| 功能 | 描述 |
|------|------|
| 条件分支 | SpEL 表达式 + Guard 实现 |
| 退回功能 | 节点路径跟踪 + 退回逻辑 |

### 12.3 P2 阶段（1-2周）
**目标：并行审批与会签**

### 12.4 P3 阶段（2-3周）
**目标：可视化设计器**

---

## 13. 测试策略

### 13.1 单元测试
- StateMachine 配置测试
- Guard 条件判断测试
- Action 执行测试
- Service 层业务逻辑测试

### 13.2 集成测试场景
- 完整流程审批测试
- 条件分支测试
- 退回功能测试
- 并发审批测试
- 状态机恢复测试
- 长时间运行测试

---

## 14. 风险与缓解

| 风险 | 缓解措施 |
|------|---------|
| Spring State Machine 学习曲线 | 参考官方文档，先实现核心功能 |
| 状态持久化复杂性 | 使用 Spring State Machine JPA 模块 |
| 前端流程图性能 | 使用虚拟滚动，大数据量时分页 |
| 并发审批一致性 | 乐观锁 + 重试机制 |
| 退回逻辑复杂性 | 使用节点路径栈，清晰记录历史 |
