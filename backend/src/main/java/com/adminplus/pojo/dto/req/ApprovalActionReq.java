package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 审批操作请求
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Builder
public record ApprovalActionReq(
        @NotBlank(message = "审批意见不能为空")
        String comment,

        String attachments
) {
}
