package com.adminplus.repository;

import com.adminplus.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
}