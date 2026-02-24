package com.adminplus.pojo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;

/**
 * Refresh Token 实体
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_refresh_token",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_refresh_token_token", columnNames = "token")
       },
       indexes = {
           @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
           @Index(name = "idx_refresh_token_token", columnList = "token"),
           @Index(name = "idx_refresh_token_expiry_date", columnList = "expiry_date"),
           @Index(name = "idx_refresh_token_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_refresh_token SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class RefreshTokenEntity extends BaseEntity {


    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "token", nullable = false, length = 255)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private Boolean revoked = false;
}