package com.adminplus.vo;

import java.time.Instant;
import java.util.List;

/**
 * 菜单视图对象
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record MenuVO(
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
        List<MenuVO> children,
        Instant createTime,
        Instant updateTime
) {
}