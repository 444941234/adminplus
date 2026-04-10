package com.adminplus.utils.masking;

import java.util.regex.Pattern;

/**
 * SQL 密码值脱敏器（优先级 600）
 * <p>
 * 匹配 VALUES(...) 中的 password=xxx
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public class SqlPasswordMasker implements LogMasker, Prioritized {

    private static final Pattern PATTERN = Pattern.compile(
            "(VALUES\\s*\\([^)]*?)(password[^,)]*=[^,)]+)([^)]*\\))",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String mask(String input) {
        return PATTERN.matcher(input).replaceAll("$1password=***$3");
    }

    @Override
    public int getPriority() {
        return 600;
    }
}
