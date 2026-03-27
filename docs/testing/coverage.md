# 测试覆盖情况

## 已完成的测试

### 后端测试

| 测试类 | 状态 | 覆盖功能 |
|--------|------|----------|
| `WorkflowInstanceControllerRollbackTest.java` | ✅ 已创建 | rollback API, getRollbackableNodes API, add-sign API, getAddSignRecords API |
| `WorkflowSecurityTest.java` | ✅ 已更新 | 新增回退、加签/转办、抄送/催办安全测试 |

### 前端测试

| 测试文件 | 状态 | 覆盖功能 |
|----------|------|----------|
| `WorkflowDetail.test.ts` | ✅ 已创建 | 回退、抄送、加签/转办功能测试 |
| `MyWorkflow.test.ts` | ✅ 已创建 | 催办功能测试 |

## 运行测试

```bash
# 运行所有后端测试
cd backend && mvn test

# 运行新增的回退和加签测试
cd backend && mvn test -Dtest=WorkflowInstanceControllerRollbackTest

# 运行工作流安全测试
cd backend && mvn test -Dtest=WorkflowSecurityTest

# 运行前端测试
cd frontend && npm run test
```

## 测试统计

- 后端测试类: 2个新增
- 前端测试文件: 2个新增
- 安全测试嵌套类: 3个新增 (RollbackAuthorization, AddSignAuthorization, CcUrgeIsolation)