package com.adminplus.pojo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 配置分组实体
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_config_group",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_config_group_code", columnNames = "code")
        },
        indexes = {
            @Index(name = "idx_config_group_code", columnList = "code"),
            @Index(name = "idx_config_group_sort_order", columnList = "sort_order"),
            @Index(name = "idx_config_group_status", columnList = "status"),
            @Index(name = "idx_config_group_deleted", columnList = "deleted")
        })
@SQLDelete(sql = "UPDATE sys_config_group SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ConfigGroupEntity extends BaseEntity {

    /**
     * 分组名称
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 分组编码（唯一标识）
     */
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    /**
     * 分组图标（Lucide 图标名）
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * 排序序号
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * 分组描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 状态（1 启用 / 0 禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
