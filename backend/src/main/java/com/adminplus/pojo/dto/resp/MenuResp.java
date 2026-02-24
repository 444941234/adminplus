package com.adminplus.pojo.dto.resp;

import com.adminplus.utils.TreeUtils;

import java.time.Instant;
import java.util.List;

/**
 * 菜单视图对象
 * <p>
 * 实现 ReadonlyTreeNode 接口以支持树形结构构建
 * </p>
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
) implements TreeUtils.ReadonlyTreeNode<MenuResp> {
}
