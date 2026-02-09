package com.adminplus.vo;

import java.time.Instant;

/**
 * 在线用户视图对象
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
public record OnlineUserVO(
        /**
         * 用户ID
         */
        String userId,

        /**
         * 用户名
         */
        String username,

        /**
         * IP地址
         */
        String ip,

        /**
         * 登录时间
         */
        Instant loginTime,

        /**
         * 浏览器
         */
        String browser,

        /**
         * 操作系统
         */
        String os
) {
}