package com.adminplus.service;

/**
 * Refresh Token 服务接口
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
public interface RefreshTokenService {

    /**
     * 创建 Refresh Token
     */
    String createRefreshToken(String userId);

    /**
     * 刷新 Access Token
     */
    String refreshAccessToken(String refreshToken);

    /**
     * 撤销 Refresh Token
     */
    void revokeRefreshToken(String token);

    /**
     * 撤销用户的所有 Refresh Token
     */
    void revokeAllUserTokens(String userId);

    /**
     * 清理过期的 Refresh Token
     */
    void cleanupExpiredTokens();
}