package com.adminplus.utils;

import java.util.regex.Pattern;

public class LogMaskingUtils {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "(password[=:]\\s*[\"']?)([^\\s\"',}]+)([\"'\\s,}]*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(Bearer\\s+)(eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
            "(\\d{6})(\\d{8})(\\d{4})"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(1[3-9]\\d)(\\d{4})(\\d{4})"
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "([a-zA-Z0-9._%+-]+)(@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"
    );

    private static final Pattern SQL_VALUE_PATTERN = Pattern.compile(
            "(VALUES\\s*\\([^)]*?)(password[^,)]*=[^,)]+)([^)]*\\))",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("(\\d{4})\\d{8,12}(\\d{4})");

    public static String mask(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String masked = message;

        masked = PASSWORD_PATTERN.matcher(masked).replaceAll("$1***$3");

        masked = TOKEN_PATTERN.matcher(masked).replaceAll(mr -> {
            String prefix = mr.group(1);
            String token = mr.group(2);
            if (token.length() > 16) {
                return prefix + token.substring(0, 8) + "..." + token.substring(token.length() - 8);
            } else {
                return prefix + "***";
            }
        });

        masked = ID_CARD_PATTERN.matcher(masked).replaceAll("$1********$3");

        masked = PHONE_PATTERN.matcher(masked).replaceAll("$1****$3");

        masked = EMAIL_PATTERN.matcher(masked).replaceAll(mr -> {
            String username = mr.group(1);
            String domain = mr.group(2);
            if (username.length() > 2) {
                return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
            } else {
                return "***" + domain;
            }
        });

        masked = SQL_VALUE_PATTERN.matcher(masked).replaceAll("$1password=***$3");

        return masked;
    }

    public static String maskUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "***";
        }
        if (username.length() <= 2) {
            return "***";
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1);
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        return EMAIL_PATTERN.matcher(email).replaceAll(mr -> {
            String username = mr.group(1);
            String domain = mr.group(2);
            if (username.length() > 2) {
                return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
            } else {
                return "***" + domain;
            }
        });
    }

    public static String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return token;
        }
        if (token.length() <= 16) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 8);
    }

    public static String maskIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return ip;
        }
        return parts[0] + "." + parts[1] + ".*.*";
    }
}
