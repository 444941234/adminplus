package com.adminplus.pojo.dto.resp;

/**
 * 系统信息视图对象
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
public record SystemInfoResp(
        /**
         * 系统名称
         */
        String systemName,

        /**
         * 系统版本
         */
        String systemVersion,

        /**
         * 操作系统
         */
        String osName,

        /**
         * JDK版本
         */
        String jdkVersion,

        /**
         * 总内存（MB）
         */
        Long totalMemory,

        /**
         * 已用内存（MB）
         */
        Long usedMemory,

        /**
         * 可用内存（MB）
         */
        Long freeMemory,

        /**
         * 数据库类型
         */
        String databaseType,

        /**
         * 数据库版本
         */
        String databaseVersion,

        /**
         * 数据库连接数
         */
        Integer databaseConnections,

        /**
         * 运行时间（秒）
         */
        Long uptime
) {
}