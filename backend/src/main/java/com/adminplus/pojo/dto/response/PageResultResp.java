package com.adminplus.pojo.dto.response;

import org.springframework.data.domain.Page;
import java.util.List;
import java.util.function.Function;

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
     * 从 Spring Data Page 对象创建分页结果（不转换数据）
     *
     * @param page Spring Data Page 对象
     * @param <T>  数据类型
     * @return 分页结果视图对象
     */
    public static <T> PageResultResp<T> from(Page<T> page) {
        return new PageResultResp<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,  // Page#getNumber() is 0-based
                page.getSize()
        );
    }

    /**
     * 从 Spring Data Page 对象创建分页结果（带数据转换）
     *
     * @param page   Spring Data Page 对象
     * @param mapper 数据转换函数 (Entity -> VO)
     * @param <E>    实体类型
     * @param <V>    视图类型
     * @return 分页结果视图对象
     */
    public static <E, V> PageResultResp<V> from(Page<E> page, Function<E, V> mapper) {
        return new PageResultResp<>(
                page.getContent().stream().map(mapper).toList(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize()
        );
    }
}
