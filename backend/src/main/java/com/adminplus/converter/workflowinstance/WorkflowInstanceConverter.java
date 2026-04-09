package com.adminplus.converter.workflowinstance;

import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class WorkflowInstanceConverter implements Converter<WorkflowInstanceEntity, WorkflowInstanceResponse> {

    @Override
    public WorkflowInstanceResponse convert(WorkflowInstanceEntity source) {
        return new WorkflowInstanceResponse(
                source.getId(),
                source.getDefinitionId(),
                source.getDefinitionName(),
                source.getUserId(),
                source.getUserName(),
                source.getDeptId(),
                null, // deptName - to be set by service layer
                source.getTitle(),
                source.getBusinessData(),
                source.getCurrentNodeId(),
                source.getCurrentNodeName(),
                normalizeStatusForResponse(source.getStatus()),
                source.getSubmitTime(),
                source.getFinishTime(),
                source.getRemark(),
                source.getCreateTime(),
                null, null, null, null, null, null, null // pendingApproval, canApprove, etc. - to be set by service layer
        );
    }

    private String normalizeStatusForResponse(String status) {
        return switch (status) {
            case "draft" -> "DRAFT";
            case "running" -> "PROCESSING";
            case "approved" -> "APPROVED";
            case "rejected" -> "REJECTED";
            case "cancelled" -> "CANCELLED";
            default -> status.toUpperCase(Locale.ROOT);
        };
    }
}