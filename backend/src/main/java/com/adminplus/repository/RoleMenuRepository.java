package com.adminplus.repository;

import com.adminplus.pojo.entity.RoleMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色-菜单关联 Repository
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenuEntity, String> {

    /**
     * 根据角色ID查询菜单ID列表
     */
    @Query("SELECT rm.menuId FROM RoleMenuEntity rm WHERE rm.roleId = :roleId")
    List<String> findMenuIdByRoleId(@Param("roleId") String roleId);

    /**
     * 根据菜单ID查询角色ID列表
     */
    @Query("SELECT rm.roleId FROM RoleMenuEntity rm WHERE rm.menuId = :menuId")
    List<String> findRoleIdByMenuId(@Param("menuId") String menuId);

    /**
     * 删除角色的所有菜单
     */
    void deleteByRoleId(String roleId);

    /**
     * 删除菜单的所有角色
     */
    void deleteByMenuId(String menuId);
}