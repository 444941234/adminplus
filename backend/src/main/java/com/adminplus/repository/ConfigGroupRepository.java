package com.adminplus.repository;

import com.adminplus.pojo.entity.ConfigGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配置分组 Repository
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Repository
public interface ConfigGroupRepository extends JpaRepository<ConfigGroupEntity, String>, JpaSpecificationExecutor<ConfigGroupEntity> {

    /**
     * 根据编码查询分组
     *
     * @param code 分组编码
     * @return 配置分组实体
     */
    Optional<ConfigGroupEntity> findByCode(String code);

    /**
     * 检查编码是否存在
     *
     * @param code 分组编码
     * @return 如果编码存在返回 true，否则返回 false
     */
    boolean existsByCode(String code);

    /**
     * 查询启用的分组按排序序号升序
     *
     * @param status 状态（0-禁用，1-启用）
     * @return 配置分组列表
     */
    List<ConfigGroupEntity> findByStatusOrderBySortOrderAsc(Integer status);
}
