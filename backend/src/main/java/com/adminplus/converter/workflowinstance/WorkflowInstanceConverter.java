package com.adminplus.converter.workflowinstance;

import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.repository.DeptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * WorkflowInstanceEntity → WorkflowInstanceResponse 转换器
 * <p>
 * 自动查询 deptName，权限字段由 Service 层补充
 */
@Component
@RequiredArgsConstructor
public class WorkflowInstanceConverter implements Converter<WorkflowInstanceEntity, WorkflowInstanceResponse> {

    private final DeptRepository deptRepository;

    @Override
    public WorkflowInstanceResponse convert(WorkflowInstanceEntity source) {
        // 查询部门名称
        String deptName = null;
        if (source.getDeptId() != null) {
            deptName = deptRepository.findById(source.getDeptId())
                    .map(DeptEntity::getName)
                    .orElse(null);
        }

        return new WorkflowInstanceResponse(
                source.getId(),
                source.getDefinitionId(),
                source.getDefinitionName(),
                source.getUserId(),
                source.getUserName(),
                source.getDeptId(),
                deptName,
                source.getTitle(),
                source.getBusinessData(),
                source.getCurrentNodeId(),
                source.getCurrentNodeName(),
                normalizeStatusForResponse(source.getStatus()),
                source.getSubmitTime(),
                source.getFinishTime(),
                source.getRemark(),
                source.getCreateTime(),
                null, null, null, null, null, null, null // 权限字段由 Service 层计算
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