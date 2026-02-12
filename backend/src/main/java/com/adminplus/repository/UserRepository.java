package com.adminplus.repository;

import com.adminplus.pojo.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
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

    /**
     * 根据部门ID查询用户列表
     */
    Page<UserEntity> findByDeptId(String deptId, Pageable pageable);

    /**
     * 根据部门ID列表查询用户列表
     */
    Page<UserEntity> findByDeptIdIn(List<String> deptIds, Pageable pageable);
}