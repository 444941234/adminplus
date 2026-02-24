package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单/权限实体
 * <p>
 * 树形结构实体，继承 TreeEntity 获得父子关系管理能力
 * </p>
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"parent", "children"})
@ToString(callSuper = true, exclude = {"parent", "children"})
@Entity
@Table(name = "sys_menu",
       indexes = {
           @Index(name = "idx_menu_parent_id", columnList = "parent_id"),
           @Index(name = "idx_menu_ancestors", columnList = "ancestors"),
           @Index(name = "idx_menu_sort_order", columnList = "sort_order"),
           @Index(name = "idx_menu_visible", columnList = "visible"),
           @Index(name = "idx_menu_status", columnList = "status"),
           @Index(name = "idx_menu_type", columnList = "type"),
           @Index(name = "idx_menu_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_menu SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class MenuEntity extends TreeEntity<MenuEntity> {

    /**
     * 类型（0=目录，1=菜单，2=按钮）
     */
    @Column(name = "type", nullable = false)
    private Integer type;

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
     * 是否可见（1=显示，0=隐藏）
     */
    @Column(name = "visible", nullable = false)
    private Integer visible = 1;

    /**
     * 状态（1=启用，0=禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 获取子菜单列表（重写父类方法以支持 JPA 映射）
     */
    @Override
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, createTime ASC")
    public List<MenuEntity> getChildren() {
        return super.getChildren();
    }

    /**
     * 设置子菜单列表
     */
    @Override
    public void setChildren(List<MenuEntity> children) {
        super.setChildren(children != null ? children : new ArrayList<>());
    }

    /**
     * 获取父菜单（重写父类方法以支持 JPA 映射）
     */
    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public MenuEntity getParent() {
        return super.getParent();
    }

    /**
     * 设置父菜单
     */
    @Override
    public void setParent(MenuEntity parent) {
        super.setParent(parent);
    }

    /**
     * 检查是否为目录
     */
    public boolean isDirectory() {
        return this.type != null && this.type == 0;
    }

    /**
     * 检查是否为菜单
     */
    public boolean isMenu() {
        return this.type != null && this.type == 1;
    }

    /**
     * 检查是否为按钮
     */
    public boolean isButton() {
        return this.type != null && this.type == 2;
    }
}
