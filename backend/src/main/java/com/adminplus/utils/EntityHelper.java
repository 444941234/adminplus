package com.adminplus.utils;

import com.adminplus.common.exception.BizException;

import java.util.function.Supplier;

/**
 * 实体查询辅助工具类
 * <p>
 * 消除重复的 findById().orElseThrow() 模式
 * </p>
 *
 * <pre>
 * // 用法示例
 * var user = EntityHelper.findByIdOrThrow(userRepository, id, "用户不存在");
 * var role = EntityHelper.findByIdOrThrow(roleRepository, id, () -> new BizException("角色不存在"));
 * </pre>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public final class EntityHelper {

    private EntityHelper() {}

    /**
     * 根据 ID 查找实体，不存在则抛出 BizException
     *
     * @param finder  查找函数（如 repository::findById）
     * @param id      实体 ID
     * @param message 不存在时的错误消息
     * @param <T>     实体类型
     * @return 实体对象
     */
    public static <T> T findByIdOrThrow(java.util.function.Function<String, java.util.Optional<T>> finder,
                                         String id, String message) {
        return finder.apply(id)
                .orElseThrow(() -> new BizException(message));
    }
}
