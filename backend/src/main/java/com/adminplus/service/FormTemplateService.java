package com.adminplus.service;

import com.adminplus.pojo.dto.request.FormTemplateRequest;
import com.adminplus.pojo.dto.response.FormTemplateResponse;

import java.util.List;

/**
 * 表单模板服务接口
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
public interface FormTemplateService {

    /**
     * 获取所有表单模板
     */
    List<FormTemplateResponse> getAllTemplates();

    /**
     * 获取启用的表单模板
     */
    List<FormTemplateResponse> getEnabledTemplates();

    /**
     * 根据分类获取表单模板
     */
    List<FormTemplateResponse> getTemplatesByCategory(String category);

    /**
     * 根据ID获取表单模板
     */
    FormTemplateResponse getTemplateById(String id);

    /**
     * 根据标识获取表单模板
     */
    FormTemplateResponse getTemplateByCode(String templateCode);

    /**
     * 创建表单模板
     */
    FormTemplateResponse createTemplate(FormTemplateRequest req);

    /**
     * 更新表单模板
     */
    FormTemplateResponse updateTemplate(String id, FormTemplateRequest req);

    /**
     * 删除表单模板
     */
    void deleteTemplate(String id);

    /**
     * 检查标识是否存在
     */
    boolean existsByCode(String templateCode);
}
