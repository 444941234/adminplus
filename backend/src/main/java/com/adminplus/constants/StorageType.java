package com.adminplus.constants;

/**
 * 存储类型枚举
 *
 * @author AdminPlus
 */
public enum StorageType {
    LOCAL("本地存储"),
    MINIO("MinIO对象存储");

    private final String description;

    StorageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}