---
name: Service Layer Optimization Design
description: Service层代码质量优化设计 - 四步骤渐进式重构方案
type: project
---

# Service 层代码质量优化设计

## 背景

AdminPlus 项目 Service 层存在以下代码质量问题，影响可维护性和一致性：

| 问题类别 | 具体表现 | 影响范围 |
|---------|---------|---------|
| 异常处理不一致 | 50处 `IllegalArgumentException`，25处 `BizException` | 7个服务文件 |
| 大型服务类 | `WorkflowInstanceServiceImpl` 1415行，职责混杂 | 1个服务文件 |
| EntityHelper使用不一致 | 部分服务使用工具类，部分直接 `orElseThrow` | 多个服务 |
| XSS处理分散 | 30+处手动调用 `XssUtils.escape()` | 6个服务文件 |
| 权限检查重复 | 多处重复 `isAdmin` + `currentUserId` 检查 | 多个服务 |
| 审计日志分散 | 每个操作手动调用 `logService.log()` | 所有服务 |

## 优化目标

- 统一异常处理风格，提升 API 响应一致性
- 拆分大型服务，提升可测试性和可维护性
- 抽取公共逻辑，减少重复代码
- 统一 XSS 处理位置，降低遗漏风险

## 设计原则

1. **渐进式**：分步骤实施，每步可独立验证
2. **向后兼容**：不改变公共 API 签名
3. **最小侵入**：优先使用工具类和 AOP，避免框架级改动
4. **可回退**：每个步骤独立提交，必要时可单独回滚

---

## 步骤一：统一异常处理

### 1.1 扩展 EntityHelper 工具类

**文件位置**：`backend/src/main/java/com/adminplus/utils/EntityHelper.java`

**新增方法**：

```java
public final class EntityHelper {
    
    // 现有方法保持不变
    public static <T> T findByIdOrThrow(
        Function<String, Optional<T>> finder, 
        String id, 
        String message
    );
    
    // 新增：支持自定义异常消息模板
    public static <T> T findByIdOrThrow(
        Function<String, Optional<T>> finder,
        String id,
        String messageTemplate,
        Object... args
    );
    
    // 新增：支持软删除实体的查找（常见模式封装）
    public static <T> T findActiveById(
        Function<String, Optional<T>> finder,
        String id,
        String entityName
    );
    
    // 新增：批量查找，缺失时抛出
    public static <T> List<T> findAllByIdsOrThrow(
        Function<List<String>, List<T>> finder,
        List<String> ids,
        String entityName
    );
    
    // 新增：查找并返回 Optional（不抛异常）
    public static <T> Optional<T> findById(
        Function<String, Optional<T>> finder,
        String id
    );
}
```

**使用示例**：

```java
// 改动前
WorkflowDefinitionEntity definition = definitionRepository.findById(definitionId)
    .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在"));

// 改动后
WorkflowDefinitionEntity definition = EntityHelper.findByIdOrThrow(
    definitionRepository::findById, 
    definitionId, 
    "工作流定义不存在"
);
```

### 1.2 创建 ServiceAssert 工具类

**新文件位置**：`backend/src/main/java/com/adminplus/utils/ServiceAssert.java`

```java
package com.adminplus.utils;

import com.adminplus.common.exception.BizException;

/**
 * 服务层断言工具类
 * 用于业务规则校验，统一抛出 BizException
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

### 1.3 异常类型统一策略

| 场景 | 当前异常 | 改为 BizException | HTTP状态码语义 |
|-----|---------|------------------|--------------|
| 实体不存在（查询） | `IllegalArgumentException` | `BizException(404, "...不存在")` | 404 |
| 业务规则违反 | `IllegalArgumentException` | `BizException(400, "...")` | 400 |
| 权限不足 | `IllegalArgumentException` | `BizException(403, "无权操作")` | 403 |
| 状态不正确 | `IllegalArgumentException` | `BizException(400, "状态不允许")` | 400 |

**改动清单**：

| 服务文件 | 需改动的 `IllegalArgumentException` 数量 |
|---------|---------------------------------------|
| WorkflowInstanceServiceImpl | 28处 |
| WorkflowStateMachineServiceImpl | 10处 |
| WorkflowDefinitionServiceImpl | 4处 |
| WorkflowUrgeServiceImpl | 4处 |
| 其他（6个文件） | 8处 |

---

## 步骤二：拆分大型服务

### 2.1 拆分 WorkflowInstanceServiceImpl

**目标**：将 1415行的服务拆分为职责清晰的小服务

**拆分后目录结构**：

```
service/
├── workflow/
│   ├── WorkflowInstanceService.java           # 主服务接口（保持不变）
│   ├── WorkflowDraftService.java              # 新增：草稿管理接口
│   ├── WorkflowApprovalService.java           # 新增：审批处理接口
│   ├── WorkflowRollbackService.java           # 新增：回退处理接口
│   ├── WorkflowAddSignService.java            # 新增：加签转办接口
│   ├── WorkflowApproverResolver.java          # 新增：审批人解析接口
│   └── impl/
│       ├── WorkflowInstanceServiceImpl.java   # 重构：协调者角色（约300行）
│       ├── WorkflowDraftServiceImpl.java      # 新增
│       ├── WorkflowApprovalServiceImpl.java   # 新增
│       ├── WorkflowRollbackServiceImpl.java   # 新增
│       ├── WorkflowAddSignServiceImpl.java    # 新增
│       ├── WorkflowApproverResolverImpl.java  # 新增
│       └── WorkflowPermissionChecker.java     # 新增（无状态工具类）
```

### 2.2 各服务职责划分

| 服务 | 职责描述 | 原方法迁移 | 预估行数 |
|-----|---------|----------|---------|
| **WorkflowInstanceServiceImpl** | 协调各子服务、公共查询 | `getDetail`, `getMyWorkflows`, `getPendingApprovals`, `countPendingApprovals`, `getApprovals` | ~300 |
| **WorkflowDraftService** | 草稿生命周期管理 | `createDraft`, `updateDraft`, `deleteDraft`, `getDraftDetail` | ~100 |
| **WorkflowApprovalService** | 审批流转逻辑 | `start`, `submit`, `approve`, `reject`, `cancel`, `withdraw`, `processApproval`, `moveToNextNode`, `createApprovalRecords` | ~350 |
| **WorkflowRollbackService** | 回退处理 | `rollback`, `getRollbackableNodes`, `findPreviousNodeId`, `cleanupAndCreateApprovals` | ~150 |
| **WorkflowAddSignService** | 加签/转办 | `addSign`, `getAddSignRecords`, `handleTransfer`, `handleAddSign` | ~200 |
| **WorkflowApproverResolver** | 审批人解析策略 | `resolveApprovers`, `batchGetApproverNames` | ~100 |
| **WorkflowPermissionChecker** | 权限检查工具 | `checkViewAccess`, `canUserApprove`, `buildOperationPermissions` | ~80 |

### 2.3 服务间协作模式

**主服务作为协调者**：

```java
@Service
@RequiredArgsConstructor
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService {
    
    private final WorkflowDraftService draftService;
    private final WorkflowApprovalService approvalService;
    private final WorkflowRollbackService rollbackService;
    private final WorkflowAddSignService addSignService;
    private final WorkflowApproverResolver approverResolver;
    private final WorkflowPermissionChecker permissionChecker;
    
    // 协调方法：组合多个子服务
    @Override
    public WorkflowInstanceResponse start(WorkflowStartRequest request) {
        WorkflowInstanceResponse draft = draftService.createDraft(request);
        return approvalService.submit(draft.id(), request);
    }
    
    // 查询方法：直接调用 repository
    @Override
    public WorkflowDetailResponse getDetail(String instanceId) {
        // 公共查询逻辑保持在这里
    }
}
```

**子服务通过接口依赖**：

```java
@Service
@RequiredArgsConstructor
public class WorkflowApprovalServiceImpl implements WorkflowApprovalService {
    
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowApproverResolver approverResolver;
    private final WorkflowHookService hookService;
    private final WorkflowCcService ccService;
    
    // 不直接依赖 WorkflowInstanceService，避免循环依赖
}
```

### 2.4 循环依赖处理策略

使用 `@Lazy` 注解解决潜在循环依赖：

```java
@Service
public class WorkflowApprovalServiceImpl implements WorkflowApprovalService {
    
    // 如需回调主服务，使用 Lazy 注入
    @Lazy
    @Autowired
    private WorkflowInstanceService instanceService;
}
```

---

## 步骤三：抽取公共逻辑

### 3.1 PermissionHelper 工具类

**新文件位置**：`backend/src/main/java/com/adminplus/utils/PermissionHelper.java`

```java
package com.adminplus.utils;

import com.adminplus.common.exception.BizException;
import com.adminplus.service.DeptService;
import java.util.List;

/**
 * 权限检查助手工具类
 * 统一处理"管理员跳过/普通用户受限"的常见模式
 */
public final class PermissionHelper {
    
    private PermissionHelper() {}
    
    /**
     * 检查当前用户是否为资源所有者或管理员
     * 非管理员只能操作自己创建的资源
     */
    public static void checkOwnerOrAdmin(String resourceOwnerId, String operation) {
        if (!SecurityUtils.isAdmin() 
            && !resourceOwnerId.equals(SecurityUtils.getCurrentUserId())) {
            throw new BizException(403, "无权执行该操作: " + operation);
        }
    }
    
    /**
     * 检查当前用户是否有权查看资源
     * 非管理员只能查看自己创建的资源
     */
    public static void checkOwnerOrAdminForView(String resourceOwnerId) {
        checkOwnerOrAdmin(resourceOwnerId, "查看");
    }
    
    /**
     * 检查当前用户是否有权修改资源
     */
    public static void checkOwnerOrAdminForEdit(String resourceOwnerId) {
        checkOwnerOrAdmin(resourceOwnerId, "修改");
    }
    
    /**
     * 检查当前用户是否有权删除资源
     */
    public static void checkOwnerOrAdminForDelete(String resourceOwnerId) {
        checkOwnerOrAdmin(resourceOwnerId, "删除");
    }
    
    /**
     * 获取当前用户可访问的部门ID列表
     * - 管理员：返回空列表（表示全部可访问）
     * - 普通用户：返回本部门及子部门ID
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

**使用示例**：

```java
// FileServiceImpl 改动前
String currentUserId = SecurityUtils.getCurrentUserId();
boolean isAdmin = SecurityUtils.isAdmin();
if (!isAdmin && !fileEntity.getCreateUser().equals(currentUserId)) {
    throw new BizException("无权删除此文件");
}

// 改动后
PermissionHelper.checkOwnerOrAdminForDelete(fileEntity.getCreateUser());
```

### 3.2 审计日志 AOP

**新注解位置**：`backend/src/main/java/com/adminplus/common/annotation/Auditable.java`

```java
package com.adminplus.common.annotation;

import java.lang.annotation.*;

/**
 * 可审计操作注解
 * 标记在 Service 方法上，自动记录审计日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    
    /**
     * 模块名称（如 "user", "role", "workflow"）
     */
    String module();
    
    /**
     * 操作类型（如 "create", "update", "delete"）
     */
    String operation();
    
    /**
     * 操作描述（支持 SpEL 表达式）
     * 示例: "创建用户: #{#req.username}"
     */
    String description();
    
    /**
     * 是否包含操作结果
     */
    boolean includeResult() default false;
}
```

**AOP 切面位置**：`backend/src/main/java/com/adminplus/common/aop/AuditLogAspect.java`

```java
package com.adminplus.common.aop;

import com.adminplus.common.annotation.Auditable;
import com.adminplus.pojo.dto.request.LogEntry;
import com.adminplus.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {
    
    private final LogService logService;
    private final ExpressionParser parser = new SpelExpressionParser();
    
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
        // 可选：记录失败操作（仅记录 BizException）
        if (ex instanceof com.adminplus.common.exception.BizException) {
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
    
    /**
     * 解析 SpEL 表达式获取动态描述
     */
    private String resolveDescription(String template, JoinPoint joinPoint, Object result) {
        if (template == null || !template.contains("#")) {
            return template;
        }
        
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // 绑定方法参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        
        for (int i = 0; i < parameters.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }
        
        // 绑定返回值
        if (result != null) {
            context.setVariable("result", result);
        }
        
        return parser.parseExpression(template, new org.springframework.expression.common.TemplateParserContext())
            .getValue(context, String.class);
    }
}
```

**使用示例**：

```java
// UserServiceImpl 改动前
@Override
@Transactional
public UserResponse createUser(UserCreateRequest req) {
    // ... 业务逻辑 ...
    user = userRepository.save(user);
    
    logService.log(LogEntry.operation("user", "create", "创建用户: " + user.getUsername()));
    
    return conversionService.convert(user, UserResponse.class);
}

// 改动后
@Auditable(module = "user", operation = "create", description = "创建用户: #{#req.username}")
@Override
@Transactional
public UserResponse createUser(UserCreateRequest req) {
    // ... 业务逻辑 ...
    return conversionService.convert(user, UserResponse.class);
}
```

### 3.3 WorkflowStatus 枚举增强

**文件位置**：`backend/src/main/java/com/adminplus/enums/WorkflowStatus.java`

```java
package com.adminplus.enums;

import lombok.Getter;

/**
 * 工作流状态枚举
 * 封装状态行为，替代分散的状态检查方法
 */
@Getter
public enum WorkflowStatus {
    
    DRAFT("draft", true, false, false, true),
    RUNNING("running", false, true, false, true),
    APPROVED("approved", false, false, true, false),
    REJECTED("rejected", false, false, true, false),
    CANCELLED("cancelled", false, false, true, false);
    
    private final String code;
    private final boolean draft;
    private final boolean running;
    private final boolean finished;
    private final boolean cancellable;
    
    WorkflowStatus(String code, boolean draft, boolean running, boolean finished, boolean cancellable) {
        this.code = code;
        this.draft = draft;
        this.running = running;
        this.finished = finished;
        this.cancellable = cancellable;
    }
    
    // 状态行为判断方法
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
        return this.cancellable;
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
        return this.finished;
    }
    
    // 从字符串解析
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
    
    // 判断是否为有效状态码
    public static boolean isValidCode(String code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
```

**使用示例**：

```java
// 改动前
if (!instance.isRunning()) {
    throw new IllegalArgumentException("只有进行中的工作流可以审批");
}

// 改动后
WorkflowStatus status = WorkflowStatus.fromCode(instance.getStatus());
ServiceAssert.isTrue(status.canApprove(), "只有进行中的工作流可以审批");
```

---

## 步骤四：统一 XSS 处理

**目标**：将 XSS 转义逻辑从 Service 层移至 Converter 层，消除 30+ 处重复代码

**现状**：项目已有成熟的 Converter 体系（`backend/src/main/java/com/adminplus/converter/`），按模块组织：
- `user/UserConverter.java` - Entity → Response
- `role/RoleCreateRequestConverter.java` - Request → Entity  
- `dept/DeptConverter.java`、`config/ConfigConverter.java` 等

**策略**：在现有 Converter 基础上增强，而非新建 converter 包

### 4.1 XssSanitizer 工具类

**新文件位置**：`backend/src/main/java/com/adminplus/utils/XssSanitizer.java`

```java
package com.adminplus.utils;

import java.util.List;

/**
 * XSS 清洗工具类
 * 供 Converter 使用，统一 XSS 处理逻辑
 */
public final class XssSanitizer {
    
    private XssSanitizer() {}
    
    // 默认不需要 XSS 处理的字段名
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
        return java.util.Arrays.stream(values)
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

### 4.2 改造现有 RequestConverter

**改动策略**：在现有 `XxxCreateRequestConverter` 和 `XxxUpdateRequestConverter` 中使用 `XssSanitizer`

**示例：RoleCreateRequestConverter 改造**

```java
// 改动前
@Component
public class RoleCreateRequestConverter implements Converter<RoleCreateRequest, RoleEntity> {
    
    @Override
    public RoleEntity convert(RoleCreateRequest source) {
        RoleEntity entity = new RoleEntity();
        entity.setCode(source.code());
        entity.setName(source.name());
        entity.setDescription(source.description());
        // ...
        return entity;
    }
}

// 改动后
@Component
public class RoleCreateRequestConverter implements Converter<RoleCreateRequest, RoleEntity> {
    
    @Override
    public RoleEntity convert(RoleCreateRequest source) {
        RoleEntity entity = new RoleEntity();
        entity.setCode(source.code());  // code 不需要 XSS
        entity.setName(XssSanitizer.sanitize(source.name()));
        entity.setDescription(XssSanitizer.sanitizeOrNull(source.description()));
        // ...
        return entity;
    }
}
```

### 4.3 改动清单

需要改造的 RequestConverter（共 11 个）：

| 文件路径 | 需处理字段 |
|---------|----------|
| `converter/role/RoleCreateRequestConverter.java` | name, description |
| `converter/menu/MenuCreateRequestConverter.java` | name, path, component, permKey, icon |
| `converter/dept/DeptConverter.java` | name, code, leader |
| `converter/config/ConfigCreateRequestConverter.java` | name, key, value, description |
| `converter/workflowdefinition/WorkflowDefinitionRequestConverter.java` | definitionName, definitionKey, description |
| `converter/workflownodehook/WorkflowNodeHookRequestConverter.java` | hookName, hookExpression |
| `converter/formtemplate/FormTemplateRequestConverter.java` | templateName, templateContent |
| `converter/dict/DictCreateRequestConverter.java` | dictName, dictCode |
| `converter/config/ConfigGroupCreateRequestConverter.java` | name, code, description |
| `converter/user/UserConverter.java` | (暂不改动，UserCreateRequest 在 Service 中处理) |
| `converter/profile/ProfileConverter.java` | (暂不改动) |

### 4.4 Service 层改动示例

```java
// UserServiceImpl 改动前
@Override
@Transactional
public UserResponse createUser(UserCreateRequest req) {
    if (userRepository.existsByUsername(req.username())) {
        throw new BizException("用户名已存在");
    }
    
    var user = new UserEntity();
    user.setUsername(req.username());
    user.setPassword(passwordEncoder.encode(req.password()));
    user.setNickname(XssUtils.escapeOrNull(req.nickname()));  // 手动 XSS
    user.setEmail(XssUtils.escapeOrNull(req.email()));        // 手动 XSS
    user.setPhone(XssUtils.escapeOrNull(req.phone()));        // 手动 XSS
    user.setAvatar(req.avatar());
    user.setDeptId(req.deptId());
    user.setStatus(UserStatus.ENABLED.getCode());
    
    user = userRepository.save(user);
    return conversionService.convert(user, UserResponse.class);
}

// 改动后（使用 Converter + XssSanitizer）
@Auditable(module = "user", operation = "create", description = "创建用户: #{#req.username}")
@Override
@Transactional
public UserResponse createUser(UserCreateRequest req) {
    ServiceAssert.notExists(
        userRepository.existsByUsername(req.username()),
        "用户名已存在"
    );
    
    // Converter 处理 XSS，Service 层专注业务逻辑
    UserEntity user = conversionService.convert(req, UserEntity.class);
    user.setPassword(passwordEncoder.encode(req.password()));
    user.setStatus(UserStatus.ENABLED.getCode());
    
    user = userRepository.save(user);
    return conversionService.convert(user, UserResponse.class);
}
```

### 4.5 需新增的 Converter

当前部分 Request → Entity 转换在 Service 中直接 new Entity，需新增 Converter：

| 新增文件 | 说明 |
|---------|-----|
| `converter/user/UserCreateRequestConverter.java` | UserCreateRequest → UserEntity |
| `converter/user/UserUpdateRequestConverter.java` | UserUpdateRequest → UserEntity（部分更新） |
| `converter/dept/DeptCreateRequestConverter.java` | DeptCreateRequest → DeptEntity |
| `converter/dept/DeptUpdateRequestConverter.java` | DeptUpdateRequest → DeptEntity（部分更新） |

**注意**：UserUpdateRequest 和 DeptUpdateRequest 是部分更新场景，Converter 只设置非 null 字段，实际合并逻辑在 Service 中完成。
        }
        
        // 处理普通类（Entity 等）
        return sanitizeObject(source, clazz, skipFields);
    }
    
    /**
     * 清洗 Record 类型
     */
    private S sanitizeRecord(S source, Class<?> clazz, List<String> skipFields) {
        RecordComponent[] components = clazz.getRecordComponents();
        Object[] values = new Object[components.length];
        
        try {
            for (int i = 0; i < components.length; i++) {
                RecordComponent comp = components[i];
                Field field = clazz.getDeclaredField(comp.getName());
                field.setAccessible(true);
                Object value = field.get(source);
                
                if (comp.getType() == String.class 
                    && value != null 
                    && !skipFields.contains(comp.getName())) {
                    values[i] = XssUtils.escape((String) value);
                } else if (comp.getType() == String.class 
                    && value != null 
                    && "nickname".equals(comp.getName()) 
                    || "email".equals(comp.getName()) 
                    || "phone".equals(comp.getName())) {
                    // nullable 字段使用 escapeOrNull
                    values[i] = XssUtils.escapeOrNull((String) value);
                } else {
                    values[i] = value;
                }
            }
            
            // 通过 canonical constructor 创建新 record 实例
            return clazz.getDeclaredConstructor(Arrays.stream(components)
                .map(RecordComponent::getType)
                .toArray(Class[][]::new))
                .newInstance(values);
                
        } catch (Exception e) {
            // 清洗失败则返回原对象
            return source;
        }
    }
    
    /**
     * 清洗普通类类型
     */
    private S sanitizeObject(S source, Class<?> clazz, List<String> skipFields) {
        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType() == String.class 
                    && !skipFields.contains(field.getName())) {
                    field.setAccessible(true);
                    Object value = field.get(source);
                    if (value != null) {
                        field.set(source, XssUtils.escape((String) value));
                    }
                }
            }
            return source;
        } catch (Exception e) {
            return source;
        }
    }
}
```

### 4.2 具体 Converter 实现

**示例：UserCreateRequest → UserEntity Converter**

**文件位置**：`backend/src/main/java/com/adminplus/common/converter/UserCreateRequestToEntityConverter.java`

```java
package com.adminplus.common.converter;

import com.adminplus.pojo.dto.request.UserCreateRequest;
import com.adminplus.pojo.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserCreateRequestToEntityConverter extends XssSafeConverter<UserCreateRequest, UserEntity> {
    
    @Override
    protected UserEntity doConvert(UserCreateRequest req) {
        UserEntity entity = new UserEntity();
        // XSS 已由基类处理，此处直接赋值
        entity.setUsername(req.username());
        entity.setNickname(req.nickname());
        entity.setEmail(req.email());
        entity.setPhone(req.phone());
        entity.setAvatar(req.avatar());
        entity.setDeptId(req.deptId());
        // 密码需要单独加密，不在此处理
        return entity;
    }
    
    @Override
    protected List<String> skipXssFields() {
        // username 是登录凭证，不转义
        return List.of("username", "password", "id");
    }
}
```

### 4.3 Converter 注册配置

**文件位置**：`backend/src/main/java/com/adminplus/common/config/ConverterConfig.java`

```java
@Configuration
public class ConverterConfig implements WebMvcConfigurer {
    
    @Autowired
    private List<Converter<?, ?>> converters;
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 注册所有 Converter Bean
        converters.forEach(registry::addConverter);
    }
}
```

### 4.4 Service 层改动示例

```java
// UserServiceImpl 改动前
@Override
@Transactional
public UserResponse createUser(UserCreateRequest req) {
    if (userRepository.existsByUsername(req.username())) {
        throw new BizException("用户名已存在");
    }
    
    var user = new UserEntity();
    user.setUsername(req.username());
    user.setPassword(passwordEncoder.encode(req.password()));
    user.setNickname(XssUtils.escapeOrNull(req.nickname()));  // 手动 XSS
    user.setEmail(XssUtils.escapeOrNull(req.email()));        // 手动 XSS
    user.setPhone(XssUtils.escapeOrNull(req.phone()));        // 手动 XSS
    user.setAvatar(req.avatar());
    user.setDeptId(req.deptId());
    user.setStatus(UserStatus.ENABLED.getCode());
    
    user = userRepository.save(user);
    return conversionService.convert(user, UserResponse.class);
}

// 改动后（Converter 自动 XSS）
@Auditable(module = "user", operation = "create", description = "创建用户: #{#req.username}")
@Override
@Transactional
public UserResponse createUser(UserCreateRequest req) {
    ServiceAssert.notExists(
        userRepository.existsByUsername(req.username()),
        "用户名已存在"
    );
    
    // Converter 自动处理 XSS
    UserEntity user = conversionService.convert(req, UserEntity.class);
    user.setPassword(passwordEncoder.encode(req.password()));
    user.setStatus(UserStatus.ENABLED.getCode());
    
    user = userRepository.save(user);
    return conversionService.convert(user, UserResponse.class);
}
```

---

## 实施计划

### 执行顺序

| 步骤 | 内容 | 预估改动文件数 | 依赖关系 |
|-----|-----|--------------|---------|
| **步骤一** | 统一异常处理 | 新增1文件，修改7文件 | 无依赖，可独立执行 |
| **步骤二** | 拆分 WorkflowInstanceService | 新增7文件，修改1文件 | 依赖步骤一（使用 ServiceAssert） |
| **步骤三** | 抽取公共逻辑 | 新增3文件，修改约10文件 | 可与步骤二并行 |
| **步骤四** | 统一 XSS 处理 | 新增3文件，修改11 Converter | 可在步骤三完成后执行 |

### 提交策略

每个步骤独立提交，提交信息格式：

```
refactor(service): step 1 - unify exception handling
- Add ServiceAssert utility class
- Extend EntityHelper with more methods
- Replace IllegalArgumentException with BizException in workflow services
```

### 回滚策略

如果某个步骤引入问题：
1. 通过 git 回滚该步骤的提交
2. 其他步骤不受影响（独立提交）

---

## 验收标准

### 步骤一验收

- [ ] 所有 Service 中无 `IllegalArgumentException` 用于业务错误
- [ ] `EntityHelper.findByIdOrThrow` 使用率 > 90%
- [ ] `ServiceAssert` 在至少 5 个服务中使用

### 步骤二验收

- [ ] `WorkflowInstanceServiceImpl` 行数 < 350
- [ ] 所有拆分服务单测通过
- [ ] 无循环依赖警告

### 步骤三验收

- [ ] `PermissionHelper` 替代至少 10 处重复权限检查
- [ ] `@Auditable` 覆盖所有 CRUD 操作
- [ ] `WorkflowStatus` 枚举包含所有状态行为方法

### 步骤四验收

- [ ] Service 层无直接调用 `XssUtils.escape*`
- [ ] 所有 Request → Entity 转换有对应的 Converter
- [ ] XSS 测试用例通过（输入恶意脚本，输出被转义）

---

## 风险评估

| 风险 | 可能性 | 影响 | 缓解措施 |
|-----|-------|-----|---------|
| Converter XSS 遗漏字段 | 中 | 安全风险 | 新增 XSS 测试用例覆盖所有 Converter |
| 拆分服务循环依赖 | 低 | 启动失败 | 使用 `@Lazy` 注解或重构接口 |
| AOP 审计日志遗漏 | 低 | 审计缺失 | 代码审查 + 测试验证 |
| EntityHelper 泛型签名复杂 | 低 | 使用困难 | 提供详细文档和示例 |

---

## 附录：改动文件清单

### 新增文件（共 18 个）

| 步骤 | 文件路径 | 说明 |
|-----|---------|-----|
| 1 | `utils/ServiceAssert.java` | 断言工具类 |
| 2 | `service/workflow/WorkflowDraftService.java` | 草稿服务接口 |
| 2 | `service/workflow/WorkflowApprovalService.java` | 审批服务接口 |
| 2 | `service/workflow/WorkflowRollbackService.java` | 回退服务接口 |
| 2 | `service/workflow/WorkflowAddSignService.java` | 加签服务接口 |
| 2 | `service/workflow/WorkflowApproverResolver.java` | 审批人解析接口 |
| 2 | `service/workflow/impl/WorkflowDraftServiceImpl.java` | 草稿服务实现 |
| 2 | `service/workflow/impl/WorkflowApprovalServiceImpl.java` | 审批服务实现 |
| 2 | `service/workflow/impl/WorkflowRollbackServiceImpl.java` | 回退服务实现 |
| 2 | `service/workflow/impl/WorkflowAddSignServiceImpl.java` | 加签服务实现 |
| 2 | `service/workflow/impl/WorkflowApproverResolverImpl.java` | 审批人解析实现 |
| 2 | `service/workflow/impl/WorkflowPermissionChecker.java` | 权限检查工具 |
| 3 | `utils/PermissionHelper.java` | 权限检查助手 |
| 3 | `common/annotation/Auditable.java` | 审计注解 |
| 3 | `common/aop/AuditLogAspect.java` | 审计日志切面 |
| 4 | `utils/XssSanitizer.java` | XSS 清洗工具类 |
| 4 | `converter/user/UserCreateRequestConverter.java` | 用户创建请求转换器 |
| 4 | `converter/dept/DeptCreateRequestConverter.java` | 部门创建请求转换器 |

### 修改文件（约 25 个）

| 步骤 | 文件路径 | 改动说明 |
|-----|---------|---------|
| 1 | `utils/EntityHelper.java` | 新增方法 |
| 1 | `service/impl/WorkflowInstanceServiceImpl.java` | 替换 IllegalArgumentException |
| 1 | `service/impl/WorkflowDefinitionServiceImpl.java` | 替换 IllegalArgumentException |
| 1 | `service/impl/WorkflowStateMachineServiceImpl.java` | 替换 IllegalArgumentException |
| 1-3 | `service/impl/UserServiceImpl.java` | 使用新工具类、@Auditable |
| 1-3 | `service/impl/RoleServiceImpl.java` | 使用新工具类、@Auditable |
| 1-3 | `service/impl/DeptServiceImpl.java` | 使用新工具类、@Auditable |
| 1-3 | `service/impl/MenuServiceImpl.java` | 使用新工具类、@Auditable |
| 1-3 | `service/impl/FileServiceImpl.java` | 使用新工具类、@Auditable |
| 1-3 | `service/impl/ConfigServiceImpl.java` | 使用新工具类、@Auditable |
| 2 | `service/impl/WorkflowInstanceServiceImpl.java` | 重构为协调者 |
| 3 | `enums/WorkflowStatus.java` | 新增状态行为方法 |
| 4 | `converter/role/RoleCreateRequestConverter.java` | 添加 XSS 处理 |
| 4 | `converter/menu/MenuCreateRequestConverter.java` | 添加 XSS 处理 |
| 4 | `converter/config/ConfigCreateRequestConverter.java` | 添加 XSS 处理 |
| 4 | `converter/config/ConfigGroupCreateRequestConverter.java` | 添加 XSS 处理 |
| 4 | `converter/workflowdefinition/WorkflowDefinitionRequestConverter.java` | 添加 XSS 处理 |
| 4 | `converter/workflownodehook/WorkflowNodeHookRequestConverter.java` | 添加 XSS 处理 |
| 4 | `converter/formtemplate/FormTemplateRequestConverter.java` | 添加 XSS 处理 |
| 4 | `converter/dict/DictCreateRequestConverter.java` | 添加 XSS 处理 |