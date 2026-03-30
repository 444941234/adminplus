package com.adminplus.repository;

import com.adminplus.pojo.entity.ConfigHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配置历史记录 Repository
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Repository
public interface ConfigHistoryRepository extends JpaRepository<ConfigHistoryEntity, String>, JpaSpecificationExecutor<ConfigHistoryEntity> {

    /**
     * 根据配置 ID 查询历史记录按创建时间降序
     *
     * @param configId 配置 ID
     * @return 历史记录列表
     */
    List<ConfigHistoryEntity> findByConfigIdOrderByCreateTimeDesc(String configId);

    /**
     * 根据配置 ID 查询最新一条历史记录
     *
     * @param configId 配置 ID
     * @return 最新历史记录
     */
    Optional<ConfigHistoryEntity> findFirstByConfigIdOrderByCreateTimeDesc(String configId);

    /**
     * 根据配置键查询历史记录按创建时间降序
     *
     * @param configKey 配置键
     * @return 历史记录列表
     */
    List<ConfigHistoryEntity> findByConfigKeyOrderByCreateTimeDesc(String configKey);
}
