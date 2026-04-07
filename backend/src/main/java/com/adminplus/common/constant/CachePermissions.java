package com.adminplus.common.constant;

/**
 * 缓存管理权限
 * <p>
 * 缓存清除和列表查看权限
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface CachePermissions {

    /**
     * 缓存 - 清除权限
     */
    String CLEAR = "cache:clear";

    /**
     * 缓存 - 列表权限
     */
    String LIST = "cache:list";
}
