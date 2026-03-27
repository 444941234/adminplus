# Workflow Frontend Contract

## 目的

这份文档描述当前前端 `workflow` 模块已经按什么接口和字段模型实现。

适用范围：

- `frontend/src/views/workflow/*`
- `frontend/src/components/workflow/*`
- `frontend/src/composables/workflow/*`
- `frontend/src/api/workflow.ts`

这不是后端设计草案，而是联调契约。后端实现应以这里的请求、响应和权限语义为准。

## 核心原则

1. 发起流程和草稿一律使用结构化 `formData`，不再使用手填 JSON 文本。
2. 页面按钮显示优先依赖细粒度权限，不再只靠粗粒度 `workflow:approve`。
3. 详情页按聚合接口展示业务信息、节点、记录和实例流转图。
4. 抄送和催办页面已接入未读和已读动作，权限需要单独控制。

## 状态枚举

前端当前已使用的实例状态：

- `DRAFT`
- `PENDING`
- `PROCESSING`
- `APPROVED`
- `REJECTED`
- `CANCELLED`
- `WITHDRAWN`
- `FINISHED`

建议后端统一返回大写状态值，不要混用大小写。

## 表单模型

### WorkflowFormConfig

```ts
interface WorkflowFormConfig {
  sections: WorkflowFormSection[]
}

interface WorkflowFormSection {
  key: string
  title: string
  fields: WorkflowFormField[]
}

interface WorkflowFormField {
  field: string
  label: string
  component: 'input' | 'textarea' | 'number' | 'select' | 'date' | 'daterange' | 'user' | 'dept' | 'file'
  required?: boolean
  readonly?: boolean
  placeholder?: string
  defaultValue?: unknown
  options?: Array<{ label: string; value: string | number }>
  rules?: {
    min?: number
    max?: number
    pattern?: string
  }
  description?: string
}
```

### 提交载荷

```ts
interface StartWorkflowPayload {
  definitionId: string
  title: string
  formData?: Record<string, unknown>
  remark?: string
}
```

前端不会再主动构造旧式 `businessData` 文本串。

## 流程定义接口

### `GET /workflow/definitions`

用于：

- 流程模板页列表
- 设计器定义列表

返回字段至少包括：

- `id`
- `definitionName`
- `definitionKey`
- `category`
- `description`
- `status`
- `version`
- `formConfig`
- `nodeCount`
- `createTime`
- `updateTime`

### `GET /workflow/definitions/enabled`

用于：

- 发起流程
- 草稿编辑时流程类型下拉

返回字段同定义列表，至少要包含 `id` 和 `status`。

### `GET /workflow/definitions/{id}`

用于：

- 发起流程时加载动态表单
- 草稿编辑时重新加载定义
- 设计器查看流程图

必须返回：

- `formConfig`
- `nodeCount`

## 流程实例接口

### `GET /workflow/instances/my`

用于：

- 我的流程

当前前端调用支持：

- 无参数
- `status`

建议后端继续扩展分页和更多筛选，但当前至少要支持按状态过滤。

### `POST /workflow/instances/start`

用于：

- 发起流程

请求体：

```json
{
  "definitionId": "def-001",
  "title": "请假审批申请",
  "formData": {
    "reason": "年假申请",
    "days": 2
  },
  "remark": "补充说明"
}
```

### `POST /workflow/instances/draft`

用于：

- 保存草稿

请求体与 `start` 相同。

### `GET /workflow/instances/{id}/draft`

用于：

- 草稿编辑

返回：

```ts
interface WorkflowDraftDetail {
  instance: WorkflowInstance
  formConfig: string | WorkflowFormConfig
  formData: Record<string, unknown>
}
```

### `PUT /workflow/instances/{id}/draft`

用于：

- 更新草稿

请求体与 `start` 相同。

### `POST /workflow/instances/{id}/submit`

用于：

- 草稿转正式提交

请求体与 `start` 相同。

### `DELETE /workflow/instances/{id}/draft`

用于：

- 删除草稿

## 审批动作接口

### `POST /workflow/instances/{id}/approve`

### `POST /workflow/instances/{id}/reject`

### `POST /workflow/instances/{id}/rollback`

### `POST /workflow/instances/{id}/add-sign`

### `POST /workflow/instances/{id}/withdraw`

### `POST /workflow/instances/{id}/cancel`

### `POST /workflow/urge/{instanceId}`

当前前端动作层统一封装在：

- `frontend/src/composables/workflow/useWorkflowActions.ts`

其中 `rollback` 和 `add-sign` 依赖请求体中的目标节点和加签对象。

## 详情聚合接口

### `GET /workflow/instances/{id}`

这是当前 workflow 模块最关键的聚合接口。

前端详情页、实例图和业务回显都依赖它。返回应至少包括：

```ts
interface WorkflowDetail {
  instance: WorkflowInstance
  approvals: WorkflowApproval[]
  nodes: WorkflowNode[]
  currentNode: WorkflowNode | null
  canApprove: boolean
  formConfig?: string | WorkflowFormConfig
  formData?: Record<string, unknown>
  ccRecords?: WorkflowCc[]
  addSignRecords?: WorkflowAddSign[]
}
```

当前前端还会额外请求：

- `GET /workflow/cc/instance/{id}`
- `GET /workflow/urge/instance/{id}`
- `GET /workflow/instances/{id}/add-sign-records`
- `GET /workflow/instances/{id}/rollbackable-nodes`

后续如果后端愿意继续聚合，这几类记录可以并回详情接口。

## 抄送接口

### `GET /workflow/cc/my`

### `GET /workflow/cc/my/unread`

### `GET /workflow/cc/my/unread/count`

### `PUT /workflow/cc/{id}/read`

### `PUT /workflow/cc/read-batch`

当前前端页面：

- `frontend/src/views/workflow/MyCc.vue`

依赖能力：

- 查看抄送列表
- 查看未读数
- 单条已读
- 批量已读

## 催办接口

### `GET /workflow/urge/received`

### `GET /workflow/urge/sent`

### `GET /workflow/urge/unread`

### `GET /workflow/urge/unread/count`

### `GET /workflow/urge/instance/{id}`

### `PUT /workflow/urge/{id}/read`

### `PUT /workflow/urge/read-batch`

当前前端页面：

- `frontend/src/views/workflow/UrgeCenter.vue`

依赖能力：

- 我收到的
- 我发出的
- 未读催办
- 单条已读
- 批量已读

## 设计器接口

### `GET /workflow/definitions`

### `POST /workflow/definitions`

### `PUT /workflow/definitions/{id}`

### `DELETE /workflow/definitions/{id}`

### `GET /workflow/definitions/{id}/nodes`

### `POST /workflow/definitions/{id}/nodes`

### `PUT /workflow/definitions/nodes/{nodeId}`

### `DELETE /workflow/definitions/nodes/{nodeId}`

当前前端节点请求体：

```ts
interface WorkflowNodeReq {
  nodeName: string
  nodeCode: string
  nodeOrder: number
  approverType: 'user' | 'role' | 'dept' | 'leader'
  approverId?: string
  isCounterSign: boolean
  autoPassSameUser: boolean
  description?: string
}
```

说明：

- `leader` 类型下，前端不再要求手工填写 `approverId`
- `user / role / dept` 类型下，前端通过选择器传递 `approverId`

## 细粒度权限

当前前端已经实际接入的权限包括：

- `workflow:start`
- `workflow:draft`
- `workflow:approve`
- `workflow:reject`
- `workflow:rollback`
- `workflow:add-sign`
- `workflow:urge`
- `workflow:withdraw`
- `workflow:cancel`
- `workflow:cc:list`
- `workflow:cc:read`
- `workflow:urge:list`
- `workflow:urge:read`
- `workflow:definition:list`
- `workflow:definition:create`
- `workflow:definition:update`
- `workflow:definition:delete`

兼容逻辑：

- 某些细粒度权限在前端仍允许回退到旧粗粒度权限，例如 `workflow:create` 或 `workflow:approve`
- 这是为了已有权限数据不立刻失效
- 新角色配置应优先下发细粒度权限

## 已完成的前端页面

当前已落地页面：

- `WorkflowCenter.vue`
- `MyWorkflow.vue`
- `PendingApproval.vue`
- `WorkflowDetail.vue`
- `MyCc.vue`
- `UrgeCenter.vue`
- `WorkflowDesigner.vue`
- `WorkflowVisualizer.vue`

## 当前测试覆盖

workflow 已有以下测试：

- `WorkflowCenter.test.ts`
- `MyWorkflow.test.ts`
- `WorkflowDetail.test.ts`
- `MyCc.test.ts`
- `UrgeCenter.test.ts`
- `WorkflowDesigner.test.ts`
- `WorkflowFormRenderer.test.ts`
- `WorkflowBusinessCard.test.ts`
- `WorkflowActionButtons.test.ts`
- `WorkflowNodeProperties.test.ts`
- `useWorkflowForm.test.ts`

## 联调注意事项

1. 所有时间字段应允许返回 `null`。
2. 后端不要再要求前端传手写 JSON 字符串。
3. `WorkflowDetail` 应尽量稳定返回 `formConfig + formData`。
4. 权限应优先按细粒度下发，避免页面入口和动作误显示。
5. `approverType`、实例状态、加签类型等枚举值应保持统一大小写。
