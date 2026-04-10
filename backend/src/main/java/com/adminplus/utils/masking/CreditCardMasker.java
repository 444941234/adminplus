package com.adminplus.utils.masking;

import java.util.regex.Pattern;

/**
 * 信用卡号脱敏器（优先级 700）
 * <p>
 * 保留前4位和后4位，中间用*代替
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public class CreditCardMasker implements LogMasker, Prioritized {

    private static final Pattern PATTERN = Pattern.compile("(\\d{4})\\d{8,12}(\\d{4})");

    @Override
    public String mask(String input) {
        if (input == null) {
            return null;
        }
        return PATTERN.matcher(input).replaceAll("$1********$2");
    }

    @Override
    public int getPriority() {
        return 700;
    }
}
