package com.adminplus.service.workflow.hook.impl;

import com.adminplus.pojo.dto.workflow.hook.HookContext;
import com.adminplus.pojo.dto.workflow.hook.HookExecutorConfig;
import com.adminplus.pojo.dto.workflow.hook.HookResult;
import com.adminplus.pojo.dto.workflow.hook.HttpConfig;
import com.adminplus.service.workflow.hook.HookExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP 钩子执行器
 * <p>
 * 支持调用外部 HTTP 接口作为钩子
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HttpHookExecutor implements HookExecutor {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public HookResult execute(HookExecutorConfig config, HookContext context) {
        HttpConfig httpConfig = (HttpConfig) config;

        try {
            String body = buildRequestBody(httpConfig.bodyTemplate(), context);

            HttpHeaders headers = new HttpHeaders();
            if (httpConfig.headers() != null) {
                httpConfig.headers().forEach(headers::add);
            }
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                httpConfig.url(),
                HttpMethod.valueOf(httpConfig.method()),
                entity,
                Map.class
            );

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
}
