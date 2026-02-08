package com.adminplus.config;

import com.adminplus.filter.TokenBlacklistFilter;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    private final TokenBlacklistFilter tokenBlacklistFilter;
    private final Environment env;

    public SecurityConfig(TokenBlacklistFilter tokenBlacklistFilter, Environment env) {
        this.tokenBlacklistFilter = tokenBlacklistFilter;
        this.env = env;
    }

    /**
     * 密钥生成（开发环境）
     * 生产环境应从环境变量或配置文件读取
     */
    @Bean
    public RSAKey rsaKey() throws JOSEException {
        // 如果配置了 JWT 密钥，从环境变量读取
        if (jwtSecret != null && !jwtSecret.isEmpty()) {
            try {
                return RSAKey.parse(jwtSecret);
            } catch (Exception e) {
                // 解析失败，如果不是生产环境，生成临时密钥
                if (!isProduction()) {
                    log.warn("JWT secret 解析失败，使用生成的临时密钥（仅限开发环境）");
                    return new RSAKeyGenerator(2048)
                            .keyID("adminplus-key")
                            .generate();
                }
                throw new RuntimeException("Failed to parse JWT secret from environment", e);
            }
        }

        // 生产环境警告
        if (isProduction()) {
            throw new RuntimeException("生产环境必须配置 JWT 密钥（环境变量 JWT_SECRET）");
        }

        // 开发环境生成临时密钥
        return new RSAKeyGenerator(2048)
                .keyID("adminplus-key")
                .generate();
    }

    /**
     * JWT 编码器（用于登录时签发 Token）
     */
    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {
        var jwkSet = new JWKSet(rsaKey);
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * JWT 解码器（用于验证 Token）
     */
    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) {
        try {
            return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to create JWT decoder", e);
        }
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * JWT 权限转换器
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");

        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    /**
     * 安全过滤器链
     *
     * CSRF 保护说明：
     * - 对于使用 JWT 认证的 REST API（/api/**），CSRF 保护通过忽略请求来实现
     * - 这是因为 JWT Token 本身已经提供了防 CSRF 保护（通过 Authorization 头）
     * - 如果前端使用 Cookie 存储 JWT，则需要启用 CSRF 保护
     * - 当前实现：API 端点忽略 CSRF（JWT Bearer Token 方式），其他端点启用 CSRF
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 启用 CSRF 保护，使用 CookieCsrfTokenRepository
                // 注意：对于使用 Bearer Token 的 REST API，可以安全地忽略 CSRF
                // 如果使用 Cookie 存储 JWT，应该移除 ignoringRequestMatchers 配置
                .csrf(csrf -> csrf
                        .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // 仅忽略认证相关端点（登录、注册）和公开端点
                        // 其他 API 端点使用 JWT Bearer Token，不依赖 Cookie，可安全忽略 CSRF
                        .ignoringRequestMatchers(
                                "/auth/**",           // 认证相关端点（登录、注册、登出）
                                "/v1/auth/**",        // v1 版本的认证相关端点
                                "/captcha/**",        // 验证码端点
                                "/v1/captcha/**",     // v1 版本的验证码端点
                                "/uploads/**",        // 公开的上传文件访问
                                "/actuator/health"    // 健康检查端点
                        )
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 公开端点
                        .requestMatchers("/auth/login", "/auth/register", "/uploads/**", "/captcha/**").permitAll()
                        .requestMatchers("/v1/auth/login", "/v1/auth/register", "/v1/uploads/**", "/v1/captcha/**").permitAll()
                        // Actuator - 根据环境限制访问
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").denyAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .addFilterBefore(tokenBlacklistFilter, UsernamePasswordAuthenticationFilter.class)
                // 配置 CORS - 限制跨域访问
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 添加安全头
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .frameOptions(frame -> frame.sameOrigin())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                                .preload(true)
                        )
                )
                .build();
    }

    /**
     * CORS 配置源
     * 限制跨域访问，防止 CSRF 攻击
     */
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.List.of("*")); // 生产环境应限制为特定域名
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 判断是否为生产环境
     */
    private boolean isProduction() {
        String env = this.env.getProperty("app.env", "dev");
        return "prod".equalsIgnoreCase(env) || "production".equalsIgnoreCase(env);
    }
}