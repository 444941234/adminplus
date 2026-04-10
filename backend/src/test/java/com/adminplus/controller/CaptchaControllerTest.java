package com.adminplus.controller;

import com.adminplus.service.CaptchaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.awt.image.BufferedImage;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CaptchaController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CaptchaController Unit Tests")
class CaptchaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private CaptchaController captchaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(captchaController).build();
    }

    @Nested
    @DisplayName("generateCaptcha Tests")
    class GenerateCaptchaTests {

        @Test
        @DisplayName("should generate captcha")
        void generateCaptcha_ShouldReturnCaptcha() throws Exception {
            // Given
            BufferedImage image = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            CaptchaService.CaptchaResult result = new CaptchaService.CaptchaResult("captcha-001", "ABCD", image);
            when(captchaService.generateCaptcha()).thenReturn(result);

            // When & Then
            mockMvc.perform(get("/captcha"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.captchaId").value("captcha-001"));
        }
    }
}