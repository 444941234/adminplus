package com.adminplus.pojo.dto.workflow.hook;

import java.util.Map;

/**
 * HTTP 执行器配置
 *
 * @param url           HTTP 请求 URL
 * @param method        HTTP 方法（GET、POST、PUT、DELETE）
 * @param headers       HTTP 请求头
 * @param bodyTemplate  请求体模板（支持变量替换）
 * @author AdminPlus
 * @since 2026-04-02
 */
public record HttpConfig(
    String url,
    String method,
    Map<String, String> headers,
    String bodyTemplate
) implements HookExecutorConfig {
}
