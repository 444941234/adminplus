package com.adminplus.utils;

import com.adminplus.common.exception.BizException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
    public static <T> T findByIdOrThrow(Function<String, Optional<T>> finder,
                                         String id, String message) {
        return finder.apply(id)
                .orElseThrow(() -> new BizException(message));
    }

    /**
     * 根据 ID 查找实体，不存在则抛出 BizException（支持消息模板）
     *
     * @param finder          查找函数（如 repository::findById）
     * @param id              实体 ID
     * @param messageTemplate 消息模板，使用 {} 作为占位符
     * @param args            模板参数
     * @param <T>             实体类型
     * @return 实体对象
     */
    public static <T> T findByIdOrThrow(Function<String, Optional<T>> finder,
                                         String id, String messageTemplate, Object... args) {
        return finder.apply(id)
                .orElseThrow(() -> new BizException(formatMessage(messageTemplate, args)));
    }

    /**
     * 查找实体（软删除场景），不存在则抛出 BizException
     *
     * @param finder     查找函数（如 repository::findById）
     * @param id         实体 ID
     * @param entityName 实体名称（用于错误消息）
     * @param <T>        实体类型
     * @return 实体对象
     */
    public static <T> T findActiveById(Function<String, Optional<T>> finder,
                                        String id, String entityName) {
        return finder.apply(id)
                .orElseThrow(() -> new BizException(404, entityName + "不存在"));
    }

    /**
     * 批量查找实体，缺失时抛出 BizException
     *
     * @param finder     批量查找函数（如 repository::findAllById）
     * @param ids        实体 ID 列表
     * @param entityName 实体名称（用于错误消息）
     * @param <T>        实体类型
     * @return 实体列表
     */
    public static <T> List<T> findAllByIdsOrThrow(Function<List<String>, List<T>> finder,
                                                   List<String> ids, String entityName) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<T> results = finder.apply(ids);
        if (results.size() != ids.size()) {
            throw new BizException(404, "部分" + entityName + "不存在");
        }
        return results;
    }

    /**
     * 查找实体并返回 Optional（不抛异常）
     *
     * @param finder 查找函数（如 repository::findById）
     * @param id     实体 ID
     * @param <T>    实体类型
     * @return Optional 包装的实体对象
     */
    public static <T> Optional<T> findById(Function<String, Optional<T>> finder, String id) {
        return finder.apply(id);
    }

    /**
     * 格式化消息模板
     *
     * @param template 消息模板，使用 {} 作为占位符
     * @param args     模板参数
     * @return 格式化后的消息
     */
    private static String formatMessage(String template, Object... args) {
        if (args == null || args.length == 0) {
            return template;
        }
        String result = template;
        for (Object arg : args) {
            result = result.replaceFirst("\\{}", String.valueOf(arg));
        }
        return result;
    }
}
