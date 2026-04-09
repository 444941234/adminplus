package com.adminplus.converter.workflowdefinition;

import com.adminplus.pojo.dto.request.WorkflowDefinitionRequest;
import com.adminplus.pojo.entity.WorkflowDefinitionEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WorkflowDefinitionRequestConverter implements Converter<WorkflowDefinitionRequest, WorkflowDefinitionEntity> {

    @Override
    public WorkflowDefinitionEntity convert(WorkflowDefinitionRequest source) {
        WorkflowDefinitionEntity entity = new WorkflowDefinitionEntity();
        entity.setDefinitionName(source.definitionName());
        entity.setDefinitionKey(source.definitionKey());
        entity.setCategory(source.category());
        entity.setDescription(source.description());
        entity.setStatus(source.status());
        entity.setFormConfig(source.formConfig());
        entity.setVersion(1);
        return entity;
    }
}