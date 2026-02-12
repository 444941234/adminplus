package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 角色-菜单关联实体
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@Entity
@Table(name = "sys_role_menu", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"role_id", "menu_id"})
})
public class RoleMenuEntity {

    @Id
    private String id;

    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false)
    private String roleId;

    /**
     * 菜单ID
     */
    @Column(name = "menu_id", nullable = false)
    private String menuId;
}