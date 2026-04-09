package com.adminplus.pojo.dto.response;

/**
 * 系统信息视图对象
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
public record SystemInfoResponse(
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
         * JVM总内存（MB）
         */
        Long totalMemory,

        /**
         * JVM已用内存（MB）
         */
        Long usedMemory,

        /**
         * JVM可用内存（MB）
         */
        Long freeMemory,

        /**
         * JVM内存使用率（百分比）
         */
        Double memoryUsage,

        /**
         * CPU使用率（百分比，0-100）
         * 注意：这是JVM进程的CPU使用率，不是系统整体的CPU使用率
         */
        Double cpuUsage,

        /**
         * 系统磁盘使用率（百分比）
         * 基于应用运行目录所在磁盘
         */
        Double diskUsage,

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