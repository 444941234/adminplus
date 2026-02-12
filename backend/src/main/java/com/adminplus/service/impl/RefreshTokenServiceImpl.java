package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.entity.RefreshTokenEntity;
import com.adminplus.repository.RefreshTokenRepository;
import com.adminplus.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Refresh Token 服务实现
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtEncoder jwtEncoder;

    // Refresh Token 有效期：7 天
    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;

    // Access Token 有效期：2 小时
    private static final long ACCESS_TOKEN_EXPIRY_HOURS = 2;

    @Override
    @Transactional
    public String createRefreshToken(String userId) {
        // 撤销用户之前的所有 Refresh Token
        refreshTokenRepository.deleteByUserId(userId);

        // 生成新的 Refresh Token
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(REFRESH_TOKEN_EXPIRY_DAYS, ChronoUnit.DAYS);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .userId(userId)
                .token(token)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("创建 Refresh Token: userId={}", userId);

        return token;
    }

    @Override
    @Transactional
    public String refreshAccessToken(String refreshToken) {
        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BizException("无效的 Refresh Token"));

        // 检查 Token 是否被撤销
        if (tokenEntity.getRevoked()) {
            throw new BizException("Refresh Token 已被撤销");
        }

        // 检查 Token 是否过期
        if (tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            throw new BizException("Refresh Token 已过期");
        }

        // 生成新的 Access Token
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("adminplus")
                .issuedAt(now)
                .expiresAt(now.plus(ACCESS_TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS))
                .subject(String.valueOf(tokenEntity.getUserId()))
                .claim("userId", tokenEntity.getUserId())
                .claim("scope", "ROLE_USER")
                .build();

        String newAccessToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        log.info("刷新 Access Token: userId={}", tokenEntity.getUserId());

        return newAccessToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BizException("无效的 Refresh Token"));

        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);
        log.info("撤销 Refresh Token: userId={}", tokenEntity.getUserId());
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("撤销用户所有 Refresh Token: userId={}", userId);
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
        log.info("清理过期的 Refresh Token");
    }
}