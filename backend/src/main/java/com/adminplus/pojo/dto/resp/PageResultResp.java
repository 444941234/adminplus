package com.adminplus.pojo.dto.resp;

import java.util.List;

/**
 * 分页结果视图对象
 *
 * @param <T> 数据类型
 * @author AdminPlus
 * @since 2026-02-07
 */
public record PageResultResp<T>(
        /**
         * 数据列表
         */
        List<T> records,

        /**
         * 总记录数
         */
        Long total,

        /**
         * 当前页码
         */
        Integer page,

        /**
         * 每页大小
         */
        Integer size
) {}