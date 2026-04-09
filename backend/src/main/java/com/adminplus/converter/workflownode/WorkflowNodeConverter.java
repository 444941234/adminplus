package com.adminplus.converter.workflownode;

import com.adminplus.pojo.dto.response.WorkflowNodeResponse;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WorkflowNodeConverter implements Converter<WorkflowNodeEntity, WorkflowNodeResponse> {

    @Override
    public WorkflowNodeResponse convert(WorkflowNodeEntity source) {
        return new WorkflowNodeResponse(
                source.getId(),
                source.getDefinitionId(),
                source.getNodeName(),
                source.getNodeCode(),
                source.getNodeOrder(),
                source.getApproverType(),
                source.getApproverId(),
                source.getIsCounterSign(),
                source.getAutoPassSameUser(),
                source.getDescription(),
                source.getCreateTime()
        );
    }
}