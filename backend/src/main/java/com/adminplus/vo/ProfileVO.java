package com.adminplus.vo;

import java.time.Instant;

/**
 * 个人资料视图对象
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public record ProfileVO(
        String id,
        String username,
        String nickname,
        String email,
        String phone,
        String avatar,
        Integer status,
        Instant createTime,
        Instant updateTime
) {
}