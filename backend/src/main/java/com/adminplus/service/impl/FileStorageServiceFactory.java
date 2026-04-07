package com.adminplus.service.impl;

import com.adminplus.common.properties.FileStorageProperties;
import com.adminplus.enums.StorageType;
import com.adminplus.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储服务工厂
 * 根据配置返回对应的存储服务实现
 *
 * @author AdminPlus
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FileStorageServiceFactory {

    private final FileStorageProperties fileStorageConfig;

    /**
     * 获取当前激活的文件存储服务
     * 如果配置了 MINIO 且可用，返回 MinIO 存储服务
     * 否则返回本地存储服务
     */
    @Bean
    @ConditionalOnMissingBean
    public FileStorageService fileStorageService(
            LocalFileStorageServiceImpl localFileStorageService,
            MinioFileStorageServiceImpl minioFileStorageService) {

        StorageType storageType = fileStorageConfig.getType();

        if (storageType == StorageType.MINIO) {
            try {
                if (minioFileStorageService.isAvailable()) {
                    log.info("使用 MinIO 文件存储服务");
                    return minioFileStorageService;
                } else {
                    log.warn("MinIO 服务不可用，回退到本地存储");
                }
            } catch (Exception e) {
                log.warn("MinIO 服务初始化失败，回退到本地存储: {}", e.getMessage());
            }
        }

        log.info("使用本地文件存储服务");
        return localFileStorageService;
    }
}