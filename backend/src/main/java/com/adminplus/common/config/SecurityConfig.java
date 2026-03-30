package com.adminplus.common.config;

import com.adminplus.common.filter.TokenBlacklistFilter;
import com.adminplus.common.properties.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
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
   <li>安全响应头</li>
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
    private final Environment env;
    private final JwtSecurityConfig jwtSecurityConfig;
    private final CorsSecurityConfig corsSecurityConfig;
    private final SecurityConstants securityConstants;

    public SecurityConfig(TokenBlacklistFilter tokenBlacklistFilter,
                          Environment env,
                          AppProperties appProperties) {
        this.tokenBlacklistFilter = tokenBlacklistFilter;
        this.env = env;
        this.jwtSecurityConfig = new JwtSecurityConfig(appProperties);
        this.corsSecurityConfig = new CorsSecurityConfig(appProperties, env);
        this.securityConstants = new SecurityConstants();
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
        boolean useCookieForJwt = resolveJwtStorageMode();

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
                .requestMatchers(securityConstants.PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/actuator/**").denyAll()
                .anyRequest().authenticated();
    }

    private void configureSecurityHeaders(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer<HttpSecurity> headersConfigurer) {
        headersConfigurer
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .frameOptions(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(securityConstants.HSTS_MAX_AGE_SECONDS)
                        .preload(true)
                );
    }

    private void configureCsrfProtection(HttpSecurity http, boolean useCookieForJwt) throws Exception {
        if (useCookieForJwt) {
            log.info("CSRF 保护已启用（Cookie 存储 JWT 模式）");
            http.csrf(csrf -> csrf
                    .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(securityConstants.AUTH_ENDPOINTS)
            );
        } else {
            log.info("CRSF 保护已禁用（Bearer Token 模式）");
            http.csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable);
        }
    }

    private boolean resolveJwtStorageMode() {
        boolean useCookieForJwt = Boolean.parseBoolean(
                env.getProperty("security.jwt.use-cookie", "false")
        );

        if (jwtSecurityConfig.isProduction() && env.getProperty("security.jwt.use-cookie") == null) {
            log.warn("生产环境未明确配置 security.jwt.use-cookie，使用默认值 false（Bearer Token 模式）");
        }

        return useCookieForJwt;
    }
}
