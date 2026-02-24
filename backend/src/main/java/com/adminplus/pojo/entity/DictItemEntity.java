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
 * 字典项实体
 * <p>
 * 树形结构实体，继承 TreeEntity 获得父子关系管理能力
 * 支持字典项的层级结构（如：地区分类、行业分类等）
 * </p>
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"parent", "children"})
@ToString(callSuper = true, exclude = {"parent", "children"})
@Entity
@Table(name = "sys_dict_item",
       indexes = {
           @Index(name = "idx_dict_item_dict_id", columnList = "dict_id"),
           @Index(name = "idx_dict_item_parent_id", columnList = "parent_id"),
           @Index(name = "idx_dict_item_ancestors", columnList = "ancestors"),
           @Index(name = "idx_dict_item_sort_order", columnList = "sort_order"),
           @Index(name = "idx_dict_item_status", columnList = "status"),
           @Index(name = "idx_dict_item_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_dict_item SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class DictItemEntity extends TreeEntity<DictItemEntity> {

    /**
     * 字典ID（所属字典）
     */
    @Column(name = "dict_id", nullable = false)
    private String dictId;

    /**
     * 字典标签（显示名称）
     * <p>
     * 注意：TreeEntity 中的 name 字段映射到此实体的 label
     * </p>
     */
    @Column(name = "label", nullable = false, length = 100)
    private String label;

    /**
     * 字典值（实际值）
     */
    @Column(name = "value", nullable = false, length = 100)
    private String value;

    /**
     * 状态（1-正常 0-禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取子节点列表（重写父类方法以支持 JPA 映射）
     */
    @Override
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, createTime ASC")
    public List<DictItemEntity> getChildren() {
        return super.getChildren();
    }

    /**
     * 设置子节点列表
     */
    @Override
    public void setChildren(List<DictItemEntity> children) {
        super.setChildren(children != null ? children : new ArrayList<>());
    }

    /**
     * 获取父节点（重写父类方法以支持 JPA 映射）
     */
    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public DictItemEntity getParent() {
        return super.getParent();
    }

    /**
     * 设置父节点
     */
    @Override
    public void setParent(DictItemEntity parent) {
        super.setParent(parent);
    }

    /**
     * 获取显示名称（与 label 字段同义，用于 TreeEntity 的 name 字段）
     */
    @Override
    public String getName() {
        return this.label;
    }

    /**
     * 设置显示名称
     */
    @Override
    public void setName(String name) {
        this.label = name;
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }
}
