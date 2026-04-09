package com.adminplus.utils;

import com.adminplus.pojo.dto.response.PageResultResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.function.Function;

/**
 * 分页工具类
 * <p>
 * 简化分页查询代码，统一处理1-based页码到0-based的转换
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public final class PageUtils {

    private PageUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 创建分页请求（无排序）
     * <p>
     * 将1-based页码转换为Spring Data的0-based页码
     * </p>
     *
     * @param page 页码（1-based）
     * @param size 每页大小
     * @return Pageable对象
     */
    public static Pageable toPageable(Integer page, Integer size) {
        return PageRequest.of(page - 1, size);
    }

    /**
     * 创建分页请求（带排序）
     *
     * @param page 页码（1-based）
     * @param size 每页大小
     * @param sort 排序对象
     * @return Pageable对象
     */
    public static Pageable toPageable(Integer page, Integer size, Sort sort) {
        return PageRequest.of(page - 1, size, sort);
    }

    /**
     * 创建分页请求（按字段降序排序）
     *
     * @param page       页码（1-based）
     * @param size       每页大小
     * @param sortField  排序字段
     * @return Pageable对象
     */
    public static Pageable toPageableDesc(Integer page, Integer size, String sortField) {
        return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sortField));
    }

    /**
     * 创建分页请求（按字段升序排序）
     *
     * @param page       页码（1-based）
     * @param size       每页大小
     * @param sortField  排序字段
     * @return Pageable对象
     */
    public static Pageable toPageableAsc(Integer page, Integer size, String sortField) {
        return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, sortField));
    }

    /**
     * 创建分页请求（按多个字段降序排序）
     *
     * @param page       页码（1-based）
     * @param size       每页大小
     * @param sortFields 排序字段数组
     * @return Pageable对象
     */
    public static Pageable toPageableDesc(Integer page, Integer size, String... sortFields) {
        return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sortFields));
    }

    /**
     * 创建分页请求（按多个字段升序排序）
     *
     * @param page       页码（1-based）
     * @param size       每页大小
     * @param sortFields 排序字段数组
     * @return Pageable对象
     */
    public static Pageable toPageableAsc(Integer page, Integer size, String... sortFields) {
        return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, sortFields));
    }

    /**
     * 将Page对象转换为分页响应结果（不转换数据）
     *
     * @param page Spring Data Page对象
     * @param <T>  数据类型
     * @return PageResultResp对象
     */
    public static <T> PageResultResponse<T> toResp(Page<T> page) {
        return new PageResultResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize()
        );
    }

    /**
     * 将Page对象转换为分页响应结果（带数据转换）
     *
     * @param page   Spring Data Page对象
     * @param mapper 数据转换函数
     * @param <E>    实体类型
     * @param <V>    视图类型
     * @return PageResultResp对象
     */
    public static <E, V> PageResultResponse<V> toResp(Page<E> page, Function<E, V> mapper) {
        return new PageResultResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize()
        );
    }
}