package com.adminplus.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 字典项实体
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@Entity
@Table(name = "sys_dict_item")
public class DictItemEntity extends BaseEntity {

    /**
     * 字典ID
     */
    @Column(name = "dict_id", nullable = false)
    private Long dictId;

    /**
     * 父节点ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 字典标签
     */
    @Column(name = "label", nullable = false, length = 100)
    private String label;

    /**
     * 字典值
     */
    @Column(name = "value", nullable = false, length = 100)
    private String value;

    /**
     * 排序
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

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
}