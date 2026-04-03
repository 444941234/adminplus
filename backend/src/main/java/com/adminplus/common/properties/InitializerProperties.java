package com.adminplus.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 初始化配置属性
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.initializer")
public class InitializerProperties {

    /**
     * 默认用户密码
     * 生产环境必须通过环境变量修改
     */
    private String defaultPassword = "admin123";
}