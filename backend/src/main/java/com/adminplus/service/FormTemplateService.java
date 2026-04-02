package com.adminplus.service;

import com.adminplus.pojo.dto.req.FormTemplateReq;
import com.adminplus.pojo.dto.resp.FormTemplateResp;
import com.adminplus.pojo.entity.FormTemplateEntity;

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
    List<FormTemplateResp> getAllTemplates();

    /**
     * 获取启用的表单模板
     */
    List<FormTemplateResp> getEnabledTemplates();

    /**
     * 根据分类获取表单模板
     */
    List<FormTemplateResp> getTemplatesByCategory(String category);

    /**
     * 根据ID获取表单模板
     */
    FormTemplateResp getTemplateById(String id);

    /**
     * 根据标识获取表单模板
     */
    FormTemplateResp getTemplateByCode(String templateCode);

    /**
     * 创建表单模板
     */
    FormTemplateResp createTemplate(FormTemplateReq req);

    /**
     * 更新表单模板
     */
    FormTemplateResp updateTemplate(String id, FormTemplateReq req);

    /**
     * 删除表单模板
     */
    void deleteTemplate(String id);

    /**
     * 检查标识是否存在
     */
    boolean existsByCode(String templateCode);
}
