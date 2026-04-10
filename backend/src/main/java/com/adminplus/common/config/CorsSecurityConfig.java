package com.adminplus.common.config;

import com.adminplus.common.constant.SecurityConfigConstants;
import com.adminplus.common.properties.AppProperties;
import com.adminplus.utils.EnvUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 安全配置
 * <p>
 * 负责跨域资源共享配置：
 * <ul>
 *   <li>允许的域名（生产环境必须配置）</li>
 *   <li>允许的 HTTP 方法</li>
 *   <li>允许的请求头</li>
 *   <li>凭证传递</li>
 * </ul>
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Component
public class CorsSecurityConfig {

    private final AppProperties appProperties;
    private final boolean production;

    public CorsSecurityConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.production = EnvUtils.isProduction(appProperties.getEnv());
    }

    /**
     * 创建 CORS 配置源
     */
    public CorsConfigurationSource createConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        String corsAllowedOrigins = appProperties.getCors().getAllowedOrigins();
        if (corsAllowedOrigins != null && !corsAllowedOrigins.isEmpty()) {
            String[] origins = corsAllowedOrigins.split(",");
            configuration.setAllowedOriginPatterns(Arrays.asList(origins));
            log.info("CORS 已配置允许的域名: {}", Arrays.toString(origins));
        } else {
            handleMissingCorsConfiguration(configuration);
        }

        applyCorsCommonSettings(configuration);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void handleMissingCorsConfiguration(CorsConfiguration configuration) {
        if (isProduction()) {
            throw new IllegalStateException(
                    "生产环境必须配置 CORS 允许的域名！请设置环境变量 CORS_ALLOWED_ORIGINS（逗号分隔的域名列表）"
            );
        }
        log.info("开发环境：CORS 允许所有来源");
        configuration.setAllowedOriginPatterns(List.of("*"));
    }

    private void applyCorsCommonSettings(CorsConfiguration configuration) {
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(SecurityConfigConstants.CORS_MAX_AGE_SECONDS);
    }

    public boolean isProduction() {
        return production;
    }
}
