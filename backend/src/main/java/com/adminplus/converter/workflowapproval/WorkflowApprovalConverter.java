package com.adminplus.converter.workflowapproval;

import com.adminplus.pojo.dto.response.WorkflowApprovalResponse;
import com.adminplus.pojo.entity.WorkflowApprovalEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WorkflowApprovalConverter implements Converter<WorkflowApprovalEntity, WorkflowApprovalResponse> {

    @Override
    public WorkflowApprovalResponse convert(WorkflowApprovalEntity source) {
        return new WorkflowApprovalResponse(
                source.getId(),
                source.getInstanceId(),
                source.getNodeId(),
                source.getNodeName(),
                source.getApproverId(),
                source.getApproverName(),
                source.getApprovalStatus(),
                source.getComment(),
                source.getAttachments(),
                source.getApprovalTime(),
                source.getCreateTime()
        );
    }
}