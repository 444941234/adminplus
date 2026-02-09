package com.adminplus.repository;

import com.adminplus.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 个人中心数据访问层
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Repository
public interface ProfileRepository extends JpaRepository<UserEntity, String> {

    /**
     * 根据用户名查找用户
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
}