package com.adminplus.utils.masking;

import java.util.regex.Pattern;

/**
 * 手机号脱敏器（优先级 400）
 * <p>
 * 保留前3位和后4位，中间4位用*代替
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public class PhoneMasker implements LogMasker, Prioritized {

    private static final Pattern PATTERN = Pattern.compile("(1[3-9]\\d)(\\d{4})(\\d{4})");

    @Override
    public String mask(String input) {
        if (input == null) {
            return null;
        }
        return PATTERN.matcher(input).replaceAll("$1****$3");
    }

    @Override
    public int getPriority() {
        return 400;
    }
}
