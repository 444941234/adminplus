package com.adminplus.constants;

/**
 * 验证码相关常量
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface CaptchaConstants {

    /**
     * 验证码过期时间（分钟）
     */
    long CAPTCHA_EXPIRE_MINUTES = 5;

    /**
     * 验证码字符集（排除易混淆字符 I、O、0、1）
     */
    String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    /**
     * 验证码长度
     */
    int CAPTCHA_LENGTH = 4;

    // ==================== 图片相关常量 ====================

    /**
     * 验证码图片宽度（像素）
     */
    int IMAGE_WIDTH = 120;

    /**
     * 验证码图片高度（像素）
     */
    int IMAGE_HEIGHT = 40;

    /**
     * 验证码字体大小
     */
    int FONT_SIZE = 28;

    /**
     * 干扰线数量
     */
    int NOISE_LINE_COUNT = 5;

    /**
     * 干扰点数量
     */
    int NOISE_DOT_COUNT = 50;

    /**
     * 字符间距（像素）
     */
    int CHAR_SPACING = 25;

    /**
     * 首字符起始 X 坐标（像素）
     */
    int CHAR_START_X = 20;

    /**
     * 字符最大旋转角度（度）
     */
    int MAX_ROTATION_DEGREES = 15;
}
