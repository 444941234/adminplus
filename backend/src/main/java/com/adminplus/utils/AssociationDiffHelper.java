package com.adminplus.utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 关联关系 Diff 精准更新工具
 * <p>
 * 适用于 user-role、role-menu 等中间表的增量更新场景，
 * 避免无脑全删全插导致的唯一约束冲突和不必要的 SQL。
 *
 * <pre>
 * // 用法示例
 * AssociationDiffHelper.diffUpdate(
 *     userId,
 *     targetRoleIds,
 *     () -> userRoleRepository.findByUserId(userId).stream()
 *             .map(UserRoleEntity::getRoleId).collect(Collectors.toSet()),
 *     toRemove -> userRoleRepository.deleteByUserIdAndRoleIdIn(userId, toRemove),
 *     toAdd -> {
 *         List<UserRoleEntity> list = toAdd.stream().map(roleId -> {
 *             var e = new UserRoleEntity();
 *             e.setUserId(userId);
 *             e.setRoleId(roleId);
 *             return e;
 *         }).toList();
 *         userRoleRepository.saveAll(list);
 *     }
 * );
 * </pre>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public final class AssociationDiffHelper {

    private AssociationDiffHelper() {}

    /**
     * 执行关联关系的 diff 精准更新
     *
     * @param ownerId        主实体 ID（如 userId / roleId），仅用于日志
     * @param targetIds      目标关联 ID 集合，null 视为空集
     * @param currentLoader  加载当前关联 ID 集合
     * @param removeConsumer 消费需要删除的 ID 集合
     * @param addConsumer    消费需要新增的 ID 集合
     * @return DiffResult 包含本次变更统计
     */
    public static DiffResult diffUpdate(
            String ownerId,
            Collection<String> targetIds,
            Function<String, Set<String>> currentLoader,
            BiConsumer<String, Set<String>> removeConsumer,
            BiConsumer<String, Set<String>> addConsumer
    ) {
        Set<String> target = (targetIds == null) ? Set.of() : new HashSet<>(targetIds);
        Set<String> current = currentLoader.apply(ownerId);

        // 需要删除的：当前有但目标没有
        Set<String> toRemove = new HashSet<>(current);
        toRemove.removeAll(target);

        // 需要新增的：目标有但当前没有
        Set<String> toAdd = new HashSet<>(target);
        toAdd.removeAll(current);

        if (!toRemove.isEmpty()) {
            removeConsumer.accept(ownerId, toRemove);
        }
        if (!toAdd.isEmpty()) {
            addConsumer.accept(ownerId, toAdd);
        }

        return new DiffResult(toAdd.size(), toRemove.size());
    }

    /**
     * Diff 结果
     */
    public record DiffResult(int added, int removed) {
        public boolean hasChanges() {
            return added > 0 || removed > 0;
        }
    }
}
