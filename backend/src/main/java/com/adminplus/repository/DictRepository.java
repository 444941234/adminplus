package com.adminplus.repository;

import com.adminplus.entity.DictEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 字典 Repository
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Repository
public interface DictRepository extends JpaRepository<DictEntity, String>, JpaSpecificationExecutor<DictEntity> {

    /**
     * 根据字典类型查询
     */
    Optional<DictEntity> findByDictType(String dictType);

    /**
     * 检查字典类型是否存在
     */
    boolean existsByDictType(String dictType);
}