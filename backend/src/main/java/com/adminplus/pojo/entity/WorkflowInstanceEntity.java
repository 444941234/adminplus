package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;

/**
 * 工作流实例实体
 * <p>
 * 工作流的运行时实例，记录工作流的执行状态
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_workflow_instance",
       indexes = {
           @Index(name = "idx_wf_inst_def_id", columnList = "definition_id"),
           @Index(name = "idx_wf_inst_user_id", columnList = "user_id"),
           @Index(name = "idx_wf_inst_status", columnList = "status"),
           @Index(name = "idx_wf_inst_current_node", columnList = "current_node_id"),
           @Index(name = "idx_wf_inst_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_workflow_instance SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class WorkflowInstanceEntity extends BaseEntity {

    /**
     * 工作流定义ID
     */
    @Column(name = "definition_id", nullable = false)
    private String definitionId;

    /**
     * 工作流定义名称（冗余字段，方便查询）
     */
    @Column(name = "definition_name", length = 100)
    private String definitionName;

    /**
     * 发起人ID
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * 发起人姓名（冗余字段）
     */
    @Column(name = "user_name", length = 50)
    private String userName;

    /**
     * 发起人部门ID
     */
    @Column(name = "dept_id")
    private String deptId;

    /**
     * 流程标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 业务数据JSON（存储表单提交的数据）
     */
    @Column(name = "business_data", columnDefinition = "TEXT")
    private String businessData;

    /**
     * 当前节点ID
     */
    @Column(name = "current_node_id")
    private String currentNodeId;

    /**
     * 当前节点名称
     */
    @Column(name = "current_node_name", length = 100)
    private String currentNodeName;

    /**
     * 流程状态（draft=草稿, running=进行中, approved=已通过, rejected=已拒绝, cancelled=已取消）
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "draft";

    /**
     * 提交时间
     */
    @Column(name = "submit_time")
    private Instant submitTime;

    /**
     * 完成时间
     */
    @Column(name = "finish_time")
    private Instant finishTime;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 是否为草稿
     */
    public boolean isDraft() {
        return "draft".equals(this.status);
    }

    /**
     * 是否运行中
     */
    public boolean isRunning() {
        return "running".equals(this.status);
    }

    /**
     * 是否已完成
     */
    public boolean isFinished() {
        return "approved".equals(this.status) || "rejected".equals(this.status) || "cancelled".equals(this.status);
    }

    /**
     * 是否已通过
     */
    public boolean isApproved() {
        return "approved".equals(this.status);
    }

    /**
     * 是否已拒绝
     */
    public boolean isRejected() {
        return "rejected".equals(this.status);
    }

    /**
     * 是否可以取消
     */
    public boolean isCancellable() {
        return "draft".equals(this.status) || "running".equals(this.status);
    }
}
