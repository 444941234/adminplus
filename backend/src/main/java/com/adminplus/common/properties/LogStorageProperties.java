package com.adminplus.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 日志存储配置属性类
 * <p>
 * 支持数据库和 Elasticsearch 双存储模式
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.logging.storage")
public class LogStorageProperties {

    /**
     * 存储模式: DATABASE, ELASTICSEARCH, AUTO
     * - DATABASE: 强制使用数据库存储
     * - ELASTICSEARCH: 强制使用 ES 存储
     * - AUTO: 自动选择（ES 可用时优先使用 ES，否则降级到数据库）
     */
    private StorageMode mode = StorageMode.AUTO;

    /**
     * ES 索引名称前缀
     * <p>
     * 其他 ES 配置（urls, username, password）在 AppProperties.elasticsearch 中
     * </p>
     */
    private String indexPrefix = "adminplus-log";

    /**
     * 日志清理配置
     */
    private CleanupConfig cleanup = new CleanupConfig();

    /**
     * 存储模式枚举
     */
    public enum StorageMode {
        /** 数据库存储 */
        DATABASE,
        /** Elasticsearch 存储 */
        ELASTICSEARCH,
        /** 自动选择 */
        AUTO
    }

    /**
     * 日志清理配置
     */
    @Data
    public static class CleanupConfig {
        /**
         * 是否启用自动清理
         */
        private boolean enabled = true;

        /**
         * 日志保留天数
         */
        private int retentionDays = 90;

        /**
         * 清理任务 Cron 表达式
         */
        private String cron = "0 0 2 * * ?";

        /**
         * 每次清理的最大批次大小
         */
        private int batchSize = 1000;
    }
}
