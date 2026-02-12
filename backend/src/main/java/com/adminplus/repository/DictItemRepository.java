package com.adminplus.repository;

import com.adminplus.pojo.entity.DictItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}