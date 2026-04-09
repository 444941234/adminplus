package com.adminplus.converter.workflowcc;

import com.adminplus.pojo.dto.response.WorkflowCcResponse;
import com.adminplus.pojo.entity.WorkflowCcEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WorkflowCcConverter implements Converter<WorkflowCcEntity, WorkflowCcResponse> {

    @Override
    public WorkflowCcResponse convert(WorkflowCcEntity source) {
        return new WorkflowCcResponse(
                source.getId(),
                source.getInstanceId(),
                source.getNodeId(),
                source.getNodeName(),
                source.getUserId(),
                source.getUserName(),
                source.getCcType(),
                source.getCcContent(),
                source.getIsRead(),
                source.getReadTime(),
                source.getCreateTime()
        );
    }
}