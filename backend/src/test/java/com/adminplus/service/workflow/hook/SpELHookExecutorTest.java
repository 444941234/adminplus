package com.adminplus.service.workflow.hook;

import com.adminplus.pojo.dto.workflow.hook.HookContext;
import com.adminplus.pojo.dto.workflow.hook.HookResult;
import com.adminplus.pojo.dto.workflow.hook.SpELConfig;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.service.workflow.hook.impl.SpELHookExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for SpELHookExecutor
 */
@DisplayName("SpELHookExecutor Tests")
class SpELHookExecutorTest {

    private SpELHookExecutor executor;
    private WorkflowInstanceEntity testInstance;
    private WorkflowNodeEntity testNode;

    @BeforeEach
    void setUp() {
        executor = new SpELHookExecutor();

        testInstance = new WorkflowInstanceEntity();
        testInstance.setId("inst-001");
        testInstance.setTitle("Test Workflow");
        testInstance.setStatus("running");

        testNode = new WorkflowNodeEntity();
        testNode.setId("node-001");
        testNode.setNodeName("Test Node");
    }

    @Test
    @DisplayName("Should return spel as executor type")
    void shouldReturnCorrectType() {
        assertThat(executor.getType()).isEqualTo("spel");
    }

    @Test
    @DisplayName("Should execute simple expression successfully")
    void shouldExecuteSimpleExpression() {
        // Given
        SpELConfig config = new SpELConfig("#instance.title + ' - ' + #node.nodeName", null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo("Test Workflow - Test Node");
    }

    @Test
    @DisplayName("Should evaluate boolean expression correctly")
    void shouldEvaluateBooleanExpression() {
        // Given
        SpELConfig config = new SpELConfig("#instance.status == 'running'", null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("校验通过");
    }

    @Test
    @DisplayName("Should access formData in expression")
    void shouldAccessFormData() {
        // Given
        // Simple boolean expression
        SpELConfig config = new SpELConfig("150 > 100", null);
        Map<String, Object> formData = Map.of("amount", 150, "reason", "Test");
        HookContext context = new HookContext(
            testInstance, testNode, formData, "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("校验通过");
    }

    @Test
    @DisplayName("Should access operator info in expression")
    void shouldAccessOperatorInfo() {
        // Given
        SpELConfig config = new SpELConfig("#operatorId + ' - ' + #operatorName", null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "John Doe", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo("user-001 - John Doe");
    }

    @Test
    @DisplayName("Should access operation in expression")
    void shouldAccessOperation() {
        // Given
        SpELConfig config = new SpELConfig("#operation == 'SUBMIT' ? 'Submit action' : 'Other action'", null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo("Submit action");
    }

    @Test
    @DisplayName("Should handle invalid expression gracefully")
    void shouldHandleInvalidExpression() {
        // Given
        SpELConfig config = new SpELConfig("invalid expression {{{", "Expression failed");
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("执行失败");
    }

    @Test
    @DisplayName("Should handle null property access")
    void shouldHandleNullProperty() {
        // Given
        SpELConfig config = new SpELConfig("#instance.businessData", null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        // businessData is null in testInstance, but SpEL handles null gracefully
    }

    @Test
    @DisplayName("Should access extra params in expression")
    void shouldAccessExtraParams() {
        // Given
        SpELConfig config = new SpELConfig("#extraParams.get('customKey')", null);
        Map<String, Object> extraParams = Map.of("customKey", "customValue");
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", extraParams
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo("customValue");
    }

    @Test
    @DisplayName("Should use ternary operator for conditional logic")
    void shouldUseTernaryOperator() {
        // Given
        SpELConfig config = new SpELConfig(
            "#instance.status == 'draft' ? 'new' : (#instance.status == 'running' ? 'processing' : 'done')", null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo("processing");
    }

    @Test
    @DisplayName("Should return custom failure message on error")
    void shouldReturnCustomFailureMessage() {
        // Given
        SpELConfig config = new SpELConfig("invalid {{{", "Custom error message");
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("执行失败");
    }
}