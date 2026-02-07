package com.adminplus.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 字典实体
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@Entity
@Table(name = "sys_dict")
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