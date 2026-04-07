package com.adminplus.service.impl;

import com.adminplus.constants.CacheConstants;
import com.adminplus.constants.CaptchaConstants;
import com.adminplus.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final StringRedisTemplate redisTemplate;
    private final Random random = new Random();

    @Override
    public CaptchaResult generateCaptcha() {
        String captchaCode = generateCaptchaCode();
        String captchaId = UUID.randomUUID().toString();

        saveCaptchaToRedis(captchaId, captchaCode);
        BufferedImage image = generateCaptchaImage(captchaCode);

        log.info("生成验证码: ID={}, 代码={}", captchaId, captchaCode);
        return new CaptchaResult(captchaId, captchaCode, image);
    }

    @Override
    public boolean validateCaptcha(String captchaId, String captchaCode) {
        if (isBlank(captchaId) || isBlank(captchaCode)) {
            return false;
        }

        String redisKey = CacheConstants.CAPTCHA_KEY_PREFIX + captchaId;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            log.warn("验证码不存在或已过期: ID={}", captchaId);
            return false;
        }

        boolean isValid = storedCode.equalsIgnoreCase(captchaCode);
        if (isValid) {
            redisTemplate.delete(redisKey);
            log.info("验证码验证成功: ID={}", captchaId);
        } else {
            log.warn("验证码验证失败: ID={}, 输入={}, 正确={}", captchaId, captchaCode, storedCode);
        }

        return isValid;
    }

    private void saveCaptchaToRedis(String captchaId, String captchaCode) {
        String redisKey = CacheConstants.CAPTCHA_KEY_PREFIX + captchaId;
        redisTemplate.opsForValue().set(redisKey, captchaCode, CaptchaConstants.CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    private static boolean isBlank(String str) {
        return str == null || str.isEmpty();
    }

    private String generateCaptchaCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CaptchaConstants.CAPTCHA_LENGTH; i++) {
            code.append(CaptchaConstants.CAPTCHA_CHARS.charAt(random.nextInt(CaptchaConstants.CAPTCHA_CHARS.length())));
        }
        return code.toString();
    }

    private BufferedImage generateCaptchaImage(String code) {
        BufferedImage image = new BufferedImage(CaptchaConstants.IMAGE_WIDTH, CaptchaConstants.IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        fillBackground(g);
        drawNoiseLines(g);
        drawNoiseDots(g);
        drawCaptchaCode(g, code);

        g.dispose();
        return image;
    }

    private void fillBackground(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, CaptchaConstants.IMAGE_WIDTH, CaptchaConstants.IMAGE_HEIGHT);
    }

    private void drawNoiseLines(Graphics2D g) {
        for (int i = 0; i < CaptchaConstants.NOISE_LINE_COUNT; i++) {
            g.setColor(getRandomColor());
            g.drawLine(random.nextInt(CaptchaConstants.IMAGE_WIDTH), random.nextInt(CaptchaConstants.IMAGE_HEIGHT),
                    random.nextInt(CaptchaConstants.IMAGE_WIDTH), random.nextInt(CaptchaConstants.IMAGE_HEIGHT));
        }
    }

    private void drawNoiseDots(Graphics2D g) {
        for (int i = 0; i < CaptchaConstants.NOISE_DOT_COUNT; i++) {
            g.setColor(getRandomColor());
            g.fillOval(random.nextInt(CaptchaConstants.IMAGE_WIDTH), random.nextInt(CaptchaConstants.IMAGE_HEIGHT), 2, 2);
        }
    }

    private void drawCaptchaCode(Graphics2D g, String code) {
        g.setFont(new Font("Arial", Font.BOLD, CaptchaConstants.FONT_SIZE));
        int centerY = CaptchaConstants.IMAGE_HEIGHT / 2;

        for (int i = 0; i < code.length(); i++) {
            g.setColor(getRandomColor());
            double angle = (random.nextInt(CaptchaConstants.MAX_ROTATION_DEGREES * 2) - CaptchaConstants.MAX_ROTATION_DEGREES) * Math.PI / 180;
            int x = CaptchaConstants.CHAR_START_X + i * CaptchaConstants.CHAR_SPACING;

            g.rotate(angle, x, centerY);
            g.drawString(String.valueOf(code.charAt(i)), x, centerY + 10);
            g.rotate(-angle, x, centerY);
        }
    }

    private Color getRandomColor() {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}