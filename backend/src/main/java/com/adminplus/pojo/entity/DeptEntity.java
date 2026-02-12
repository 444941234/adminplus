package com.adminplus.pojo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "sys_dept")
public class DeptEntity extends BaseEntity {

    /**
     * 父部门ID
     */
    @Column(name = "parent_id")
    private String parentId;

    /**
     * 部门名称
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

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
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 状态（1=正常，0=禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;
}