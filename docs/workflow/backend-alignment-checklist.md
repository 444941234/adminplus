# Workflow Backend Alignment Checklist

## 用途

这份清单给后端开发直接使用，用来对齐当前前端 `workflow` 模块的实际需求。

配套文档：

- [frontend-contract.md](./frontend-contract.md)

## P0：必须先补齐

这些不补，前端虽然能编译，但主链路无法真正稳定联调。

### 1. 流程发起改为结构化 `formData`

涉及接口：

- `POST /workflow/instances/start`
- `POST /workflow/instances/draft`
- `PUT /workflow/instances/{id}/draft`
- `POST /workflow/instances/{id}/submit`

后端要求：

- 接收 `formData: Record<string, unknown>`
- 不再要求前端传手写 `businessData` 字符串
- `remark` 允许为空

完成标准：

- 发起流程成功
- 保存草稿成功
- 编辑草稿后可再次保存
- 草稿可正式提交

### 2. 流程定义详情必须返回 `formConfig`

涉及接口：

- `GET /workflow/definitions/{id}`

后端要求：

- 返回 `formConfig`
- 返回 `nodeCount`
- `formConfig` 结构与前端类型一致

完成标准：

- `WorkflowCenter` 能按模板动态渲染表单
- `MyWorkflow` 草稿编辑能重新加载定义表单

### 3. 草稿详情接口必须返回 `formConfig + formData`

涉及接口：

- `GET /workflow/instances/{id}/draft`

后端要求：

- 返回 `instance`
- 返回 `formConfig`
- 返回 `formData`

完成标准：

- 草稿打开后可完整回填

### 4. 详情聚合接口必须稳定返回业务信息

涉及接口：

- `GET /workflow/instances/{id}`

后端要求：

- 返回 `instance`
- 返回 `approvals`
- 返回 `nodes`
- 返回 `currentNode`
- 返回 `formConfig`
- 返回 `formData`
- 返回 `canApprove`

建议一并聚合：

- `ccRecords`
- `addSignRecords`

完成标准：

- `WorkflowDetail` 能展示申请信息
- `WorkflowVisualizer` 能在实例模式下渲染节点状态

### 5. 细粒度权限数据要下发

前端已接入权限：

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

后端要求：

- 菜单和权限数据里补齐这些 code
- 新角色配置优先下发细粒度权限

完成标准：

- 页面入口、按钮、已读动作按细粒度权限正确显示

## P1：建议尽快补齐

这些不一定阻塞主链路，但不补会影响体验和数据闭环。

### 6. 抄送接口保持稳定

涉及接口：

- `GET /workflow/cc/my`
- `GET /workflow/cc/my/unread`
- `GET /workflow/cc/my/unread/count`
- `PUT /workflow/cc/{id}/read`
- `PUT /workflow/cc/read-batch`

后端要求：

- 返回数组格式统一
- `isRead`、`readTime` 字段稳定
- 批量已读支持 `string[]`

完成标准：

- `MyCc` 可查看、未读筛选、单条已读、全部已读

### 7. 催办接口保持稳定

涉及接口：

- `GET /workflow/urge/received`
- `GET /workflow/urge/sent`
- `GET /workflow/urge/unread`
- `GET /workflow/urge/unread/count`
- `GET /workflow/urge/instance/{id}`
- `PUT /workflow/urge/{id}/read`
- `PUT /workflow/urge/read-batch`

后端要求：

- 返回 `urgeUserName`、`urgeTargetName`
- `isRead`、`readTime` 字段稳定
- 批量已读支持 `string[]`

完成标准：

- `UrgeCenter` 可查看、切 tab、单条已读、全部已读

### 8. 待我审批计数稳定

涉及接口：

- `GET /workflow/instances/pending/count`

完成标准：

- `Layout` 顶部待审批角标稳定显示

## P2：平台能力补齐

这些是下一阶段增强项，不属于本轮前端主链路阻塞。

### 9. 设计器扩展能力

建议后端后续补：

- 定义版本管理
- 定义发布/停用
- 节点排序持久化
- 条件分支规则
- 节点候选人策略

### 10. 实例图增强数据

当前前端实例图依赖详情中的：

- `nodes`
- `currentNode`
- `approvals`

后续如果要展示更完整路径，建议后端补：

- 回退路径
- 加签链路
- 转办链路
- 节点状态历史

## 字段对齐要求

### 时间字段

以下字段必须允许 `null`：

- `submitTime`
- `finishTime`
- `approvalTime`
- `readTime`

### 枚举值

统一使用大写或固定字符串，不要混用：

- 实例状态：`DRAFT / PENDING / PROCESSING / APPROVED / REJECTED / CANCELLED / WITHDRAWN / FINISHED`
- 加签类型：`BEFORE / AFTER / TRANSFER`
- 审批类型：`user / role / dept / leader`

### 附件字段

建议统一为数组，不要用逗号拼接字符串。

## 联调顺序建议

1. `GET /workflow/definitions/{id}`
2. `POST /workflow/instances/draft`
3. `GET /workflow/instances/{id}/draft`
4. `PUT /workflow/instances/{id}/draft`
5. `POST /workflow/instances/{id}/submit`
6. `POST /workflow/instances/start`
7. `GET /workflow/instances/{id}`
8. 抄送接口
9. 催办接口
10. 权限数据和菜单

## 联调验收建议

### 发起与草稿

- 能选择流程模板
- 能看到动态表单
- 能保存草稿
- 能编辑草稿
- 能从草稿提交

### 详情

- 能看到业务表单内容
- 能看到节点流转图
- 能看到审批记录
- 能执行通过、驳回、回退、加签

### 协同

- 抄送页有数据
- 催办页有数据
- 已读后列表和未读数同步变化

### 权限

- 无 `workflow:cc:list` 时不显示抄送入口
- 无 `workflow:urge:list` 时不显示催办入口
- 无 `workflow:cc:read` / `workflow:urge:read` 时不显示已读按钮

## 当前前端已完成范围

前端已落地：

- 动态表单发起
- 草稿闭环
- 我的流程 / 待我审批
- 流程详情业务回显
- 抄送中心
- 催办中心
- 顶部消息入口
- 设计器节点属性组件化
- 实例流转图入口
- 相关页面与组件测试

后端按这份清单对齐后，可以直接进入联调和验收。
