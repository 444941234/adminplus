package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 字典更新请求
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record DictUpdateReq(
        @NotBlank(message = "字典名称不能为空")
        @Size(max = 100, message = "字典名称长度不能超过100")
        String dictName,

        Integer status,

        @Size(max = 500, message = "备注长度不能超过500")
        String remark
) {}