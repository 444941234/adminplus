package com.adminplus.common.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置
 *
 * @author AdminPlus
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.file-storage.type", havingValue = "MINIO")
public class MinioClientConfig {

    @Bean
    public MinioClient minioClient(FileStorageConfig fileStorageConfig) {
        FileStorageConfig.MinioConfig minioConfig = fileStorageConfig.getMinio();

        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();

        // 检查并创建存储桶（如果不存在）
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .build()
                );
                log.info("MinIO 存储桶 {} 创建成功", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("MinIO 存储桶初始化失败", e);
            throw new RuntimeException("MinIO 存储桶初始化失败: " + e.getMessage(), e);
        }

        log.info("MinIO 客户端初始化成功: {}", minioConfig.getEndpoint());
        return minioClient;
    }
}