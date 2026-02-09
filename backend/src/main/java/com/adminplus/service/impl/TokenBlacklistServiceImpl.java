package com.adminplus.service.impl;

import com.adminplus.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

/**
 * JWT Token 撤销服务实现（基于 Redis 黑名单）
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    // Token 黑名单 Redis 键前缀
    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";

    // 用户 Token 集合键前缀
    private static final String USER_TOKENS_KEY_PREFIX = "user:tokens:";

    // Token 过期时间（2 小时，与 JWT 过期时间一致）
    private static final Duration TOKEN_EXPIRATION = Duration.ofHours(2);

    @Override
    public void blacklistToken(String token, String userId) {
        if (token == null || token.isEmpty()) {
            return;
        }

        // 生成 Token 的哈希值作为键
        String tokenHash = hashToken(token);
        String blacklistKey = BLACKLIST_KEY_PREFIX + tokenHash;

        // 将 Token 加入黑名单，设置过期时间为 2 小时
        redisTemplate.opsForValue().set(blacklistKey, String.valueOf(userId), TOKEN_EXPIRATION);

        // 将 Token 添加到用户的 Token 集合
        String userTokensKey = USER_TOKENS_KEY_PREFIX + userId;
        redisTemplate.opsForSet().add(userTokensKey, tokenHash);

        log.info("Token 已加入黑名单: userId={}, tokenHash={}", userId, tokenHash);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String tokenHash = hashToken(token);
        String blacklistKey = BLACKLIST_KEY_PREFIX + tokenHash;

        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }

    @Override
    public void blacklistAllUserTokens(String userId) {
        if (userId == null) {
            return;
        }

        String userTokensKey = USER_TOKENS_KEY_PREFIX + userId;

        // 获取用户的所有 Token 哈希
        Set<String> tokenHashes = redisTemplate.opsForSet().members(userTokensKey);
        if (tokenHashes != null && !tokenHashes.isEmpty()) {
            // 将所有 Token 加入黑名单
            for (String tokenHash : tokenHashes) {
                String blacklistKey = BLACKLIST_KEY_PREFIX + tokenHash;
                redisTemplate.opsForValue().set(blacklistKey, String.valueOf(userId), TOKEN_EXPIRATION);
            }
        }

        log.info("用户的所有 Token 已加入黑名单: userId={}, count={}", userId, tokenHashes != null ? tokenHashes.size() : 0);
    }

    @Override
    public void cleanupExpiredTokens() {
        // Redis 会自动清理过期的键，这里主要用于日志记录
        log.debug("Token 黑名单清理任务执行（Redis 自动处理过期键）");
    }

    /**
     * 对 Token 进行哈希处理
     *
     * @param token Token 字符串
     * @return 哈希值
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash).substring(0, 32);
        } catch (Exception e) {
            log.error("Token 哈希失败", e);
            return token;
        }
    }
}