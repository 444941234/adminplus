package com.adminplus.utils;

import com.adminplus.utils.masking.*;

/**
 * 日志脱敏工具类
 * <p>
 * 使用责任链模式实现可扩展的脱敏规则：
 * <ul>
 *   <li>内置规则：密码、Token、身份证、手机号、邮箱、SQL密码、信用卡、用户名、IP</li>
 *   <li>支持优先级排序：实现 {@link Prioritized} 接口指定执行顺序</li>
 *   <li>支持自定义规则：通过 {@link LogMaskerChain#withAdditional(LogMasker...)} 添加</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 使用默认脱敏规则
 * String masked = LogMaskingUtils.mask("password=123456");
 * // 结果: password=***
 *
 * // 单独脱敏手机号
 * String masked = LogMaskingUtils.maskPhone("13800138000");
 * // 结果: 138****8000
 *
 * // 添加自定义脱敏规则
 * LogMaskerChain customChain = LogMaskerChain.createDefault()
 *     .withAdditional(new MyCustomMasker());
 * }</pre>
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public final class LogMaskingUtils {

    private static final LogMasker DEFAULT_MASK_CHAIN = LogMaskerChain.createDefault();
    private static final PasswordMasker PASSWORD_MASKER = new PasswordMasker();
    private static final TokenMasker TOKEN_MASKER = new TokenMasker();
    private static final IdCardMasker ID_CARD_MASKER = new IdCardMasker();
    private static final PhoneMasker PHONE_MASKER = new PhoneMasker();
    private static final EmailMasker EMAIL_MASKER = new EmailMasker();
    private static final CreditCardMasker CREDIT_CARD_MASKER = new CreditCardMasker();
    private static final UsernameMasker USERNAME_MASKER = new UsernameMasker();
    private static final IpMasker IP_MASKER = new IpMasker();

    private LogMaskingUtils() {
    }

    /**
     * 对消息进行脱敏处理（使用默认规则链）
     *
     * @param message 原始消息
     * @return 脱敏后的消息
     */
    public static String mask(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        return DEFAULT_MASK_CHAIN.mask(message);
    }

    /**
     * 脱敏用户名
     *
     * @param username 原始用户名
     * @return 脱敏后的用户名
     */
    public static String maskUsername(String username) {
        return USERNAME_MASKER.mask(username);
    }

    /**
     * 脱敏手机号
     *
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        return PHONE_MASKER.mask(phone);
    }

    /**
     * 脱敏邮箱
     *
     * @param email 原始邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        return EMAIL_MASKER.mask(email);
    }

    /**
     * 脱敏 Token
     *
     * @param token 原始 Token
     * @return 脱敏后的 Token
     */
    public static String maskToken(String token) {
        return TOKEN_MASKER.mask(token);
    }

    /**
     * 脱敏 IP 地址
     *
     * @param ip 原始 IP 地址
     * @return 脱敏后的 IP 地址
     */
    public static String maskIp(String ip) {
        return IP_MASKER.mask(ip);
    }

    /**
     * 脱敏身份证号
     *
     * @param idCard 原始身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        return ID_CARD_MASKER.mask(idCard);
    }

    /**
     * 脱敏信用卡号
     *
     * @param cardNumber 原始信用卡号
     * @return 脱敏后的信用卡号
     */
    public static String maskCreditCard(String cardNumber) {
        return CREDIT_CARD_MASKER.mask(cardNumber);
    }

    /**
     * 脱敏密码
     *
     * @param passwordText 包含密码的文本
     * @return 脱敏后的文本
     */
    public static String maskPassword(String passwordText) {
        return PASSWORD_MASKER.mask(passwordText);
    }
}
