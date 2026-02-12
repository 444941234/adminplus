package com.adminplus.pojo.entity;

import com.adminplus.utils.IdUtils;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    private String id;

    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    private Instant updateTime;

    @CreatedBy
    @Column(name = "create_user", nullable = false, updatable = false)
    private String createUser;

    @LastModifiedBy
    @Column(name = "update_user", nullable = false)
    private String updateUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = IdUtils.nextIdStr();
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
    }
}