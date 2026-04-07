package com.adminplus.constants;

/**
 * 操作类型枚举
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public enum OperationType {
    QUERY(1, "查询"),
    CREATE(2, "新增"),
    UPDATE(3, "修改"),
    DELETE(4, "删除"),
    EXPORT(5, "导出"),
    IMPORT(6, "导入"),
    OTHER(7, "其他");

    private final int code;
    private final String description;

    OperationType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OperationType fromCode(int code) {
        for (OperationType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown OperationType code: " + code);
    }
}
