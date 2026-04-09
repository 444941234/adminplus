package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 催办操作请求
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
public record UrgeActionReq(
        @NotBlank(message = "催办内容不能为空")
        String content,

        /**
         * 目标审批人ID（不指定则催办当前节点的所有待审批人）
         */
        String targetApproverId
) {
}