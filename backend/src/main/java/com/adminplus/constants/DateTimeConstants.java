package com.adminplus.constants;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间格式常量
 * <p>
 * 统一管理项目中使用的日期时间格式，避免重复定义
 * <p>
 * 注意：不带 ZoneId 的格式器用于 LocalDateTime/LocalDate；
 * 带 ZoneId 的格式器用于 Instant
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface DateTimeConstants {

    // ==================== 标准日期时间格式（不带 ZoneId） ====================
    // 用于 LocalDateTime 的 parse/format

    /**
     * 标准日期时间格式：yyyy-MM-dd HH:mm:ss
     * <p>
     * 用于 LocalDateTime 格式化/解析
     */
    DateTimeFormatter STANDARD_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 标准日期格式：yyyy-MM-dd
     * <p>
     * 用于 LocalDate 格式化/解析
     */
    DateTimeFormatter STANDARD_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 简短日期格式：MM-dd
     * <p>
     * 用于图表显示、简短日期展示（LocalDate）
     */
    DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MM-dd");

    // ==================== 标准日期时间格式（带 ZoneId） ====================
    // 用于 Instant 的 format

    /**
     * 标准日期时间格式（带系统时区）：yyyy-MM-dd HH:mm:ss
     * <p>
     * 用于 Instant 格式化显示
     */
    DateTimeFormatter STANDARD_DATE_TIME_WITH_ZONE = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    // ==================== 文件路径日期格式 ====================

    /**
     * 文件路径日期格式：yyyy/MM/dd
     * <p>
     * 用于文件存储目录结构（LocalDate）
     */
    DateTimeFormatter FILE_PATH_DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    // ==================== 时间戳格式 ====================

    /**
     * 时间戳文件名格式：yyyyMMdd_HHmmss
     * <p>
     * 用于导出文件名、备份文件名等（LocalDateTime）
     */
    DateTimeFormatter TIMESTAMP_FILENAME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
}
