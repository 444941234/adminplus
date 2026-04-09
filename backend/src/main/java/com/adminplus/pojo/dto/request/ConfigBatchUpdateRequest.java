package com.adminplus.pojo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 配置批量更新请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigBatchUpdateRequest(
        @NotEmpty(message = "更新项列表不能为空")
        @Valid
        List<ConfigItemUpdate> items
) {
    /**
     * 配置项更新
     */
    public record ConfigItemUpdate(
            String id,
            String value
    ) {}
}
