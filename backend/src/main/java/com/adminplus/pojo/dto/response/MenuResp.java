package com.adminplus.pojo.dto.response;

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

    /**
     * 重写 getId 方法，提供 id 字段值
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * 重写 getParentId 方法，提供 parentId 字段值
     */
    @Override
    public String getParentId() {
        return parentId;
    }
}
