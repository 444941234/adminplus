package com.adminplus.converter.workflowdefinition;

import com.adminplus.pojo.dto.response.WorkflowDefinitionResponse;
import com.adminplus.pojo.entity.WorkflowDefinitionEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WorkflowDefinitionConverter implements Converter<WorkflowDefinitionEntity, WorkflowDefinitionResponse> {

    @Override
    public WorkflowDefinitionResponse convert(WorkflowDefinitionEntity source) {
        return new WorkflowDefinitionResponse(
                source.getId(),
                source.getDefinitionName(),
                source.getDefinitionKey(),
                source.getCategory(),
                source.getDescription(),
                source.getStatus(),
                source.getVersion(),
                source.getFormConfig(),
                null,
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}