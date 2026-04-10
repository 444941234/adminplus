package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.common.properties.AppProperties;
import com.adminplus.common.security.JwtTokenProvider;
import com.adminplus.pojo.entity.RefreshTokenEntity;
import com.adminplus.repository.RefreshTokenRepository;
import com.adminplus.service.RefreshTokenService;
import com.adminplus.utils.ServiceAssert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
    private final JwtTokenProvider jwtTokenProvider;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public String createRefreshToken(String userId) {
        refreshTokenRepository.deleteByUserId(userId);

        String token = jwtTokenProvider.generateRefreshToken(userId);
        Instant expiryDate = Instant.now().plus(
                appProperties.getJwt().getRefreshTokenExpirationDays(), ChronoUnit.DAYS);

        refreshTokenRepository.save(RefreshTokenEntity.builder()
                .userId(userId)
                .token(token)
                .expiryDate(expiryDate)
                .revoked(false)
                .build());

        log.info("创建 Refresh Token: userId={}", userId);
        return token;
    }

    @Override
    @Transactional
    public String refreshAccessToken(String token) {
        RefreshTokenEntity tokenEntity = findAndValidateToken(token);

        String userId = tokenEntity.getUserId();
        log.info("刷新 Access Token: userId={}", userId);
        return jwtTokenProvider.generateAccessToken(userId);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshTokenEntity tokenEntity = findAndValidateToken(token);
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

    private RefreshTokenEntity findAndValidateToken(String token) {
        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BizException("无效的 Refresh Token"));

        if (tokenEntity.getRevoked()) {
            ServiceAssert.fail("Refresh Token 已被撤销");
        }

        if (tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            ServiceAssert.fail("Refresh Token 已过期");
        }

        return tokenEntity;
    }
}