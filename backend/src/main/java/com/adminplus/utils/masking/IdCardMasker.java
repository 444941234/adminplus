package com.adminplus.utils.masking;

import java.util.regex.Pattern;

/**
 * 身份证号脱敏器（优先级 300）
 * <p>
 * 保留前6位和后4位，中间8位用*代替
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public class IdCardMasker implements LogMasker, Prioritized {

    private static final Pattern PATTERN = Pattern.compile("(\\d{6})(\\d{8})(\\d{4})");

    @Override
    public String mask(String input) {
        if (input == null) {
            return null;
        }
        return PATTERN.matcher(input).replaceAll("$1********$3");
    }

    @Override
    public int getPriority() {
        return 300;
    }
}
