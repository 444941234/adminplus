package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 工作流定义实体
 * <p>
 * 定义工作流模板，包括工作流的基本信息和节点配置
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_workflow_definition",
       indexes = {
           @Index(name = "idx_wf_def_key", columnList = "definition_key"),
           @Index(name = "idx_wf_def_status", columnList = "status"),
           @Index(name = "idx_wf_def_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_workflow_definition SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class WorkflowDefinitionEntity extends BaseEntity {

    /**
     * 工作流定义名称
     */
    @Column(name = "definition_name", nullable = false, length = 100)
    private String definitionName;

    /**
     * 工作流定义键（唯一标识）
     */
    @Column(name = "definition_key", nullable = false, length = 50, unique = true)
    private String definitionKey;

    /**
     * 工作流分类
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * 描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 状态（1=启用，0=禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 版本号
     */
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    /**
     * 表单配置JSON（存储表单字段定义）
     */
    @Column(name = "form_config", columnDefinition = "TEXT")
    private String formConfig;

    /**
     * 是否启用（便捷方法）
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }
}
