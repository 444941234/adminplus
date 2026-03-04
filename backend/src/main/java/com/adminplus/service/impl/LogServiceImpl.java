package com.adminplus.service.impl;

import com.adminplus.common.config.LogStorageProperties;
import com.adminplus.common.exception.BizException;
import com.adminplus.constants.LogStatus;
import com.adminplus.constants.LogType;
import com.adminplus.pojo.dto.req.LogQueryDTO;
import com.adminplus.pojo.dto.resp.LogPageVO;
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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 日志服务实现
 * 使用存储策略模式支持数据库和 ES 双存储
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
    @Transactional
    public void log(String module, Integer operationType, String description) {
        log(module, operationType, description, null, null, null);
    }

    @Override
    @Async
    @Transactional
    public void log(String module, Integer operationType, String description,
                    String method, String params, String ip) {
        try {
            if (!SecurityUtils.isAuthenticated()) {
                log.debug("用户已登出，跳过日志保存: module={}, operationType={}, description={}", module, operationType, description);
                return;
            }

            LogEntity logEntity = new LogEntity();
            logEntity.setUserId(SecurityUtils.getCurrentUserId());
            logEntity.setUsername(SecurityUtils.getCurrentUsername());
            logEntity.setModule(module);
            logEntity.setLogType(LogType.OPERATION);
            logEntity.setOperationType(operationType);
            logEntity.setDescription(description);
            logEntity.setMethod(method);
            logEntity.setParams(params);
            logEntity.setIp(ip);
            logEntity.setStatus(LogStatus.SUCCESS);
            logEntity.setCostTime(0L);

            getStorageStrategy().save(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    @Override
    @Async
    @Transactional
    public void log(String module, Integer operationType, String description, Long costTime) {
        try {
            if (!SecurityUtils.isAuthenticated()) {
                log.debug("用户已登出，跳过日志保存: module={}, operationType={}, description={}", module, operationType, description);
                return;
            }

            LogEntity logEntity = new LogEntity();
            logEntity.setUserId(SecurityUtils.getCurrentUserId());
            logEntity.setUsername(SecurityUtils.getCurrentUsername());
            logEntity.setModule(module);
            logEntity.setLogType(LogType.OPERATION);
            logEntity.setOperationType(operationType);
            logEntity.setDescription(description);
            logEntity.setCostTime(costTime);
            logEntity.setStatus(LogStatus.SUCCESS);

            getStorageStrategy().save(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    @Override
    @Async
    @Transactional
    public void log(String module, Integer operationType, String description,
                    Integer status, String errorMsg) {
        try {
            if (!SecurityUtils.isAuthenticated()) {
                log.debug("用户已登出，跳过日志保存: module={}, operationType={}, description={}", module, operationType, description);
                return;
            }

            LogEntity logEntity = new LogEntity();
            logEntity.setUserId(SecurityUtils.getCurrentUserId());
            logEntity.setUsername(SecurityUtils.getCurrentUsername());
            logEntity.setModule(module);
            logEntity.setLogType(LogType.OPERATION);
            logEntity.setOperationType(operationType);
            logEntity.setDescription(description);
            logEntity.setStatus(status);
            logEntity.setErrorMsg(errorMsg);

            getStorageStrategy().save(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    @Override
    @Async
    @Transactional
    public void logLogin(String username, Integer status, String errorMsg) {
        try {
            LogEntity logEntity = new LogEntity();
            logEntity.setUserId(username);
            logEntity.setUsername(username);
            logEntity.setModule("用户登录");
            logEntity.setLogType(LogType.LOGIN);
            logEntity.setOperationType(status == LogStatus.SUCCESS ? 1 : 2);
            logEntity.setDescription(status == LogStatus.SUCCESS ? "用户登录成功" : "用户登录失败");
            logEntity.setStatus(status);
            logEntity.setErrorMsg(errorMsg);
            logEntity.setCostTime(0L);

            getStorageStrategy().save(logEntity);
        } catch (Exception e) {
            log.error("保存登录日志失败", e);
        }
    }

    @Override
    @Async
    @Transactional
    public void logSystem(String module, String message, String errorMsg) {
        try {
            LogEntity logEntity = new LogEntity();
            logEntity.setUserId("system");
            logEntity.setUsername("system");
            logEntity.setModule(module);
            logEntity.setLogType(LogType.SYSTEM);
            logEntity.setOperationType(7); // 其他
            logEntity.setDescription(message);
            logEntity.setStatus(errorMsg == null ? LogStatus.SUCCESS : LogStatus.FAILED);
            logEntity.setErrorMsg(errorMsg);
            logEntity.setCostTime(0L);

            getStorageStrategy().save(logEntity);
        } catch (Exception e) {
            log.error("保存系统日志失败", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultResp<LogPageVO> findPage(LogQueryDTO query) {
        return getStorageStrategy().findPage(query);
    }

    @Override
    @Transactional(readOnly = true)
    public LogPageVO findById(String id) {
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
    public Integer deleteByCondition(LogQueryDTO query) {
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

    /**
     * 获取当前存储策略
     */
    private LogStorageStrategy getStorageStrategy() {
        return storageStrategySelector.getStrategy();
    }

    /**
     * 转换为 VO
     */
    private LogPageVO toLogPageVO(LogEntity entity) {
        return new LogPageVO(
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
