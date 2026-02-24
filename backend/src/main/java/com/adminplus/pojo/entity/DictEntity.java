package com.adminplus.pojo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典实体
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dict",
       indexes = {
           @Index(name = "idx_dict_type", columnList = "dict_type"),
           @Index(name = "idx_dict_status", columnList = "status"),
           @Index(name = "idx_dict_deleted", columnList = "deleted")
       })
public class DictEntity extends BaseEntity {

    /**
     * 字典名称
     */
    @Column(name = "dict_name", nullable = false, length = 100)
    private String dictName;

    /**
     * 字典类型（唯一标识）
     */
    @Column(name = "dict_type", nullable = false, unique = true, length = 100)
    private String dictType;

    /**
     * 状态（1 正常 / 0 禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
}