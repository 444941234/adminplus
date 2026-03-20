package com.adminplus.pojo.dto.resp;

import java.time.Instant;
import java.util.List;

/**
 * 个人资料视图对象
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public record ProfileResp(
        String id,
        String username,
        String nickname,
        String email,
        String phone,
        String avatar,
        Integer status,
        String deptName,
        List<String> roles,
        Instant createTime,
        Instant updateTime
) {
}