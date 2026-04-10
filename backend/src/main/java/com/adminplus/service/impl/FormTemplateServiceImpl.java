package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.request.FormTemplateRequest;
import com.adminplus.pojo.dto.response.FormTemplateResponse;
import com.adminplus.pojo.entity.FormTemplateEntity;
import com.adminplus.repository.FormTemplateRepository;
import com.adminplus.service.FormTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表单模板服务实现
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormTemplateServiceImpl implements FormTemplateService {

    private final FormTemplateRepository templateRepository;
    private final ConversionService conversionService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "formTemplates", key = "'all'")
    public List<FormTemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(t -> conversionService.convert(t, FormTemplateResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "formTemplates", key = "'enabled'")
    public List<FormTemplateResponse> getEnabledTemplates() {
        return templateRepository.findByStatusOrderByCreateTimeDesc(1).stream()
                .map(t -> conversionService.convert(t, FormTemplateResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "formTemplates", key = "'category:' + #category")
    public List<FormTemplateResponse> getTemplatesByCategory(String category) {
        return templateRepository.findByCategoryAndStatusOrderByCreateTimeDesc(category, 1).stream()
                .map(t -> conversionService.convert(t, FormTemplateResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "formTemplate", key = "#id")
    public FormTemplateResponse getTemplateById(String id) {
        return templateRepository.findById(id)
                .map(t -> conversionService.convert(t, FormTemplateResponse.class))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "formTemplate", key = "'code:' + #templateCode")
    public FormTemplateResponse getTemplateByCode(String templateCode) {
        return templateRepository.findByTemplateCode(templateCode)
                .map(t -> conversionService.convert(t, FormTemplateResponse.class))
                .orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"formTemplates", "formTemplate"}, allEntries = true)
    public FormTemplateResponse createTemplate(FormTemplateRequest req) {
        // 检查标识是否已存在
        if (templateRepository.existsByTemplateCode(req.templateCode())) {
            throw new BizException("表单标识已存在: " + req.templateCode());
        }

        FormTemplateEntity entity = new FormTemplateEntity();
        entity.setTemplateName(req.templateName());
        entity.setTemplateCode(req.templateCode());
        entity.setCategory(req.category());
        entity.setDescription(req.description());
        entity.setStatus(req.status());
        entity.setFormConfig(req.formConfig());

        entity = templateRepository.save(entity);

        return conversionService.convert(entity, FormTemplateResponse.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"formTemplates", "formTemplate"}, allEntries = true)
    public FormTemplateResponse updateTemplate(String id, FormTemplateRequest req) {
        FormTemplateEntity entity = templateRepository.findById(id)
                .orElseThrow(() -> new BizException("表单模板不存在: " + id));

        // 检查标识是否被其他记录使用
        if (!entity.getTemplateCode().equals(req.templateCode()) &&
            templateRepository.existsByTemplateCode(req.templateCode())) {
            throw new BizException("表单标识已存在: " + req.templateCode());
        }

        entity.setTemplateName(req.templateName());
        entity.setTemplateCode(req.templateCode());
        entity.setCategory(req.category());
        entity.setDescription(req.description());
        entity.setStatus(req.status());
        entity.setFormConfig(req.formConfig());

        entity = templateRepository.save(entity);

        return conversionService.convert(entity, FormTemplateResponse.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"formTemplates", "formTemplate"}, allEntries = true)
    public void deleteTemplate(String id) {
        if (!templateRepository.existsById(id)) {
            throw new BizException("表单模板不存在: " + id);
        }

        templateRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String templateCode) {
        return templateRepository.existsByTemplateCode(templateCode);
    }
}
