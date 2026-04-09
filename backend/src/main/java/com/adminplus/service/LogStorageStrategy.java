package com.adminplus.service;

import com.adminplus.pojo.dto.query.LogQuery;
import com.adminplus.pojo.dto.response.LogPageResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.entity.LogEntity;

import java.util.List;

/**
 * 日志存储策略接口
 * 支持数据库和 Elasticsearch 双存储模式
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
public interface LogStorageStrategy {

    /**
     * 保存日志
     *
     * @param log 日志实体
     * @return 保存后的日志实体
     */
    LogEntity save(LogEntity log);

    /**
     * 批量保存日志
     *
     * @param logs 日志实体列表
     * @return 保存后的日志实体列表
     */
    List<LogEntity> saveAll(List<LogEntity> logs);

    /**
     * 根据ID查询日志
     *
     * @param id 日志ID
     * @return 日志实体
     */
    LogEntity findById(String id);

    /**
     * 分页查询日志
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResultResponse<LogPageResponse> findPage(LogQuery query);

    /**
     * 统计日志总数
     *
     * @return 日志总数
     */
    Long count();

    /**
     * 根据条件统计日志数
     *
     * @param query 查询条件
     * @return 日志数
     */
    Long countByCondition(LogQuery query);

    /**
     * 删除日志
     *
     * @param id 日志ID
     */
    void deleteById(String id);

    /**
     * 批量删除日志
     *
     * @param ids 日志ID列表
     * @return 删除的记录数
     */
    Integer deleteByIds(List<String> ids);

    /**
     * 根据条件删除日志
     *
     * @param query 查询条件
     * @return 删除的记录数
     */
    Integer deleteByCondition(LogQuery query);

    /**
     * 清理过期日志
     *
     * @param retentionDays 保留天数
     * @param batchSize 每次清理的批次大小
     * @return 清理的记录数
     */
    Integer cleanupExpiredLogs(int retentionDays, int batchSize);

    /**
     * 获取策略名称
     *
     * @return 策略名称（DATABASE, ELASTICSEARCH）
     */
    String getStrategyName();

    /**
     * 检查策略是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}
