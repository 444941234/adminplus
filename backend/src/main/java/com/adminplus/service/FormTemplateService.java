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
     *
     * @return 所有表单模板列表
     */
    List<FormTemplateResponse> getAllTemplates();

    /**
     * 获取启用的表单模板
     *
     * @return 启用状态的表单模板列表
     */
    List<FormTemplateResponse> getEnabledTemplates();

    /**
     * 根据分类获取表单模板
     *
     * @param category 模板分类
     * @return 该分类下的表单模板列表
     */
    List<FormTemplateResponse> getTemplatesByCategory(String category);

    /**
     * 根据ID获取表单模板
     *
     * @param id 模板ID
     * @return 表单模板信息
     * @throws BizException 当模板不存在时抛出
     */
    FormTemplateResponse getTemplateById(String id);

    /**
     * 根据标识获取表单模板
     *
     * @param templateCode 模板标识编码
     * @return 表单模板信息
     * @throws BizException 当模板不存在时抛出
     */
    FormTemplateResponse getTemplateByCode(String templateCode);

    /**
     * 创建表单模板
     *
     * @param req 模板创建请求
     * @return 创建的模板信息
     * @throws BizException 当模板编码已存在时抛出
     */
    FormTemplateResponse createTemplate(FormTemplateRequest req);

    /**
     * 更新表单模板
     *
     * @param id  模板ID
     * @param req 模板更新请求
     * @return 更新后的模板信息
     * @throws BizException 当模板不存在时抛出
     */
    FormTemplateResponse updateTemplate(String id, FormTemplateRequest req);

    /**
     * 删除表单模板
     *
     * @param id 模板ID
     * @throws BizException 当模板不存在时抛出
     */
    void deleteTemplate(String id);

    /**
     * 检查标识是否存在
     *
     * @param templateCode 模板标识编码
     * @return 是否存在
     */
    boolean existsByCode(String templateCode);
}
