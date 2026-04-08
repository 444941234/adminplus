package com.adminplus.service.workflow.hook.impl;

import com.adminplus.common.properties.AppProperties;
import com.adminplus.pojo.dto.workflow.hook.HookContext;
import com.adminplus.pojo.dto.workflow.hook.HookExecutorConfig;
import com.adminplus.pojo.dto.workflow.hook.HookResult;
import com.adminplus.pojo.dto.workflow.hook.HttpConfig;
import com.adminplus.service.workflow.hook.HookExecutor;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;

/**
 * HTTP 钩子执行器
 * <p>
 * 支持调用外部 HTTP 接口作为钩子
 * 使用 Spring Boot 4 推荐的 RestClient 替代 RestTemplate
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HttpHookExecutor implements HookExecutor {

    private final RestClient restClient;
    private final JsonMapper objectMapper;
    private final AppProperties appProperties;

    @Override
    public HookResult execute(HookExecutorConfig config, HookContext context) {
        HttpConfig httpConfig = (HttpConfig) config;

        // URL 安全验证
        String url = httpConfig.url();
        HookResult validationResult = validateUrl(url);
        if (validationResult != null) {
            return validationResult;
        }

        try {
            String body = buildRequestBody(httpConfig.bodyTemplate(), context);

            // 使用 RestClient 的流畅 API
            ResponseEntity<Map> response = restClient
                    .method(HttpMethod.valueOf(httpConfig.method()))
                    .uri(httpConfig.url())
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        if (httpConfig.headers() != null) {
                            httpConfig.headers().forEach(headers::add);
                        }
                    })
                    .body(body)
                    .retrieve()
                    .toEntity(Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> bodyMap = response.getBody();
                if (bodyMap == null) {
                    return new HookResult(true, "SUCCESS", "执行成功");
                }
                boolean success = (boolean) bodyMap.getOrDefault("success", true);
                String code = (String) bodyMap.getOrDefault("code", "SUCCESS");
                String message = (String) bodyMap.getOrDefault("message", "执行成功");
                return new HookResult(success, code, message, bodyMap.get("data"));
            }

            return new HookResult(false, "HTTP_ERROR",
                "HTTP " + response.getStatusCode() + ": " + response.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP钩子执行失败: url={}, status={}", httpConfig.url(), e.getStatusCode());
            return new HookResult(false, "HTTP_ERROR",
                "HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("HTTP钩子执行失败: url={}", httpConfig.url(), e);
            return new HookResult(false, "HTTP_ERROR", "执行失败: " + e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "http";
    }

    private String buildRequestBody(String template, HookContext context) {
        if (template == null || template.isBlank()) {
            return "{}";
        }

        try {
            // 简单的变量替换：#instance.id -> 实际值
            String result = template;
            result = result.replace("#instance.id", context.instance().getId());
            result = result.replace("#node.id", context.node() != null ? context.node().getId() : "");
            result = result.replace("#operatorId", context.operatorId() != null ? context.operatorId() : "");
            result = result.replace("#operatorName", context.operatorName() != null ? context.operatorName() : "");

            // 对于复杂的 formData，使用 JSON 序列化
            if (context.formData() != null) {
                String formDataJson = objectMapper.writeValueAsString(context.formData());
                result = result.replace("#formData", formDataJson);
            }

            return result;
        } catch (Exception e) {
            log.warn("构建请求体失败: template={}", template, e);
            return "{}";
        }
    }

    /**
     * 验证URL安全性，防止SSRF攻击
     */
    private HookResult validateUrl(String url) {
        if (url == null || url.isBlank()) {
            return new HookResult(false, "INVALID_URL", "URL不能为空");
        }

        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            String scheme = uri.getScheme();

            // 只允许 http/https
            if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
                return new HookResult(false, "INVALID_URL", "只支持HTTP/HTTPS协议");
            }

            AppProperties.WorkflowHook hookConfig = appProperties.getWorkflowHook();

            // 检查是否允许内网URL
            if (!hookConfig.isAllowInternalUrls() && isInternalHost(host)) {
                return new HookResult(false, "INVALID_URL", "不允许访问内网地址");
            }

            // 检查URL模式白名单
            String allowedPatterns = hookConfig.getAllowedUrlPatterns();
            if (allowedPatterns != null && !allowedPatterns.isBlank()) {
                if (!matchesAllowedPattern(url, allowedPatterns)) {
                    return new HookResult(false, "INVALID_URL", "URL不在允许列表中");
                }
            }

            return null; // 验证通过
        } catch (Exception e) {
            return new HookResult(false, "INVALID_URL", "URL格式无效: " + e.getMessage());
        }
    }

    /**
     * 检查是否为内网地址
     */
    private boolean isInternalHost(String host) {
        if (host == null) return false;
        String lowerHost = host.toLowerCase();
        return lowerHost.equals("localhost")
                || lowerHost.equals("127.0.0.1")
                || lowerHost.startsWith("10.")
                || lowerHost.startsWith("192.168.")
                || lowerHost.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*")
                || lowerHost.equals("0.0.0.0")
                || lowerHost.endsWith(".local")
                || lowerHost.endsWith(".internal");
    }

    /**
     * 检查URL是否匹配允许的模式
     */
    private boolean matchesAllowedPattern(String url, String allowedPatterns) {
        String[] patterns = allowedPatterns.split(",");
        for (String pattern : patterns) {
            String trimmedPattern = pattern.trim();
            if (trimmedPattern.endsWith("*")) {
                String prefix = trimmedPattern.substring(0, trimmedPattern.length() - 1);
                if (url.startsWith(prefix)) {
                    return true;
                }
            } else if (url.equals(trimmedPattern)) {
                return true;
            }
        }
        return false;
    }
}
