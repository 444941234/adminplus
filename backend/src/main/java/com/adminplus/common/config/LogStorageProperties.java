package com.adminplus.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 日志存储配置属性类
 * 支持数据库和 Elasticsearch 双存储模式
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
     * Elasticsearch 配置
     */
    private ElasticsearchConfig elasticsearch = new ElasticsearchConfig();

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
     * Elasticsearch 配置
     */
    @Data
    public static class ElasticsearchConfig {
        /**
         * 是否启用 Elasticsearch
         */
        private boolean enabled = false;

        /**
         * ES 服务器地址（逗号分隔多个地址）
         */
        private String urls = "http://localhost:9200";

        /**
         * 索引名称前缀
         */
        private String indexPrefix = "adminplus-log";

        /**
         * 用户名
         */
        private String username = "";

        /**
         * 密码
         */
        private String password = "";

        /**
         * 连接超时（秒）
         */
        private int connectionTimeout = 10;

        /**
         * Socket 超时（秒）
         */
        private int socketTimeout = 30;
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
