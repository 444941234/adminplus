package com.adminplus.pojo.dto.response;

import com.adminplus.utils.TreeUtils;

import java.time.Instant;
import java.util.List;

/**
 * 字典项视图对象
 * <p>
 * 实现 ReadonlyTreeNode 接口以支持树形结构构建
 * </p>
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record DictItemResponse(
        String id,
        String dictId,
        String dictType,
        String parentId,
        String label,
        String value,
        Integer sortOrder,
        Integer status,
        String remark,
        List<DictItemResponse> children,
        Instant createTime,
        Instant updateTime
) implements TreeUtils.ReadonlyTreeNode<DictItemResponse> {

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