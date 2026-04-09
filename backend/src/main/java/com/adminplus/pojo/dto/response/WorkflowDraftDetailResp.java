package com.adminplus.pojo.dto.response;

import java.util.Map;

/**
 * 工作流草稿详情响应
 *
 * @author AdminPlus
 * @since 2026-03-27
 */
public record WorkflowDraftDetailResp(
        WorkflowInstanceResp instance,
        String formConfig,
        Map<String, Object> formData
) {
}
