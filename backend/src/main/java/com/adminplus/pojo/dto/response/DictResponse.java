package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 字典视图对象
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record DictResponse(
        String id,
        String dictType,
        String dictName,
        Integer status,
        String remark,
        Instant createTime,
        Instant updateTime
) {}