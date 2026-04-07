package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.common.properties.LogStorageProperties;
import com.adminplus.pojo.dto.req.LogEntry;
import com.adminplus.pojo.dto.req.LogQueryReq;
import com.adminplus.pojo.dto.resp.LogPageResp;
import com.adminplus.pojo.dto.resp.LogStatisticsResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.pojo.entity.LogEntity;
import com.adminplus.service.LogService;
import com.adminplus.service.LogStatisticsService;
import com.adminplus.service.LogStorageStrategy;
import com.adminplus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 日志服务实现
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogStorageStrategySelector storageStrategySelector;
    private final LogStorageProperties logStorageProperties;
    private final LogStatisticsService logStatisticsService;

    @Override
    @Async
    public void log(LogEntry entry) {
        try {
            // 需要认证检查时，验证用户状态
            if (!entry.skipAuthCheck() && !SecurityUtils.isAuthenticated()) {
                log.debug("用户已登出，跳过日志保存: module={}, description={}", entry.module(), entry.description());
                return;
            }

            LogEntity entity = buildEntity(entry);
            getStorageStrategy().save(entity);
        } catch (Exception e) {
            log.error("保存日志失败: module={}, description={}", entry.module(), entry.description(), e);
        }
    }

    /**
     * 构建日志实体
     */
    private LogEntity buildEntity(LogEntry entry) {
        LogEntity entity = new LogEntity();

        // 用户信息
        if (entry.skipAuthCheck()) {
            entity.setUserId(entry.username());
            entity.setUsername(entry.username());
        } else {
            entity.setUserId(SecurityUtils.getCurrentUserId());
            entity.setUsername(SecurityUtils.getCurrentUsername());
        }

        // 日志内容
        entity.setModule(entry.module());
        entity.setLogType(entry.logType());
        entity.setOperationType(entry.operationType());
        entity.setDescription(entry.description());

        // 请求信息
        entity.setMethod(entry.method());
        entity.setParams(entry.params());
        entity.setIp(entry.ip());

        // 状态信息
        entity.setCostTime(entry.costTime() != null ? entry.costTime() : 0L);
        entity.setStatus(entry.status());
        entity.setErrorMsg(entry.errorMsg());

        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultResp<LogPageResp> findPage(LogQueryReq query) {
        return getStorageStrategy().findPage(query);
    }

    @Override
    @Transactional(readOnly = true)
    public LogPageResp findById(String id) {
        LogEntity logEntity = getStorageStrategy().findById(id);
        if (logEntity == null) {
            throw new BizException("日志不存在");
        }
        return toLogPageVO(logEntity);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        getStorageStrategy().deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByIds(List<String> ids) {
        getStorageStrategy().deleteByIds(ids);
    }

    @Override
    @Transactional
    public Integer deleteByCondition(LogQueryReq query) {
        return getStorageStrategy().deleteByCondition(query);
    }

    @Override
    @Transactional
    public Integer cleanupExpiredLogs() {
        var cleanupConfig = logStorageProperties.getCleanup();
        if (!cleanupConfig.isEnabled()) {
            log.info("日志清理功能未启用");
            return 0;
        }
        return getStorageStrategy().cleanupExpiredLogs(
            cleanupConfig.getRetentionDays(),
            cleanupConfig.getBatchSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LogStatisticsResp getStatistics() {
        return logStatisticsService.getStatistics();
    }

    private LogStorageStrategy getStorageStrategy() {
        return storageStrategySelector.getStrategy();
    }

    private LogPageResp toLogPageVO(LogEntity entity) {
        return new LogPageResp(
            entity.getId(),
            entity.getUsername(),
            entity.getModule(),
            entity.getLogType(),
            entity.getOperationType(),
            entity.getDescription(),
            entity.getMethod(),
            entity.getParams(),
            entity.getIp(),
            entity.getLocation(),
            entity.getCostTime(),
            entity.getStatus(),
            entity.getErrorMsg(),
            entity.getCreateTime()
        );
    }
}