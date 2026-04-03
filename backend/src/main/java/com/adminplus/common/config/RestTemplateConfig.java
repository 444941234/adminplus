package com.adminplus.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置
 * <p>
 * 使用 Apache HttpClient 作为底层实现，支持连接池
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        // 设置连接超时时间（5秒）
        factory.setConnectTimeout(5000);
        // 设置读取超时时间（30秒）
        factory.setReadTimeout(30000);
        // 设置从连接池获取连接的超时时间（3秒）
        factory.setConnectionRequestTimeout(3000);
        return new RestTemplate(factory);
    }
}
