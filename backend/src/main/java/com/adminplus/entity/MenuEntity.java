package com.adminplus.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

/**
 * 菜单/权限实体
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@Entity
@Table(name = "sys_menu")
public class MenuEntity extends BaseEntity {

    /**
     * 父菜单ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 类型（0=目录，1=菜单，2=按钮）
     */
    @Column(name = "type", nullable = false)
    private Integer type;

    /**
     * 菜单名称
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 路由路径
     */
    @Column(name = "path", length = 200)
    private String path;

    /**
     * 组件路径
     */
    @Column(name = "component", length = 200)
    private String component;

    /**
     * 权限标识符（如 user:add）
     */
    @Column(name = "perm_key", length = 100)
    private String permKey;

    /**
     * 图标
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 是否可见（1=显示，0=隐藏）
     */
    @Column(name = "visible", nullable = false)
    private Integer visible = 1;

    /**
     * 状态（1=正常，0=禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;
}