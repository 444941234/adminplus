package com.adminplus.converter.formtemplate;

import com.adminplus.pojo.dto.response.FormTemplateResponse;
import com.adminplus.pojo.entity.FormTemplateEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FormTemplateConverter implements Converter<FormTemplateEntity, FormTemplateResponse> {

    @Override
    public FormTemplateResponse convert(FormTemplateEntity source) {
        return new FormTemplateResponse(
                source.getId(),
                source.getTemplateName(),
                source.getTemplateCode(),
                source.getCategory(),
                source.getDescription(),
                source.getFormConfig(),
                source.getStatus(),
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}