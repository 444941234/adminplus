package com.adminplus.common.config;

import com.adminplus.common.filter.TokenBlacklistFilter;
import com.adminplus.common.properties.AppProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

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

    // ==================== 常量定义 ====================

    /**
     * RSA 密钥最小位数（符合 NIST 推荐）
     */
    private static final int MIN_RSA_KEY_SIZE = 2048;

    /**
     * HSTS 最大缓存时间（1 年）
     */
    private static final long HSTS_MAX_AGE_SECONDS = Duration.ofDays(365).toSeconds();

    /**
     * CORS 预检请求缓存时间（1 小时）
     */
    private static final long CORS_MAX_AGE_SECONDS = Duration.ofHours(1).toSeconds();

    /**
     * 开发环境默认密钥 ID
     */
    private static final String DEV_KEY_ID = "adminplus-dev-key";

    /**
     * JWT 权限前缀
     */
    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * JWT 权限声明名称
     */
    private static final String AUTHORITIES_CLAIM_NAME = "scope";

    // ==================== 公开端点路径 ====================

    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/auth/register",
            "/uploads/**",
            "/captcha/**",
            "/v1/auth/login",
            "/v1/auth/register",
            "/v1/uploads/**",
            "/v1/captcha/**",
            "/actuator/health"
    };

    private static final String[] AUTH_ENDPOINTS = {
            "/auth/login",
            "/auth/register",
            "/v1/auth/login",
            "/v1/auth/register"
    };

    // ==================== 依赖注入 ====================

    private final TokenBlacklistFilter tokenBlacklistFilter;
    private final Environment env;
    private final AppProperties appProperties;
    private final boolean production;

    public SecurityConfig(TokenBlacklistFilter tokenBlacklistFilter, Environment env,
                          AppProperties appProperties) {
        this.tokenBlacklistFilter = tokenBlacklistFilter;
        this.env = env;
        this.appProperties = appProperties;
        this.production = isProductionEnv(appProperties.getEnv());
    }

    private static boolean isProductionEnv(String env) {
        return "prod".equalsIgnoreCase(env) || "production".equalsIgnoreCase(env);
    }

    /**
     * 密钥生成（开发环境）
     * 生产环境强制从环境变量读取 JWT_SECRET
     *
     * <p>安全要求：
     * <ul>
     *   <li>生产环境必须配置 JWT_SECRET 环境变量</li>
     *   <li>密钥长度至少 2048 位（RSA-2048，符合 NIST 推荐）</li>
     *   <li>开发环境使用固定密钥或生成临时密钥</li>
     *   <li>不记录密钥内容到日志，只记录密钥长度</li>
     * </ul>
     *
     * @return RSA 密钥
     * @throws JOSEException 密钥生成失败
     * @throws IllegalStateException 生产环境未配置密钥
     */
    @Bean
    public RSAKey rsaKey() throws JOSEException {
        if (isProduction()) {
            return createProductionRsaKey();
        }
        return createDevelopmentRsaKey();
    }

    /**
     * 创建生产环境 RSA 密钥
     */
    private RSAKey createProductionRsaKey() {
        String jwtSecret = appProperties.getJwt().getSecret();

        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new IllegalStateException(
                    "生产环境必须配置 JWT 密钥！请设置环境变量 JWT_SECRET（至少 2048 位 RSA）"
            );
        }

        try {
            RSAKey rsaKey = RSAKey.parse(jwtSecret);
            validateKeySize(rsaKey);
            log.info("JWT 密钥已从环境变量加载，密钥长度：{} 位", getKeySize(rsaKey));
            return rsaKey;
        } catch (Exception e) {
            throw new IllegalStateException("JWT 密钥解析失败！请检查环境变量 JWT_SECRET 格式（应为 JWK 格式的 RSA 密钥）", e);
        }
    }

    /**
     * 创建开发环境 RSA 密钥
     */
    private RSAKey createDevelopmentRsaKey() throws JOSEException {
        // 优先使用配置文件中的固定密钥
        String devSecret = appProperties.getJwt().getDevSecret();
        if (devSecret != null && !devSecret.isEmpty()) {
            try {
                RSAKey devKey = RSAKey.parse(devSecret);
                log.info("开发环境：使用配置文件中的 JWT 密钥，密钥长度：{} 位", getKeySize(devKey));
                return devKey;
            } catch (Exception e) {
                log.debug("开发环境配置密钥解析失败，将生成临时密钥: {}", e.getMessage());
            }
        }

        // 生成临时密钥
        RSAKey tempKey = new RSAKeyGenerator(MIN_RSA_KEY_SIZE)
                .keyID(DEV_KEY_ID)
                .generate();

        log.info("开发环境：已生成临时 JWT 密钥，长度：{} 位", MIN_RSA_KEY_SIZE);
        return tempKey;
    }

    /**
     * 验证 RSA 密钥长度
     *
     * @param rsaKey RSA 密钥
     * @throws IllegalStateException 密钥长度不足
     */
    private void validateKeySize(RSAKey rsaKey) {
        int keySize = getKeySize(rsaKey);
        if (keySize < MIN_RSA_KEY_SIZE) {
            throw new IllegalStateException(
                    String.format("JWT 密钥长度不足！当前：%d 位，要求：至少 %d 位（NIST 推荐）",
                            keySize, MIN_RSA_KEY_SIZE)
            );
        }
    }

    /**
     * 获取 RSA 密钥位数
     *
     * @param rsaKey RSA 密钥
     * @return 密钥位数
     */
    private int getKeySize(RSAKey rsaKey) {
        try {
            return rsaKey.toRSAPublicKey().getModulus().bitLength();
        } catch (JOSEException e) {
            throw new IllegalStateException("无法获取密钥长度", e);
        }
    }

    /**
     * JWT 编码器（用于登录时签发 Token）
     *
     * @param rsaKey RSA 密钥
     * @return JwtEncoder 实例
     */
    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * JWT 解码器（用于验证 Token）
     *
     * @param rsaKey RSA 密钥
     * @return JwtDecoder 实例
     */
    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) {
        try {
            return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        } catch (JOSEException e) {
            throw new IllegalStateException("创建 JWT 解码器失败", e);
        }
    }

    /**
     * 密码编码器（使用 BCrypt 算法）
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     *
     * @param config 认证配置
     * @return AuthenticationManager 实例
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * JWT 权限转换器
     * <p>将 JWT 中的 scope 声明转换为 Spring Security 的权限
     *
     * @return JwtAuthenticationConverter 实例
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix(ROLE_PREFIX);
        grantedAuthoritiesConverter.setAuthoritiesClaimName(AUTHORITIES_CLAIM_NAME);

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
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
     * <p>生产环境强制检查 security.jwt.use-cookie 配置
     *
     * @param http HttpSecurity 配置
     * @return SecurityFilterChain 实例
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        boolean useCookieForJwt = resolveJwtStorageMode();

        http
                .sessionManagement(this::configureSessionManagement)
                .authorizeHttpRequests(this::configureAuthorization)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .addFilterBefore(tokenBlacklistFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(this::configureSecurityHeaders);

        configureCsrfProtection(http, useCookieForJwt);

        return http.build();
    }

    /**
     * 配置会话管理策略
     */
    private void configureSessionManagement(org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer<HttpSecurity> session) {
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    /**
     * 配置授权规则
     */
    private void configureAuthorization(org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/actuator/**").denyAll()
                .anyRequest().authenticated();
    }

    /**
     * 配置安全响应头
     */
    private void configureSecurityHeaders(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer<HttpSecurity> headersConfigurer) {
        headersConfigurer
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(HSTS_MAX_AGE_SECONDS)
                        .preload(true)
                );
    }

    /**
     * 配置 CSRF 保护
     *
     * @param http            HttpSecurity
     * @param useCookieForJwt 是否使用 Cookie 存储 JWT
     * @throws Exception 配置过程中的异常
     */
    private void configureCsrfProtection(HttpSecurity http, boolean useCookieForJwt) throws Exception {
        if (useCookieForJwt) {
            log.info("CSRF 保护已启用（Cookie 存储 JWT 模式）");
            http.csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(AUTH_ENDPOINTS)
            );
        } else {
            log.info("CSRF 保护已禁用（Bearer Token 模式）");
            http.csrf(AbstractHttpConfigurer::disable);
        }
    }

    /**
     * 解析 JWT 存储方式配置
     *
     * @return true 表示使用 Cookie 存储，false 表示使用 Bearer Token
     */
    private boolean resolveJwtStorageMode() {
        boolean useCookieForJwt = Boolean.parseBoolean(
                env.getProperty("security.jwt.use-cookie", "false")
        );

        if (isProduction() && env.getProperty("security.jwt.use-cookie") == null) {
            log.warn("生产环境未明确配置 security.jwt.use-cookie，使用默认值 false（Bearer Token 模式）");
        }

        return useCookieForJwt;
    }

    /**
     * CORS 配置源
     * <p>从配置文件读取允许的域名，限制跨域访问，防止 CSRF 攻击
     * <p>生产环境必须配置 CORS 允许的域名
     *
     * @return CorsConfigurationSource 实例
     * @throws IllegalStateException 生产环境未配置 CORS 域名
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        String corsAllowedOrigins = appProperties.getCors().getAllowedOrigins();
        if (hasText(corsAllowedOrigins)) {
            String[] origins = corsAllowedOrigins.split(",");
            configuration.setAllowedOriginPatterns(Arrays.asList(origins));
            log.info("CORS 已配置允许的域名: {}", Arrays.toString(origins));
        } else {
            handleMissingCorsConfiguration();
            configuration.setAllowedOriginPatterns(List.of("*"));
        }

        applyCorsCommonSettings(configuration);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 处理缺失的 CORS 配置
     */
    private void handleMissingCorsConfiguration() {
        if (isProduction()) {
            throw new IllegalStateException(
                    "生产环境必须配置 CORS 允许的域名！请设置环境变量 CORS_ALLOWED_ORIGINS（逗号分隔的域名列表）"
            );
        }
        log.info("开发环境：CORS 允许所有来源");
    }

    /**
     * 应用 CORS 通用设置
     */
    private void applyCorsCommonSettings(CorsConfiguration configuration) {
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(CORS_MAX_AGE_SECONDS);
    }

    /**
     * 判断是否为生产环境
     *
     * @return true 如果当前是生产环境
     */
    private boolean isProduction() {
        return production;
    }
}