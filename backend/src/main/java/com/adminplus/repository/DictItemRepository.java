package com.adminplus.repository;

import com.adminplus.pojo.entity.DictItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典项 Repository
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Repository
public interface DictItemRepository extends JpaRepository<DictItemEntity, String>, JpaSpecificationExecutor<DictItemEntity> {

    /**
     * 根据字典ID查询字典项列表
     */
    List<DictItemEntity> findByDictIdOrderBySortOrderAsc(String dictId);

    /**
     * 根据字典ID和状态查询字典项列表
     */
    List<DictItemEntity> findByDictIdAndStatusOrderBySortOrderAsc(String dictId, Integer status);

    /**
     * 根据父节点ID查询子节点（兼容 parentId 字段）
     */
    @Query("SELECT d FROM DictItemEntity d WHERE d.parent.id = :parentId ORDER BY d.sortOrder ASC")
    List<DictItemEntity> findByParentIdOrderBySortOrderAsc(@Param("parentId") String parentId);

    /**
     * 根据父节点ID列表查询子节点
     */
    @Query("SELECT d FROM DictItemEntity d WHERE d.parent.id IN :parentIds ORDER BY d.sortOrder ASC")
    List<DictItemEntity> findByParentIdInOrderBySortOrderAsc(@Param("parentIds") List<String> parentIds);

    /**
     * 根据 ancestors 查找子孙节点
     */
    @Query("SELECT d FROM DictItemEntity d WHERE d.ancestors LIKE :ancestorsPrefix || '%' ORDER BY d.sortOrder ASC")
    List<DictItemEntity> findByAncestorsStartingWith(@Param("ancestorsPrefix") String ancestorsPrefix);

    /**
     * 查找指定字典的根节点（parent 为 null 或 parent.id = '0'）
     */
    @Query("SELECT d FROM DictItemEntity d WHERE (d.parent IS NULL OR d.parent.id = '0') AND d.dictId = :dictId ORDER BY d.sortOrder ASC")
    List<DictItemEntity> findRootNodesByDictIdOrderBySortOrderAsc(@Param("dictId") String dictId);

    /**
     * 统计指定字典下的子节点数量
     */
    @Query("SELECT COUNT(d) FROM DictItemEntity d WHERE d.parent.id = :parentId AND d.dictId = :dictId")
    long countByParentIdAndDictId(@Param("parentId") String parentId, @Param("dictId") String dictId);
}
