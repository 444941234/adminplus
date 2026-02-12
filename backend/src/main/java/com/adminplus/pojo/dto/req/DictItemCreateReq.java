package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 字典项创建请求
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record DictItemCreateReq(
        @NotNull(message = "字典ID不能为空")
        String dictId,

        String parentId,

        @NotBlank(message = "字典标签不能为空")
        @Size(max = 100, message = "字典标签长度不能超过100")
        String label,

        @NotBlank(message = "字典值不能为空")
        @Size(max = 100, message = "字典值长度不能超过100")
        String value,

        Integer sortOrder,

        Integer status,

        @Size(max = 500, message = "备注长度不能超过500")
        String remark
) {}