package com.adminplus.pojo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 配置项实体
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Getter
@Setter
@Entity
@Table(name = "sys_config",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_config_key", columnNames = "key")
        },
        indexes = {
            @Index(name = "idx_config_group_id", columnList = "group_id"),
            @Index(name = "idx_config_key", columnList = "key"),
            @Index(name = "idx_config_status", columnList = "status"),
            @Index(name = "idx_config_deleted", columnList = "deleted")
        })
@SQLDelete(sql = "UPDATE sys_config SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ConfigEntity extends BaseEntity {

    /**
     * 所属分组 ID
     */
    @Column(name = "group_id", nullable = false, length = 32)
    private String groupId;

    /**
     * 配置名称（显示名）
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 配置键（唯一）
     */
    @Column(name = "key", nullable = false, length = 100, unique = true)
    private String key;

    /**
     * 配置值（JSON 存储复杂类型）
     */
    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    /**
     * 值类型：STRING/NUMBER/BOOLEAN/JSON/ARRAY/SECRET/FILE
     */
    @Column(name = "value_type", nullable = false, length = 20)
    private String valueType = "STRING";

    /**
     * 生效方式：IMMEDIATE/MANUAL/RESTART
     */
    @Column(name = "effect_type", nullable = false, length = 20)
    private String effectType = "IMMEDIATE";

    /**
     * 默认值
     */
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    /**
     * 配置说明
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 是否必填
     */
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    /**
     * 校验规则（正则或范围）
     */
    @Column(name = "validation_rule", length = 200)
    private String validationRule;

    /**
     * 排序序号
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * 状态（1 启用 / 0 禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
