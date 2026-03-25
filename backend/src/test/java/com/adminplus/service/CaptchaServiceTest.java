package com.adminplus.service;

import com.adminplus.service.CaptchaService.CaptchaResult;
import com.adminplus.service.impl.CaptchaServiceImpl;
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

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CaptchaService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CaptchaService Unit Tests")
class CaptchaServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CaptchaServiceImpl captchaService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("generateCaptcha Tests")
    class GenerateCaptchaTests {

        @Test
        @DisplayName("should generate captcha with valid result")
        void generateCaptcha_ShouldReturnValidResult() {
            // Given
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // When
            CaptchaResult result = captchaService.generateCaptcha();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.captchaId()).isNotNull().isNotEmpty();
            assertThat(result.captchaCode()).isNotNull().hasSize(4);
            assertThat(result.image()).isNotNull().isInstanceOf(BufferedImage.class);
        }

        @Test
        @DisplayName("should save captcha to Redis")
        void generateCaptcha_ShouldSaveToRedis() {
            // When
            captchaService.generateCaptcha();

            // Then
            verify(valueOperations).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        }

        @Test
        @DisplayName("should generate captcha with valid characters")
        void generateCaptcha_ShouldUseValidCharacters() {
            // When
            CaptchaResult result = captchaService.generateCaptcha();

            // Then
            String validChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
            assertThat(result.captchaCode().chars()
                    .allMatch(c -> validChars.indexOf(c) >= 0)).isTrue();
        }

        @Test
        @DisplayName("should generate captcha with correct image dimensions")
        void generateCaptcha_ShouldGenerateCorrectImageSize() {
            // When
            CaptchaResult result = captchaService.generateCaptcha();

            // Then
            assertThat(result.image().getWidth()).isEqualTo(120);
            assertThat(result.image().getHeight()).isEqualTo(40);
        }
    }

    @Nested
    @DisplayName("validateCaptcha Tests")
    class ValidateCaptchaTests {

        @Test
        @DisplayName("should return true for valid captcha")
        void validateCaptcha_WithValidCode_ShouldReturnTrue() {
            // Given
            String captchaId = "test-captcha-id";
            String captchaCode = "ABCD";
            when(valueOperations.get("captcha:" + captchaId)).thenReturn("ABCD");

            // When
            boolean result = captchaService.validateCaptcha(captchaId, captchaCode);

            // Then
            assertThat(result).isTrue();
            verify(redisTemplate).delete("captcha:" + captchaId);
        }

        @Test
        @DisplayName("should return true for valid captcha ignoring case")
        void validateCaptcha_WithDifferentCase_ShouldReturnTrue() {
            // Given
            String captchaId = "test-captcha-id";
            String captchaCode = "abcd";
            when(valueOperations.get("captcha:" + captchaId)).thenReturn("ABCD");

            // When
            boolean result = captchaService.validateCaptcha(captchaId, captchaCode);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false for invalid captcha")
        void validateCaptcha_WithInvalidCode_ShouldReturnFalse() {
            // Given
            String captchaId = "test-captcha-id";
            String captchaCode = "WRONG";
            when(valueOperations.get("captcha:" + captchaId)).thenReturn("ABCD");

            // When
            boolean result = captchaService.validateCaptcha(captchaId, captchaCode);

            // Then
            assertThat(result).isFalse();
            verify(redisTemplate, never()).delete(anyString());
        }

        @Test
        @DisplayName("should return false for non-existent captcha")
        void validateCaptcha_WithNonExistentCaptcha_ShouldReturnFalse() {
            // Given
            String captchaId = "non-existent-id";
            String captchaCode = "ABCD";
            when(valueOperations.get("captcha:" + captchaId)).thenReturn(null);

            // When
            boolean result = captchaService.validateCaptcha(captchaId, captchaCode);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false for null captchaId")
        void validateCaptcha_WithNullCaptchaId_ShouldReturnFalse() {
            // When
            boolean result = captchaService.validateCaptcha(null, "ABCD");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false for empty captchaId")
        void validateCaptcha_WithEmptyCaptchaId_ShouldReturnFalse() {
            // When
            boolean result = captchaService.validateCaptcha("", "ABCD");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false for null captchaCode")
        void validateCaptcha_WithNullCaptchaCode_ShouldReturnFalse() {
            // When
            boolean result = captchaService.validateCaptcha("test-id", null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false for empty captchaCode")
        void validateCaptcha_WithEmptyCaptchaCode_ShouldReturnFalse() {
            // When
            boolean result = captchaService.validateCaptcha("test-id", "");

            // Then
            assertThat(result).isFalse();
        }
    }
}