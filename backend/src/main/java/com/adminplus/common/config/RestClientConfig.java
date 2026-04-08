package com.adminplus.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient 配置
 *
 * Spring Boot 4.0 推荐使用 RestClient 替代 RestTemplate。
 * RestClient 提供了更现代的函数式 API，支持流畅的请求构建。
 *
 * @author AdminPlus
 * @since 2026-04-08
 */
@Configuration
public class RestClientConfig {

    /**
     * 创建 RestClient.Builder Bean
     *
     * 注入此 Builder 到需要使用 HTTP 客户端的 Service 中，
     * 可以针对不同服务创建定制化的 RestClient 实例。
     *
     * Spring Boot 会自动配置：
     * - Jackson 3 的 HttpMessageConverter
     * - 合理的连接超时和读取超时
     * - 连接池等优化
     *
     * 示例用法：
     * <pre>
     * &#64;Service
     * public class MyService {
     *     private final RestClient restClient;
     *
     *     public MyService(RestClient.Builder builder) {
     *         this.restClient = builder
     *             .baseUrl("https://api.example.com")
     *             .build();
     *     }
     * }
     * </pre>
     *
     * 如需自定义超时等配置，可在 application.yml 中设置：
     * <pre>
     * spring:
     *   http:
     *     clients:
     *       imperative:
     *         connect-timeout: 5s
     *         read-timeout: 30s
     * </pre>
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * 创建默认 RestClient Bean
     *
     * 用于 HttpHookExecutor 等需要直接注入 RestClient 的组件。
     * 此 Bean 使用 Spring Boot 自动配置的默认设置。
     */
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}
