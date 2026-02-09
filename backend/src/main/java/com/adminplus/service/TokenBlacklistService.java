package com.adminplus.service;

/**
 * JWT Token 撤销服务接口
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public interface TokenBlacklistService {

    /**
     * 将 Token 加入黑名单
     *
     * @param token Token 字符串
     * @param userId 用户ID
     */
    void blacklistToken(String token, String userId);

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param token Token 字符串
     * @return 是否在黑名单中
     */
    boolean isTokenBlacklisted(String token);

    /**
     * 将用户的所有 Token 加入黑名单（用户登出时）
     *
     * @param userId 用户ID
     */
    void blacklistAllUserTokens(String userId);

    /**
     * 清理过期的黑名单记录
     */
    void cleanupExpiredTokens();
}