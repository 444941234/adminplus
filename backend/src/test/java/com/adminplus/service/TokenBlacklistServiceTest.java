package com.adminplus.service;

import com.adminplus.service.impl.TokenBlacklistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.SetOperations;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TokenBlacklistService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService Unit Tests")
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private TokenBlacklistServiceImpl tokenBlacklistService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Nested
    @DisplayName("blacklistToken Tests")
    class BlacklistTokenTests {

        @Test
        @DisplayName("should blacklist valid token")
        void blacklistToken_ShouldAddToBlacklist() {
            // Given
            String token = "valid.jwt.token";
            String userId = "user-001";

            // When
            tokenBlacklistService.blacklistToken(token, userId);

            // Then
            verify(valueOperations).set(anyString(), eq(userId), any(Duration.class));
            verify(setOperations).add(anyString(), anyString());
        }

        @Test
        @DisplayName("should do nothing when token is null")
        void blacklistToken_WithNullToken_ShouldDoNothing() {
            // When
            tokenBlacklistService.blacklistToken(null, "user-001");

            // Then
            verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
        }

        @Test
        @DisplayName("should do nothing when token is empty")
        void blacklistToken_WithEmptyToken_ShouldDoNothing() {
            // When
            tokenBlacklistService.blacklistToken("", "user-001");

            // Then
            verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
        }
    }

    @Nested
    @DisplayName("isTokenBlacklisted Tests")
    class IsTokenBlacklistedTests {

        @Test
        @DisplayName("should return true when token is blacklisted")
        void isTokenBlacklisted_WhenBlacklisted_ShouldReturnTrue() {
            // Given
            String token = "blacklisted.jwt.token";
            when(redisTemplate.hasKey(anyString())).thenReturn(true);

            // When
            boolean result = tokenBlacklistService.isTokenBlacklisted(token);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when token is not blacklisted")
        void isTokenBlacklisted_WhenNotBlacklisted_ShouldReturnFalse() {
            // Given
            String token = "valid.jwt.token";
            when(redisTemplate.hasKey(anyString())).thenReturn(false);

            // When
            boolean result = tokenBlacklistService.isTokenBlacklisted(token);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when token is null")
        void isTokenBlacklisted_WithNullToken_ShouldReturnFalse() {
            // When
            boolean result = tokenBlacklistService.isTokenBlacklisted(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when token is empty")
        void isTokenBlacklisted_WithEmptyToken_ShouldReturnFalse() {
            // When
            boolean result = tokenBlacklistService.isTokenBlacklisted("");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("blacklistAllUserTokens Tests")
    class BlacklistAllUserTokensTests {

        @Test
        @DisplayName("should blacklist all user tokens")
        void blacklistAllUserTokens_ShouldBlacklistAll() {
            // Given
            String userId = "user-001";
            Set<String> tokenHashes = Set.of("hash1", "hash2", "hash3");
            when(setOperations.members(anyString())).thenReturn(tokenHashes);

            // When
            tokenBlacklistService.blacklistAllUserTokens(userId);

            // Then
            verify(valueOperations, times(3)).set(anyString(), anyString(), any(Duration.class));
        }

        @Test
        @DisplayName("should do nothing when userId is null")
        void blacklistAllUserTokens_WithNullUserId_ShouldDoNothing() {
            // When
            tokenBlacklistService.blacklistAllUserTokens(null);

            // Then
            verify(setOperations, never()).members(anyString());
        }

        @Test
        @DisplayName("should handle empty token set")
        void blacklistAllUserTokens_WithEmptyTokenSet_ShouldDoNothing() {
            // Given
            String userId = "user-001";
            when(setOperations.members(anyString())).thenReturn(Set.of());

            // When
            tokenBlacklistService.blacklistAllUserTokens(userId);

            // Then
            verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
        }

        @Test
        @DisplayName("should handle null token set")
        void blacklistAllUserTokens_WithNullTokenSet_ShouldDoNothing() {
            // Given
            String userId = "user-001";
            when(setOperations.members(anyString())).thenReturn(null);

            // When
            tokenBlacklistService.blacklistAllUserTokens(userId);

            // Then
            verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
        }
    }

    @Nested
    @DisplayName("cleanupExpiredTokens Tests")
    class CleanupExpiredTokensTests {

        @Test
        @DisplayName("should execute without errors")
        void cleanupExpiredTokens_ShouldExecuteWithoutErrors() {
            // When
            tokenBlacklistService.cleanupExpiredTokens();

            // Then - No exception should be thrown
        }
    }
}