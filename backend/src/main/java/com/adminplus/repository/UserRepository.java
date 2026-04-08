package com.adminplus.repository;

import com.adminplus.pojo.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * 根据关键词搜索用户（用户名、昵称、邮箱、手机号）
     */
    @Query("SELECT u FROM UserEntity u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "u.phone LIKE CONCAT('%', :keyword, '%')")
    Page<UserEntity> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据关键词和部门ID列表搜索用户
     */
    @Query("SELECT u FROM UserEntity u WHERE u.deptId IN :deptIds AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "u.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<UserEntity> findByKeywordAndDeptIdIn(@Param("keyword") String keyword,
                                               @Param("deptIds") List<String> deptIds,
                                               Pageable pageable);

    /**
     * 根据状态查询用户
     */
    Page<UserEntity> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据部门和状态查询用户
     */
    Page<UserEntity> findByDeptIdInAndStatus(List<String> deptIds, Integer status, Pageable pageable);

    /**
     * 根据关键词和状态搜索用户
     */
    @Query("SELECT u FROM UserEntity u WHERE u.status = :status AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "u.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<UserEntity> findByKeywordAndStatus(@Param("keyword") String keyword,
                                             @Param("status") Integer status,
                                             Pageable pageable);

    /**
     * 根据关键词、部门ID列表和状态搜索用户
     */
    @Query("SELECT u FROM UserEntity u WHERE u.deptId IN :deptIds AND u.status = :status AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "u.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<UserEntity> findByKeywordAndDeptIdInAndStatus(@Param("keyword") String keyword,
                                                        @Param("deptIds") List<String> deptIds,
                                                        @Param("status") Integer status,
                                                        Pageable pageable);
}