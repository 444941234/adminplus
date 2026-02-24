package com.adminplus.pojo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 角色实体
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "sys_role",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_role_code", columnNames = "code")
       },
       indexes = {
           @Index(name = "idx_role_status", columnList = "status"),
           @Index(name = "idx_role_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_role SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class RoleEntity extends BaseEntity {

    /**
     * 角色编码（如 ROLE_ADMIN）
     */
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    /**
     * 角色名称
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 数据权限范围（1=全部，2=本部门，3=本部门及以下，4=仅本人）
     */
    @Column(name = "data_scope", nullable = false)
    private Integer dataScope = 1;

    /**
     * 状态（1=正常，0=禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
}