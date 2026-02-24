package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.Size;

import java.util.Optional;

/**
 * 字典项更新请求
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record DictItemUpdateReq(
        String parentId,

        @Size(max = 100, message = "字典标签长度不能超过100")
        String label,

        @Size(max = 100, message = "字典值长度不能超过100")
        String value,

        Integer sortOrder,

        Integer status,

        @Size(max = 500, message = "备注长度不能超过500")
        String remark
) {
    public Optional<String> getParentId() { return Optional.ofNullable(parentId); }
    public Optional<String> getLabel() { return Optional.ofNullable(label); }
    public Optional<String> getValue() { return Optional.ofNullable(value); }
    public Optional<Integer> getSortOrder() { return Optional.ofNullable(sortOrder); }
    public Optional<Integer> getStatus() { return Optional.ofNullable(status); }
    public Optional<String> getRemark() { return Optional.ofNullable(remark); }
}