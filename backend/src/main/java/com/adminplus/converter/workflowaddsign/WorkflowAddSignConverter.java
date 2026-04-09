package com.adminplus.converter.workflowaddsign;

import com.adminplus.pojo.dto.response.WorkflowAddSignResponse;
import com.adminplus.pojo.entity.WorkflowAddSignEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WorkflowAddSignConverter implements Converter<WorkflowAddSignEntity, WorkflowAddSignResponse> {

    @Override
    public WorkflowAddSignResponse convert(WorkflowAddSignEntity source) {
        return new WorkflowAddSignResponse(
                source.getId(),
                source.getInstanceId(),
                source.getNodeId(),
                source.getNodeName(),
                source.getInitiatorId(),
                source.getInitiatorName(),
                source.getAddUserId(),
                source.getAddUserName(),
                source.getAddType(),
                source.getAddReason(),
                source.getOriginalApproverId(),
                source.getCreateTime()
        );
    }
}