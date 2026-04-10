# Service Layer Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor Service layer to improve code quality, maintainability, and consistency through unified exception handling, service decomposition, and centralized XSS processing.

**Architecture:** Four-phase progressive refactoring: (1) unified exception handling utilities, (2) WorkflowInstanceService decomposition, (3) common logic extraction (permissions, auditing), (4) XSS handling centralization in Converters.

**Tech Stack:** Spring Boot 3.5, JDK 21, Spring AOP, Lombok

---

## Phase 1: Unified Exception Handling

### Task 1.1: Create ServiceAssert Utility Class

**Files:**
- Create: `backend/src/main/java/com/adminplus/utils/ServiceAssert.java`

- [ ] **Step 1: Write the utility class**

```java
package com.adminplus.utils;

import com.adminplus.common.exception.BizException;

/**
 * 服务层断言工具类
 * 用于业务规则校验，统一抛出 BizException
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public final class ServiceAssert {

    private ServiceAssert() {}

    /**
     * 断言条件为真
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new BizException(message);
        }
    }

    /**
     * 断言条件为真（带错误码）
     */
    public static void isTrue(boolean condition, int code, String message) {
        if (!condition) {
            throw new BizException(code, message);
        }
    }

    /**
     * 断言对象不为空
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BizException(message);
        }
    }

    /**
     * 断言实体不存在（用于唯一性检查）
     */
    public static void notExists(boolean exists, String message) {
        if (exists) {
            throw new BizException(message);
        }
    }

    /**
     * 断言实体存在
     */
    public static void exists(boolean exists, String message) {
        if (!exists) {
            throw new BizException(message);
        }
    }

    /**
     * 断言状态匹配
     */
    public static void isStatus(Integer actual, Integer expected, String message) {
        if (!expected.equals(actual)) {
            throw new BizException(message);
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/adminplus/utils/ServiceAssert.java
git commit -m "feat(utils): add ServiceAssert utility for unified business assertions"
```

---

### Task 1.2: Extend EntityHelper Utility Class

**Files:**
- Modify: `backend/src/main/java/com/adminplus/utils/EntityHelper.java`

- [ ] **Step 1: Read current EntityHelper implementation**

Run: Read file to understand current structure

- [ ] **Step 2: Add new methods to EntityHelper**

```java
package com.adminplus.utils;

import com.adminplus.common.exception.BizException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 实体查询辅助工具类
 * <p>
 * 消除重复的 findById().orElseThrow() 模式
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public final class EntityHelper {

    private EntityHelper() {}

    /**
     * 根据 ID 查找实体，不存在则抛出 BizException
     *
     * @param finder  查找函数（如 repository::findById）
     * @param id      实体 ID
     * @param message 不存在时的错误消息
     * @param <T>     实体类型
     * @return 实体对象
     */
    public static <T> T findByIdOrThrow(Function<String, Optional<T>> finder,
                                         String id, String message) {
        return finder.apply(id)
                .orElseThrow(() -> new BizException(message));
    }

    /**
     * 根据 ID 查找实体，不存在则抛出 BizException（支持消息模板）
     *
     * @param finder          查找函数
     * @param id              实体 ID
     * @param messageTemplate 消息模板（支持 {} 占位符）
     * @param args            模板参数
     * @param <T>             实体类型
     * @return 实体对象
     */
    public static <T> T findByIdOrThrow(Function<String, Optional<T>> finder,
                                         String id, String messageTemplate, Object... args) {
        return finder.apply(id)
                .orElseThrow(() -> new BizException(formatMessage(messageTemplate, args)));
    }

    /**
     * 查找实体（软删除场景），不存在则抛出 BizException
     *
     * @param finder     查找函数
     * @param id         实体 ID
     * @param entityName 实体名称（用于错误消息）
     * @param <T>        实体类型
     * @return 实体对象
     */
    public static <T> T findActiveById(Function<String, Optional<T>> finder,
                                        String id, String entityName) {
        return finder.apply(id)
                .orElseThrow(() -> new BizException(404, entityName + "不存在"));
    }

    /**
     * 批量查找实体，缺失时抛出 BizException
     *
     * @param finder     批量查找函数
     * @param ids        实体 ID 列表
     * @param entityName 实体名称
     * @param <T>        实体类型
     * @return 实体列表
     */
    public static <T> List<T> findAllByIdsOrThrow(Function<List<String>, List<T>> finder,
                                                   List<String> ids, String entityName) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<T> results = finder.apply(ids);
        if (results.size() != ids.size()) {
            throw new BizException(404, "部分" + entityName + "不存在");
        }
        return results;
    }

    /**
     * 查找实体并返回 Optional（不抛异常）
     *
     * @param finder 查找函数
     * @param id     实体 ID
     * @param <T>    实体类型
     * @return Optional 包装的实体
     */
    public static <T> Optional<T> findById(Function<String, Optional<T>> finder, String id) {
        return finder.apply(id);
    }

    private static String formatMessage(String template, Object... args) {
        if (args == null || args.length == 0) {
            return template;
        }
        String result = template;
        for (Object arg : args) {
            result = result.replaceFirst("\\{}", String.valueOf(arg));
        }
        return result;
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/adminplus/utils/EntityHelper.java
git commit -m "feat(utils): extend EntityHelper with batch find and message template support"
```

---

### Task 1.3: Replace IllegalArgumentException in WorkflowDefinitionServiceImpl

**Files:**
- Modify: `backend/src/main/java/com/adminplus/service/impl/WorkflowDefinitionServiceImpl.java`

- [ ] **Step 1: Read current implementation**

Run: Read `WorkflowDefinitionServiceImpl.java` to identify all `IllegalArgumentException` usages

- [ ] **Step 2: Replace exception throws with EntityHelper**

Find and replace the following patterns:

```java
// Line 46-48: Replace
if (definitionRepository.existsByDefinitionKeyAndDeletedFalse(request.definitionKey())) {
    throw new IllegalArgumentException("工作流标识已存在: " + request.definitionKey());
}
// With:
ServiceAssert.notExists(
    definitionRepository.existsByDefinitionKeyAndDeletedFalse(request.definitionKey()),
    "工作流标识已存在: " + request.definitionKey()
);
```

```java
// Line 71-72: Replace
WorkflowDefinitionEntity entity = definitionRepository.findById(id)
    .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在: " + id));
// With:
WorkflowDefinitionEntity entity = EntityHelper.findByIdOrThrow(
    definitionRepository::findById, id, "工作流定义不存在"
);
```

```java
// Line 75-80: Replace
definitionRepository.findByDefinitionKeyAndDeletedFalse(request.definitionKey())
    .ifPresent(existing -> {
        if (!existing.getId().equals(id)) {
            throw new IllegalArgumentException("工作流标识已被使用: " + request.definitionKey());
        }
    });
// With:
definitionRepository.findByDefinitionKeyAndDeletedFalse(request.definitionKey())
    .ifPresent(existing -> {
        ServiceAssert.isTrue(existing.getId().equals(id), "工作流标识已被使用: " + request.definitionKey());
    });
```

```java
// Line 116-117: Replace
WorkflowDefinitionEntity entity = definitionRepository.findById(id)
    .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在: " + id));
// With:
WorkflowDefinitionEntity entity = EntityHelper.findByIdOrThrow(
    definitionRepository::findById, id, "工作流定义不存在"
);
```

```java
// Line 175-176: Replace
WorkflowDefinitionEntity definition = definitionRepository.findById(definitionId)
    .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在: " + definitionId));
// With:
WorkflowDefinitionEntity definition = EntityHelper.findByIdOrThrow(
    definitionRepository::findById, definitionId, "工作流定义不存在"
);
```

```java
// Line 200-201: Replace
WorkflowNodeEntity entity = nodeRepository.findById(nodeId)
    .orElseThrow(() -> new IllegalArgumentException("工作流节点不存在: " + nodeId));
// With:
WorkflowNodeEntity entity = EntityHelper.findByIdOrThrow(
    nodeRepository::findById, nodeId, "工作流节点不存在"
);
```

- [ ] **Step 3: Add import statements**

Add at the top of the file:
```java
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.ServiceAssert;
```

- [ ] **Step 4: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/adminplus/service/impl/WorkflowDefinitionServiceImpl.java
git commit -m "refactor(workflow): replace IllegalArgumentException with BizException in WorkflowDefinitionServiceImpl"
```

---

### Task 1.4: Replace IllegalArgumentException in WorkflowInstanceServiceImpl (Part 1)

**Files:**
- Modify: `backend/src/main/java/com/adminplus/service/impl/WorkflowInstanceServiceImpl.java`

**Note:** This file has 28 `IllegalArgumentException` occurrences. We'll process in batches.

- [ ] **Step 1: Add import statements**

Add at the top of the file (if not already present):
```java
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.ServiceAssert;
```

- [ ] **Step 2: Replace in createDraft method (Lines 69-73)**

```java
// Replace:
WorkflowDefinitionEntity definition = definitionRepository.findById(request.definitionId())
    .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在"));

UserEntity user = userRepository.findById(userId)
    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

// With:
WorkflowDefinitionEntity definition = EntityHelper.findByIdOrThrow(
    definitionRepository::findById, request.definitionId(), "工作流定义不存在");

UserEntity user = EntityHelper.findByIdOrThrow(
    userRepository::findById, userId, "用户不存在");
```

- [ ] **Step 3: Replace in submit method (Lines 98-109, 122)**

```java
// Replace:
WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

// With:
WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
    instanceRepository::findById, instanceId, "工作流实例不存在");

// Replace:
if (!instance.getUserId().equals(userId)) {
    throw new IllegalArgumentException("只有发起人可以提交工作流");
}
// With:
ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以提交工作流");

// Replace:
if (nodes.isEmpty()) {
    throw new IllegalArgumentException("工作流没有配置审批节点");
}
// With:
ServiceAssert.isTrue(!nodes.isEmpty(), "工作流没有配置审批节点");
```

- [ ] **Step 4: Replace in getDraftDetail method (Lines 180-188)**

```java
// Replace:
WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

if (!instance.getUserId().equals(userId)) {
    throw new IllegalArgumentException("只有发起人可以查看草稿");
}
if (!instance.isDraft()) {
    throw new IllegalArgumentException("当前流程不是草稿状态");
}

WorkflowDefinitionEntity definition = definitionRepository.findById(instance.getDefinitionId())
    .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在"));

// With:
WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
    instanceRepository::findById, instanceId, "工作流实例不存在");

ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以查看草稿");
ServiceAssert.isTrue(instance.isDraft(), "当前流程不是草稿状态");

WorkflowDefinitionEntity definition = EntityHelper.findByIdOrThrow(
    definitionRepository::findById, instance.getDefinitionId(), "工作流定义不存在");
```

- [ ] **Step 5: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 1.5: Replace IllegalArgumentException in WorkflowInstanceServiceImpl (Part 2)

**Files:**
- Modify: `backend/src/main/java/com/adminplus/service/impl/WorkflowInstanceServiceImpl.java`

- [ ] **Step 1: Replace in updateDraft and deleteDraft methods**

```java
// updateDraft method (Lines 203-211):
WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以更新草稿");
ServiceAssert.isTrue(instance.isDraft(), "只有草稿状态可以更新");

// deleteDraft method (Lines 223-230):
WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以删除草稿");
ServiceAssert.isTrue(instance.isDraft(), "只有草稿状态可以删除");
```

- [ ] **Step 2: Replace in getDetail method (Lines 242-246)**

```java
WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

WorkflowDefinitionEntity definition = definitionRepository.findById(instance.getDefinitionId())
    .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在"));

// With:
WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
    instanceRepository::findById, instanceId, "工作流实例不存在");

WorkflowDefinitionEntity definition = EntityHelper.findByIdOrThrow(
    definitionRepository::findById, instance.getDefinitionId(), "工作流定义不存在");
```

- [ ] **Step 3: Replace in processApproval method (Lines 515-540)**

```java
// Replace:
UserEntity user = userRepository.findById(userId)
    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

ServiceAssert.isTrue(instance.isRunning(), "只有进行中的工作流可以审批");

// ... findFirst().orElseThrow() pattern:
WorkflowApprovalEntity myApproval = pendingApprovals.stream()
    .filter(a -> a.getApproverId().equals(userId))
    .findFirst()
    .orElseThrow(() -> new IllegalArgumentException("您没有权限审批此工作流"));

WorkflowNodeEntity currentNode = nodeRepository.findById(instance.getCurrentNodeId())
    .orElseThrow(() -> new IllegalArgumentException("当前节点不存在"));

// With:
UserEntity user = EntityHelper.findByIdOrThrow(
    userRepository::findById, userId, "用户不存在");

WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
    instanceRepository::findById, instanceId, "工作流实例不存在");

ServiceAssert.isTrue(instance.isRunning(), "只有进行中的工作流可以审批");

// For the orElseThrow in stream:
ServiceAssert.isTrue(
    pendingApprovals.stream().anyMatch(a -> a.getApproverId().equals(userId)),
    "您没有权限审批此工作流"
);
WorkflowApprovalEntity myApproval = pendingApprovals.stream()
    .filter(a -> a.getApproverId().equals(userId))
    .findFirst()
    .orElseThrow();

WorkflowNodeEntity currentNode = EntityHelper.findByIdOrThrow(
    nodeRepository::findById, instance.getCurrentNodeId(), "当前节点不存在");
```

- [ ] **Step 4: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 1.6: Replace IllegalArgumentException in WorkflowInstanceServiceImpl (Part 3)

**Files:**
- Modify: `backend/src/main/java/com/adminplus/service/impl/WorkflowInstanceServiceImpl.java`

- [ ] **Step 1: Replace in cancel method (Lines 386-395)**

```java
WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

ServiceAssert.isTrue(instance.isCancellable(), "当前状态不允许取消");
ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以取消工作流");
```

- [ ] **Step 2: Replace in withdraw method (Lines 437-445)**

```java
WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以撤回工作流");
ServiceAssert.isTrue(instance.isDraft() || instance.isRejected(), "只有草稿或被拒绝的流程可以撤回");
```

- [ ] **Step 3: Replace in getApprovals method (Lines 498-501)**

```java
WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));
```

- [ ] **Step 4: Replace in rollback method (Lines 918-959)**

```java
WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

ServiceAssert.isTrue(instance.isRunning(), "只有进行中的工作流可以回退");

WorkflowNodeEntity targetNode = nodeRepository.findById(finalTargetNodeId)
    .orElseThrow(() -> new IllegalArgumentException("目标节点不存在"));

WorkflowNodeEntity currentNode = nodeRepository.findById(instance.getCurrentNodeId())
    .orElseThrow(() -> new IllegalArgumentException("当前节点不存在"));
```

- [ ] **Step 5: Replace in getRollbackableNodes and addSign methods**

```java
// getRollbackableNodes:
WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

// addSign:
WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
    .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

ServiceAssert.isTrue(instance.isRunning(), "只有运行中的工作流可以加签/转办");

WorkflowNodeEntity currentNode = nodeRepository.findById(instance.getCurrentNodeId())
    .orElseThrow(() -> new IllegalArgumentException("当前节点不存在"));

UserEntity addUser = userRepository.findById(request.addUserId())
    .orElseThrow(() -> new IllegalArgumentException("被加签人不存在"));

UserEntity initiator = userRepository.findById(initiatorId)
    .orElseThrow(() -> new IllegalArgumentException("加签发起人不存在"));
```

- [ ] **Step 6: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit Phase 1 changes**

```bash
git add backend/src/main/java/com/adminplus/service/impl/WorkflowInstanceServiceImpl.java
git commit -m "refactor(workflow): replace IllegalArgumentException with BizException in WorkflowInstanceServiceImpl"
```

---

### Task 1.7: Replace IllegalArgumentException in Other Workflow Services

**Files:**
- Modify: `backend/src/main/java/com/adminplus/service/impl/WorkflowStateMachineServiceImpl.java`
- Modify: `backend/src/main/java/com/adminplus/service/impl/WorkflowUrgeServiceImpl.java`

- [ ] **Step 1: Process WorkflowStateMachineServiceImpl**

Follow the same pattern to replace `IllegalArgumentException` with `BizException` using `EntityHelper` and `ServiceAssert`.

- [ ] **Step 2: Process WorkflowUrgeServiceImpl**

Follow the same pattern.

- [ ] **Step 3: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/adminplus/service/impl/WorkflowStateMachineServiceImpl.java \
        backend/src/main/java/com/adminplus/service/impl/WorkflowUrgeServiceImpl.java
git commit -m "refactor(workflow): replace IllegalArgumentException in remaining workflow services"
```

---

## Phase 2: Split WorkflowInstanceService

### Task 2.1: Create WorkflowApproverResolver Interface and Implementation

**Files:**
- Create: `backend/src/main/java/com/adminplus/service/workflow/WorkflowApproverResolver.java`
- Create: `backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowApproverResolverImpl.java`

- [ ] **Step 1: Create interface**

```java
package com.adminplus.service.workflow;

import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;

import java.util.List;

/**
 * 工作流审批人解析器
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface WorkflowApproverResolver {

    /**
     * 解析审批人ID列表
     *
     * @param instance 工作流实例
     * @param node     审批节点
     * @return 审批人ID列表
     */
    List<String> resolveApprovers(WorkflowInstanceEntity instance, WorkflowNodeEntity node);

    /**
     * 批量获取用户名称
     *
     * @param userIds 用户ID列表
     * @return 用户ID -> 昵称映射
     */
    java.util.Map<String, String> batchGetApproverNames(List<String> userIds);
}
```

- [ ] **Step 2: Create implementation (extract from WorkflowInstanceServiceImpl)**

```java
package com.adminplus.service.workflow.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.entity.*;
import com.adminplus.repository.DeptRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.workflow.WorkflowApproverResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流审批人解析器实现
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowApproverResolverImpl implements WorkflowApproverResolver {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final DeptRepository deptRepository;

    @Override
    public List<String> resolveApprovers(WorkflowInstanceEntity instance, WorkflowNodeEntity node) {
        List<String> approvers = new ArrayList<>();

        switch (node.getApproverType()) {
            case "user":
                if (node.getApproverId() != null) {
                    approvers.add(node.getApproverId());
                }
                break;

            case "role":
                if (node.getApproverId() != null) {
                    String roleId = node.getApproverId();
                    RoleEntity role = null;

                    if (roleId.startsWith("ROLE_")) {
                        role = roleRepository.findByCode(roleId).orElse(null);
                        if (role == null) {
                            log.warn("找不到角色编码: {}", node.getApproverId());
                        }
                    } else {
                        role = roleRepository.findByName(roleId).orElse(null);
                        if (role == null) {
                            role = roleRepository.findById(roleId).orElse(null);
                        }
                        if (role == null) {
                            log.warn("找不到角色: {}", node.getApproverId());
                        }
                    }

                    if (role != null) {
                        roleId = role.getId();
                        List<UserRoleEntity> userRoles = userRoleRepository.findByRoleId(roleId);
                        approvers.addAll(userRoles.stream().map(UserRoleEntity::getUserId).toList());
                    }
                }
                break;

            case "dept":
            case "leader":
                if (instance.getDeptId() != null) {
                    DeptEntity dept = deptRepository.findById(instance.getDeptId()).orElse(null);
                    if (dept != null && dept.getLeader() != null) {
                        approvers.add(dept.getLeader());
                    }
                }
                break;

            default:
                break;
        }

        if (approvers.isEmpty()) {
            log.error("无法解析审批人: type={}, node={}", node.getApproverType(), node.getNodeName());
            throw new BizException("无法解析审批人，请联系管理员配置审批流程: " + node.getNodeName());
        }

        return approvers;
    }

    @Override
    public Map<String, String> batchGetApproverNames(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<String> distinctIds = userIds.stream()
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .toList();

        if (distinctIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(distinctIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getNickname));
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/adminplus/service/workflow/WorkflowApproverResolver.java \
        backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowApproverResolverImpl.java
git commit -m "refactor(workflow): extract WorkflowApproverResolver from WorkflowInstanceService"
```

---

### Task 2.2: Create WorkflowPermissionChecker

**Files:**
- Create: `backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowPermissionChecker.java`

- [ ] **Step 1: Create the permission checker**

```java
package com.adminplus.service.workflow.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.response.WorkflowOperationPermissionsResponse;
import com.adminplus.pojo.entity.WorkflowApprovalEntity;
import com.adminplus.pojo.entity.WorkflowCcEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.repository.WorkflowApprovalRepository;
import com.adminplus.repository.WorkflowCcRepository;
import com.adminplus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 工作流权限检查器
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Component
@RequiredArgsConstructor
public class WorkflowPermissionChecker {

    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowCcRepository ccRepository;

    /**
     * 检查用户是否有权限查看工作流实例详情
     */
    public void checkViewAccess(WorkflowInstanceEntity instance, String userId) {
        // 发起人
        if (instance.getUserId().equals(userId)) {
            return;
        }

        // 审批人
        boolean isApprover = approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instance.getId())
                .stream()
                .anyMatch(a -> a.getApproverId().equals(userId));

        if (isApprover) {
            return;
        }

        // 抄送人
        boolean isCcReceiver = ccRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instance.getId())
                .stream()
                .anyMatch(cc -> cc.getUserId().equals(userId));

        if (isCcReceiver) {
            return;
        }

        throw new BizException("您无权查看该工作流实例");
    }

    /**
     * 判断用户是否可以审批
     */
    public boolean canUserApprove(WorkflowInstanceEntity instance, String userId) {
        if (!instance.isRunning() || instance.getCurrentNodeId() == null) {
            return false;
        }

        List<WorkflowApprovalEntity> pendingApprovals = approvalRepository
                .findByInstanceIdAndNodeIdAndDeletedFalse(instance.getId(), instance.getCurrentNodeId())
                .stream()
                .filter(WorkflowApprovalEntity::isPending)
                .toList();

        return pendingApprovals.stream()
                .anyMatch(a -> a.getApproverId().equals(userId));
    }

    /**
     * 构建操作权限响应
     */
    public WorkflowOperationPermissionsResponse buildOperationPermissions(
            WorkflowInstanceEntity instance, boolean canApprove) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isOwner = Objects.equals(instance.getUserId(), currentUserId);

        return new WorkflowOperationPermissionsResponse(
                canApprove,
                canApprove,
                canApprove,
                canApprove,
                canApprove,
                isOwner && instance.isRunning(),
                isOwner && (instance.isDraft() || instance.isRejected()),
                isOwner && instance.isCancellable()
        );
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowPermissionChecker.java
git commit -m "refactor(workflow): extract WorkflowPermissionChecker from WorkflowInstanceService"
```

---

### Task 2.3: Create WorkflowDraftService

**Files:**
- Create: `backend/src/main/java/com/adminplus/service/workflow/WorkflowDraftService.java`
- Create: `backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowDraftServiceImpl.java`

- [ ] **Step 1: Create interface**

```java
package com.adminplus.service.workflow;

import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.WorkflowDraftDetailResponse;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;

/**
 * 工作流草稿服务
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface WorkflowDraftService {

    /**
     * 创建草稿
     */
    WorkflowInstanceResponse createDraft(WorkflowStartRequest request);

    /**
     * 获取草稿详情
     */
    WorkflowDraftDetailResponse getDraftDetail(String instanceId);

    /**
     * 更新草稿
     */
    WorkflowInstanceResponse updateDraft(String instanceId, WorkflowStartRequest request);

    /**
     * 删除草稿
     */
    void deleteDraft(String instanceId);
}
```

- [ ] **Step 2: Create implementation**

```java
package com.adminplus.service.workflow.impl;

import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.WorkflowDraftDetailResponse;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.WorkflowDefinitionEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.repository.DefinitionRepository;
import com.adminplus.repository.InstanceRepository;
import com.adminplus.repository.UserRepository;
import com.adminplus.service.workflow.WorkflowDraftService;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.XssUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowDraftServiceImpl implements WorkflowDraftService {

    private final InstanceRepository instanceRepository;
    private final DefinitionRepository definitionRepository;
    private final UserRepository userRepository;
    private final ConversionService conversionService;
    // JsonMapper for serialization (inject via constructor)

    @Override
    @Transactional
    public WorkflowInstanceResponse createDraft(WorkflowStartRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("创建工作流草稿: userId={}, definitionId={}", userId, request.definitionId());

        WorkflowDefinitionEntity definition = EntityHelper.findByIdOrThrow(
            definitionRepository::findById, request.definitionId(), "工作流定义不存在");

        UserEntity user = EntityHelper.findByIdOrThrow(
            userRepository::findById, userId, "用户不存在");

        WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        instance.setDefinitionId(request.definitionId());
        instance.setDefinitionName(definition.getDefinitionName());
        instance.setUserId(userId);
        instance.setUserName(user.getNickname());
        instance.setDeptId(user.getDeptId());
        instance.setTitle(XssUtils.escape(request.title()));
        // instance.setBusinessData(serializeFormData(request.formData()));
        instance.setStatus("draft");
        instance.setRemark(XssUtils.escape(request.remark()));

        instance = instanceRepository.save(instance);
        log.info("工作流草稿创建成功: id={}", instance.getId());

        return conversionService.convert(instance, WorkflowInstanceResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowDraftDetailResponse getDraftDetail(String instanceId) {
        String userId = SecurityUtils.getCurrentUserId();
        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以查看草稿");
        ServiceAssert.isTrue("draft".equals(instance.getStatus()), "当前流程不是草稿状态");

        WorkflowDefinitionEntity definition = EntityHelper.findByIdOrThrow(
            definitionRepository::findById, instance.getDefinitionId(), "工作流定义不存在");

        return new WorkflowDraftDetailResponse(
            conversionService.convert(instance, WorkflowInstanceResponse.class),
            definition.getFormConfig(),
            Collections.emptyMap() // deserializeFormData
        );
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse updateDraft(String instanceId, WorkflowStartRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以更新草稿");
        ServiceAssert.isTrue("draft".equals(instance.getStatus()), "只有草稿状态可以更新");

        instance.setTitle(XssUtils.escape(request.title()));
        instance.setRemark(XssUtils.escape(request.remark()));

        WorkflowInstanceEntity saved = instanceRepository.save(instance);
        return conversionService.convert(saved, WorkflowInstanceResponse.class);
    }

    @Override
    @Transactional
    public void deleteDraft(String instanceId) {
        String userId = SecurityUtils.getCurrentUserId();
        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以删除草稿");
        ServiceAssert.isTrue("draft".equals(instance.getStatus()), "只有草稿状态可以删除");

        instanceRepository.delete(instance);
        log.info("草稿已删除: instanceId={}", instanceId);
    }
}
```

- [ ] **Step 3: Add ServiceAssert import**

Add `import com.adminplus.utils.ServiceAssert;` if not auto-imported.

- [ ] **Step 4: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS (may have some missing pieces to adjust)

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/adminplus/service/workflow/WorkflowDraftService.java \
        backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowDraftServiceImpl.java
git commit -m "refactor(workflow): extract WorkflowDraftService from WorkflowInstanceService"
```

---

### Task 2.4: Create WorkflowApprovalService

**Files:**
- Create: `backend/src/main/java/com/adminplus/service/workflow/WorkflowApprovalService.java`
- Create: `backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowApprovalServiceImpl.java`

- [ ] **Step 1: Create interface**

```java
package com.adminplus.service.workflow;

import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;

/**
 * 工作流审批服务
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface WorkflowApprovalService {

    /**
     * 提交工作流
     */
    WorkflowInstanceResponse submit(String instanceId, WorkflowStartRequest request);

    /**
     * 同意审批
     */
    WorkflowInstanceResponse approve(String instanceId, ApprovalActionRequest request);

    /**
     * 拒绝审批
     */
    WorkflowInstanceResponse reject(String instanceId, ApprovalActionRequest request);

    /**
     * 取消工作流
     */
    void cancel(String instanceId);

    /**
     * 撤回工作流
     */
    void withdraw(String instanceId);
}
```

- [ ] **Step 2: Create implementation skeleton**

```java
package com.adminplus.service.workflow.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
import com.adminplus.service.workflow.WorkflowApprovalService;
import com.adminplus.service.workflow.WorkflowApproverResolver;
import com.adminplus.service.workflow.hook.WorkflowHookService;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowApprovalServiceImpl implements WorkflowApprovalService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowApproverResolver approverResolver;
    private final WorkflowHookService hookService;
    private final ConversionService conversionService;

    @Override
    @Transactional
    public WorkflowInstanceResponse submit(String instanceId, WorkflowStartRequest request) {
        // TODO: Copy implementation from WorkflowInstanceServiceImpl.submit()
        // Lines 94-162 in original file
        throw new BizException("Not implemented - migrate from WorkflowInstanceServiceImpl");
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse approve(String instanceId, ApprovalActionRequest request) {
        return processApproval(instanceId, request, "approved");
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse reject(String instanceId, ApprovalActionRequest request) {
        return processApproval(instanceId, request, "rejected");
    }

    private WorkflowInstanceResponse processApproval(String instanceId, ApprovalActionRequest request, String action) {
        // TODO: Copy implementation from WorkflowInstanceServiceImpl.processApproval()
        // Lines 513-610 in original file
        throw new BizException("Not implemented - migrate from WorkflowInstanceServiceImpl");
    }

    @Override
    @Transactional
    public void cancel(String instanceId) {
        // TODO: Copy implementation from WorkflowInstanceServiceImpl.cancel()
        // Lines 382-429 in original file
    }

    @Override
    @Transactional
    public void withdraw(String instanceId) {
        // TODO: Copy implementation from WorkflowInstanceServiceImpl.withdraw()
        // Lines 433-490 in original file
    }
}
```

**Note:** The actual implementation code is ~400 lines. Copy from `WorkflowInstanceServiceImpl.java` lines 94-610, adapting to use `EntityHelper` and `ServiceAssert`.

- [ ] **Step 3: Migrate code from WorkflowInstanceServiceImpl**

For each method:
1. Copy the method from `WorkflowInstanceServiceImpl`
2. Replace `IllegalArgumentException` with `BizException` using `EntityHelper`/`ServiceAssert`
3. Replace `this.resolveApprovers()` with `approverResolver.resolveApprovers()`
4. Replace direct repository access patterns with helper methods

- [ ] **Step 4: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS (may need to adjust imports and method calls)

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/adminplus/service/workflow/WorkflowApprovalService.java \
        backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowApprovalServiceImpl.java
git commit -m "refactor(workflow): extract WorkflowApprovalService from WorkflowInstanceService"
```

---

### Task 2.5: Create WorkflowRollbackService

**Files:**
- Create: `backend/src/main/java/com/adminplus/service/workflow/WorkflowRollbackService.java`
- Create: `backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowRollbackServiceImpl.java`

- [ ] **Step 1: Create interface**

```java
package com.adminplus.service.workflow;

import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.dto.response.WorkflowNodeResponse;

import java.util.List;

/**
 * 工作流回退服务
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface WorkflowRollbackService {

    /**
     * 回退工作流
     */
    WorkflowInstanceResponse rollback(String instanceId, ApprovalActionRequest request);

    /**
     * 获取可回退的节点列表
     */
    List<WorkflowNodeResponse> getRollbackableNodes(String instanceId);
}
```

- [ ] **Step 2: Create implementation skeleton**

```java
package com.adminplus.service.workflow.impl;

import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.dto.response.WorkflowNodeResponse;
import com.adminplus.pojo.entity.WorkflowApprovalEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.repository.WorkflowApprovalRepository;
import com.adminplus.repository.WorkflowInstanceRepository;
import com.adminplus.repository.WorkflowNodeRepository;
import com.adminplus.service.workflow.WorkflowRollbackService;
import com.adminplus.utils.EntityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowRollbackServiceImpl implements WorkflowRollbackService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final ConversionService conversionService;

    @Override
    @Transactional
    public WorkflowInstanceResponse rollback(String instanceId, ApprovalActionRequest request) {
        // TODO: Migrate from WorkflowInstanceServiceImpl.rollback()
        // Lines 914-1009 in original file
        throw new UnsupportedOperationException("Migrate from WorkflowInstanceServiceImpl");
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowNodeResponse> getRollbackableNodes(String instanceId) {
        // TODO: Migrate from WorkflowInstanceServiceImpl.getRollbackableNodes()
        // Lines 1013-1039 in original file
        return List.of();
    }
}
```

**Note:** Copy rollback logic (~100 lines) from `WorkflowInstanceServiceImpl.java` lines 914-1039.

- [ ] **Step 3: Migrate code and verify compilation**

Run: `cd backend && mvn compile -q`

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/adminplus/service/workflow/WorkflowRollbackService.java \
        backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowRollbackServiceImpl.java
git commit -m "refactor(workflow): extract WorkflowRollbackService from WorkflowInstanceService"
```

---

### Task 2.6: Create WorkflowAddSignService

**Files:**
- Create: `backend/src/main/java/com/adminplus/service/workflow/WorkflowAddSignService.java`
- Create: `backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowAddSignServiceImpl.java`

- [ ] **Step 1: Create interface**

```java
package com.adminplus.service.workflow;

import com.adminplus.pojo.dto.request.AddSignRequest;
import com.adminplus.pojo.dto.response.WorkflowAddSignResponse;

import java.util.List;

/**
 * 工作流加签服务
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface WorkflowAddSignService {

    /**
     * 加签/转办
     */
    WorkflowAddSignResponse addSign(String instanceId, AddSignRequest request);

    /**
     * 获取加签记录
     */
    List<WorkflowAddSignResponse> getAddSignRecords(String instanceId);
}
```

- [ ] **Step 2: Create implementation skeleton**

```java
package com.adminplus.service.workflow.impl;

import com.adminplus.pojo.dto.request.AddSignRequest;
import com.adminplus.pojo.dto.response.WorkflowAddSignResponse;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.WorkflowAddSignEntity;
import com.adminplus.pojo.entity.WorkflowApprovalEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.WorkflowAddSignRepository;
import com.adminplus.repository.WorkflowApprovalRepository;
import com.adminplus.repository.WorkflowInstanceRepository;
import com.adminplus.repository.WorkflowNodeRepository;
import com.adminplus.service.workflow.WorkflowAddSignService;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowAddSignServiceImpl implements WorkflowAddSignService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowAddSignRepository addSignRepository;
    private final UserRepository userRepository;
    private final ConversionService conversionService;

    @Override
    @Transactional
    public WorkflowAddSignResponse addSign(String instanceId, AddSignRequest request) {
        // TODO: Migrate from WorkflowInstanceServiceImpl.addSign()
        // Lines 1160-1237 in original file
        throw new UnsupportedOperationException("Migrate from WorkflowInstanceServiceImpl");
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowAddSignResponse> getAddSignRecords(String instanceId) {
        // TODO: Migrate from WorkflowInstanceServiceImpl.getAddSignRecords()
        // Lines 1339-1354 in original file
        return List.of();
    }
}
```

**Note:** Copy addSign logic (~180 lines) from `WorkflowInstanceServiceImpl.java` lines 1160-1354.

- [ ] **Step 3: Migrate code and verify compilation**

Run: `cd backend && mvn compile -q`

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/adminplus/service/workflow/WorkflowAddSignService.java \
        backend/src/main/java/com/adminplus/service/workflow/impl/WorkflowAddSignServiceImpl.java
git commit -m "refactor(workflow): extract WorkflowAddSignService from WorkflowInstanceService"
```

---

### Task 2.7: Refactor WorkflowInstanceServiceImpl as Coordinator

**Files:**
- Modify: `backend/src/main/java/com/adminplus/service/impl/WorkflowInstanceServiceImpl.java`

- [ ] **Step 1: Inject the new sub-services**

Add the new dependencies:
```java
private final WorkflowDraftService draftService;
private final WorkflowApprovalService approvalService;
private final WorkflowRollbackService rollbackService;
private final WorkflowAddSignService addSignService;
private final WorkflowApproverResolver approverResolver;
private final WorkflowPermissionChecker permissionChecker;
```

- [ ] **Step 2: Delegate methods to sub-services**

Rewrite each method to delegate to the appropriate sub-service:
```java
@Override
public WorkflowInstanceResponse createDraft(WorkflowStartRequest request) {
    return draftService.createDraft(request);
}

@Override
public WorkflowInstanceResponse submit(String instanceId, WorkflowStartRequest request) {
    return approvalService.submit(instanceId, request);
}
// ... etc
```

- [ ] **Step 3: Remove extracted methods**

Delete the methods that have been moved to sub-services.

- [ ] **Step 4: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Run tests**

Run: `cd backend && mvn test -Dtest=WorkflowInstanceServiceTest -q`
Expected: Tests pass (or note any failures to fix)

- [ ] **Step 6: Commit Phase 2**

```bash
git add backend/src/main/java/com/adminplus/service/
git commit -m "refactor(workflow): decompose WorkflowInstanceService into focused services

- Extract WorkflowApproverResolver for approver resolution
- Extract WorkflowPermissionChecker for permission checks
- Extract WorkflowDraftService for draft management
- Extract WorkflowApprovalService for approval flow
- Extract WorkflowRollbackService for rollback handling
- Extract WorkflowAddSignService for add-sign/transfer
- Refactor WorkflowInstanceServiceImpl as coordinator"
```

---

## Phase 3: Extract Common Logic

### Task 3.1: Create PermissionHelper Utility

**Files:**
- Create: `backend/src/main/java/com/adminplus/utils/PermissionHelper.java`

- [ ] **Step 1: Create the utility class**

```java
package com.adminplus.utils;

import com.adminplus.common.exception.BizException;
import com.adminplus.service.DeptService;

import java.util.List;

/**
 * 权限检查助手工具类
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public final class PermissionHelper {

    private PermissionHelper() {}

    /**
     * 检查当前用户是否为资源所有者或管理员
     */
    public static void checkOwnerOrAdmin(String resourceOwnerId, String operation) {
        if (!SecurityUtils.isAdmin()
            && !resourceOwnerId.equals(SecurityUtils.getCurrentUserId())) {
            throw new BizException(403, "无权执行该操作: " + operation);
        }
    }

    public static void checkOwnerOrAdminForView(String resourceOwnerId) {
        checkOwnerOrAdmin(resourceOwnerId, "查看");
    }

    public static void checkOwnerOrAdminForEdit(String resourceOwnerId) {
        checkOwnerOrAdmin(resourceOwnerId, "修改");
    }

    public static void checkOwnerOrAdminForDelete(String resourceOwnerId) {
        checkOwnerOrAdmin(resourceOwnerId, "删除");
    }

    /**
     * 获取当前用户可访问的部门ID列表
     */
    public static List<String> getAccessibleDeptIds(DeptService deptService) {
        if (SecurityUtils.isAdmin()) {
            return List.of();
        }
        String currentDeptId = SecurityUtils.getCurrentUserDeptId();
        if (currentDeptId == null) {
            return List.of();
        }
        return deptService.getDeptAndChildrenIds(currentDeptId);
    }

    /**
     * 判断是否需要按部门过滤
     */
    public static boolean needDeptFilter() {
        return !SecurityUtils.isAdmin()
            && SecurityUtils.getCurrentUserDeptId() != null;
    }
}
```

- [ ] **Step 2: Verify compilation and commit**

---

### Task 3.2: Create Auditable Annotation

**Files:**
- Create: `backend/src/main/java/com/adminplus/common/annotation/Auditable.java`

- [ ] **Step 1: Create the annotation**

```java
package com.adminplus.common.annotation;

import java.lang.annotation.*;

/**
 * 可审计操作注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String module();
    String operation();
    String description();
    boolean includeResult() default false;
}
```

- [ ] **Step 2: Verify compilation and commit**

---

### Task 3.3: Create AuditLogAspect

**Files:**
- Create: `backend/src/main/java/com/adminplus/common/aop/AuditLogAspect.java`

- [ ] **Step 1: Create the aspect**

```java
package com.adminplus.common.aop;

import com.adminplus.common.annotation.Auditable;
import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.request.LogEntry;
import com.adminplus.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final LogService logService;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logSuccess(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            String description = resolveDescription(
                auditable.description(),
                joinPoint,
                auditable.includeResult() ? result : null
            );

            logService.log(LogEntry.operation(
                auditable.module(),
                auditable.operation(),
                description
            ));
        } catch (Exception e) {
            log.warn("审计日志记录失败: {}", e.getMessage());
        }
    }

    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "ex")
    public void logFailure(JoinPoint joinPoint, Auditable auditable, Exception ex) {
        if (ex instanceof BizException) {
            try {
                String description = resolveDescription(auditable.description(), joinPoint, null);
                logService.log(LogEntry.operation(
                    auditable.module(),
                    auditable.operation(),
                    description + " [失败: " + ex.getMessage() + "]"
                ));
            } catch (Exception e) {
                log.warn("审计日志记录失败: {}", e.getMessage());
            }
        }
    }

    private String resolveDescription(String template, JoinPoint joinPoint, Object result) {
        if (template == null || !template.contains("#")) {
            return template;
        }

        StandardEvaluationContext context = new StandardEvaluationContext();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }

        if (result != null) {
            context.setVariable("result", result);
        }

        return parser.parseExpression(template, new TemplateParserContext())
            .getValue(context, String.class);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/adminplus/common/aop/AuditLogAspect.java
git commit -m "feat(aop): add AuditLogAspect for automatic audit logging"
```

---

### Task 3.4: Enhance WorkflowStatus Enum

**Files:**
- Modify: `backend/src/main/java/com/adminplus/enums/WorkflowStatus.java`

- [ ] **Step 1: Read current WorkflowStatus implementation**

Run: Read file to see current structure

- [ ] **Step 2: Add status behavior methods**

Add these methods to the existing enum:

```java
// Add to existing WorkflowStatus enum:

public boolean canSubmit() {
    return this == DRAFT || this == RUNNING;
}

public boolean canApprove() {
    return this == RUNNING;
}

public boolean canReject() {
    return this == RUNNING;
}

public boolean canCancel() {
    return this == DRAFT || this == RUNNING;
}

public boolean canWithdraw() {
    return this == DRAFT || this == REJECTED;
}

public boolean canRollback() {
    return this == RUNNING;
}

public boolean canUpdateDraft() {
    return this == DRAFT;
}

public boolean canDeleteDraft() {
    return this == DRAFT;
}

public boolean isFinished() {
    return this == APPROVED || this == REJECTED || this == CANCELLED;
}

/**
 * 从字符串解析状态
 */
public static WorkflowStatus fromCode(String code) {
    if (code == null) {
        return null;
    }
    for (WorkflowStatus status : values()) {
        if (status.code.equalsIgnoreCase(code)
            || status.name().equalsIgnoreCase(code)) {
            return status;
        }
    }
    throw new IllegalArgumentException("未知工作流状态: " + code);
}

/**
 * 判断是否为有效状态码
 */
public static boolean isValidCode(String code) {
    try {
        fromCode(code);
        return true;
    } catch (IllegalArgumentException e) {
        return false;
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/adminplus/enums/WorkflowStatus.java
git commit -m "feat(workflow): add status behavior methods to WorkflowStatus enum"
```

---

### Task 3.5: Apply PermissionHelper to Services

**Files:**
- Modify: `backend/src/main/java/com/adminplus/service/impl/FileServiceImpl.java`

- [ ] **Step 1: Replace permission checks in FileServiceImpl**

Find patterns like:
```java
String currentUserId = SecurityUtils.getCurrentUserId();
boolean isAdmin = SecurityUtils.isAdmin();
if (!isAdmin && !fileEntity.getCreateUser().equals(currentUserId)) {
    throw new BizException("无权删除此文件");
}
```

Replace with:
```java
PermissionHelper.checkOwnerOrAdminForDelete(fileEntity.getCreateUser());
```

- [ ] **Step 2: Verify compilation and commit**

---

### Task 3.6: Apply @Auditable to Services

**Files:**
- Modify: Multiple service implementation files

- [ ] **Step 1: Add @Auditable to UserServiceImpl CRUD methods**

- [ ] **Step 2: Add @Auditable to RoleServiceImpl CRUD methods**

- [ ] **Step 3: Add @Auditable to DeptServiceImpl CRUD methods**

- [ ] **Step 4: Remove manual logService.log() calls**

- [ ] **Step 5: Verify and commit**

---

## Phase 4: Centralize XSS Handling

### Task 4.1: Create XssSanitizer Utility

**Files:**
- Create: `backend/src/main/java/com/adminplus/utils/XssSanitizer.java`

- [ ] **Step 1: Create the utility class**

```java
package com.adminplus.utils;

import java.util.Arrays;
import java.util.List;

/**
 * XSS 清洗工具类
 * 供 Converter 使用，统一 XSS 处理逻辑
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public final class XssSanitizer {

    private XssSanitizer() {}

    private static final List<String> DEFAULT_SKIP_FIELDS =
        List.of("password", "username", "id", "code", "key", "token", "path", "url");

    /**
     * 清洗字符串（返回新字符串）
     */
    public static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return XssUtils.escape(value);
    }

    /**
     * 清洗字符串（null 安全，返回 null）
     */
    public static String sanitizeOrNull(String value) {
        if (value == null) {
            return null;
        }
        return XssUtils.escape(value);
    }

    /**
     * 批量清洗字符串数组
     */
    public static String[] sanitize(String[] values) {
        if (values == null) {
            return null;
        }
        return Arrays.stream(values)
            .map(XssSanitizer::sanitize)
            .toArray(String[]::new);
    }

    /**
     * 判断字段是否需要跳过 XSS 处理
     */
    public static boolean shouldSkip(String fieldName) {
        return DEFAULT_SKIP_FIELDS.contains(fieldName);
    }

    /**
     * 判断字段是否需要跳过 XSS 处理（带自定义跳过列表）
     */
    public static boolean shouldSkip(String fieldName, List<String> additionalSkipFields) {
        return DEFAULT_SKIP_FIELDS.contains(fieldName)
            || (additionalSkipFields != null && additionalSkipFields.contains(fieldName));
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/adminplus/utils/XssSanitizer.java
git commit -m "feat(utils): add XssSanitizer utility for Converter XSS handling"
```

---

### Task 4.2: Enhance Request Converters with XSS

**Files:**
- Modify: `backend/src/main/java/com/adminplus/converter/role/RoleCreateRequestConverter.java`
- Modify: `backend/src/main/java/com/adminplus/converter/menu/MenuCreateRequestConverter.java`
- Modify: `backend/src/main/java/com/adminplus/converter/dept/DeptConverter.java`
- Modify: Multiple other converters

- [ ] **Step 1: Enhance RoleCreateRequestConverter**

```java
@Override
public RoleEntity convert(RoleCreateRequest source) {
    RoleEntity entity = new RoleEntity();
    entity.setCode(source.code());
    entity.setName(XssSanitizer.sanitize(source.name()));
    entity.setDescription(XssSanitizer.sanitizeOrNull(source.description()));
    entity.setDataScope(source.dataScope());
    entity.setStatus(source.status() != null ? source.status() : 1);
    entity.setSortOrder(source.sortOrder());
    return entity;
}
```

- [ ] **Step 2: Apply same pattern to other converters**

- [ ] **Step 3: Verify compilation and commit**

---

### Task 4.3: Create UserCreateRequestConverter

**Files:**
- Create: `backend/src/main/java/com/adminplus/converter/user/UserCreateRequestConverter.java`

- [ ] **Step 1: Create the converter**

```java
package com.adminplus.converter.user;

import com.adminplus.pojo.dto.request.UserCreateRequest;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.utils.XssSanitizer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserCreateRequestConverter implements Converter<UserCreateRequest, UserEntity> {

    @Override
    public UserEntity convert(UserCreateRequest source) {
        UserEntity entity = new UserEntity();
        entity.setUsername(source.username());  // 不需要 XSS
        entity.setNickname(XssSanitizer.sanitizeOrNull(source.nickname()));
        entity.setEmail(XssSanitizer.sanitizeOrNull(source.email()));
        entity.setPhone(XssSanitizer.sanitizeOrNull(source.phone()));
        entity.setAvatar(source.avatar());
        entity.setDeptId(source.deptId());
        // 密码和状态在 Service 中处理
        return entity;
    }
}
```

- [ ] **Step 2: Verify compilation and commit**

---

### Task 4.4: Update UserServiceImpl to Use Converter

**Files:**
- Modify: `backend/src/main/java/com/adminplus/service/impl/UserServiceImpl.java`

- [ ] **Step 1: Replace manual entity creation with converter**

- [ ] **Step 2: Remove manual XSS calls**

- [ ] **Step 3: Verify compilation and commit**

---

## Final Verification

### Task 5.1: Run Full Test Suite

- [ ] **Step 1: Run all backend tests**

Run: `cd backend && mvn test`
Expected: All tests pass

- [ ] **Step 2: Run application to verify startup**

Run: `cd backend && mvn spring-boot:run`
Expected: Application starts successfully

---

### Task 5.2: Create Summary Commit

- [ ] **Step 1: Create final summary commit**

```bash
git add -A
git commit -m "refactor(service): complete service layer optimization

Phase 1: Unified exception handling
- Add ServiceAssert utility class
- Extend EntityHelper with new methods
- Replace IllegalArgumentException with BizException across services

Phase 2: Decompose WorkflowInstanceService
- Extract WorkflowApproverResolver for approver resolution
- Extract WorkflowPermissionChecker for permission checks
- Extract WorkflowDraftService, WorkflowApprovalService
- Extract WorkflowRollbackService, WorkflowAddSignService

Phase 3: Extract common logic
- Add PermissionHelper for owner/admin checks
- Add @Auditable annotation and AuditLogAspect
- Enhance WorkflowStatus with behavior methods

Phase 4: Centralize XSS handling
- Add XssSanitizer utility
- Enhance Request converters with XSS protection
- Create UserCreateRequestConverter"
```

---

## Acceptance Criteria

After completing all tasks, verify:

- [ ] No `IllegalArgumentException` for business errors in Service layer
- [ ] `WorkflowInstanceServiceImpl` < 350 lines
- [ ] All new sub-services compile and inject correctly
- [ ] `PermissionHelper` used in at least 3 services
- [ ] `@Auditable` applied to CRUD operations in main services
- [ ] No direct `XssUtils.escape*` calls in Service layer
- [ ] All tests pass
- [ ] Application starts successfully