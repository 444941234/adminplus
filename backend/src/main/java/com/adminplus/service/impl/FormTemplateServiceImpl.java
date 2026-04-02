package com.adminplus.service.impl;

import com.adminplus.pojo.dto.req.FormTemplateReq;
import com.adminplus.pojo.dto.resp.FormTemplateResp;
import com.adminplus.pojo.entity.FormTemplateEntity;
import com.adminplus.repository.FormTemplateRepository;
import com.adminplus.service.FormTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Cacheable(value = "formTemplates", key = "'all'")
    public List<FormTemplateResp> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::toResp)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "formTemplates", key = "'enabled'")
    public List<FormTemplateResp> getEnabledTemplates() {
        return templateRepository.findByStatusOrderByCreateTimeDesc(1).stream()
                .map(this::toResp)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "formTemplates", key = "'category:' + #category")
    public List<FormTemplateResp> getTemplatesByCategory(String category) {
        return templateRepository.findByCategoryAndStatusOrderByCreateTimeDesc(category, 1).stream()
                .map(this::toResp)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "formTemplate", key = "#id")
    public FormTemplateResp getTemplateById(String id) {
        return templateRepository.findById(id)
                .map(this::toResp)
                .orElse(null);
    }

    @Override
    @Cacheable(value = "formTemplate", key = "'code:' + #templateCode")
    public FormTemplateResp getTemplateByCode(String templateCode) {
        return templateRepository.findByTemplateCode(templateCode)
                .map(this::toResp)
                .orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"formTemplates", "formTemplate"}, allEntries = true)
    public FormTemplateResp createTemplate(FormTemplateReq req) {
        // 检查标识是否已存在
        if (templateRepository.existsByTemplateCode(req.templateCode())) {
            throw new IllegalArgumentException("表单标识已存在: " + req.templateCode());
        }

        FormTemplateEntity entity = new FormTemplateEntity();
        entity.setTemplateName(req.templateName());
        entity.setTemplateCode(req.templateCode());
        entity.setCategory(req.category());
        entity.setDescription(req.description());
        entity.setStatus(req.status());
        entity.setFormConfig(req.formConfig());

        entity = templateRepository.save(entity);
        log.info("创建表单模板成功: id={}, code={}", entity.getId(), entity.getTemplateCode());

        return toResp(entity);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"formTemplates", "formTemplate"}, allEntries = true)
    public FormTemplateResp updateTemplate(String id, FormTemplateReq req) {
        FormTemplateEntity entity = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("表单模板不存在: " + id));

        // 检查标识是否被其他记录使用
        if (!entity.getTemplateCode().equals(req.templateCode()) &&
            templateRepository.existsByTemplateCode(req.templateCode())) {
            throw new IllegalArgumentException("表单标识已存在: " + req.templateCode());
        }

        entity.setTemplateName(req.templateName());
        entity.setTemplateCode(req.templateCode());
        entity.setCategory(req.category());
        entity.setDescription(req.description());
        entity.setStatus(req.status());
        entity.setFormConfig(req.formConfig());

        entity = templateRepository.save(entity);
        log.info("更新表单模板成功: id={}, code={}", entity.getId(), entity.getTemplateCode());

        return toResp(entity);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"formTemplates", "formTemplate"}, allEntries = true)
    public void deleteTemplate(String id) {
        if (!templateRepository.existsById(id)) {
            throw new IllegalArgumentException("表单模板不存在: " + id);
        }

        templateRepository.deleteById(id);
        log.info("删除表单模板成功: id={}", id);
    }

    @Override
    public boolean existsByCode(String templateCode) {
        return templateRepository.existsByTemplateCode(templateCode);
    }

    private FormTemplateResp toResp(FormTemplateEntity entity) {
        return new FormTemplateResp(
                entity.getId(),
                entity.getTemplateName(),
                entity.getTemplateCode(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getFormConfig(),
                entity.getStatus(),
                entity.getCreateTime(),
                entity.getUpdateTime()
        );
    }
}
