package com.adminplus.utils.masking;

/**
 * IP 地址脱敏器（优先级 900）
 * <p>
 * 保留前两段，后两段用*.*代替
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public class IpMasker implements LogMasker, Prioritized {

    private static final String MASKED_SUFFIX = ".*.*";

    @Override
    public String mask(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] parts = input.split("\\.");
        if (parts.length != 4) {
            return input;
        }
        return parts[0] + "." + parts[1] + MASKED_SUFFIX;
    }

    @Override
    public int getPriority() {
        return 900;
    }
}
