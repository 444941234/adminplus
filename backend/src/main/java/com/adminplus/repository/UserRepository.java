package com.adminplus.repository;

import com.adminplus.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * 用户 Repository
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    /**
     * 根据用户名查询用户
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 统计未删除的用户数量
     */
    long countByDeletedFalse();

    /**
     * 统计指定时间范围内创建的用户数量
     */
    long countByCreateTimeBetweenAndDeletedFalse(Instant startTime, Instant endTime);
}