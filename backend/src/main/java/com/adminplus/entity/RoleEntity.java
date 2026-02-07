package com.adminplus.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 角色实体
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@Entity
@Table(name = "sys_role", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
public class RoleEntity extends BaseEntity {

    /**
     * 角色编码（如 ROLE_ADMIN）
     */
    @Column(name = "code", nullable = false, length = 50, unique = true)
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