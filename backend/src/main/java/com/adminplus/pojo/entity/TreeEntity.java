package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

/**
 * 树形结构基类
 * <p>
 * 规范说明：
 * 1. 所有树形实体（部门、菜单等）应继承此类
 * 2. 使用 @ToString(exclude = {"parent", "children"}) 防止循环引用导致 StackOverflow
 * 3. 父子关系使用 LAZY 懒加载，避免 N+1 查询
 * 4. ancestors 字段使用 Materialized Path 模式优化查询性能
 * 5. 子类需要添加 @SQLDelete 和 @Where 注解实现逻辑删除
 * </p>
 *
 * @param <E> 子类类型 (用于 self-reference)
 * @author AdminPlus
 * @since 2026-02-24
 */
@Getter
@Setter
@MappedSuperclass
@Where(clause = "deleted = false")
public abstract class TreeEntity<E extends TreeEntity<E>> extends BaseEntity {

    /**
     * 节点名称/标题
     */
    @Column(name = "name", nullable = false, length = 100)
    protected String name;

    /**
     * 父节点
     * <p>
     * 规范1: fetch = LAZY (必须懒加载，防止级联全表查询)
     * 规范2: insertable/updatable = true (由 JPA 维护外键)
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    protected E parent;

    /**
     * 子节点列表
     * <p>
     * 规范1: mappedBy 指向子类的 parent 属性
     * 规范2: CascadeType.ALL (父节点删，子节点全删)
     * 规范3: orphanRemoval = true (从 List 移除即删库)
     * 规范4: 使用 @OrderBy 指定默认排序
     * </p>
     * <p>
     * 注意：子类需要重写此属性并指定正确的泛型类型
     * </p>
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, createTime ASC")
    protected List<E> children = new ArrayList<>();

    /**
     * 排序权重（数字越小越靠前）
     */
    @Column(name = "sort_order")
    protected Integer sortOrder = 0;

    /**
     * 祖级列表 (Materialized Path 优化查询)
     * <p>
     * 格式: "0,1,5," 表示根节点(0) -> 节点1 -> 节点5
     * 用于快速查找所有子孙：WHERE ancestors LIKE '0,1,5,%'
     * </p>
     */
    @Column(name = "ancestors", length = 500)
    protected String ancestors;

    /**
     * 辅助方法：添加子节点
     * <p>
     * 自动维护父子关系和 ancestors 字段
     * </p>
     *
     * @param child 子节点
     */
    public void addChild(E child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
        child.setParent((E) this);

        // 更新子节点的 ancestors
        String parentAncestors = this.getAncestors() != null ? this.getAncestors() : "";
        child.setAncestors(parentAncestors + this.getId() + ",");
    }

    /**
     * 辅助方法：移除子节点
     *
     * @param child 子节点
     */
    public void removeChild(E child) {
        if (this.children != null) {
            this.children.remove(child);
            child.setParent(null);
        }
    }

    /**
     * 检查是否为根节点
     *
     * @return true 如果是根节点
     */
    public boolean isRoot() {
        return this.parent == null ||
               (this.getId() != null && this.getId().equals("0"));
    }

    /**
     * 检查是否为叶子节点
     *
     * @return true 如果是叶子节点
     */
    public boolean isLeaf() {
        return this.children == null || this.children.isEmpty();
    }

    /**
     * 获取层级深度（根节点为 0）
     *
     * @return 层级深度
     */
    public int getLevel() {
        if (this.ancestors == null || this.ancestors.isEmpty()) {
            return 0;
        }
        // ancestors 格式: "0,1,5,"，计算逗号数量即为层级
        return (int) this.ancestors.chars().filter(ch -> ch == ',').count();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        @SuppressWarnings("unchecked")
        TreeEntity<?> that = (TreeEntity<?>) o;

        // 基于ID判断相等性
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        // 返回固定值，防止 Entity 在持久化前后(ID生成前后) hashCode 变化导致 HashSet 丢失对象
        return getClass().hashCode();
    }
}
