package com.adminplus.utils.masking;

import java.util.regex.Pattern;

/**
 * JWT Token 脱敏器（优先级 200）
 * <p>
 * 保留前8位和后8位，中间用...代替
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public class TokenMasker implements LogMasker, Prioritized {

    private static final Pattern PATTERN = Pattern.compile(
            "(Bearer\\s+)(eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String mask(String input) {
        return PATTERN.matcher(input).replaceAll(mr -> {
            String prefix = mr.group(1);
            String token = mr.group(2);
            if (token.length() > 16) {
                return prefix + token.substring(0, 8) + "..." + token.substring(token.length() - 8);
            }
            return prefix + "***";
        });
    }

    @Override
    public int getPriority() {
        return 200;
    }
}
