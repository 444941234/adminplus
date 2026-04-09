package com.adminplus.common.security;

import com.adminplus.common.properties.AppProperties;
import com.adminplus.pojo.dto.response.UserResponse;
import com.adminplus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * JWT Token 生成工具类
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final AppProperties appProperties;
    private final UserService userService;

    /**
     * 生成 Access Token（短期，用于 API 认证）
     *
     * @param userId 用户ID
     * @return JWT token
     */
    public String generateAccessToken(String userId) {
        UserResponse user = userService.getUserById(userId);

        Instant now = Instant.now();
        int expirationHours = appProperties.getJwt().getAccessTokenExpirationHours();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("adminplus")
                .issuedAt(now)
                .expiresAt(now.plus(expirationHours, ChronoUnit.HOURS))
                .subject(user.username())
                .claim("userId", user.id())
                .claim("username", user.username())
                .claim("deptId", user.deptId())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * 生成 Refresh Token（长期，用于刷新 Access Token）
     *
     * @param userId 用户ID
     * @return JWT token
     */
    public String generateRefreshToken(String userId) {
        Instant now = Instant.now();
        int expirationDays = appProperties.getJwt().getRefreshTokenExpirationDays();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("adminplus")
                .issuedAt(now)
                .expiresAt(now.plus(expirationDays, ChronoUnit.DAYS))
                .subject(userId)
                .claim("userId", userId)
                .claim("type", "refresh")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}