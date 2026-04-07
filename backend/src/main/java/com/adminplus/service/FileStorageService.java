package com.adminplus.service;

import com.adminplus.enums.StorageType;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 *
 * @author AdminPlus
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param file     要上传的文件
     * @param directory 子目录(如 avatars, files 等)
     * @return 访问URL
     */
    String uploadFile(MultipartFile file, String directory);

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 获取文件访问URL
     *
     * @param objectName 对象名称/文件路径
     * @return 完整访问URL
     */
    String getAccessUrl(String objectName);

    /**
     * 获取存储类型
     */
    StorageType getStorageType();

    /**
     * 检查存储服务是否可用
     */
    default boolean isAvailable() {
        return true;
    }
}