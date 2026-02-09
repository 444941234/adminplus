package com.adminplus.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * 基础实体类
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    private String id;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private Instant updateTime;

    /**
     * 创建人
     */
    @Column(name = "create_user", nullable = false, updatable = false)
    private String createUser;

    /**
     * 更新人
     */
    @Column(name = "update_user", nullable = false)
    private String updateUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}