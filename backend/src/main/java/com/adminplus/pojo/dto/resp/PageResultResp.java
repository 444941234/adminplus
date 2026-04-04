package com.adminplus.pojo.dto.resp;

import org.springframework.data.domain.Page;
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
) {

    /**
     * 从 Spring Data Page 对象创建分页结果
     *
     * @param page Spring Data Page 对象
     * @param <T>  数据类型
     * @return 分页结果视图对象
     */
    public static <T> PageResultResp<T> from(Page<T> page) {
        return new PageResultResp<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,  // Page<number()> is 0-based
                page.getSize()
        );
    }
}
