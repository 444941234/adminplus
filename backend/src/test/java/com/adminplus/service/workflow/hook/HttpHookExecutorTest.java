package com.adminplus.service.workflow.hook;

import com.adminplus.common.properties.AppProperties;
import com.adminplus.pojo.dto.workflow.hook.HookContext;
import com.adminplus.pojo.dto.workflow.hook.HookResult;
import com.adminplus.pojo.dto.workflow.hook.HttpConfig;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.service.workflow.hook.impl.HttpHookExecutor;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Unit tests for HttpHookExecutor using MockRestServiceServer
 */
@DisplayName("HttpHookExecutor Tests")
class HttpHookExecutorTest {

    private RestClient restClient;
    private MockRestServiceServer mockServer;
    private HttpHookExecutor executor;
    private JsonMapper objectMapper;
    private AppProperties appProperties;
    private WorkflowInstanceEntity testInstance;
    private WorkflowNodeEntity testNode;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().build();
        appProperties = new AppProperties();

        // Create RestClient.Builder and bind MockRestServiceServer BEFORE building RestClient
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();

        executor = new HttpHookExecutor(restClient, objectMapper, appProperties);

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

        // Mock server expects GET request and returns success response
        mockServer.expect(requestTo("http://example.com/api"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                    "{\"success\":true,\"message\":\"ok\",\"data\":\"result\"}",
                    MediaType.APPLICATION_JSON
                ));

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("ok");
        mockServer.verify();
    }

    @Test
    @DisplayName("Should execute HTTP POST request with body")
    void shouldExecutePostRequest() {
        // Given
        HttpConfig config = new HttpConfig("http://example.com/api", "POST", null, "{\"instanceId\":\"inst-001\"}");
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        mockServer.expect(requestTo("http://example.com/api"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.instanceId").value("inst-001"))
                .andRespond(withSuccess(
                    "{\"success\":true,\"created\":true}",
                    MediaType.APPLICATION_JSON
                ));

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        mockServer.verify();
    }

    @Test
    @DisplayName("Should handle HTTP error response")
    void shouldHandleHttpError() {
        // Given
        HttpConfig config = new HttpConfig("http://example.com/api", "GET", null, null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        mockServer.expect(requestTo("http://example.com/api"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"not found\"}")
                    .contentType(MediaType.APPLICATION_JSON));

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo("HTTP_ERROR");
        mockServer.verify();
    }

    @Test
    @DisplayName("Should include custom headers")
    void shouldIncludeCustomHeaders() {
        // Given
        HttpConfig config = new HttpConfig("http://example.com/api", "GET", Map.of("X-Custom-Header", "test-value"), null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        mockServer.expect(requestTo("http://example.com/api"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Custom-Header", "test-value"))
                .andRespond(withSuccess("{\"success\":true}", MediaType.APPLICATION_JSON));

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        mockServer.verify();
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

        mockServer.expect(requestTo("http://localhost/api"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"success\":true}", MediaType.APPLICATION_JSON));

        // When
        HookResult result = executor.execute(config, context);

        // Then
        assertThat(result.success()).isTrue();
        mockServer.verify();
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

    @Test
    @DisplayName("Should reject 127.0.0.1 internal URL")
    void shouldRejectLocalhostIp() {
        HttpConfig config = new HttpConfig("http://127.0.0.1/api", "GET", null, null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        HookResult result = executor.execute(config, context);

        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo("INVALID_URL");
        assertThat(result.message()).contains("不允许访问内网地址");
    }

    @Test
    @DisplayName("Should reject 10.x.x.x private IP")
    void shouldRejectPrivateIp10() {
        HttpConfig config = new HttpConfig("http://10.0.0.1/api", "GET", null, null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        HookResult result = executor.execute(config, context);

        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo("INVALID_URL");
        assertThat(result.message()).contains("不允许访问内网地址");
    }

    @Test
    @DisplayName("Should reject 192.168.x.x private IP")
    void shouldRejectPrivateIp192() {
        HttpConfig config = new HttpConfig("http://192.168.1.1/api", "GET", null, null);
        HookContext context = new HookContext(
            testInstance, testNode, Map.of(), "SUBMIT", "user-001", "Test User", Map.of()
        );

        HookResult result = executor.execute(config, context);

        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo("INVALID_URL");
        assertThat(result.message()).contains("不允许访问内网地址");
    }
}