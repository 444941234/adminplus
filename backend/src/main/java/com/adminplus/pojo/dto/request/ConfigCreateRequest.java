package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 配置创建请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigCreateReq(
        @NotBlank(message = "配置组ID不能为空")
        String groupId,

        @NotBlank(message = "配置名称不能为空")
        @Size(max = 100, message = "配置名称长度不能超过100")
        String name,

        @NotBlank(message = "配置键不能为空")
        @Size(max = 100, message = "配置键长度不能超过100")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "配置键只能包含字母、数字、点、下划线和连字符")
        String key,

        String value,

        @NotBlank(message = "值类型不能为空")
        @Pattern(regexp = "^(STRING|NUMBER|BOOLEAN|JSON|ARRAY|SECRET|FILE)$", message = "值类型必须为STRING、NUMBER、BOOLEAN、JSON、ARRAY、SECRET或FILE")
        String valueType,

        @Pattern(regexp = "^(IMMEDIATE|MANUAL|RESTART)$", message = "生效类型必须为IMMEDIATE、MANUAL或RESTART")
        String effectType,

        String defaultValue,

        @Size(max = 500, message = "描述长度不能超过500")
        String description,

        Boolean isRequired,

        @Size(max = 200, message = "验证规则长度不能超过200")
        String validationRule,

        Integer sortOrder
) {}
