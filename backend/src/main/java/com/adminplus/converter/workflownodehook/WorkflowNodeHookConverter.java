package com.adminplus.converter.workflownodehook;

import com.adminplus.pojo.dto.response.WorkflowNodeHookResponse;
import com.adminplus.pojo.entity.WorkflowNodeHookEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WorkflowNodeHookConverter implements Converter<WorkflowNodeHookEntity, WorkflowNodeHookResponse> {

    @Override
    public WorkflowNodeHookResponse convert(WorkflowNodeHookEntity source) {
        return new WorkflowNodeHookResponse(
                source.getId(),
                source.getNodeId(),
                source.getHookPoint(),
                source.getHookType(),
                source.getExecutorType(),
                source.getExecutorConfig(),
                source.getAsyncExecution(),
                source.getBlockOnFailure(),
                source.getFailureMessage(),
                source.getPriority(),
                source.getConditionExpression(),
                source.getRetryCount(),
                source.getRetryInterval(),
                source.getHookName(),
                source.getDescription(),
                source.getCreateUser(),
                source.getUpdateUser(),
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}