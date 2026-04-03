package com.adminplus.common.config;

import com.adminplus.common.properties.AppProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RateLimitInterceptor 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitInterceptor Unit Tests")
class RateLimitInterceptorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private RateLimitInterceptor interceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Setup default rate limit config
        AppProperties.RateLimit rateLimit = new AppProperties.RateLimit();
        rateLimit.setLoginMaxRequests(5);
        rateLimit.setLoginTimeWindow(60);
        rateLimit.setGeneralMaxRequests(100);
        rateLimit.setGeneralTimeWindow(60);
        when(appProperties.getRateLimit()).thenReturn(rateLimit);
    }

    @Nested
    @DisplayName("Login Endpoint Tests")
    class LoginEndpointTests {

        @Test
        @DisplayName("should allow request when under limit")
        void preHandle_LoginUnderLimit_ShouldAllow() throws Exception {
            request.setRequestURI("/v1/auth/login");
            request.setRemoteAddr("192.168.1.1");

            when(valueOperations.increment(anyString())).thenReturn(1L);

            boolean result = interceptor.preHandle(request, response, null);

            assertThat(result).isTrue();
            assertThat(response.getStatus()).isEqualTo(200);
            verify(redisTemplate).expire(anyString(), anyLong(), any());
        }

        @Test
        @DisplayName("should block request when over limit")
        void preHandle_LoginOverLimit_ShouldBlock() throws Exception {
            request.setRequestURI("/v1/auth/login");
            request.setRemoteAddr("192.168.1.1");

            when(valueOperations.increment(anyString())).thenReturn(6L);

            boolean result = interceptor.preHandle(request, response, null);

            assertThat(result).isFalse();
            assertThat(response.getStatus()).isEqualTo(429);
            assertThat(response.getContentAsString()).contains("请求过于频繁");
        }

        @Test
        @DisplayName("should allow request exactly at limit")
        void preHandle_LoginAtLimit_ShouldAllow() throws Exception {
            request.setRequestURI("/v1/auth/login");
            request.setRemoteAddr("192.168.1.1");

            when(valueOperations.increment(anyString())).thenReturn(5L);

            boolean result = interceptor.preHandle(request, response, null);

            assertThat(result).isTrue();
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("General Endpoint Tests")
    class GeneralEndpointTests {

        @Test
        @DisplayName("should apply general rate limit for other endpoints")
        void preHandle_GeneralEndpoint_ShouldApplyGeneralLimit() throws Exception {
            request.setRequestURI("/v1/users");
            request.setRemoteAddr("192.168.1.2");

            when(valueOperations.increment(anyString())).thenReturn(1L);

            boolean result = interceptor.preHandle(request, response, null);

            assertThat(result).isTrue();
            verify(valueOperations).increment(contains("general"));
        }

        @Test
        @DisplayName("should block when general limit exceeded")
        void preHandle_GeneralOverLimit_ShouldBlock() throws Exception {
            request.setRequestURI("/v1/files/upload");
            request.setRemoteAddr("192.168.1.2");

            when(valueOperations.increment(anyString())).thenReturn(101L);

            boolean result = interceptor.preHandle(request, response, null);

            assertThat(result).isFalse();
            assertThat(response.getStatus()).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("Different IP Tests")
    class DifferentIpTests {

        @Test
        @DisplayName("should track different IPs separately")
        void preHandle_DifferentIps_ShouldTrackSeparately() throws Exception {
            request.setRequestURI("/v1/auth/login");

            // First IP
            request.setRemoteAddr("192.168.1.1");
            when(valueOperations.increment(contains("192.168.1.1"))).thenReturn(1L);

            boolean result1 = interceptor.preHandle(request, response, null);
            assertThat(result1).isTrue();

            // Second IP - separate counter
            response = new MockHttpServletResponse();
            request.setRemoteAddr("192.168.1.2");
            when(valueOperations.increment(contains("192.168.1.2"))).thenReturn(1L);

            boolean result2 = interceptor.preHandle(request, response, null);
            assertThat(result2).isTrue();
        }
    }

    @Nested
    @DisplayName("Configurable Values Tests")
    class ConfigurableValuesTests {

        @Test
        @DisplayName("should use configured rate limit values")
        void preHandle_ShouldUseConfiguredValues() throws Exception {
            AppProperties.RateLimit customLimit = new AppProperties.RateLimit();
            customLimit.setLoginMaxRequests(3);
            customLimit.setLoginTimeWindow(30);
            customLimit.setGeneralMaxRequests(50);
            customLimit.setGeneralTimeWindow(30);
            when(appProperties.getRateLimit()).thenReturn(customLimit);

            request.setRequestURI("/v1/auth/login");
            request.setRemoteAddr("192.168.1.1");

            when(valueOperations.increment(anyString())).thenReturn(3L);

            boolean result = interceptor.preHandle(request, response, null);

            assertThat(result).isTrue();  // 3 is exactly at limit

            when(valueOperations.increment(anyString())).thenReturn(4L);
            response = new MockHttpServletResponse();

            result = interceptor.preHandle(request, response, null);
            assertThat(result).isFalse();  // 4 exceeds limit of 3
        }
    }
}