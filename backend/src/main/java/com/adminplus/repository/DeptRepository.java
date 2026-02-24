package com.adminplus.repository;

import com.adminplus.pojo.entity.DeptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 部门 Repository
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
@Repository
public interface DeptRepository extends JpaRepository<DeptEntity, String> {

    /**
     * 查询所有部门（按排序字段排序）
     */
    List<DeptEntity> findAllByOrderBySortOrderAsc();

    /**
     * 统计未删除的部门数量
     */
    long countByDeletedFalse();

    /**
     * 根据父部门ID查询子部门（兼容 parentId 字段）
     */
    @Query("SELECT d FROM DeptEntity d WHERE d.parent.id = :parentId ORDER BY d.sortOrder ASC")
    List<DeptEntity> findByParentIdOrderBySortOrderAsc(@Param("parentId") String parentId);

    /**
     * 根据父部门ID列表查询子部门
     */
    @Query("SELECT d FROM DeptEntity d WHERE d.parent.id IN :parentIds ORDER BY d.sortOrder ASC")
    List<DeptEntity> findByParentIdInOrderBySortOrderAsc(@Param("parentIds") List<String> parentIds);

    /**
     * 检查部门名称是否存在（排除指定ID）
     */
    boolean existsByNameAndIdNotAndDeletedFalse(String name, String id);

    /**
     * 检查部门名称是否存在
     */
    boolean existsByNameAndDeletedFalse(String name);

    /**
     * 根据 ancestors 查找子孙节点
     */
    @Query("SELECT d FROM DeptEntity d WHERE d.ancestors LIKE :ancestorsPrefix || '%' ORDER BY d.sortOrder ASC")
    List<DeptEntity> findByAncestorsStartingWith(@Param("ancestorsPrefix") String ancestorsPrefix);

    /**
     * 查找根节点（parent 为 null 或 parent.id = '0'）
     */
    @Query("SELECT d FROM DeptEntity d WHERE d.parent IS NULL OR d.parent.id = '0' ORDER BY d.sortOrder ASC")
    List<DeptEntity> findRootNodesOrderBySortOrderAsc();
}