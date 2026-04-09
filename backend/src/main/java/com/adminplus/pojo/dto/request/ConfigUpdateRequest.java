package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 配置更新请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigUpdateRequest(
        @Size(max = 100, message = "配置名称长度不能超过100")
        String name,

        String value,

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

        Integer sortOrder,

        Integer status
) {}
