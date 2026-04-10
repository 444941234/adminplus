package com.adminplus.constants;

/**
 * 文件上传相关常量
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface FileConstants {

    // ==================== 文件类型限制 ====================

    /**
     * 允许的图片 MIME 类型
     */
    String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    };

    // ==================== 文件大小限制 ====================

    /**
     * 最大头像文件大小（2MB）
     */
    long MAX_AVATAR_SIZE = 2 * 1024 * 1024;

    /**
     * 最大普通文件大小（10MB）
     */
    long MAX_FILE_SIZE = 10 * 1024 * 1024;
}
