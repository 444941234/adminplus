package com.adminplus.pojo.dto.resp;

import java.time.Instant;
import java.util.List;

/**
 * 用户视图对象
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record UserResp(
        String id,
        String username,
        String nickname,
        String email,
        String phone,
        String avatar,
        Integer status,
        String deptId,
        String deptName,
        List<String> roles,
        Instant createTime,
        Instant updateTime
) {
}