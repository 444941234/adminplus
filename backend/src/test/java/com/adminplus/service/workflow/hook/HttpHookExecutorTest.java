package com.adminplus.service.workflow.hook;

import com.adminplus.common.properties.AppProperties;
import com.adminplus.pojo.dto.workflow.hook.HookContext;
import com.adminplus.pojo.dto.workflow.hook.HookResult;
import com.adminplus.pojo.dto.workflow.hook.HttpConfig;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.service.workflow.hook.impl.HttpHookExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HttpHookExecutor
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HttpHookExecutor Tests")
class HttpHookExecutorTest {

    @Mock
    private RestTemplate restTemplate;

    private HttpHookExecutor executor;
    private ObjectMapper objectMapper;
    private AppProperties appProperties;
    private WorkflowInstanceEntity testInstance;
    private WorkflowNodeEntity testNode;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        appProperties = new AppProperties();
        executor = new HttpHookExecutor(restTemplate, objectMapper, appProperties);

        testInstance = new WorkflowInstanceEntity();
        testInstance.setId("inst-001");
        testInstance.setTitle("Test Workflow");

        testNode = new WorkflowNodeEntity();
        testNode.setId("node-001");
        testNode.setNodeName("Test Node");
    }

    @Test
    @DisplayName("Should return http as executor type")
    void shouldReturnCorrectType() {
        assertThat(executor.getType()).isEqualTo("http");
    }

    @Test
    @DisplayName("Should execute HTTP GET request successfully")
    void shouldExecuteGetRequest() {
        // Given
        HttpConfig config = new HttpConfig("http://example.com/api", "GET", null, null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        ResponseEntity<Map> response = new ResponseEntity<>(
            Map.of("success", true, "message", "ok", "data", "result"),
            HttpStatus.OK
        );
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Map.class)
        )).thenReturn(response);

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("ok");
    }

    @Test
    @DisplayName("Should execute HTTP POST request with body")
    void shouldExecutePostRequest() {
        // Given
        HttpConfig config = new HttpConfig("http://example.com/api", "POST", null, "{\"instanceId\":\"#instance.id\"}");
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        ResponseEntity<Map> response = new ResponseEntity<>(
            Map.of("success", true, "created", true),
            HttpStatus.CREATED
        );
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(Map.class)
        )).thenReturn(response);

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
    }

    @Test
    @DisplayName("Should handle HTTP error response")
    void shouldHandleHttpError() {
        // Given
        HttpConfig config = new HttpConfig("http://example.com/api", "GET", null, null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        ResponseEntity<Map> response = new ResponseEntity<>(
            Map.of("error", "not found"),
            HttpStatus.NOT_FOUND
        );
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Map.class)
        )).thenReturn(response);

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo("HTTP_ERROR");
    }

    @Test
    @DisplayName("Should handle connection error")
    void shouldHandleConnectionError() {
        // Given
        HttpConfig config = new HttpConfig("http://nonexistent.example.com/api", "GET", null, null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Map.class)
        )).thenThrow(new RestClientException("Connection refused"));

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("执行失败");
    }

    @Test
    @DisplayName("Should include custom headers")
    void shouldIncludeCustomHeaders() {
        // Given
        HttpConfig config = new HttpConfig("http://example.com/api", "GET", Map.of("X-Custom-Header", "test-value"), null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        ResponseEntity<Map> response = new ResponseEntity<>(Map.of("success", true), HttpStatus.OK);
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Map.class)
        )).thenReturn(response);

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        verify(restTemplate).exchange(
            anyString(),
            eq(HttpMethod.GET),
            argThat(entity -> entity.getHeaders().get("X-Custom-Header") != null),
            eq(Map.class)
        );
    }

    @Test
    @DisplayName("Should reject invalid URL schemes")
    void shouldRejectInvalidScheme() {
        // Given
        HttpConfig config = new HttpConfig("ftp://example.com/file", "GET", null, null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo("INVALID_URL");
        assertThat(result.message()).contains("只支持HTTP/HTTPS");
    }

    @Test
    @DisplayName("Should reject internal URLs by default")
    void shouldRejectInternalUrlsByDefault() {
        // Given - allowInternalUrls defaults to false
        HttpConfig config = new HttpConfig("http://localhost/api", "GET", null, null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo("INVALID_URL");
        assertThat(result.message()).contains("不允许访问内网地址");
    }

    @Test
    @DisplayName("Should allow internal URLs when explicitly enabled")
    void shouldAllowInternalUrlsWhenEnabled() {
        // Given
        appProperties.getWorkflowHook().setAllowInternalUrls(true);
        HttpConfig config = new HttpConfig("http://localhost/api", "GET", null, null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        ResponseEntity<Map> response = new ResponseEntity<>(Map.of("success", true), HttpStatus.OK);
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Map.class)
        )).thenReturn(response);

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
    }

    @Test
    @DisplayName("Should enforce URL whitelist when configured")
    void shouldEnforceUrlWhitelist() {
        // Given
        appProperties.getWorkflowHook().setAllowedUrlPatterns("https://api.example.com/*");
        HttpConfig config = new HttpConfig("https://other.com/api", "GET", null, null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo("INVALID_URL");
        assertThat(result.message()).contains("URL不在允许列表中");
    }
}