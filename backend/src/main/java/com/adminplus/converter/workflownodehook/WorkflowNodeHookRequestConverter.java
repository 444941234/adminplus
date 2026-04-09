package com.adminplus.converter.workflownodehook;

import com.adminplus.pojo.dto.workflow.hook.WorkflowNodeHookRequest;
import com.adminplus.pojo.entity.WorkflowNodeHookEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WorkflowNodeHookRequestConverter implements Converter<WorkflowNodeHookRequest, WorkflowNodeHookEntity> {

    @Override
    public WorkflowNodeHookEntity convert(WorkflowNodeHookRequest source) {
        WorkflowNodeHookEntity entity = new WorkflowNodeHookEntity();
        entity.setNodeId(source.nodeId());
        entity.setHookPoint(source.hookPoint());
        entity.setHookType(source.hookType());
        entity.setExecutorType(source.executorType());
        entity.setExecutorConfig(source.executorConfig());
        entity.setAsyncExecution(source.asyncExecution());
        entity.setBlockOnFailure(source.blockOnFailure());
        entity.setFailureMessage(source.failureMessage());
        entity.setPriority(source.priority());
        entity.setConditionExpression(source.conditionExpression());
        entity.setRetryCount(source.retryCount());
        entity.setRetryInterval(source.retryInterval() != null ? source.retryInterval() : 1000);
        entity.setHookName(source.hookName());
        entity.setDescription(source.description());
        return entity;
    }
}