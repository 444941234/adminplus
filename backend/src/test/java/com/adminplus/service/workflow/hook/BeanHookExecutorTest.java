package com.adminplus.service.workflow.hook;

import com.adminplus.pojo.dto.workflow.hook.BeanConfig;
import com.adminplus.pojo.dto.workflow.hook.HookContext;
import com.adminplus.pojo.dto.workflow.hook.HookResult;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.service.workflow.hook.impl.BeanHookExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BeanHookExecutor
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BeanHookExecutor Tests")
class BeanHookExecutorTest {

    @Mock
    private ApplicationContext applicationContext;

    private BeanHookExecutor executor;
    private WorkflowInstanceEntity testInstance;
    private WorkflowNodeEntity testNode;

    @BeforeEach
    void setUp() {
        executor = new BeanHookExecutor(applicationContext);

        testInstance = new WorkflowInstanceEntity();
        testInstance.setId("inst-001");
        testInstance.setTitle("Test Workflow");

        testNode = new WorkflowNodeEntity();
        testNode.setId("node-001");
        testNode.setNodeName("Test Node");
    }

    @Test
    @DisplayName("Should return bean as executor type")
    void shouldReturnCorrectType() {
        assertThat(executor.getType()).isEqualTo("bean");
    }

    @Test
    @DisplayName("Should execute bean method successfully")
    void shouldExecuteBeanMethod() {
        // Given
        TestHookBean testBean = new TestHookBean();
        when(applicationContext.getBean("testHookBean")).thenReturn(testBean);

        BeanConfig config = new BeanConfig("testHookBean", "execute", Collections.emptyList());
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo("executed");
        assertThat(testBean.isCalled()).isTrue();
    }

    @Test
    @DisplayName("Should handle bean not found")
    void shouldHandleBeanNotFound() {
        // Given
        when(applicationContext.getBean("nonExistentBean"))
            .thenThrow(new org.springframework.beans.factory.NoSuchBeanDefinitionException("Bean not found"));

        BeanConfig config = new BeanConfig("nonExistentBean", "execute", Collections.emptyList());
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("Bean");
    }

    @Test
    @DisplayName("Should handle method not found")
    void shouldHandleMethodNotFound() {
        // Given
        TestHookBean testBean = new TestHookBean();
        when(applicationContext.getBean("testHookBean")).thenReturn(testBean);

        BeanConfig config = new BeanConfig("testHookBean", "nonExistentMethod", Collections.emptyList());
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo("METHOD_NOT_FOUND");
    }

    @Test
    @DisplayName("Should execute method with arguments")
    void shouldExecuteMethodWithArguments() {
        // Given
        TestHookBean testBean = new TestHookBean();
        when(applicationContext.getBean("testHookBean")).thenReturn(testBean);

        BeanConfig config = new BeanConfig("testHookBean", "executeWithArg", java.util.List.of("'test-arg'"));
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo("received: test-arg");
    }

    /**
     * Test bean for hook execution
     */
    public static class TestHookBean {
        private boolean called = false;

        public String execute() {
            this.called = true;
            return "executed";
        }

        public String executeWithArg(String arg) {
            this.called = true;
            return "received: " + arg;
        }

        public boolean isCalled() {
            return called;
        }
    }
}