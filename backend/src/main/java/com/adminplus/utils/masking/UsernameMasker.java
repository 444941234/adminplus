package com.adminplus.utils.masking;

/**
 * 用户名脱敏器（优先级 800）
 * <p>
 * 保留首尾字符，中间用***代替
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public class UsernameMasker implements LogMasker, Prioritized {

    @Override
    public String mask(String input) {
        if (input == null || input.isEmpty()) {
            return "***";
        }
        if (input.length() <= 2) {
            return "***";
        }
        return input.charAt(0) + "***" + input.charAt(input.length() - 1);
    }

    @Override
    public int getPriority() {
        return 800;
    }
}
