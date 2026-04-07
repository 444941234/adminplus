package com.adminplus.common.config;

import com.adminplus.common.filter.XssFilter;
import com.adminplus.common.interceptor.RateLimitInterceptor;
import com.adminplus.common.properties.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册限流拦截器 - 认证接口
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/v1/auth/**")
                .order(1);

        // 限流拦截器 - 敏感操作接口
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                        "/v1/sys/users/*/password",    // 密码重置
                        "/v1/files/upload",            // 文件上传
                        "/v1/workflow/**/approve",     // 工作流审批
                        "/v1/workflow/**/reject",      // 工作流驳回
                        "/v1/workflow/**/submit"       // 工作流提交
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
    }
}