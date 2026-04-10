package com.adminplus.common.config;

import com.adminplus.common.filter.XssFilter;
import com.adminplus.common.interceptor.RateLimitInterceptor;
import com.adminplus.common.properties.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final FileStorageProperties fileStorageProperties;

    /**
     * 配置 API 版本管理（Spring Boot 4.0 原生支持）
     * <p>
     * 支持以下版本解析方式：
     * 1. 请求头：X-API-Version
     * 2. URL 路径：/{version}/api/xxx
     * 3. 请求参数：?version=1.0
     * 4. 媒体类型参数：Accept: application/json;version=1.0
     * <p>
     * 使用方式：
     * - @GetMapping(version = "1.0") 匹配特定版本
     * - @GetMapping(version = "1.1+") 匹配 1.1 及以上版本
     * - 不指定 version 则匹配任意版本（优先级最低）
     */
    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        // 从请求头 X-API-Version 解析版本
        configurer.useRequestHeader("X-API-Version");

        // 可选：从 URL 路径段解析版本（如 /v1/users）
        // configurer.usePathSegment(0);  // 索引 0 表示第一个路径段

        // 可选：从请求参数解析版本
        // configurer.useRequestParameter("version");

        // 可选：从媒体类型参数解析版本
        // configurer.useMediaTypeParameter("version");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册限流拦截器 - 认证接口
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/auth/**")
                .order(1);

        // 限流拦截器 - 敏感操作接口
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                        "/sys/users/*/password",    // 密码重置
                        "/files/upload",            // 文件上传
                        "/workflow/**/approve",     // 工作流审批
                        "/workflow/**/reject",      // 工作流驳回
                        "/workflow/**/submit"       // 工作流提交
                )
                .order(2);
    }

    /**
     * 注册 XSS 过滤器
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("xssFilter");
        return registration;
    }

    /**
     * 配置静态资源映射 - 本地文件存储
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /uploads/** 到本地存储目录
        String basePath = fileStorageProperties.getLocal().getBasePath();
        String accessPrefix = fileStorageProperties.getLocal().getAccessPrefix();

        // 确保 basePath 以 file: 协议开头
        String location = basePath.startsWith("file:") ? basePath : "file:" + basePath + "/";

        registry.addResourceHandler(accessPrefix + "/**")
                .addResourceLocations(location);

        // Swagger UI 静态资源（springdoc-openapi 需要这些资源）
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");

        // Webjars 资源
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}