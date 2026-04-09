package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 配置回滚请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigRollbackRequest(
        @NotBlank(message = "历史记录ID不能为空")
        String historyId,

        String remark
) {}
