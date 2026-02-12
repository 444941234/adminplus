package com.adminplus.repository;

import com.adminplus.pojo.entity.DeptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * 根据父部门ID查询子部门
     */
    List<DeptEntity> findByParentIdOrderBySortOrderAsc(String parentId);

    /**
     * 检查部门名称是否存在（排除指定ID）
     */
    boolean existsByNameAndIdNotAndDeletedFalse(String name, String id);

    /**
     * 检查部门名称是否存在
     */
    boolean existsByNameAndDeletedFalse(String name);

    /**
     * 根据父部门ID列表查询子部门
     */
    List<DeptEntity> findByParentIdInOrderBySortOrderAsc(List<String> parentIds);
}