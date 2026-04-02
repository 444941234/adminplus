package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 工作流节点钩子配置实体
 * <p>
 * 定义工作流节点的钩子配置，支持SpEL、Bean、HTTP三种执行方式
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_workflow_node_hook",
       indexes = {
           @Index(name = "idx_wf_hook_node_id", columnList = "node_id"),
           @Index(name = "idx_wf_hook_point", columnList = "hook_point"),
           @Index(name = "idx_wf_hook_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_workflow_node_hook SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class WorkflowNodeHookEntity extends BaseEntity {

    /**
     * 关联节点ID
     */
    @Column(name = "node_id", nullable = false)
    private String nodeId;

    /**
     * 钩子点：PRE_SUBMIT, POST_APPROVE 等
     */
    @Column(name = "hook_point", nullable = false, length = 30)
    private String hookPoint;

    /**
     * 类型：validate / execute
     */
    @Column(name = "hook_type", nullable = false, length = 20)
    private String hookType;

    /**
     * 执行方式：spel / bean / http
     */
    @Column(name = "executor_type", nullable = false, length = 20)
    private String executorType;

    /**
     * 执行配置（JSON格式）
     * spel: {"expression": "#formData.amount > 100", "failureMessage": "金额必须大于100"}
     * bean: {"beanName": "myHookService", "methodName": "validateSubmit", "args": ["#instance"]}
     * http: {"url": "http://api.example.com/hook", "method": "POST", "headers": {}, "bodyTemplate": "{}"}
     */
    @Column(name = "executor_config", columnDefinition = "TEXT")
    private String executorConfig;

    /**
     * 是否异步执行
     */
    @Column(name = "async_execution", nullable = false)
    private Boolean asyncExecution = false;

    /**
     * 失败时是否阻断流程
     */
    @Column(name = "block_on_failure", nullable = false)
    private Boolean blockOnFailure = true;

    /**
     * 默认失败提示消息
     */
    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    /**
     * 执行优先级（数字越小越先执行）
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    /**
     * 触发条件（可选 SpEL 表达式）
     */
    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    /**
     * 重试次数
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    /**
     * 重试间隔（毫秒）
     */
    @Column(name = "retry_interval")
    private Integer retryInterval = 1000;

    /**
     * 钩子名称
     */
    @Column(name = "hook_name", length = 100)
    private String hookName;

    /**
     * 钩子描述
     */
    @Column(name = "description", length = 500)
    private String description;
}
