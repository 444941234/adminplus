package com.adminplus.pojo.dto.response;

import com.adminplus.enums.StorageType;

import java.time.Instant;

/**
 * 文件响应对象
 *
 * @author AdminPlus
 */
public record FileResponse(
        String id,
        String originalName,
        String fileName,
        String fileExt,
        Long fileSize,
        String contentType,
        String fileUrl,
        StorageType storageType,
        String directory,
        Integer status,
        String createUser,
        String updateUser,
        Instant createTime,
        Instant updateTime
) {
}
