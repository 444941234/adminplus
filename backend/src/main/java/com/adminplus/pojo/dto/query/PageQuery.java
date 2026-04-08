package com.adminplus.pojo.dto.query;

/**
 * 分页查询基础接口
 * <p>
 * 所有分页查询条件类都需要实现此接口，确保分页参数的一致性
 *
 * @author AdminPlus
 * @since 2026-04-08
 */
public interface PageQuery {

    /**
     * 获取页码
     *
     * @return 页码，从1开始
     */
    Integer getPage();

    /**
     * 获取每页大小
     *
     * @return 每页大小
     */
    Integer getSize();

    /**
     * 计算偏移量
     * <p>
     * 用于数据库分页查询，计算公式: (page - 1) * size
     *
     * @return 偏移量
     */
    default long getOffset() {
        Integer page = getPage();
        Integer size = getSize();
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        return (long) (page - 1) * size;
    }
}
