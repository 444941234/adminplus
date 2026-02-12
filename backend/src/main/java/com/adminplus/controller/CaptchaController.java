package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * 验证码控制器
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@RestController
@RequestMapping("/v1/captcha")
@RequiredArgsConstructor
@Tag(name = "验证码管理", description = "生成和验证验证码")
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "生成验证码")
    public ApiResponse<CaptchaResponse> generateCaptcha() throws IOException {
        var result = captchaService.generateCaptcha();

        // 将图片转换为 Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(result.image(), "png", baos);
        byte[] imageBytes = baos.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        CaptchaResponse response = new CaptchaResponse(result.captchaId(), "data:image/png;base64," + base64Image);
        return ApiResponse.ok(response);
    }

    /**
     * 验证码响应
     */
    record CaptchaResponse(
            String captchaId,
            String captchaImage
    ) {
    }
}