package com.adminplus.pojo.dto.response;

import com.adminplus.utils.TreeUtils;

import java.time.Instant;
import java.util.List;

/**
 * 部门视图对象
 * <p>
 * 实现 ReadonlyTreeNode 接口以支持树形结构构建
 * </p>
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
public record DeptResponse(
        String id,
        String parentId,
        String name,
        String code,
        String leader,
        String phone,
        String email,
        Integer sortOrder,
        Integer status,
        List<DeptResponse> children,
        Instant createTime,
        Instant updateTime
) implements TreeUtils.ReadonlyTreeNode<DeptResponse> {

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public List<?> getChildren() {
        return children;
    }
}
