package com.adminplus.repository;

import com.adminplus.pojo.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 用户-角色关联 Repository
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, String> {

    /**
     * 根据用户ID查询角色关联列表
     */
    List<UserRoleEntity> findByUserId(String userId);

    /**
     * 根据用户ID列表查询角色关联列表（批量查询）
     */
    List<UserRoleEntity> findByUserIdIn(List<String> userIds);

    /**
     * 根据角色ID查询用户关联列表
     */
    List<UserRoleEntity> findByRoleId(String roleId);

    /**
     * 精准删除：删除指定用户的指定角色
     */
    void deleteByUserIdAndRoleIdIn(String userId, Collection<String> roleIds);

    /**
     * 删除用户的所有角色
     */
    void deleteByUserId(String userId);

    /**
     * 删除角色的所有用户
     */
    void deleteByRoleId(String roleId);

    /**
     * 检查角色是否已分配给用户
     */
    boolean existsByRoleId(String roleId);
}