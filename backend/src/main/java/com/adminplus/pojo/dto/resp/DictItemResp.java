package com.adminplus.pojo.dto.resp;

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
public record DictItemResp(
        String id,
        String dictId,
        String dictType,
        String parentId,
        String label,
        String value,
        Integer sortOrder,
        Integer status,
        String remark,
        List<DictItemResp> children,
        Instant createTime,
        Instant updateTime
) implements TreeUtils.ReadonlyTreeNode<DictItemResp> {
}