package com.adminplus.utils.masking;

import java.util.regex.Pattern;

/**
 * 密码脱敏器（优先级 100）
 * <p>
 * 匹配: password=xxx, password:xxx, password "xxx" 等
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public class PasswordMasker implements LogMasker, Prioritized {

    private static final Pattern PATTERN = Pattern.compile(
            "(password[=:]\\s*[\"']?)([^\\s\"',}]+)([\"'\\s,}]*)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String mask(String input) {
        return PATTERN.matcher(input).replaceAll("$1***$3");
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
