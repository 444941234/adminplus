package com.adminplus.pojo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 配置历史实体
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_config_history",
        indexes = {
            @Index(name = "idx_config_history_config_id", columnList = "config_id"),
            @Index(name = "idx_config_history_config_key", columnList = "config_key"),
            @Index(name = "idx_config_history_create_time", columnList = "create_time"),
            @Index(name = "idx_config_history_deleted", columnList = "deleted")
        })
@SQLDelete(sql = "UPDATE sys_config_history SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ConfigHistoryEntity extends BaseEntity {

    /**
     * 配置项 ID
     */
    @Column(name = "config_id", nullable = false, length = 32)
    private String configId;

    /**
     * 配置键（冗余存储，便于查询）
     */
    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    /**
     * 旧值
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * 新值
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * 操作备注
     */
    @Column(name = "remark", length = 200)
    private String remark;
}
