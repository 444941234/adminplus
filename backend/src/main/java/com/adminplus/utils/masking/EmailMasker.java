package com.adminplus.utils.masking;

import java.util.regex.Pattern;

/**
 * 邮箱脱敏器（优先级 500）
 * <p>
 * 保留首尾字符，中间用*代替
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public class EmailMasker implements LogMasker, Prioritized {

    private static final Pattern PATTERN = Pattern.compile(
            "([a-zA-Z0-9._%+-]+)(@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"
    );

    @Override
    public String mask(String input) {
        return PATTERN.matcher(input).replaceAll(mr -> {
            String username = mr.group(1);
            String domain = mr.group(2);
            if (username.length() > 2) {
                return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
            }
            return "***" + domain;
        });
    }

    @Override
    public int getPriority() {
        return 500;
    }
}
