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
 * 部门实体
 * <p>
 * 树形结构实体，继承 TreeEntity 获得父子关系管理能力
 * </p>
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"parent", "children"})
@ToString(callSuper = true, exclude = {"parent", "children"})
@Entity
@Table(name = "sys_dept",
       indexes = {
           @Index(name = "idx_dept_parent_id", columnList = "parent_id"),
           @Index(name = "idx_dept_ancestors", columnList = "ancestors"),
           @Index(name = "idx_dept_sort_order", columnList = "sort_order"),
           @Index(name = "idx_dept_status", columnList = "status"),
           @Index(name = "idx_dept_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_dept SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class DeptEntity extends TreeEntity<DeptEntity> {

    /**
     * 部门编码
     */
    @Column(name = "code", length = 50)
    private String code;

    /**
     * 部门负责人
     */
    @Column(name = "leader", length = 50)
    private String leader;

    /**
     * 联系电话
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 状态（1=正常，0=禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 获取子部门列表（重写父类方法以支持 JPA 映射）
     */
    @Override
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, createTime ASC")
    public List<DeptEntity> getChildren() {
        return super.getChildren();
    }

    /**
     * 设置子部门列表
     */
    @Override
    public void setChildren(List<DeptEntity> children) {
        super.setChildren(children != null ? children : new ArrayList<>());
    }

    /**
     * 获取父部门（重写父类方法以支持 JPA 映射）
     */
    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public DeptEntity getParent() {
        return super.getParent();
    }

    /**
     * 设置父部门
     */
    @Override
    public void setParent(DeptEntity parent) {
        super.setParent(parent);
    }
}
