package com.adminplus.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 用户-角色关联实体
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@Entity
@Table(name = "sys_user_role", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "role_id"})
})
public class UserRoleEntity {

    @Id
    private String id;

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