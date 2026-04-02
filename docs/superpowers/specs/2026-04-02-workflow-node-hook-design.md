# 工作流节点钩子系统设计

## 概述

为 AdminPlus 工作流系统添加节点级别的钩子机制，支持在流程操作的各个阶段执行校验和自定义逻辑。

**目标**：
- 提交/审批/退回/取消/撤回/加签等操作的前后钩子
- 支持业务校验（阻断流程）和外部系统集成（执行操作）
- 简单场景用节点字段，复杂场景用独立钩子表
- 可配置同步/异步执行、失败阻断策略、重试机制

---

## 第一部分：钩子触发点

定义 14 个钩子触发点，覆盖工作流全生命周期：

### 提交阶段
| 钩子点 | 类型 | 说明 |
|--------|------|------|
| `PRE_SUBMIT` | 校验 | 提交前校验表单完整性、业务规则 |
| `POST_SUBMIT` | 执行 | 提交后发送通知、记录日志 |

### 审批阶段
| 钩子点 | 类型 | 说明 |
|--------|------|------|
| `PRE_APPROVE` | 校验 | 同意前校验是否有权限、数据合规 |
| `POST_APPROVE` | 执行 | 同意后更新业务数据、通知下一审批人 |
| `PRE_REJECT` | 校验 | 拒绝前校验拒绝原因是否必填 |
| `POST_REJECT` | 执行 | 拒绝后通知发起人、记录审计 |

### 退回阶段
| 钩子点 | 类型 | 说明 |
|--------|------|------|
| `PRE_ROLLBACK` | 校验 | 退回前校验目标节点是否合法 |
| `POST_ROLLBACK` | 执行 | 退回后通知目标节点审批人 |

### 取消阶段
| 钩子点 | 类型 | 说明 |
|--------|------|------|
| `PRE_CANCEL` | 校验 | 取消前校验是否允许取消 |
| `POST_CANCEL` | 执行 | 取消后通知相关人员 |

### 撤回阶段
| 钩子点 | 类型 | 说明 |
|--------|------|------|
| `PRE_WITHDRAW` | 校验 | 撤回前校验状态是否允许 |
| `POST_WITHDRAW` | 执行 | 撤回后清理相关数据 |

### 加签/转办阶段
| 钩子点 | 类型 | 说明 |
|--------|------|------|
| `PRE_ADD_SIGN` | 校验 | 加签前校验被加签人是否存在 |
| `POST_ADD_SIGN` | 执行 | 加签后通知被加签人 |

---

## 第二部分：数据结构设计

### 1. WorkflowNodeEntity 扩展字段（简单钩子）

在现有节点实体中添加 SpEL 表达式字段：

```java
// 校验类钩子（SpEL 表达式，返回 boolean）
@Column(name = "pre_submit_validate", columnDefinition = "TEXT")
private String preSubmitValidate;

@Column(name = "pre_approve_validate", columnDefinition = "TEXT")
private String preApproveValidate;

@Column(name = "pre_reject_validate", columnDefinition = "TEXT")
private String preRejectValidate;

@Column(name = "pre_rollback_validate", columnDefinition = "TEXT")
private String preRollbackValidate;

@Column(name = "pre_cancel_validate", columnDefinition = "TEXT")
private String preCancelValidate;

@Column(name = "pre_withdraw_validate", columnDefinition = "TEXT")
private String preWithdrawValidate;

@Column(name = "pre_add_sign_validate", columnDefinition = "TEXT")
private String preAddSignValidate;

// 执行类钩子（SpEL 表达式，可调用 Bean 方法）
@Column(name = "post_submit_action", columnDefinition = "TEXT")
private String postSubmitAction;

@Column(name = "post_approve_action", columnDefinition = "TEXT")
private String postApproveAction;

@Column(name = "post_reject_action", columnDefinition = "TEXT")
private String postRejectAction;

@Column(name = "post_rollback_action", columnDefinition = "TEXT")
private String postRollbackAction;

@Column(name = "post_cancel_action", columnDefinition = "TEXT")
private String postCancelAction;

@Column(name = "post_withdraw_action", columnDefinition = "TEXT")
private String postWithdrawAction;

@Column(name = "post_add_sign_action", columnDefinition = "TEXT")
private String postAddSignAction;
```

### 2. WorkflowNodeHookEntity（复杂钩子）

独立表支持 Bean 引用和 HTTP 调用：

```java
@Data
@Entity
@Table(name = "sys_workflow_node_hook",
       indexes = {
           @Index(name = "idx_wf_hook_node_id", columnList = "node_id"),
           @Index(name = "idx_wf_hook_point", columnList = "hook_point"),
           @Index(name = "idx_wf_hook_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_workflow_node_hook SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class WorkflowNodeHookEntity extends BaseEntity {

    /**
     * 关联节点ID
     */
    @Column(name = "node_id", nullable = false)
    private String nodeId;

    /**
     * 钩子点：PRE_SUBMIT, POST_APPROVE 等
     */
    @Column(name = "hook_point", nullable = false, length = 30)
    private String hookPoint;

    /**
     * 类型：validate / execute
     */
    @Column(name = "hook_type", nullable = false, length = 20)
    private String hookType;

    /**
     * 执行方式：spel / bean / http
     */
    @Column(name = "executor_type", nullable = false, length = 20)
    private String executorType;

    /**
     * 执行配置（JSON格式）
     * spel: {"expression": "#formData.amount > 100", "failureMessage": "金额必须大于100"}
     * bean: {"beanName": "myHookService", "methodName": "validateSubmit", "args": ["#instance"]}
     * http: {"url": "http://api.example.com/hook", "method": "POST", "headers": {}, "bodyTemplate": "{}"}
     */
    @Column(name = "executor_config", columnDefinition = "TEXT")
    private String executorConfig;

    /**
     * 是否异步执行
     */
    @Column(name = "async_execution", nullable = false)
    private Boolean asyncExecution = false;

    /**
     * 失败时是否阻断流程
     */
    @Column(name = "block_on_failure", nullable = false)
    private Boolean blockOnFailure = true;

    /**
     * 默认失败提示消息
     */
    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    /**
     * 执行优先级（数字越小越先执行）
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    /**
     * 触发条件（可选 SpEL 表达式）
     */
    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    /**
     * 重试次数
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    /**
     * 重试间隔（毫秒）
     */
    @Column(name = "retry_interval")
    private Integer retryInterval = 1000;

    /**
     * 钩子名称
     */
    @Column(name = "hook_name", length = 100)
    private String hookName;

    /**
     * 钩子描述
     */
    @Column(name = "description", length = 500)
    private String description;
}
```

### 3. WorkflowHookLogEntity（执行日志）

记录钩子执行结果：

```java
@Data
@Entity
@Table(name = "sys_workflow_hook_log",
       indexes = {
           @Index(name = "idx_wf_hook_log_instance", columnList = "instance_id"),
           @Index(name = "idx_wf_hook_log_node", columnList = "node_id"),
           @Index(name = "idx_wf_hook_log_point", columnList = "hook_point"),
           @Index(name = "idx_wf_hook_log_time", columnList = "create_time")
       })
public class WorkflowHookLogEntity extends BaseEntity {

    /**
     * 工作流实例ID
     */
    @Column(name = "instance_id", nullable = false)
    private String instanceId;

    /**
     * 节点ID（可为空，如提交前）
     */
    @Column(name = "node_id")
    private String nodeId;

    /**
     * 钩子配置ID（独立表钩子）
     */
    @Column(name = "hook_id")
    private String hookId;

    /**
     * 来源：node_field / hook_table
     */
    @Column(name = "hook_source", length = 20)
    private String hookSource;

    /**
     * 钩子点
     */
    @Column(name = "hook_point", nullable = false, length = 30)
    private String hookPoint;

    /**
     * 执行方式
     */
    @Column(name = "executor_type", length = 20)
    private String executorType;

    /**
     * 执行配置
     */
    @Column(name = "executor_config", columnDefinition = "TEXT")
    private String executorConfig;

    /**
     * 是否成功
     */
    @Column(name = "success", nullable = false)
    private Boolean success;

    /**
     * 结果码
     */
    @Column(name = "result_code", length = 50)
    private String resultCode;

    /**
     * 结果消息
     */
    @Column(name = "result_message", columnDefinition = "TEXT")
    private String resultMessage;

    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "execution_time")
    private Long executionTime;

    /**
     * 实际重试次数
     */
    @Column(name = "retry_attempts")
    private Integer retryAttempts;

    /**
     * 是否异步执行
     */
    @Column(name = "async")
    private Boolean async;

    /**
     * 操作人ID
     */
    @Column(name = "operator_id", length = 50)
    private String operatorId;

    /**
     * 操作人姓名
     */
    @Column(name = "operator_name", length = 100)
    private String operatorName;
}
```

---

## 第三部分：执行器设计

### 1. HookResult（统一返回结构）

```java
public record HookResult(
    boolean success,        // 是否成功
    String code,            // 结果码
    String message,         // 结果消息
    Object data,            // 附加数据（可选）
    Long executionTime,     // 执行耗时（毫秒）
    Integer retryAttempts   // 实际重试次数
) {
    public HookResult(boolean success, String code, String message) {
        this(success, code, message, null, null, 0);
    }

    public HookResult(boolean success, String code, String message, Object data) {
        this(success, code, message, data, null, 0);
    }

    public HookResult withExecutionTime(Long time) {
        return new HookResult(success, code, message, data, time, retryAttempts);
    }

    public HookResult withRetryAttempts(Integer attempts) {
        return new HookResult(success, code, message, data, executionTime, attempts);
    }
}
```

### 2. HookContext（执行上下文）

```java
public record HookContext(
    WorkflowInstanceEntity instance,   // 工作流实例
    WorkflowNodeEntity node,           // 当前节点
    Map<String, Object> formData,      // 表单数据
    String operation,                  // 操作类型
    String operatorId,                 // 操作人ID
    String operatorName,               // 操作人姓名
    Map<String, Object> extraParams    // 额外参数
) {}
```

### 3. HookExecutor 接口

```java
public interface HookExecutor {

    /**
     * 执行钩子
     */
    HookResult execute(HookExecutorConfig config, HookContext context);

    /**
     * 获取执行器类型
     */
    String getType();
}
```

### 4. SpEL 执行器

```java
@Component
public class SpELHookExecutor implements HookExecutor {

    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public HookResult execute(HookExecutorConfig config, HookContext context) {
        SpELConfig spelConfig = (SpELConfig) config;

        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        evalContext.setVariable("instance", context.instance());
        evalContext.setVariable("node", context.node());
        evalContext.setVariable("formData", context.formData());
        evalContext.setVariable("operation", context.operation());
        evalContext.setVariable("operatorId", context.operatorId());
        evalContext.setVariable("operatorName", context.operatorName());
        evalContext.setVariable("extraParams", context.extraParams());

        try {
            Expression expr = parser.parseExpression(spelConfig.expression());
            Object result = expr.getValue(evalContext);

            if (result instanceof HookResult hookResult) {
                return hookResult;
            }
            if (result instanceof Boolean bool) {
                return new HookResult(bool,
                    bool ? "SUCCESS" : "VALIDATION_FAILED",
                    bool ? "校验通过" : spelConfig.failureMessage());
            }
            return new HookResult(true, "SUCCESS", "执行成功", result);

        } catch (Exception e) {
            return new HookResult(false, "EXECUTION_ERROR", e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "spel";
    }
}
```

### 5. Bean 执行器

```java
@Component
@RequiredArgsConstructor
public class BeanHookExecutor implements HookExecutor {

    private final ApplicationContext applicationContext;

    @Override
    public HookResult execute(HookExecutorConfig config, HookContext context) {
        BeanConfig beanConfig = (BeanConfig) config;

        try {
            Object bean = applicationContext.getBean(beanConfig.beanName());
            Method method = findMethod(bean.getClass(), beanConfig.methodName(), beanConfig.args().size());

            Object[] args = resolveArgs(beanConfig.args(), context);
            Object result = method.invoke(bean, args);

            return wrapResult(result, beanConfig);

        } catch (NoSuchBeanException e) {
            return new HookResult(false, "BEAN_NOT_FOUND", "Bean不存在: " + beanConfig.beanName());
        } catch (NoSuchMethodException e) {
            return new HookResult(false, "METHOD_NOT_FOUND", "方法不存在: " + beanConfig.methodName());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof BizException biz) {
                return new HookResult(false, biz.getCode(), biz.getMessage());
            }
            return new HookResult(false, "EXECUTION_ERROR", cause.getMessage());
        } catch (Exception e) {
            return new HookResult(false, "EXECUTION_ERROR", e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "bean";
    }

    private Object[] resolveArgs(List<String> argExpressions, HookContext context) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        evalContext.setVariable("instance", context.instance());
        evalContext.setVariable("node", context.node());
        evalContext.setVariable("formData", context.formData());
        evalContext.setVariable("operation", context.operation());
        evalContext.setVariable("operatorId", context.operatorId());
        evalContext.setVariable("operatorName", context.operatorName());
        evalContext.setVariable("extraParams", context.extraParams());

        return argExpressions.stream()
            .map(expr -> parser.parseExpression(expr).getValue(evalContext))
            .toArray();
    }
}
```

### 6. HTTP 执行器

```java
@Component
@RequiredArgsConstructor
public class HttpHookExecutor implements HookExecutor {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public HookResult execute(HookExecutorConfig config, HookContext context) {
        HttpConfig httpConfig = (HttpConfig) config;

        try {
            String body = buildRequestBody(httpConfig.bodyTemplate(), context);

            HttpHeaders headers = new HttpHeaders();
            httpConfig.headers().forEach(headers::add);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                httpConfig.url(),
                HttpMethod.valueOf(httpConfig.method()),
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> bodyMap = response.getBody();
                if (bodyMap == null) {
                    return new HookResult(true, "SUCCESS", "执行成功");
                }
                boolean success = (boolean) bodyMap.getOrDefault("success", true);
                String code = (String) bodyMap.getOrDefault("code", "SUCCESS");
                String message = (String) bodyMap.getOrDefault("message", "执行成功");
                return new HookResult(success, code, message, bodyMap.get("data"));
            }

            return new HookResult(false, "HTTP_ERROR",
                "HTTP " + response.getStatusCode() + ": " + response.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return new HookResult(false, "HTTP_ERROR",
                "HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            return new HookResult(false, "HTTP_ERROR", e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "http";
    }

    private String buildRequestBody(String template, HookContext context) {
        if (template == null || template.isBlank()) {
            return "{}";
        }

        // 替换模板变量：#instance.id -> 实际值
        Map<String, Object> variables = new HashMap<>();
        variables.put("instance", context.instance());
        variables.put("node", context.node());
        variables.put("formData", context.formData());
        variables.put("operation", context.operation());
        variables.put("operatorId", context.operatorId());
        variables.put("operatorName", context.operatorName());

        // 使用 StrSubstitutor 或自定义替换逻辑
        return replaceTemplateVariables(template, variables);
    }
}
```

---

## 第四部分：钩子服务设计

### 1. HookExecutionSummary（执行汇总）

```java
public record HookExecutionSummary(
    boolean allPassed,                    // 校验类钩子是否全部通过
    List<HookResult> validationResults,   // 校验类钩子结果
    List<HookResult> executionResults,    // 执行类钩子结果
    List<String> blockingMessages,        // 阻断性失败消息
    List<String> warningMessages          // 非阻断性失败消息
) {}
```

### 2. WorkflowHookService 接口

```java
public interface WorkflowHookService {

    /**
     * 执行节点字段钩子（简单场景）
     */
    List<HookResult> executeNodeFieldHooks(
        String hookPoint,
        WorkflowInstanceEntity instance,
        WorkflowNodeEntity node,
        Map<String, Object> formData,
        Map<String, Object> extraParams
    );

    /**
     * 执行独立表钩子（复杂场景）
     */
    List<HookResult> executeTableHooks(
        String hookPoint,
        WorkflowInstanceEntity instance,
        WorkflowNodeEntity node,
        Map<String, Object> formData,
        Map<String, Object> extraParams
    );

    /**
     * 执行所有钩子并处理阻断逻辑
     */
    HookExecutionSummary executeAllHooks(
        String hookPoint,
        WorkflowInstanceEntity instance,
        WorkflowNodeEntity node,
        Map<String, Object> formData,
        Map<String, Object> extraParams
    );

    /**
     * 获取节点配置的所有钩子点
     */
    List<String> getConfiguredHookPoints(String nodeId);
}
```

### 3. WorkflowHookServiceImpl 核心逻辑

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowHookServiceImpl implements WorkflowHookService {

    private final WorkflowNodeHookRepository hookRepository;
    private final WorkflowHookLogRepository hookLogRepository;
    private final Map<String, HookExecutor> executors;
    private final ObjectMapper objectMapper;
    private final AsyncTaskExecutor asyncExecutor;

    // 钩子点类型映射
    private static final Set<String> VALIDATION_HOOKS = Set.of(
        "PRE_SUBMIT", "PRE_APPROVE", "PRE_REJECT", "PRE_ROLLBACK",
        "PRE_CANCEL", "PRE_WITHDRAW", "PRE_ADD_SIGN"
    );

    // 节点字段名映射
    private static final Map<String, String> NODE_FIELD_MAPPING = Map.ofEntries(
        Map.entry("PRE_SUBMIT", "preSubmitValidate"),
        Map.entry("PRE_APPROVE", "preApproveValidate"),
        Map.entry("PRE_REJECT", "preRejectValidate"),
        Map.entry("PRE_ROLLBACK", "preRollbackValidate"),
        Map.entry("PRE_CANCEL", "preCancelValidate"),
        Map.entry("PRE_WITHDRAW", "preWithdrawValidate"),
        Map.entry("PRE_ADD_SIGN", "preAddSignValidate"),
        Map.entry("POST_SUBMIT", "postSubmitAction"),
        Map.entry("POST_APPROVE", "postApproveAction"),
        Map.entry("POST_REJECT", "postRejectAction"),
        Map.entry("POST_ROLLBACK", "postRollbackAction"),
        Map.entry("POST_CANCEL", "postCancelAction"),
        Map.entry("POST_WITHDRAW", "postWithdrawAction"),
        Map.entry("POST_ADD_SIGN", "postAddSignAction")
    );

    @Override
    public HookExecutionSummary executeAllHooks(
        String hookPoint,
        WorkflowInstanceEntity instance,
        WorkflowNodeEntity node,
        Map<String, Object> formData,
        Map<String, Object> extraParams
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        String userName = SecurityUtils.getCurrentUserName();

        HookContext context = new HookContext(
            instance, node, formData,
            extractOperation(hookPoint),
            userId, userName, extraParams
        );

        // 1. 执行节点字段钩子
        List<HookResult> fieldResults = executeNodeFieldHooksInternal(hookPoint, context);

        // 2. 执行独立表钩子（按优先级排序）
        List<WorkflowNodeHookEntity> tableHooks = hookRepository
            .findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc(
                node != null ? node.getId() : null, hookPoint);
        List<HookResult> tableResults = executeTableHooksInternal(tableHooks, context);

        // 3. 合并结果并分类
        List<HookResult> validationResults = new ArrayList<>();
        List<HookResult> executionResults = new ArrayList<>();
        List<String> blockingMessages = new ArrayList<>();
        List<String> warningMessages = new ArrayList<>();

        for (HookResult result : concat(fieldResults, tableResults)) {
            if (isValidationHook(hookPoint)) {
                validationResults.add(result);
                if (!result.success() && shouldBlock(result)) {
                    blockingMessages.add(result.message());
                } else if (!result.success()) {
                    warningMessages.add(result.message());
                }
            } else {
                executionResults.add(result);
            }
        }

        return new HookExecutionSummary(
            blockingMessages.isEmpty(),
            validationResults,
            executionResults,
            blockingMessages,
            warningMessages
        );
    }

    private List<HookResult> executeNodeFieldHooksInternal(String hookPoint, HookContext context) {
        List<HookResult> results = new ArrayList<>();

        if (context.node() == null) {
            return results;
        }

        String fieldName = NODE_FIELD_MAPPING.get(hookPoint);
        if (fieldName == null) {
            return results;
        }

        try {
            String expression = getFieldValue(context.node(), fieldName);
            if (expression == null || expression.isBlank()) {
                return results;
            }

            HookExecutor executor = executors.get("spel");
            SpELConfig config = new SpELConfig(expression, null);
            HookResult result = executor.execute(config, context);

            saveLog(null, hookPoint, "node_field", "spel",
                expression, result, context, false);

            results.add(result);

        } catch (Exception e) {
            log.error("节点字段钩子执行失败: hookPoint={}, node={}", hookPoint, context.node().getId(), e);
            results.add(new HookResult(false, "EXECUTION_ERROR", e.getMessage()));
        }

        return results;
    }

    private List<HookResult> executeTableHooksInternal(
        List<WorkflowNodeHookEntity> hooks,
        HookContext context
    ) {
        List<HookResult> results = new ArrayList<>();

        for (WorkflowNodeHookEntity hook : hooks) {
            // 检查触发条件
            if (!evaluateCondition(hook.getConditionExpression(), context)) {
                continue;
            }

            HookExecutor executor = executors.get(hook.getExecutorType());
            if (executor == null) {
                log.warn("未找到执行器: type={}", hook.getExecutorType());
                continue;
            }

            HookExecutorConfig config = parseConfig(hook);

            if (hook.getAsyncExecution()) {
                // 异步执行
                asyncExecutor.execute(() -> {
                    HookResult result = executeWithRetry(executor, config, context, hook);
                    saveLog(hook, hook.getHookPoint(), "hook_table",
                        hook.getExecutorType(), hook.getExecutorConfig(),
                        result, context, true);
                });
                results.add(new HookResult(true, "ASYNC_EXECUTING", "异步执行中"));
            } else {
                // 同步执行
                HookResult result = executeWithRetry(executor, config, context, hook);
                results.add(result);
                saveLog(hook, hook.getHookPoint(), "hook_table",
                    hook.getExecutorType(), hook.getExecutorConfig(),
                    result, context, false);
            }
        }

        return results;
    }

    private HookResult executeWithRetry(
        HookExecutor executor,
        HookExecutorConfig config,
        HookContext context,
        WorkflowNodeHookEntity hook
    ) {
        int attempts = 0;
        HookResult result;
        long startTime = System.currentTimeMillis();

        do {
            attempts++;
            result = executor.execute(config, context);

            if (result.success()) {
                break;
            }

            if (attempts <= hook.getRetryCount()) {
                sleep(hook.getRetryInterval());
            }
        } while (attempts <= hook.getRetryCount() && !result.success());

        return result
            .withExecutionTime(System.currentTimeMillis() - startTime)
            .withRetryAttempts(attempts - 1);
    }

    private boolean evaluateCondition(String conditionExpression, HookContext context) {
        if (conditionExpression == null || conditionExpression.isBlank()) {
            return true;
        }

        try {
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext evalContext = new StandardEvaluationContext();
            evalContext.setVariable("instance", context.instance());
            evalContext.setVariable("node", context.node());
            evalContext.setVariable("formData", context.formData());
            evalContext.setVariable("operation", context.operation());

            Expression expr = parser.parseExpression(conditionExpression);
            Boolean result = expr.getValue(evalContext, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("条件表达式求值失败: {}", conditionExpression, e);
            return true; // 条件失败默认执行
        }
    }

    private void saveLog(WorkflowNodeHookEntity hook, String hookPoint,
                         String hookSource, String executorType,
                         String executorConfig, HookResult result,
                         HookContext context, boolean async) {
        WorkflowHookLogEntity log = new WorkflowHookLogEntity();
        log.setInstanceId(context.instance().getId());
        log.setNodeId(context.node() != null ? context.node().getId() : null);
        log.setHookId(hook != null ? hook.getId() : null);
        log.setHookSource(hookSource);
        log.setHookPoint(hookPoint);
        log.setExecutorType(executorType);
        log.setExecutorConfig(executorConfig);
        log.setSuccess(result.success());
        log.setResultCode(result.code());
        log.setResultMessage(result.message());
        log.setExecutionTime(result.executionTime());
        log.setRetryAttempts(result.retryAttempts());
        log.setAsync(async);
        log.setOperatorId(context.operatorId());
        log.setOperatorName(context.operatorName());

        hookLogRepository.save(log);
    }

    private boolean isValidationHook(String hookPoint) {
        return VALIDATION_HOOKS.contains(hookPoint);
    }

    private String extractOperation(String hookPoint) {
        return hookPoint.replace("PRE_", "").replace("POST_", "");
    }

    private void sleep(Integer interval) {
        if (interval != null && interval > 0) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

### 4. 集成到 WorkflowInstanceServiceImpl

在各操作方法中调用钩子：

```java
// submit 方法
@Override
@Transactional
public WorkflowInstanceResp submit(String instanceId, WorkflowStartReq req) {
    // ... 现有前置逻辑 ...

    // 获取第一个节点
    WorkflowNodeEntity firstNode = nodes.get(0);

    // 提交前钩子校验
    HookExecutionSummary preResult = hookService.executeAllHooks(
        "PRE_SUBMIT", instance, firstNode,
        deserializeFormData(instance.getBusinessData()),
        Map.of("req", req)
    );

    if (!preResult.allPassed()) {
        throw new BizException(preResult.blockingMessages().get(0));
    }

    // ... 执行提交逻辑 ...

    // 提交后钩子执行
    HookExecutionSummary postResult = hookService.executeAllHooks(
        "POST_SUBMIT", instance, firstNode,
        deserializeFormData(instance.getBusinessData()),
        Map.of()
    );
    if (!postResult.warningMessages().isEmpty()) {
        log.warn("提交后钩子警告: {}", postResult.warningMessages());
    }

    return toInstanceResponse(instance, false, false);
}

// approve 方法
@Override
@Transactional
public WorkflowInstanceResp approve(String instanceId, ApprovalActionReq req) {
    // ... 现有前置逻辑 ...

    WorkflowNodeEntity currentNode = nodeRepository.findById(instance.getCurrentNodeId())
        .orElseThrow(() -> new IllegalArgumentException("当前节点不存在"));

    // 同意前钩子校验
    HookExecutionSummary preResult = hookService.executeAllHooks(
        "PRE_APPROVE", instance, currentNode,
        deserializeFormData(instance.getBusinessData()),
        Map.of("req", req)
    );

    if (!preResult.allPassed()) {
        throw new BizException(preResult.blockingMessages().get(0));
    }

    // ... 执行审批逻辑 ...

    // 同意后钩子执行
    HookExecutionSummary postResult = hookService.executeAllHooks(
        "POST_APPROVE", instance, currentNode,
        deserializeFormData(instance.getBusinessData()),
        Map.of()
    );

    return toInstanceResponse(instance, false, canUserApprove(instance, userId));
}

// 同样模式集成到 reject、rollback、cancel、withdraw、addSign 方法
```

---

## 第五部分：前端配置界面

### 1. 节点属性面板扩展

在 `WorkflowNodeProperties.vue` 中添加钩子配置区域，包含：
- 简单钩子（SpEL 表达式）输入框
- 高级钩子管理按钮（打开弹窗）

### 2. 高级钩子管理弹窗

功能：
- 钩子点选择（下拉框）
- 类型选择（校验/执行）
- 执行方式选择（SpEL/Bean/HTTP）
- 执行配置（根据方式动态显示表单）
- 执行选项（异步、阻断）
- 触发条件、重试配置

### 3. 钩子列表展示

显示节点配置的所有钩子，支持编辑、删除、排序。

### 4. 钩子执行日志查看

在流程详情页添加钩子日志 Tab，显示：
- 执行时间、钩子点、执行方式
- 结果（成功/失败）、耗时、消息
- 点击展开查看详细配置和返回数据

---

## 第六部分：数据库迁移

### SQL 脚本

```sql
-- 1. 扩展节点表字段
ALTER TABLE sys_workflow_node ADD COLUMN pre_submit_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN pre_approve_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN pre_reject_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN pre_rollback_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN pre_cancel_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN pre_withdraw_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN pre_add_sign_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN post_submit_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN post_approve_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN post_reject_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN post_rollback_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN post_cancel_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN post_withdraw_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN post_add_sign_action TEXT;

-- 2. 创建钩子配置表
CREATE TABLE sys_workflow_node_hook (
    id VARCHAR(50) PRIMARY KEY,
    node_id VARCHAR(50) NOT NULL,
    hook_point VARCHAR(30) NOT NULL,
    hook_type VARCHAR(20) NOT NULL,
    executor_type VARCHAR(20) NOT NULL,
    executor_config TEXT,
    async_execution BOOLEAN NOT NULL DEFAULT FALSE,
    block_on_failure BOOLEAN NOT NULL DEFAULT TRUE,
    failure_message VARCHAR(500),
    priority INTEGER NOT NULL DEFAULT 0,
    condition_expression TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    retry_interval INTEGER DEFAULT 1000,
    hook_name VARCHAR(100),
    description VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_wf_hook_node_id ON sys_workflow_node_hook(node_id);
CREATE INDEX idx_wf_hook_point ON sys_workflow_node_hook(hook_point);
CREATE INDEX idx_wf_hook_deleted ON sys_workflow_node_hook(deleted);

-- 3. 创建钩子日志表
CREATE TABLE sys_workflow_hook_log (
    id VARCHAR(50) PRIMARY KEY,
    instance_id VARCHAR(50) NOT NULL,
    node_id VARCHAR(50),
    hook_id VARCHAR(50),
    hook_source VARCHAR(20),
    hook_point VARCHAR(30) NOT NULL,
    executor_type VARCHAR(20),
    executor_config TEXT,
    success BOOLEAN NOT NULL,
    result_code VARCHAR(50),
    result_message TEXT,
    execution_time INTEGER,
    retry_attempts INTEGER,
    async BOOLEAN,
    operator_id VARCHAR(50),
    operator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_wf_hook_log_instance ON sys_workflow_hook_log(instance_id);
CREATE INDEX idx_wf_hook_log_node ON sys_workflow_hook_log(node_id);
CREATE INDEX idx_wf_hook_log_point ON sys_workflow_hook_log(hook_point);
CREATE INDEX idx_wf_hook_log_time ON sys_workflow_hook_log(create_time);
```

---

## 第七部分：权限配置

在 `SecurityConstants.java` 中添加钩子管理权限：

```java
// 钩子配置管理
public static final String WORKFLOW_HOOK_CREATE = "workflow:hook:create";
public static final String WORKFLOW_HOOK_UPDATE = "workflow:hook:update";
public static final String WORKFLOW_HOOK_DELETE = "workflow:hook:delete";
public static final String WORKFLOW_HOOK_VIEW = "workflow:hook:view";
```

---

## 第八部分：实现步骤

### 后端实现顺序

1. 创建实体类和 Repository
   - WorkflowNodeHookEntity
   - WorkflowHookLogEntity
   - WorkflowNodeHookRepository
   - WorkflowHookLogRepository

2. 创建执行器
   - HookResult、HookContext、HookExecutorConfig
   - HookExecutor 接口
   - SpELHookExecutor、BeanHookExecutor、HttpHookExecutor

3. 创建钩子服务
   - WorkflowHookService 接口
   - WorkflowHookServiceImpl

4. 集成到工作流服务
   - 扩展 WorkflowNodeEntity 字段
   - 在 WorkflowInstanceServiceImpl 各方法中调用钩子

5. 创建 API 接口
   - WorkflowNodeHookController（钩子配置 CRUD）
   - WorkflowHookLogController（日志查询）

### 前端实现顺序

1. 扩展节点属性面板
   - 添加简单钩子输入字段
   - 添加高级钩子管理按钮

2. 创建高级钩子配置弹窗
   - 钩子点、类型、执行方式选择
   - 动态配置表单

3. 创建钩子列表组件
   - 展示节点所有钩子
   - 编辑、删除、排序功能

4. 添加钩子日志 Tab
   - 在流程详情页展示执行日志

---

## 附录：使用示例

### SpEL 校验示例

```java
// 提交前校验金额必须大于100
preSubmitValidate = "#formData.amount > 100"

// 同意前校验必须填写备注
preApproveValidate = "#extraParams.get('req').comment() != null && !#extraParams.get('req').comment().isEmpty()"
```

### Bean 执行示例

```java
// Bean 配置
executorConfig = {
    "beanName": "orderWorkflowHookService",
    "methodName": "syncToErp",
    "args": ["#instance", "#formData"]
}

// Bean 实现
@Service
public class OrderWorkflowHookService {
    public HookResult syncToErp(WorkflowInstanceEntity instance, Map<String, Object> formData) {
        // 同步订单到 ERP 系统
        boolean success = erpClient.syncOrder(instance.getId(), formData);
        return new HookResult(success, success ? "SUCCESS" : "ERP_SYNC_FAILED",
            success ? "同步成功" : "ERP同步失败");
    }
}
```

### HTTP 执行示例

```java
executorConfig = {
    "url": "http://erp.example.com/api/workflow/sync",
    "method": "POST",
    "headers": {
        "Authorization": "Bearer xxx",
        "Content-Type": "application/json"
    },
    "bodyTemplate": "{\"instanceId\":\"#instance.id\",\"formData\":#formData}"
}
```