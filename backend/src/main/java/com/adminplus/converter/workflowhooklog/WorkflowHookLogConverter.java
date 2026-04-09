package com.adminplus.converter.workflowhooklog;

import com.adminplus.pojo.dto.response.WorkflowHookLogResponse;
import com.adminplus.pojo.entity.WorkflowHookLogEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WorkflowHookLogConverter implements Converter<WorkflowHookLogEntity, WorkflowHookLogResponse> {

    @Override
    public WorkflowHookLogResponse convert(WorkflowHookLogEntity source) {
        return new WorkflowHookLogResponse(
                source.getId(),
                source.getInstanceId(),
                source.getNodeId(),
                source.getHookId(),
                source.getHookSource(),
                source.getHookPoint(),
                source.getExecutorType(),
                source.getExecutorConfig(),
                source.getSuccess(),
                source.getResultCode(),
                source.getResultMessage(),
                source.getExecutionTime(),
                source.getRetryAttempts(),
                source.getAsync(),
                source.getOperatorId(),
                source.getOperatorName(),
                source.getCreateUser(),
                source.getUpdateUser(),
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}