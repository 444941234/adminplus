package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 工作流节点实体
 * <p>
 * 定义工作流中的审批节点，包括审批人、审批顺序等
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_workflow_node",
       indexes = {
           @Index(name = "idx_wf_node_def_id", columnList = "definition_id"),
           @Index(name = "idx_wf_node_order", columnList = "node_order"),
           @Index(name = "idx_wf_node_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_workflow_node SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class WorkflowNodeEntity extends BaseEntity {

    /**
     * 所属工作流定义ID
     */
    @Column(name = "definition_id", nullable = false)
    private String definitionId;

    /**
     * 节点名称
     */
    @Column(name = "node_name", nullable = false, length = 100)
    private String nodeName;

    /**
     * 节点编码
     */
    @Column(name = "node_code", nullable = false, length = 50)
    private String nodeCode;

    /**
     * 审批顺序（数字越小越先执行）
     */
    @Column(name = "node_order", nullable = false)
    private Integer nodeOrder;

    /**
     * 审批类型（user=指定用户, role=角色, dept=部门, leader=部门领导）
     */
    @Column(name = "approver_type", nullable = false, length = 20)
    private String approverType;

    /**
     * 审批人ID（根据type存储用户ID、角色ID、部门ID等）
     */
    @Column(name = "approver_id", length = 100)
    private String approverId;

    /**
     * 是否会签（true=所有审批人都需审批，false=任一人审批即可）
     */
    @Column(name = "is_counter_sign", nullable = false)
    private Boolean isCounterSign = false;

    /**
     * 是否允许自动通过（true=审批人与发起人相同时自动通过）
     */
    @Column(name = "auto_pass_same_user", nullable = false)
    private Boolean autoPassSameUser = false;

    /**
     * 节点描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 条件表达式（SpEL表达式，用于条件分支）
     */
    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    /**
     * 下一节点列表（存储可能的下一节点ID数组的JSON字符串）
     */
    @Column(name = "next_nodes", columnDefinition = "TEXT")
    private String nextNodes;

    /**
     * 抄送人ID列表（JSON字符串，存储用户ID数组）
     */
    @Column(name = "cc_user_ids", columnDefinition = "TEXT")
    private String ccUserIds;

    /**
     * 抄送角色ID列表（JSON字符串，存储角色ID数组）
     */
    @Column(name = "cc_role_ids", columnDefinition = "TEXT")
    private String ccRoleIds;

    // ==================== 钩子字段（简单场景 - SpEL表达式） ====================

    /**
     * 提交前校验（SpEL表达式）
     */
    @Column(name = "pre_submit_validate", columnDefinition = "TEXT")
    private String preSubmitValidate;

    /**
     * 同意前校验（SpEL表达式）
     */
    @Column(name = "pre_approve_validate", columnDefinition = "TEXT")
    private String preApproveValidate;

    /**
     * 拒绝前校验（SpEL表达式）
     */
    @Column(name = "pre_reject_validate", columnDefinition = "TEXT")
    private String preRejectValidate;

    /**
     * 退回前校验（SpEL表达式）
     */
    @Column(name = "pre_rollback_validate", columnDefinition = "TEXT")
    private String preRollbackValidate;

    /**
     * 取消前校验（SpEL表达式）
     */
    @Column(name = "pre_cancel_validate", columnDefinition = "TEXT")
    private String preCancelValidate;

    /**
     * 撤回前校验（SpEL表达式）
     */
    @Column(name = "pre_withdraw_validate", columnDefinition = "TEXT")
    private String preWithdrawValidate;

    /**
     * 加签前校验（SpEL表达式）
     */
    @Column(name = "pre_add_sign_validate", columnDefinition = "TEXT")
    private String preAddSignValidate;

    /**
     * 提交后执行（SpEL表达式）
     */
    @Column(name = "post_submit_action", columnDefinition = "TEXT")
    private String postSubmitAction;

    /**
     * 同意后执行（SpEL表达式）
     */
    @Column(name = "post_approve_action", columnDefinition = "TEXT")
    private String postApproveAction;

    /**
     * 拒绝后执行（SpEL表达式）
     */
    @Column(name = "post_reject_action", columnDefinition = "TEXT")
    private String postRejectAction;

    /**
     * 退回后执行（SpEL表达式）
     */
    @Column(name = "post_rollback_action", columnDefinition = "TEXT")
    private String postRollbackAction;

    /**
     * 取消后执行（SpEL表达式）
     */
    @Column(name = "post_cancel_action", columnDefinition = "TEXT")
    private String postCancelAction;

    /**
     * 撤回后执行（SpEL表达式）
     */
    @Column(name = "post_withdraw_action", columnDefinition = "TEXT")
    private String postWithdrawAction;

    /**
     * 加签后执行（SpEL表达式）
     */
    @Column(name = "post_add_sign_action", columnDefinition = "TEXT")
    private String postAddSignAction;
}
