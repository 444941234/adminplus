package com.adminplus.utils;

import java.util.List;
import java.util.function.Function;

/**
 * 层级结构工具类
 * 处理树形结构中 ancestors 字段的公共逻辑
 */
public final class HierarchyHelper {

    private HierarchyHelper() {}

    /**
     * 检查 targetId 是否是 parentId 的子孙（通过 ancestors 路径判断）
     */
    public static boolean isDescendant(String parentId, String targetId,
                                        Function<String, java.util.Optional<String>> ancestorsLoader) {
        if (targetId == null || targetId.equals("0")) {
            return false;
        }
        return ancestorsLoader.apply(targetId)
                .map(ancestors -> ancestors.contains(parentId + ","))
                .orElse(false);
    }

    /**
     * 级联更新子孙节点的 ancestors 前缀
     */
    public static <T> void cascadeUpdateAncestors(
            String oldAncestors, String newAncestors, String nodeId,
            Function<String, List<T>> descendantsLoader,
            Function<T, String> ancestorsGetter,
            java.util.function.BiConsumer<T, String> ancestorsSetter,
            java.util.function.Consumer<List<T>> saver) {
        String descendantsPrefix = oldAncestors + nodeId + ",";
        List<T> descendants = descendantsLoader.apply(descendantsPrefix);
        String newPrefix = newAncestors + nodeId + ",";

        for (T descendant : descendants) {
            String currentAncestors = ancestorsGetter.apply(descendant);
            if (currentAncestors != null && currentAncestors.startsWith(descendantsPrefix)) {
                String updatedAncestors = newPrefix + currentAncestors.substring(descendantsPrefix.length());
                ancestorsSetter.accept(descendant, updatedAncestors);
            }
        }
        saver.accept(descendants);
    }
}
