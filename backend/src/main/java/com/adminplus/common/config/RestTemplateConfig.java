package com.adminplus.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置
 * <p>
 * 基础 RestTemplate 配置
 * 注：超时等配置已移至 application.yml 的 spring.http.clients 属性
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
