package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-菜单关联实体（中间表）
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "sys_role_menu",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_role_menu_role_menu", columnNames = {"role_id", "menu_id"})
       },
       indexes = {
           @Index(name = "idx_role_menu_role_id", columnList = "role_id"),
           @Index(name = "idx_role_menu_menu_id", columnList = "menu_id")
       })
public class RoleMenuEntity extends BaseEntity {

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
