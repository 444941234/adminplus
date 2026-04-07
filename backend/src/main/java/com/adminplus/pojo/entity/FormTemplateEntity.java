package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 表单模板实体
 * <p>
 * 用于存储可复用的表单配置模板，在流程设计中可以直接引用
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_form_template",
       indexes = {
           @Index(name = "idx_form_template_code", columnList = "template_code"),
           @Index(name = "idx_form_template_category", columnList = "category"),
           @Index(name = "idx_form_template_status", columnList = "status"),
           @Index(name = "idx_form_template_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_form_template SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class FormTemplateEntity extends BaseEntity {

    /**
     * 表单名称
     */
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    /**
     * 表单标识（唯一标识）
     */
    @Column(name = "template_code", nullable = false, length = 50, unique = true)
    private String templateCode;

    /**
     * 分类
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * 描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 表单配置JSON
     */
    @Column(name = "form_config", columnDefinition = "TEXT")
    private String formConfig;

    /**
     * 状态（1=启用，0=禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 是否启用（便捷方法）
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }
}
