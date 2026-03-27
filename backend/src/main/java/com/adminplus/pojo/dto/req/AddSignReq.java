package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 加签操作请求
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
public record AddSignReq(
        @NotBlank(message = "被加签人ID不能为空")
        String addUserId,

        @NotNull(message = "加签类型不能为空")
        AddSignType addType,

        @NotBlank(message = "加签原因不能为空")
        String reason
) {
    /**
     * 加签类型
     */
    public enum AddSignType {
        /**
         * 前加签：在当前审批人之前增加审批人
         */
        BEFORE,

        /**
         * 后加签：在当前审批人之后增加审批人
         */
        AFTER,

        /**
         * 转办：将审批权转给他人
         */
        TRANSFER
    }
}