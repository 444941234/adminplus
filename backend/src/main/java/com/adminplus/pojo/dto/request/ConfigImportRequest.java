package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 配置导入请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigImportReq(
        @NotBlank(message = "导入内容不能为空")
        String content,

        @NotBlank(message = "格式不能为空")
        @Pattern(regexp = "^(JSON|YAML)$", message = "格式必须为JSON或YAML")
        String format,

        @Pattern(regexp = "^(OVERWRITE|MERGE|VALIDATE)$", message = "模式必须为OVERWRITE、MERGE或VALIDATE")
        String mode
) {}
