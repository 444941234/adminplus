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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
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

    @Value("${spring.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String corsAllowedOrigins;

    private final TokenBlacklistFilter tokenBlacklistFilter;
    private final Environment env;

    public SecurityConfig(TokenBlacklistFilter tokenBlacklistFilter, Environment env) {
        this.tokenBlacklistFilter = tokenBlacklistFilter;
        this.env = env;
    }

    /**
     * 密钥生成（开发环境）
     * 生产环境强制从环境变量读取 JWT_SECRET
     *
     * 安全要求：
     * - 生产环境必须配置 JWT_SECRET 环境变量
     * - 密钥长度至少 256 位（RSA 2048 位）
     * - 开发环境使用固定密钥或生成临时密钥
     * - 不记录密钥内容到日志，只记录密钥长度
     */
    @Bean
    public RSAKey rsaKey() throws JOSEException {
        // 生产环境：强制从环境变量读取 JWT_SECRET
        if (isProduction()) {
            if (jwtSecret == null || jwtSecret.isEmpty()) {
                throw new RuntimeException(
                    "生产环境必须配置 JWT 密钥！请设置环境变量 JWT_SECRET（至少 256 位）"
                );
            }

            try {
                RSAKey rsaKey = RSAKey.parse(jwtSecret);

                // 验证密钥长度（至少 2048 位，即 256 字节）
                int keySize = rsaKey.toRSAPublicKey().getModulus().bitLength();
                if (keySize < 2048) {
                    throw new RuntimeException(
                        String.format("JWT 密钥长度不足！当前：%d 位，要求：至少 2048 位", keySize)
                    );
                }

                // 只记录密钥长度，不记录密钥内容（防止密钥泄露）
                log.info("JWT 密��已从环境变量加载，密钥长度：{} 位", keySize);
                return rsaKey;

            } catch (Exception e) {
                throw new RuntimeException("JWT 密钥解析失败！请检查环境变量 JWT_SECRET 格式是否正确", e);
            }
        }

        // 开发环境：优先使用配置文件中的固定密钥，否则生成临时密钥
        String devSecret = env.getProperty("jwt.dev-secret");
        if (devSecret != null && !devSecret.isEmpty()) {
            try {
                RSAKey devKey = RSAKey.parse(devSecret);
                int keySize = devKey.toRSAPublicKey().getModulus().bitLength();
                log.info("开发环境：使用配置文件中的 JWT 密钥，密钥长度：{} 位", keySize);
                return devKey;
            } catch (Exception e) {
                log.warn("开发环境密钥解析失败，使用临时密钥");
            }
        }

        // 生成临时密钥（开发环境）
        RSAKey tempKey = new RSAKeyGenerator(2048)
                .keyID("adminplus-dev-key")
                .generate();

        // 只记录密钥长度，不记录密钥内容
        log.warn("开发环境：使用临时生成的 JWT 密钥，密钥长度：2048 位");
        return tempKey;
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
     * - 根据前端 JWT 存储方式决定是否启用 CSRF 保护
     * - 如果使用 Cookie 存储 JWT，必须启用 CSRF 保护（防止 CSRF 攻击）
     * - 如果使用 Bearer Token（localStorage/sessionStorage），可以安全地禁用 CSRF
     * - 生产环境强制检查 JWT 存储方式配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 读取 JWT 存储方式配置（默认为 false，即使用 Bearer Token）
        boolean useCookieForJwt = Boolean.parseBoolean(env.getProperty("security.jwt.use-cookie", "false"));

        // 生产环境强制检查 JWT 存储方式配置
        if (isProduction() && env.getProperty("security.jwt.use-cookie") == null) {
            log.warn("生产环境未明确配置 security.jwt.use-cookie，使用默认值 false（Bearer Token 方式）");
        }

        // 构建 SecurityFilterChain
        http
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
                );

        // 根据前端 JWT 存储方式配置 CSRF 保护
        if (useCookieForJwt) {
            // 如果使用 Cookie 存储 JWT，启用 CSRF 保护
            log.info("CSRF 保护已启用（Cookie 存储 JWT 模式）");

            http.csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    // 只忽略登录和注册端点（这些端点需要先获取 CSRF Token）
                    .ignoringRequestMatchers(
                            "/auth/login",
                            "/auth/register",
                            "/v1/auth/login",
                            "/v1/auth/register"
                    )
            );
        } else {
            // 如果使用 Bearer Token，可以安全地禁用 CSRF
            log.info("CSRF 保护已禁用（Bearer Token 模式）");
            http.csrf(AbstractHttpConfigurer::disable);
        }

        return http.build();
    }

    /**
     * CORS 配置源
     * 从配置文件读取允许的域名，限制跨域访问，防止 CSRF 攻击
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 从配置文件读取允许的域名
        if (corsAllowedOrigins != null && !corsAllowedOrigins.trim().isEmpty()) {
            String[] origins = corsAllowedOrigins.split(",");
            configuration.setAllowedOriginPatterns(java.util.Arrays.asList(origins));
            log.info("CORS 已配置允许的域名: {}", java.util.Arrays.toString(origins));
        } else {
            // 如果未配置，仅允许本地开发（生产环境会报错）
            if (isProduction()) {
                throw new RuntimeException(
                    "生产环境必须配置 CORS 允许的域名！请设置环境变量 CORS_ALLOWED_ORIGINS"
                );
            }
            configuration.setAllowedOriginPatterns(java.util.List.of("http://localhost:5173", "http://localhost:3000"));
            log.warn("⚠️  开发环境：CORS 使用默认配置（仅允许本地开发服务器）");
            log.warn("⚠️  警告：生产环境必须配置 CORS_ALLOWED_ORIGINS 环境变量！");
        }

        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
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