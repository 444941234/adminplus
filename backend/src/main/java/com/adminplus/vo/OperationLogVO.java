package com.adminplus.vo;

import java.time.Instant;

/**
 * 操作日志视图对象
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
public record OperationLogVO(
        /**
         * 日志ID
         */
        String id,

        /**
         * 操作人用户名
         */
        String username,

        /**
         * 操作模块
         */
        String module,

        /**
         * 操作类型（1=查询，2=新增，3=修改，4=删除，5=导出，6=导入，7=其他）
         */
        Integer operationType,

        /**
         * 操作描述
         */
        String description,

        /**
         * IP地址
         */
        String ip,

        /**
         * 操作时间
         */
        Instant createTime,

        /**
         * 状态（1=成功，0=失败）
         */
        Integer status,

        /**
         * 执行时长（毫秒）
         */
        Long costTime
) {
}