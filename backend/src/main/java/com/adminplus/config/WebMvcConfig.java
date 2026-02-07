package com.adminplus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置路径匹配，使用后缀匹配模式
     * 确保 REST 控制器的路由优先级高于静态资源
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }

    /**
     * 配置静态资源处理器
     * 允许访问上传的头像文件
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置头像文件的访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}