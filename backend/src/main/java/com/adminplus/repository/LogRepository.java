package com.adminplus.repository;

import com.adminplus.pojo.entity.LogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * 操作日志 Repository
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Repository
public interface LogRepository extends JpaRepository<LogEntity, String>, JpaSpecificationExecutor<LogEntity> {

    /**
     * 统计未删除的日志数量
     */
    long countByDeletedFalse();

    /**
     * 获取最近10条操作日志
     */
    List<LogEntity> findTop10ByDeletedFalseOrderByCreateTimeDesc();

    /**
     * 获取最近成功的登录日志（用于在线用户）
     */
    List<LogEntity> findTop10ByStatusAndDeletedFalseOrderByCreateTimeDesc(Integer status);

    /**
     * 统计指定时间范围内的日志数量
     */
    long countByCreateTimeBetweenAndDeletedFalse(Instant startTime, Instant endTime);

    /**
     * 统计今日访问量（操作日志数量）
     */
    long countByCreateTimeGreaterThanEqualAndDeletedFalse(Instant startTime);

    /**
     * 查询指定时间范围内的不同用户数（活跃用户）
     * 使用 JPQL 查询以确保跨数据库兼容性
     */
    @Query("SELECT COUNT(DISTINCT log.userId) FROM LogEntity log WHERE log.createTime >= :startTime AND log.createTime < :endTime AND log.deleted = false AND log.userId IS NOT NULL")
    long countDistinctUsersByTimeRange(Instant startTime, Instant endTime);

    /**
     * 查询从某个时间开始的不同用户数（活跃用户）
     */
    @Query("SELECT COUNT(DISTINCT log.userId) FROM LogEntity log WHERE log.createTime >= :startTime AND log.deleted = false AND log.userId IS NOT NULL")
    long countDistinctUsersSince(Instant startTime);

    /**
     * 查询指定时间范围内的日志（用于备用方案）
     */
    List<LogEntity> findByCreateTimeBetweenAndDeletedFalseOrderByCreateTimeDesc(Instant startTime, Instant endTime);
}