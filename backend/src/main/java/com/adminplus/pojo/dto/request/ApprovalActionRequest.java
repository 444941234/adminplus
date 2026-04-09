package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * 审批操作请求
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Builder
public record ApprovalActionRequest(
        @NotBlank(message = "审批意见不能为空")
        String comment,

        String attachments,

        /**
         * 目标节点ID（用于回退操作）
         * 如果为空，则回退到上一节点
         */
        String targetNodeId
) {
}
