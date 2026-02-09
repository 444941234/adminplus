package com.adminplus.entity;

import jakarta.persistence.*;

/**
 * 字典项实体
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Entity
@Table(name = "sys_dict_item")
public class DictItemEntity extends BaseEntity {

    /**
     * 字典ID
     */
    @Column(name = "dict_id", nullable = false)
    private String dictId;

    /**
     * 父节点ID
     */
    @Column(name = "parent_id")
    private String parentId;

    /**
     * 字典标签
     */
    @Column(name = "label", nullable = false, length = 100)
    private String label;

    /**
     * 字典值
     */
    @Column(name = "value", nullable = false, length = 100)
    private String value;

    /**
     * 排序
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * 状态（1-正常 0-禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    // Getter and Setter methods
    public String getDictId() {
        return dictId;
    }

    public void setDictId(String dictId) {
        this.dictId = dictId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "DictItemEntity{" +
                "dictId=" + dictId +
                ", parentId=" + parentId +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                ", sortOrder=" + sortOrder +
                ", status=" + status +
                ", remark='" + remark + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DictItemEntity that = (DictItemEntity) o;

        if (dictId != null ? !dictId.equals(that.dictId) : that.dictId != null) return false;
        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (sortOrder != null ? !sortOrder.equals(that.sortOrder) : that.sortOrder != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        return remark != null ? remark.equals(that.remark) : that.remark == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (dictId != null ? dictId.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (sortOrder != null ? sortOrder.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        return result;
    }
}