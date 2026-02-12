package com.adminplus.repository;

import com.adminplus.pojo.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜单 Repository
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, String> {

    /**
     * 查询所有菜单（按排序字段排序）
     */
    List<MenuEntity> findAllByOrderBySortOrderAsc();

    /**
     * 统计未删除的菜单数量
     */
    long countByDeletedFalse();

    /**
     * 统计指定类型的菜单数量
     */
    long countByTypeAndDeletedFalse(Integer type);
}