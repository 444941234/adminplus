package com.adminplus.repository;

import com.adminplus.pojo.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Refresh Token 仓库
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByToken(String token);

    List<RefreshTokenEntity> findByUserIdAndRevokedFalse(String userId);

    void deleteByExpiryDateBefore(Instant date);

    void deleteByUserId(String userId);

    /**
     * 查询有效的（未过期且未撤销）Refresh Token
     * 用于获取真正的在线用户
     */
    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.revoked = false AND rt.expiryDate > :now AND rt.deleted = false")
    List<RefreshTokenEntity> findValidTokens(Instant now);

    /**
     * 查询指定用户的有效 Token 数量
     */
    @Query("SELECT COUNT(rt) FROM RefreshTokenEntity rt WHERE rt.userId = :userId AND rt.revoked = false AND rt.expiryDate > :now AND rt.deleted = false")
    long countValidTokensByUserId(String userId, Instant now);
}