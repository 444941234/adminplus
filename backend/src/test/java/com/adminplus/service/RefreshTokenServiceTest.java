package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.entity.RefreshTokenEntity;
import com.adminplus.repository.RefreshTokenRepository;
import com.adminplus.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RefreshTokenService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private RefreshTokenEntity testToken;
    private Jwt mockJwt;

    @BeforeEach
    void setUp() {
        testToken = new RefreshTokenEntity();
        testToken.setUserId("user-001");
        testToken.setToken("refresh-token-uuid");
        testToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        testToken.setRevoked(false);

        mockJwt = mock(Jwt.class);
        lenient().when(mockJwt.getTokenValue()).thenReturn("new-access-token");
    }

    @Nested
    @DisplayName("createRefreshToken Tests")
    class CreateRefreshTokenTests {

        @Test
        @DisplayName("should create new refresh token")
        void createRefreshToken_ShouldCreateToken() {
            // Given
            String userId = "user-001";
            doNothing().when(refreshTokenRepository).deleteByUserId(userId);
            when(refreshTokenRepository.save(any())).thenReturn(testToken);

            // When
            String result = refreshTokenService.createRefreshToken(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            verify(refreshTokenRepository).deleteByUserId(userId);
            verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
        }

        @Test
        @DisplayName("should revoke previous tokens before creating new one")
        void createRefreshToken_ShouldRevokePreviousTokens() {
            // Given
            String userId = "user-001";
            doNothing().when(refreshTokenRepository).deleteByUserId(userId);
            when(refreshTokenRepository.save(any())).thenReturn(testToken);

            // When
            refreshTokenService.createRefreshToken(userId);

            // Then
            verify(refreshTokenRepository).deleteByUserId(userId);
        }
    }

    @Nested
    @DisplayName("refreshAccessToken Tests")
    class RefreshAccessTokenTests {

        @Test
        @DisplayName("should return new access token for valid refresh token")
        void refreshAccessToken_WithValidToken_ShouldReturnAccessToken() {
            // Given
            when(refreshTokenRepository.findByToken("refresh-token-uuid")).thenReturn(Optional.of(testToken));
            when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

            // When
            String result = refreshTokenService.refreshAccessToken("refresh-token-uuid");

            // Then
            assertThat(result).isEqualTo("new-access-token");
        }

        @Test
        @DisplayName("should throw exception for invalid refresh token")
        void refreshAccessToken_WithInvalidToken_ShouldThrowException() {
            // Given
            when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.refreshAccessToken("invalid-token"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("无效的 Refresh Token");
        }

        @Test
        @DisplayName("should throw exception for revoked token")
        void refreshAccessToken_WithRevokedToken_ShouldThrowException() {
            // Given
            testToken.setRevoked(true);
            when(refreshTokenRepository.findByToken("refresh-token-uuid")).thenReturn(Optional.of(testToken));

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.refreshAccessToken("refresh-token-uuid"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("已被撤销");
        }

        @Test
        @DisplayName("should throw exception for expired token")
        void refreshAccessToken_WithExpiredToken_ShouldThrowException() {
            // Given
            testToken.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));
            when(refreshTokenRepository.findByToken("refresh-token-uuid")).thenReturn(Optional.of(testToken));

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.refreshAccessToken("refresh-token-uuid"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("已过期");
        }
    }

    @Nested
    @DisplayName("revokeRefreshToken Tests")
    class RevokeRefreshTokenTests {

        @Test
        @DisplayName("should revoke valid token")
        void revokeRefreshToken_ShouldRevokeToken() {
            // Given
            when(refreshTokenRepository.findByToken("refresh-token-uuid")).thenReturn(Optional.of(testToken));
            when(refreshTokenRepository.save(any())).thenReturn(testToken);

            // When
            refreshTokenService.revokeRefreshToken("refresh-token-uuid");

            // Then
            verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
        }

        @Test
        @DisplayName("should throw exception for invalid token")
        void revokeRefreshToken_WithInvalidToken_ShouldThrowException() {
            // Given
            when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> refreshTokenService.revokeRefreshToken("invalid-token"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("无效的 Refresh Token");
        }
    }

    @Nested
    @DisplayName("revokeAllUserTokens Tests")
    class RevokeAllUserTokensTests {

        @Test
        @DisplayName("should revoke all tokens for user")
        void revokeAllUserTokens_ShouldDeleteAllTokens() {
            // Given
            String userId = "user-001";
            doNothing().when(refreshTokenRepository).deleteByUserId(userId);

            // When
            refreshTokenService.revokeAllUserTokens(userId);

            // Then
            verify(refreshTokenRepository).deleteByUserId(userId);
        }
    }

    @Nested
    @DisplayName("cleanupExpiredTokens Tests")
    class CleanupExpiredTokensTests {

        @Test
        @DisplayName("should delete expired tokens")
        void cleanupExpiredTokens_ShouldDeleteExpired() {
            // Given
            doNothing().when(refreshTokenRepository).deleteByExpiryDateBefore(any(Instant.class));

            // When
            refreshTokenService.cleanupExpiredTokens();

            // Then
            verify(refreshTokenRepository).deleteByExpiryDateBefore(any(Instant.class));
        }
    }
}