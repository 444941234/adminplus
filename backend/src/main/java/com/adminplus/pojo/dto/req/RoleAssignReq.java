package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 角色分配请求 DTO
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
public record RoleAssignReq(
        @NotEmpty(message = "角色ID列表不能为空")
        List<@NotBlank(message = "角色ID不能为空") String> roleIds
) {
}
