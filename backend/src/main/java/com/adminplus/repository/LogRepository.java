package com.adminplus.repository;

import com.adminplus.entity.LogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 操作日志 Repository
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Repository
public interface LogRepository extends JpaRepository<LogEntity, String> {

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
}