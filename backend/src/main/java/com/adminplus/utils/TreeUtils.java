package com.adminplus.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 树形结构构建工具类
 * <p>
 * 提供 O(n) 复杂度的树形结构构建方法，避免递归查库导致的性能问题
 * </p>
 *
 * @author AdminPlus
 * @since 2026-02-24
 */
public class TreeUtils {

    /**
     * 树节点接口（可变版本）
     *
     * @param <T> 节点类型
     */
    public interface TreeNode<T extends TreeNode<T>> {
        /**
         * 获取节点ID
         */
        String getId();

        /**
         * 获取父节点ID
         */
        String getParentId();

        /**
         * 获取子节点列表
         */
        List<T> getChildren();

        /**
         * 设置子节点列表
         */
        void setChildren(List<T> children);
    }

    /**
     * 只读树节点接口（用于不可变 record 类型）
     *
     * @param <T> 节点类型
     */
    public interface ReadonlyTreeNode<T> {
        /**
         * 获取节点ID
         * 默认返回 null，子类可重写
         */
        default String getId() {
            return null;
        }

        /**
         * 获取父节点ID
         * 默认返回 null，子类可重写
         */
        default String getParentId() {
            return null;
        }

        /**
         * 获取子节点列表
         * 默认返回 null，子类可重写
         */
        default List<?> getChildren() {
            return null;
        }
    }

    /**
     * 内存中构建树形结构
     * <p>
     * 时间复杂度: O(n)
     * 空间复杂度: O(n)
     * </p>
     *
     * @param list      扁平的节点列表
     * @param rootPid   根节点的父ID（通常为 "0" 或 null）
     * @param <T>       节点类型，必须实现 TreeNode 接口
     * @return 树形结构列表
     */
    public static <T extends TreeNode<T>> List<T> buildTree(List<T> list, String rootPid) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> tree = new ArrayList<>();

        // 1. 将 List 转为 Map (Key: ID, Value: Node)
        Map<String, T> map = list.stream()
                .collect(Collectors.toMap(TreeNode::getId, node -> node, (a, b) -> a));

        // 2. 遍历组装父子关系
        for (T node : list) {
            String pid = node.getParentId();

            // 判断是否为根节点
            if (isRoot(pid, rootPid)) {
                tree.add(node);
            } else {
                // 找父节点
                T parent = map.get(pid);
                if (parent != null) {
                    List<T> children = parent.getChildren();
                    if (children == null) {
                        children = new ArrayList<>();
                        parent.setChildren(children);
                    }
                    children.add(node);
                }
                // 如果找不到父节点，将孤立节点也加入树根
                else if (!pid.equals(node.getId())) {
                    tree.add(node);
                }
            }
        }

        return tree;
    }

    /**
     * 构建树形结构（根节点父ID默认为 "0"）
     *
     * @param list 扁平的节点列表
     * @param <T>  节点类型
     * @return 树形结构列表
     */
    public static <T extends TreeNode<T>> List<T> buildTree(List<T> list) {
        return buildTree(list, "0");
    }

    /**
     * 为 record 类型构建树形结构的函数式接口
     * <p>
     * 由于 record 不可变，需要提供创建新实例的函数
     * </p>
     *
     * @param <T> 节点类型
     */
    @FunctionalInterface
    public interface RecordNodeFactory<T> {
        /**
         * 创建带有子节点列表的新节点实例
         *
         * @param original 原始节点
         * @param children 子节点列表
         * @return 包含子节点的新节点实例
         */
        T createWithChildren(T original, List<T> children);
    }

    /**
     * 为 record/不可变对象构建树形结构
     * <p>
     * 时间复杂度: O(n)
     * 空间复杂度: O(n)
     * </p>
     *
     * @param list      扁平的节点列表
     * @param factory   创建包含子节点的新实例的工厂函数
     * @param rootPid   根节点的父ID（通常为 "0" 或 null）
     * @param <T>       节点类型，必须实现 ReadonlyTreeNode 接口
     * @return 树形结构列表
     */
    public static <T> List<T> buildTreeForRecord(
            List<T> list,
            RecordNodeFactory<T> factory,
            String rootPid) {

        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 构建 ID -> 节点 的映射
        Map<String, T> idToNode = list.stream()
                .collect(Collectors.toMap(
                        node -> ((ReadonlyTreeNode<T>) node).getId(),
                        node -> node,
                        (a, b) -> a
                ));

        // 2. 构建 ParentID -> Children 的映射
        Map<String, List<String>> parentIdToChildren = new java.util.HashMap<>();
        for (T node : list) {
            ReadonlyTreeNode<T> treeNode = (ReadonlyTreeNode<T>) node;
            String parentId = treeNode.getParentId();
            if (parentId != null && !parentId.equals("0") && !parentId.equals(treeNode.getId())) {
                parentIdToChildren.computeIfAbsent(parentId, k -> new ArrayList<>())
                        .add(treeNode.getId());
            }
        }

        // 3. 递归构建树
        List<T> result = new ArrayList<>();
        for (T node : list) {
            ReadonlyTreeNode<T> treeNode = (ReadonlyTreeNode<T>) node;
            String parentId = treeNode.getParentId();

            if (isRoot(parentId, rootPid)) {
                result.add(buildNodeRecursive(node, idToNode, parentIdToChildren, factory));
            }
        }

        return result;
    }

    /**
     * 为 record/不可变对象构建树形结构（根节点父ID默认为 "0"）
     *
     * @param list    扁平的节点列表
     * @param factory 创建包含子节点的新实例的工厂函数
     * @param <T>     节点类型
     * @return 树形结构列表
     */
    public static <T> List<T> buildTreeForRecord(List<T> list, RecordNodeFactory<T> factory) {
        return buildTreeForRecord(list, factory, "0");
    }

    /**
     * 递归构建节点
     */
    private static <T> T buildNodeRecursive(
            T node,
            Map<String, T> idToNode,
            Map<String, List<String>> parentIdToChildren,
            RecordNodeFactory<T> factory) {

        ReadonlyTreeNode<T> treeNode = (ReadonlyTreeNode<T>) node;
        String nodeId = treeNode.getId();

        // 获取子节点ID列表
        List<String> childIds = parentIdToChildren.getOrDefault(nodeId, new ArrayList<>());

        // 递归构建子节点
        List<T> children = new ArrayList<>();
        for (String childId : childIds) {
            T childNode = idToNode.get(childId);
            if (childNode != null) {
                children.add(buildNodeRecursive(childNode, idToNode, parentIdToChildren, factory));
            }
        }

        // 使用工厂函数创建包含子节点的新实例
        return factory.createWithChildren(node, children);
    }

    /**
     * 判断是否为根节点
     *
     * @param pid    当前节点的父ID
     * @param rootPid 根节点的父ID
     * @return true 如果是根节点
     */
    private static boolean isRoot(String pid, String rootPid) {
        if (pid == null) {
            return true;
        }
        if (rootPid == null) {
            return true;
        }
        return pid.equals(rootPid) || pid.isEmpty();
    }

    /**
     * 扁平化树形结构（将树转为列表）
     * <p>
     * 深度优先遍历
     * </p>
     *
     * @param tree 树形结构列表
     * @param <T>  节点类型
     * @return 扁平的节点列表
     */
    public static <T extends TreeNode<T>> List<T> flatten(List<T> tree) {
        List<T> result = new ArrayList<>();
        for (T node : tree) {
            flatten(node, result);
        }
        return result;
    }

    /**
     * 递归扁平化
     */
    private static <T extends TreeNode<T>> void flatten(T node, List<T> result) {
        if (node == null) {
            return;
        }
        result.add(node);
        List<T> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            for (T child : children) {
                flatten(child, result);
            }
        }
    }

    /**
     * 获取指定节点的所有子孙节点ID
     *
     * @param tree    树形结构列表
     * @param nodeId  节点ID
     * @param <T>     节点类型
     * @return 所有子孙节点ID列表（包含自身）
     */
    public static <T extends TreeNode<T>> List<String> getDescendantIds(List<T> tree, String nodeId) {
        List<String> ids = new ArrayList<>();
        List<T> flatList = flatten(tree);

        for (T node : flatList) {
            if (nodeId.equals(node.getId())) {
                // 找到目标节点，收集其所有子孙
                ids.add(node.getId());
                collectDescendantIds(node, ids);
                break;
            }
        }

        return ids;
    }

    /**
     * 递归收集子孙节点ID
     */
    private static <T extends TreeNode<T>> void collectDescendantIds(T node, List<String> ids) {
        List<T> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            for (T child : children) {
                ids.add(child.getId());
                collectDescendantIds(child, ids);
            }
        }
    }

    /**
     * 过滤树节点（保留符合条件的节点及其祖先链）
     * <p>
     * 例如：只保留有权限访问的节点，同时保留这些节点的父节点（用于显示完整路径）
     * </p>
     *
     * @param tree      树形结构列表
     * @param predicate 过滤条件
     * @param <T>       节点类型
     * @return 过滤后的树形结构
     */
    public static <T extends TreeNode<T>> List<T> filterTree(List<T> tree, java.util.function.Predicate<T> predicate) {
        if (tree == null || tree.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> result = new ArrayList<>();
        List<String> keepIds = new ArrayList<>();

        // 先扁平化，收集所有需要保留的节点ID
        List<T> flatList = flatten(tree);
        for (T node : flatList) {
            if (predicate.test(node)) {
                collectAncestorIds(node, tree, keepIds);
            }
        }

        // 重建树（只保留 keepIds 中的节点）
        for (T node : tree) {
            T filtered = filterNode(node, keepIds);
            if (filtered != null) {
                result.add(filtered);
            }
        }

        return result;
    }

    /**
     * 收集节点及其所有祖先的ID
     */
    private static <T extends TreeNode<T>> void collectAncestorIds(T node, List<T> tree, List<String> ids) {
        if (node == null || ids.contains(node.getId())) {
            return;
        }
        ids.add(node.getId());

        // 向上查找父节点
        String parentId = node.getParentId();
        if (parentId != null && !parentId.equals("0")) {
            for (T root : tree) {
                T parent = findParent(root, parentId);
                if (parent != null) {
                    collectAncestorIds(parent, tree, ids);
                    break;
                }
            }
        }
    }

    /**
     * 在树中查找指定ID的节点
     */
    private static <T extends TreeNode<T>> T findParent(T node, String parentId) {
        if (node == null) {
            return null;
        }
        if (parentId.equals(node.getId())) {
            return node;
        }
        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                T found = findParent(child, parentId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * 递归过滤节点
     */
    private static <T extends TreeNode<T>> T filterNode(T node, List<String> keepIds) {
        if (node == null || !keepIds.contains(node.getId())) {
            return null;
        }

        // 复制当前节点
        T filteredNode = node;

        // 递归处理子节点
        List<T> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            List<T> filteredChildren = new ArrayList<>();
            for (T child : children) {
                T filteredChild = filterNode(child, keepIds);
                if (filteredChild != null) {
                    filteredChildren.add(filteredChild);
                }
            }
            filteredNode.setChildren(filteredChildren);
        }

        return filteredNode;
    }
}
