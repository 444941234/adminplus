package com.adminplus.converter.workflowurge;

import com.adminplus.pojo.dto.response.WorkflowUrgeResponse;
import com.adminplus.pojo.entity.WorkflowUrgeEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WorkflowUrgeConverter implements Converter<WorkflowUrgeEntity, WorkflowUrgeResponse> {

    @Override
    public WorkflowUrgeResponse convert(WorkflowUrgeEntity source) {
        return new WorkflowUrgeResponse(
                source.getId(),
                source.getInstanceId(),
                source.getNodeId(),
                source.getNodeName(),
                source.getUrgeUserId(),
                source.getUrgeUserName(),
                source.getUrgeTargetId(),
                source.getUrgeTargetName(),
                source.getUrgeContent(),
                source.getIsRead(),
                source.getReadTime(),
                source.getCreateTime()
        );
    }
}