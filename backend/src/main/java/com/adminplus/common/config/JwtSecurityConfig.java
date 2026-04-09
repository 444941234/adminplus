package com.adminplus.common.config;

import com.adminplus.common.properties.AppProperties;
import com.adminplus.service.PermissionService;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JWT 安全配置
 * <p>
 * 负责 JWT 相关的 Bean 配置：
 * <ul>
 *   <li>RSA 密钥生成与管理</li>
 *   <li>JWT 编码器与解码器</li>
   *   <li>权限转换器</li>
 *   <li>密码编码器</li>
 *   <li>认证管理器</li>
 * </ul>
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Configuration
public class JwtSecurityConfig {

    private static final int MIN_RSA_KEY_SIZE = 2048;
    private static final String DEV_KEY_ID = "adminplus-dev-key";

    private final AppProperties appProperties;
    private final boolean production;
    private final PermissionService permissionService;

    public JwtSecurityConfig(AppProperties appProperties, PermissionService permissionService) {
        this.appProperties = appProperties;
        this.permissionService = permissionService;
        this.production = isProductionEnv(appProperties.getEnv());
    }

    private static boolean isProductionEnv(String env) {
        return "prod".equalsIgnoreCase(env) || "production".equalsIgnoreCase(env);
    }

    @Bean
    public RSAKey rsaKey() throws JOSEException {
        if (isProduction()) {
            return createProductionRsaKey();
        }
        return createDevelopmentRsaKey();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) {
        try {
            return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        } catch (JOSEException e) {
            throw new IllegalStateException("创建 JWT 解码器失败", e);
        }
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();

            // 从数据库加载用户的具体权限（如 workflow:form:view）
            String userId = jwt.getClaimAsString("userId");
            if (userId != null) {
                try {
                    List<String> permissions = permissionService.getUserPermissions(userId);
                    for (String permission : permissions) {
                        authorities.add(new SimpleGrantedAuthority(permission));
                    }
                } catch (Exception e) {
                    log.warn("加载用户权限失败: userId={}, error={}", userId, e.getMessage());
                }
            }

            return authorities;
        });
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

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

    private RSAKey createDevelopmentRsaKey() throws JOSEException {
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

        RSAKey tempKey = new RSAKeyGenerator(MIN_RSA_KEY_SIZE)
                .keyID(DEV_KEY_ID)
                .generate();

        log.info("开发环境：已生成临时 JWT 密钥，长度：{} 位", MIN_RSA_KEY_SIZE);
        return tempKey;
    }

    private void validateKeySize(RSAKey rsaKey) {
        int keySize = getKeySize(rsaKey);
        if (keySize < MIN_RSA_KEY_SIZE) {
            throw new IllegalStateException(
                    String.format("JWT 密钥长度不足！当前：%d 位，要求：至少 %d 位（NIST 推荐）",
                            keySize, MIN_RSA_KEY_SIZE)
            );
        }
    }

    private int getKeySize(RSAKey rsaKey) {
        try {
            return rsaKey.toRSAPublicKey().getModulus().bitLength();
        } catch (JOSEException e) {
            throw new IllegalStateException("无法获取密钥长度", e);
        }
    }

    public boolean isProduction() {
        return production;
    }
}
