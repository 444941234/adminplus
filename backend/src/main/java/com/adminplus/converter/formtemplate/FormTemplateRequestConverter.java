package com.adminplus.converter.formtemplate;

import com.adminplus.pojo.dto.request.FormTemplateRequest;
import com.adminplus.pojo.entity.FormTemplateEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FormTemplateRequestConverter implements Converter<FormTemplateRequest, FormTemplateEntity> {

    @Override
    public FormTemplateEntity convert(FormTemplateRequest source) {
        FormTemplateEntity entity = new FormTemplateEntity();
        entity.setTemplateName(source.templateName());
        entity.setTemplateCode(source.templateCode());
        entity.setCategory(source.category());
        entity.setDescription(source.description());
        entity.setStatus(source.status());
        entity.setFormConfig(source.formConfig());
        return entity;
    }
}