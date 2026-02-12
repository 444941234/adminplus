package com.adminplus.pojo.dto.resp;

import java.time.Instant;
import java.util.List;

/**
 * 部门视图对象
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
public record DeptResp(
        String id,
        String parentId,
        String name,
        String code,
        String leader,
        String phone,
        String email,
        Integer sortOrder,
        Integer status,
        List<DeptResp> children,
        Instant createTime,
        Instant updateTime
) {
}