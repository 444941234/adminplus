package com.adminplus.pojo.dto.response;

/**
 * 验证码响应
 *
 * @param captchaId    验证码ID
 * @param captchaImage 验证码图片（Base64编码）
 * @author AdminPlus
 * @since 2026-02-07
 */
public record CaptchaResponse(
        String captchaId,
        String captchaImage
) {
}
