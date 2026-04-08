package com.adminplus.service.workflow.hook.impl;

import com.adminplus.pojo.dto.workflow.hook.*;
import com.adminplus.pojo.entity.WorkflowHookLogEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.pojo.entity.WorkflowNodeHookEntity;
import com.adminplus.repository.WorkflowHookLogRepository;
import com.adminplus.repository.WorkflowNodeHookRepository;
import com.adminplus.service.workflow.hook.HookExecutor;
import com.adminplus.service.workflow.hook.WorkflowHookService;
import com.adminplus.utils.SecurityUtils;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * 工作流钩子服务实现
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowHookServiceImpl implements WorkflowHookService {

    private final WorkflowNodeHookRepository hookRepository;
    private final WorkflowHookLogRepository hookLogRepository;
    private final Map<String, HookExecutor> executors;
    private final JsonMapper objectMapper;
    private final ThreadPoolTaskExecutor asyncExecutor;

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
    public List<HookResult> executeNodeFieldHooks(
        String hookPoint,
        WorkflowInstanceEntity instance,
        WorkflowNodeEntity node,
        Map<String, Object> formData,
        Map<String, Object> extraParams
    ) {
        String userId = getCurrentUserId();
        String userName = getCurrentUserName();

        HookContext context = new HookContext(
            instance, node, formData,
            extractOperation(hookPoint),
            userId, userName, extraParams
        );

        return executeNodeFieldHooksInternal(hookPoint, context);
    }

    @Override
    public List<HookResult> executeTableHooks(
        String hookPoint,
        WorkflowInstanceEntity instance,
        WorkflowNodeEntity node,
        Map<String, Object> formData,
        Map<String, Object> extraParams
    ) {
        String userId = getCurrentUserId();
        String userName = getCurrentUserName();

        HookContext context = new HookContext(
            instance, node, formData,
            extractOperation(hookPoint),
            userId, userName, extraParams
        );

        List<WorkflowNodeHookEntity> tableHooks = hookRepository
            .findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc(
                node != null ? node.getId() : null, hookPoint);

        return executeTableHooksInternal(tableHooks, context);
    }

    @Override
    @Transactional
    public HookExecutionSummary executeAllHooks(
        String hookPoint,
        WorkflowInstanceEntity instance,
        WorkflowNodeEntity node,
        Map<String, Object> formData,
        Map<String, Object> extraParams
    ) {
        String userId = getCurrentUserId();
        String userName = getCurrentUserName();

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

        for (HookResult result : Stream.concat(fieldResults.stream(), tableResults.stream()).toList()) {
            if (isValidationHook(hookPoint)) {
                validationResults.add(result);
                if (!result.success()) {
                    // 对于校验类钩子，默认阻断
                    blockingMessages.add(result.message());
                }
            } else {
                executionResults.add(result);
                if (!result.success()) {
                    warningMessages.add(result.message());
                }
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

    @Override
    public List<String> getConfiguredHookPoints(String nodeId) {
        List<WorkflowNodeHookEntity> hooks = hookRepository.findByNodeIdAndDeletedFalseOrderByPriorityAsc(nodeId);
        return hooks.stream()
            .map(WorkflowNodeHookEntity::getHookPoint)
            .distinct()
            .toList();
    }

    /**
     * 内部方法：执行节点字段钩子
     */
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

    /**
     * 内部方法：执行表钩子
     */
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

    /**
     * 带重试的执行
     */
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

    /**
     * 评估条件表达式
     */
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

    /**
     * 保存日志
     */
    private void saveLog(WorkflowNodeHookEntity hook, String hookPoint,
                         String hookSource, String executorType,
                         String executorConfig, HookResult result,
                         HookContext context, boolean async) {
        try {
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
        } catch (Exception e) {
            log.error("保存钩子日志失败: hookPoint={}", hookPoint, e);
        }
    }

    /**
     * 解析配置
     */
    private HookExecutorConfig parseConfig(WorkflowNodeHookEntity hook) {
        try {
            String executorConfig = hook.getExecutorConfig();
            if (executorConfig == null || executorConfig.isBlank()) {
                throw new IllegalArgumentException("执行配置为空");
            }

            // 根据 executorType 创建相应的配置对象
            return switch (hook.getExecutorType()) {
                case "spel" -> objectMapper.readValue(executorConfig, SpELConfig.class);
                case "bean" -> objectMapper.readValue(executorConfig, BeanConfig.class);
                case "http" -> objectMapper.readValue(executorConfig, HttpConfig.class);
                default -> throw new IllegalArgumentException("未知的执行器类型: " + hook.getExecutorType());
            };
        } catch (Exception e) {
            log.error("解析执行配置失败: executorConfig={}", hook.getExecutorConfig(), e);
            throw new RuntimeException("解析执行配置失败", e);
        }
    }

    /**
     * 获取字段值（通过反射）
     */
    private String getFieldValue(WorkflowNodeEntity node, String fieldName) {
        try {
            Field field = WorkflowNodeEntity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(node);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("获取字段值失败: fieldName={}", fieldName, e);
            return null;
        }
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

    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUserIdOrDefault();
    }

    private String getCurrentUserName() {
        return SecurityUtils.getCurrentUsernameOrDefault();
    }
}
