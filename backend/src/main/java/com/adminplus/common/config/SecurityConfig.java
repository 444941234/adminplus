package com.adminplus.common.config;

import com.adminplus.common.constant.PublicEndpointConstants;
import com.adminplus.common.constant.SecurityConfigConstants;
import com.adminplus.common.filter.TokenBlacklistFilter;
import com.adminplus.common.properties.AppProperties;
import com.adminplus.utils.EnvUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;


/**
 * Spring Security 主配置
 * <p>
 * 组合各安全模块，提供完整的安全配置：
 * <ul>
 *   <li>JWT 认证</li>
 *   <li>CSRF 保护</li>
 *   <li>CORS 跨域</li>
 *   <li>安全响应头</li>
 * </ul>
 * </p>
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@Component
@EnableWebSecurity
public class SecurityConfig {

    private final TokenBlacklistFilter tokenBlacklistFilter;
    private final JwtSecurityConfig jwtSecurityConfig;
    private final CorsSecurityConfig corsSecurityConfig;
    private final boolean useCookieForJwt;

    public SecurityConfig(TokenBlacklistFilter tokenBlacklistFilter,
                          JwtSecurityConfig jwtSecurityConfig,
                          CorsSecurityConfig corsSecurityConfig,
                          AppProperties appProperties) {
        this.tokenBlacklistFilter = tokenBlacklistFilter;
        this.jwtSecurityConfig = jwtSecurityConfig;
        this.corsSecurityConfig = corsSecurityConfig;
        this.useCookieForJwt = resolveJwtStorageMode(appProperties);
    }

    /**
     * 安全过滤器链配置
     *
     * <p>CSRF 保护策略：
     * <ul>
     *   <li>Cookie 存储 JWT：启用 CSRF 保护（使用 CookieCsrfTokenRepository）</li>
     *   <li>Bearer Token（localStorage/sessionStorage）：安全禁用 CSRF</li>
     * </ul>
     *
     * @param http HttpSecurity 配置
     * @return SecurityFilterChain 实例
     */
    @org.springframework.context.annotation.Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(this::configureSessionManagement)
                .authorizeHttpRequests(this::configureAuthorization)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtSecurityConfig.jwtAuthenticationConverter())
                        )
                )
                .addFilterBefore(tokenBlacklistFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsSecurityConfig.createConfigurationSource()))
                .headers(this::configureSecurityHeaders);

        configureCsrfProtection(http, useCookieForJwt);

        return http.build();
    }

    private void configureSessionManagement(org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer<HttpSecurity> session) {
        session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS);
    }

    private void configureAuthorization(org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers(PublicEndpointConstants.PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/**").denyAll()
                .anyRequest().authenticated();
    }

    private void configureSecurityHeaders(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer<HttpSecurity> headersConfigurer) {
        headersConfigurer
                // CSP 配置：允许 Swagger UI 和 API 文档正常工作
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data:; " +
                        "font-src 'self' data:; " +
                        "frame-ancestors 'self'; " +
                        "form-action 'self'"))
                .frameOptions(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(SecurityConfigConstants.HSTS_MAX_AGE_SECONDS)
                        .preload(true)
                );
    }

    private void configureCsrfProtection(HttpSecurity http, boolean useCookieForJwt) throws Exception {
        if (useCookieForJwt) {
            log.info("CSRF 保护已启用（Cookie 存储 JWT 模式）");
            http.csrf(csrf -> csrf
                    .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(PublicEndpointConstants.AUTH_ENDPOINTS)
            );
        } else {
            log.info("CRSF 保护已禁用（Bearer Token 模式）");
            http.csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable);
        }
    }

    private boolean resolveJwtStorageMode(AppProperties appProperties) {
        boolean useCookieForJwt = appProperties.getJwt().isUseCookie();

        if (EnvUtils.isProduction(appProperties.getEnv()) && !useCookieForJwt) {
            log.warn("生产环境未明确配置 security.jwt.use-cookie，使用默认值 false（Bearer Token 模式）");
        }

        return useCookieForJwt;
    }
}
