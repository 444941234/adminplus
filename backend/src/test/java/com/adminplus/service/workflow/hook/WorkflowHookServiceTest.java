package com.adminplus.service.workflow.hook;

import com.adminplus.pojo.dto.workflow.hook.*;
import com.adminplus.pojo.entity.WorkflowHookLogEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.pojo.entity.WorkflowNodeHookEntity;
import com.adminplus.repository.WorkflowHookLogRepository;
import com.adminplus.repository.WorkflowNodeHookRepository;
import com.adminplus.service.workflow.hook.impl.WorkflowHookServiceImpl;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * WorkflowHookService 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowHookService Unit Tests")
class WorkflowHookServiceTest {

    @Mock
    private WorkflowNodeHookRepository hookRepository;

    @Mock
    private WorkflowHookLogRepository hookLogRepository;

    @Mock
    private JsonMapper objectMapper;

    @Mock
    private ThreadPoolTaskExecutor asyncExecutor;

    @Mock
    private HookExecutor spelExecutor;

    @InjectMocks
    private WorkflowHookServiceImpl hookService;

    private WorkflowInstanceEntity testInstance;
    private WorkflowNodeEntity testNode;
    private WorkflowNodeHookEntity testHook;
    private Map<String, Object> testFormData;

    @BeforeEach
    void setUp() {
        // Setup test instance
        testInstance = new WorkflowInstanceEntity();
        testInstance.setId("instance-001");
        testInstance.setTitle("Test Workflow");
        testInstance.setStatus("running");

        // Setup test node
        testNode = new WorkflowNodeEntity();
        testNode.setId("node-001");
        testNode.setNodeName("审批节点");
        testNode.setPreApproveValidate("#formData.amount > 100");

        // Setup test hook
        testHook = new WorkflowNodeHookEntity();
        testHook.setId("hook-001");
        testHook.setNodeId("node-001");
        testHook.setHookPoint("PRE_APPROVE");
        testHook.setHookType("validate");
        testHook.setExecutorType("spel");
        testHook.setExecutorConfig("{\"expression\":\"#formData.amount > 100\"}");
        testHook.setAsyncExecution(false);
        testHook.setBlockOnFailure(true);
        testHook.setPriority(0);
        testHook.setRetryCount(0);
        testHook.setRetryInterval(1000);

        // Setup test form data
        testFormData = Map.of("amount", 200);

        // Setup executors map
        setExecutors(Map.of("spel", spelExecutor));
    }

    @Nested
    @DisplayName("executeNodeFieldHooks Tests")
    class ExecuteNodeFieldHooksTests {

        @Test
        @DisplayName("should execute SpEL expression from node field")
        void shouldExecuteSpELExpressionFromNodeField() {
            // Given
            when(spelExecutor.execute(any(SpELConfig.class), any(HookContext.class)))
                .thenReturn(new HookResult(true, "SUCCESS", "校验通过"));
            when(hookLogRepository.save(any())).thenReturn(new WorkflowHookLogEntity());

            // When
            List<HookResult> results = hookService.executeNodeFieldHooks(
                "PRE_APPROVE", testInstance, testNode, testFormData, Map.of()
            );

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).success()).isTrue();
            verify(hookLogRepository).save(any(WorkflowHookLogEntity.class));
        }

        @Test
        @DisplayName("should return empty list when node is null")
        void shouldReturnEmptyListWhenNodeIsNull() {
            // When
            List<HookResult> results = hookService.executeNodeFieldHooks(
                "PRE_APPROVE", testInstance, null, testFormData, Map.of()
            );

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when expression is blank")
        void shouldReturnEmptyListWhenExpressionIsBlank() {
            // Given
            testNode.setPreApproveValidate("");

            // When
            List<HookResult> results = hookService.executeNodeFieldHooks(
                "PRE_APPROVE", testInstance, testNode, testFormData, Map.of()
            );

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("executeTableHooks Tests")
    class ExecuteTableHooksTests {

        @Test
        @DisplayName("should execute hooks from table")
        void shouldExecuteHooksFromTable() throws Exception {
            // Given
            when(hookRepository.findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc("node-001", "PRE_APPROVE"))
                .thenReturn(List.of(testHook));
            when(spelExecutor.execute(any(SpELConfig.class), any(HookContext.class)))
                .thenReturn(new HookResult(true, "SUCCESS", "校验通过"));
            when(hookLogRepository.save(any())).thenReturn(new WorkflowHookLogEntity());
            lenient().when(objectMapper.readValue(anyString(), eq(SpELConfig.class)))
                .thenReturn(new SpELConfig("#formData.amount > 100", null));

            // When
            List<HookResult> results = hookService.executeTableHooks(
                "PRE_APPROVE", testInstance, testNode, testFormData, Map.of()
            );

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).success()).isTrue();
        }

        @Test
        @DisplayName("should skip hook when condition is not met")
        void shouldSkipHookWhenConditionNotMet() {
            // Given - condition evaluates to false (formData['amount'] is 200, not > 500)
            testHook.setConditionExpression("#formData['amount'] > 500");
            when(hookRepository.findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc("node-001", "PRE_APPROVE"))
                .thenReturn(List.of(testHook));

            // When
            List<HookResult> results = hookService.executeTableHooks(
                "PRE_APPROVE", testInstance, testNode, testFormData, Map.of()
            );

            // Then
            assertThat(results).isEmpty();
            verify(spelExecutor, never()).execute(any(), any());
        }

        @Test
        @DisplayName("should execute asynchronously when asyncExecution is true")
        void shouldExecuteAsynchronouslyWhenConfigured() throws Exception {
            // Given
            testHook.setAsyncExecution(true);
            when(hookRepository.findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc("node-001", "PRE_APPROVE"))
                .thenReturn(List.of(testHook));
            lenient().when(objectMapper.readValue(anyString(), eq(SpELConfig.class)))
                .thenReturn(new SpELConfig("#formData.amount > 100", null));

            // When
            List<HookResult> results = hookService.executeTableHooks(
                "PRE_APPROVE", testInstance, testNode, testFormData, Map.of()
            );

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).code()).isEqualTo("ASYNC_EXECUTING");
            verify(asyncExecutor).execute(any(Runnable.class));
        }
    }

    @Nested
    @DisplayName("executeAllHooks Tests")
    class ExecuteAllHooksTests {

        @Test
        @DisplayName("should return all passed when all validations succeed")
        void shouldReturnAllPassedWhenValidationsSucceed() {
            // Given
            when(hookRepository.findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc(any(), any()))
                .thenReturn(List.of());
            when(spelExecutor.execute(any(SpELConfig.class), any(HookContext.class)))
                .thenReturn(new HookResult(true, "SUCCESS", "校验通过"));
            when(hookLogRepository.save(any())).thenReturn(new WorkflowHookLogEntity());

            // When
            HookExecutionSummary summary = hookService.executeAllHooks(
                "PRE_APPROVE", testInstance, testNode, testFormData, Map.of()
            );

            // Then
            assertThat(summary.allPassed()).isTrue();
            assertThat(summary.blockingMessages()).isEmpty();
        }

        @Test
        @DisplayName("should return blocking messages when validation fails")
        void shouldReturnBlockingMessagesWhenValidationFails() {
            // Given
            when(hookRepository.findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc(any(), any()))
                .thenReturn(List.of());
            when(spelExecutor.execute(any(SpELConfig.class), any(HookContext.class)))
                .thenReturn(new HookResult(false, "VALIDATION_FAILED", "金额必须大于100"));
            when(hookLogRepository.save(any())).thenReturn(new WorkflowHookLogEntity());

            // When
            HookExecutionSummary summary = hookService.executeAllHooks(
                "PRE_APPROVE", testInstance, testNode, testFormData, Map.of()
            );

            // Then
            assertThat(summary.allPassed()).isFalse();
            assertThat(summary.blockingMessages()).contains("金额必须大于100");
        }

        @Test
        @DisplayName("should add warning messages for execution hooks on failure")
        void shouldAddWarningMessagesForExecutionHooksOnFailure() {
            // Given
            testNode.setPostApproveAction("#formData.notify = true");
            when(hookRepository.findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc(any(), any()))
                .thenReturn(List.of());
            when(spelExecutor.execute(any(SpELConfig.class), any(HookContext.class)))
                .thenReturn(new HookResult(false, "EXECUTION_ERROR", "通知发送失败"));
            when(hookLogRepository.save(any())).thenReturn(new WorkflowHookLogEntity());

            // When
            HookExecutionSummary summary = hookService.executeAllHooks(
                "POST_APPROVE", testInstance, testNode, testFormData, Map.of()
            );

            // Then
            assertThat(summary.allPassed()).isTrue(); // 执行类钩子失败不影响 allPassed
            assertThat(summary.warningMessages()).contains("通知发送失败");
        }
    }

    @Nested
    @DisplayName("getConfiguredHookPoints Tests")
    class GetConfiguredHookPointsTests {

        @Test
        @DisplayName("should return distinct hook points for node")
        void shouldReturnDistinctHookPointsForNode() {
            // Given
            WorkflowNodeHookEntity hook1 = new WorkflowNodeHookEntity();
            hook1.setHookPoint("PRE_APPROVE");

            WorkflowNodeHookEntity hook2 = new WorkflowNodeHookEntity();
            hook2.setHookPoint("POST_APPROVE");

            WorkflowNodeHookEntity hook3 = new WorkflowNodeHookEntity();
            hook3.setHookPoint("PRE_APPROVE"); // Duplicate

            when(hookRepository.findByNodeIdAndDeletedFalseOrderByPriorityAsc("node-001"))
                .thenReturn(List.of(hook1, hook2, hook3));

            // When
            List<String> hookPoints = hookService.getConfiguredHookPoints("node-001");

            // Then
            assertThat(hookPoints).containsExactlyInAnyOrder("PRE_APPROVE", "POST_APPROVE");
            assertThat(hookPoints).hasSize(2); // No duplicates
        }
    }

    @Nested
    @DisplayName("Retry Logic Tests")
    class RetryLogicTests {

        @Test
        @DisplayName("should retry on failure when retryCount is configured")
        void shouldRetryOnFailureWhenRetryCountConfigured() throws Exception {
            // Given
            testHook.setRetryCount(2);
            when(hookRepository.findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc("node-001", "PRE_APPROVE"))
                .thenReturn(List.of(testHook));
            lenient().when(objectMapper.readValue(anyString(), eq(SpELConfig.class)))
                .thenReturn(new SpELConfig("#formData.amount > 100", null));
            when(hookLogRepository.save(any())).thenReturn(new WorkflowHookLogEntity());
            when(spelExecutor.execute(any(SpELConfig.class), any(HookContext.class)))
                .thenReturn(new HookResult(false, "FAILED", "失败"))  // First attempt
                .thenReturn(new HookResult(false, "FAILED", "失败"))  // Second attempt
                .thenReturn(new HookResult(true, "SUCCESS", "成功")); // Third attempt

            // When
            List<HookResult> results = hookService.executeTableHooks(
                "PRE_APPROVE", testInstance, testNode, testFormData, Map.of()
            );

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).success()).isTrue();
            assertThat(results.get(0).retryAttempts()).isEqualTo(2);
            verify(spelExecutor, times(3)).execute(any(), any());
        }
    }

    /**
     * Helper method to set executors map via reflection
     */
    private void setExecutors(Map<String, HookExecutor> executors) {
        try {
            var field = WorkflowHookServiceImpl.class.getDeclaredField("executors");
            field.setAccessible(true);
            field.set(hookService, executors);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}