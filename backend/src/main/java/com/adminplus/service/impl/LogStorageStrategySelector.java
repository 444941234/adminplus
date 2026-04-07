package com.adminplus.service.impl;

import com.adminplus.common.properties.LogStorageProperties;
import com.adminplus.service.LogStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 日志存储策略选择器
 * 根据配置自动选择存储策略
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogStorageStrategySelector {

    private final List<LogStorageStrategy> strategies;
    private final LogStorageProperties logStorageProperties;

    private LogStorageStrategy currentStrategy;

    /**
     * 获取当前存储策略
     *
     * @return 存储策略
     */
    public LogStorageStrategy getStrategy() {
        if (currentStrategy != null) {
            return currentStrategy;
        }

        currentStrategy = selectStrategy();
        log.info("日志存储策略: {}", currentStrategy.getStrategyName());
        return currentStrategy;
    }

    /**
     * 根据配置选择存储策略
     */
    private LogStorageStrategy selectStrategy() {
        LogStorageProperties.StorageMode mode = logStorageProperties.getMode();

        return switch (mode) {
            case DATABASE -> {
                // 强制使用数据库
                yield findStrategy("DATABASE");
            }
            case ELASTICSEARCH -> {
                // 强制使用 ES
                LogStorageStrategy esStrategy = findStrategy("ELASTICSEARCH");
                if (esStrategy != null && esStrategy.isAvailable()) {
                    yield esStrategy;
                }
                log.warn("Elasticsearch 不可用，降级到数据库存储");
                yield findStrategy("DATABASE");
            }
            case AUTO -> {
                // 自动选择：ES 可用时优先使用 ES
                LogStorageStrategy esStrategy = findStrategy("ELASTICSEARCH");
                if (esStrategy != null && esStrategy.isAvailable()) {
                    log.info("Elasticsearch 可用，使用 ES 存储");
                    yield esStrategy;
                }
                log.info("Elasticsearch 不可用，使用数据库存储");
                yield findStrategy("DATABASE");
            }
        };
    }

    /**
     * 查找指定名称的策略
     */
    private LogStorageStrategy findStrategy(String name) {
        return strategies.stream()
                .filter(s -> s.getStrategyName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到存储策略: " + name));
    }

    /**
     * 重置策略选择（用于配置变更后）
     */
    public void reset() {
        currentStrategy = null;
    }
}
