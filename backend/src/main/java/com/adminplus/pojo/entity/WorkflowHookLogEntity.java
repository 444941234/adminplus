package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 工作流钩子执行日志实体
 * <p>
 * 记录工作流钩子的执行结果，用于调试和审计
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_workflow_hook_log",
       indexes = {
           @Index(name = "idx_wf_hook_log_instance", columnList = "instance_id"),
           @Index(name = "idx_wf_hook_log_node", columnList = "node_id"),
           @Index(name = "idx_wf_hook_log_point", columnList = "hook_point"),
           @Index(name = "idx_wf_hook_log_time", columnList = "create_time"),
           @Index(name = "idx_wf_hook_log_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_workflow_hook_log SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class WorkflowHookLogEntity extends BaseEntity {

    /**
     * 工作流实例ID
     */
    @Column(name = "instance_id", nullable = false)
    private String instanceId;

    /**
     * 节点ID（可为空，如提交前）
     */
    @Column(name = "node_id")
    private String nodeId;

    /**
     * 钩子配置ID（独立表钩子）
     */
    @Column(name = "hook_id")
    private String hookId;

    /**
     * 来源：node_field / hook_table
     */
    @Column(name = "hook_source", length = 20)
    private String hookSource;

    /**
     * 钩子点
     */
    @Column(name = "hook_point", nullable = false, length = 30)
    private String hookPoint;

    /**
     * 执行方式
     */
    @Column(name = "executor_type", length = 20)
    private String executorType;

    /**
     * 执行配置
     */
    @Column(name = "executor_config", columnDefinition = "TEXT")
    private String executorConfig;

    /**
     * 是否成功
     */
    @Column(name = "success", nullable = false)
    private Boolean success;

    /**
     * 结果码
     */
    @Column(name = "result_code", length = 50)
    private String resultCode;

    /**
     * 结果消息
     */
    @Column(name = "result_message", columnDefinition = "TEXT")
    private String resultMessage;

    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "execution_time")
    private Long executionTime;

    /**
     * 实际重试次数
     */
    @Column(name = "retry_attempts")
    private Integer retryAttempts;

    /**
     * 是否异步执行
     */
    @Column(name = "async")
    private Boolean async;

    /**
     * 操作人ID
     */
    @Column(name = "operator_id", length = 50)
    private String operatorId;

    /**
     * 操作人姓名
     */
    @Column(name = "operator_name", length = 100)
    private String operatorName;
}
