package com.adminplus.repository;

import com.adminplus.pojo.entity.ConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配置项 Repository
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Repository
public interface ConfigRepository extends JpaRepository<ConfigEntity, String>, JpaSpecificationExecutor<ConfigEntity> {

    /**
     * 根据配置键查询
     *
     * @param key 配置键
     * @return 配置实体
     */
    Optional<ConfigEntity> findByKey(String key);

    /**
     * 检查配置键是否存在
     *
     * @param key 配置键
     * @return 如果配置键存在返回 true，否则返回 false
     */
    boolean existsByKey(String key);

    /**
     * 根据分组 ID 查询配置项按排序序号升序
     *
     * @param groupId 分组 ID
     * @return 配置项列表
     */
    List<ConfigEntity> findByGroupIdOrderBySortOrderAsc(String groupId);

    /**
     * 根据生效类型查询配置项
     *
     * @param effectType 生效类型（immediate-立即生效，restart-重启生效）
     * @return 配置项列表
     */
    List<ConfigEntity> findByEffectType(String effectType);

    /**
     * 根据分组 ID 和状态查询配置项按排序序号升序
     *
     * @param groupId 分组 ID
     * @param status 状态（0-禁用，1-启用）
     * @return 配置项列表
     */
    List<ConfigEntity> findByGroupIdAndStatusOrderBySortOrderAsc(String groupId, Integer status);

    /**
     * 统计分组下的配置项数量
     *
     * @param groupId 分组 ID
     * @return 配置项数量
     */
    long countByGroupId(String groupId);
}
