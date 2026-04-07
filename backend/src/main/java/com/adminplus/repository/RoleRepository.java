package com.adminplus.repository;

import com.adminplus.pojo.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色 Repository
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String>, JpaSpecificationExecutor<RoleEntity> {

    /**
     * 根据角色编码查询角色
     */
    Optional<RoleEntity> findByCode(String code);

    /**
     * 根据角色名称查询角色
     */
    Optional<RoleEntity> findByName(String name);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 统计未删除的角色数量
     */
    long countByDeletedFalse();

    /**
     * 查询所有未删除的角色
     */
    List<RoleEntity> findByDeletedFalse();

    /**
     * 查询未删除且编码不等于指定值的角色
     */
    List<RoleEntity> findByDeletedFalseAndCodeNot(String code);
}