package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户-角色关联实体（中间表）
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "sys_user_role",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_role_user_role", columnNames = {"user_id", "role_id"})
       },
       indexes = {
           @Index(name = "idx_user_role_user_id", columnList = "user_id"),
           @Index(name = "idx_user_role_role_id", columnList = "role_id")
       })
public class UserRoleEntity extends BaseEntity {

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false)
    private String roleId;
}
