package com.adminplus.pojo.dto.resp;

import java.time.Instant;
import java.util.List;

/**
 * 菜单视图对象
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record MenuResp(
        String id,
        String parentId,
        Integer type,
        String name,
        String path,
        String component,
        String permKey,
        String icon,
        Integer sortOrder,
        Integer visible,
        Integer status,
        List<MenuResp> children,
        Instant createTime,
        Instant updateTime
) {
}