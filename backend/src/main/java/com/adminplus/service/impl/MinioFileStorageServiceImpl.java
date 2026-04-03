package com.adminplus.service.impl;

import com.adminplus.common.config.FileStorageConfig;
import com.adminplus.constants.StorageType;
import com.adminplus.service.FileStorageService;
import com.adminplus.utils.FileContentValidator;
import com.adminplus.utils.XssUtils;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * MinIO 文件存储服务实现
 *
 * @author AdminPlus
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.file-storage.type", havingValue = "MINIO")
public class MinioFileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final FileStorageConfig config;

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        try {
            // 验证目录参数安全性（防止路径遍历）
            if (!XssUtils.isSafePath(directory)) {
                throw new IllegalArgumentException("目录参数包含非法字符");
            }

            // 获取并验证原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IllegalArgumentException("文件名不能为空");
            }

            // 验证文件名不包含非法字符
            String sanitizedFilename = XssUtils.sanitizeFilename(originalFilename);
            if (!originalFilename.equals(sanitizedFilename)) {
                throw new IllegalArgumentException("文件名包含非法字符");
            }

            // 验证文件扩展名
            if (!XssUtils.isAllowedExtension(originalFilename, FileContentValidator.ALLOWED_EXTENSIONS)) {
                throw new IllegalArgumentException("不支持的文件格式");
            }

            // 生成唯一文件名（使用 UUID 避免文件名冲突）
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID() + extension;

            // 按日期创建目录
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

            // 构建对象名称
            String objectName = directory + "/" + datePath + "/" + filename;

            // 上传到 MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(config.getMinio().getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(Objects.requireNonNull(file.getContentType()))
                            .build()
            );

            // 返回访问URL
            String fileUrl = getAccessUrl(objectName);
            log.info("文件上传成功(MinIO): {}", fileUrl);

            return fileUrl;

        } catch (Exception e) {
            log.error("文件上传失败(MinIO)", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL提取objectName
            String objectName = extractObjectName(fileUrl);
            if (objectName == null) {
                log.warn("无法解析文件URL: {}", fileUrl);
                return false;
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(config.getMinio().getBucketName())
                            .object(objectName)
                            .build()
            );

            log.info("文件删除成功(MinIO): {}", fileUrl);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败(MinIO): {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public String getAccessUrl(String objectName) {
        String accessDomain = config.getMinio().getAccessDomain();
        if (accessDomain != null && !accessDomain.isEmpty()) {
            return accessDomain + "/" + objectName;
        }
        // 如果没有配置访问域名，使用默认格式
        String scheme = config.getMinio().isSecure() ? "https://" : "http://";
        return scheme + config.getMinio().getEndpoint().replace("http://", "").replace("https://", "")
                + "/" + config.getMinio().getBucketName() + "/" + objectName;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.MINIO;
    }

    @Override
    public boolean isAvailable() {
        try {
            return minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder()
                            .bucket(config.getMinio().getBucketName())
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO 服务不可用", e);
            return false;
        }
    }

    /**
     * 从文件URL中提取对象名称
     */
    private String extractObjectName(String fileUrl) {
        String bucketName = config.getMinio().getBucketName();
        String accessDomain = config.getMinio().getAccessDomain();
        String endpoint = config.getMinio().getEndpoint();

        // 如果配置了访问域名
        if (accessDomain != null && !accessDomain.isEmpty() && fileUrl.contains(accessDomain)) {
            return fileUrl.substring(fileUrl.indexOf(accessDomain) + accessDomain.length() + 1);
        }

        // 如果URL包含bucket名称
        if (fileUrl.contains(bucketName)) {
            return fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);
        }

        // 否则尝试去除endpoint前缀
        if (fileUrl.contains(endpoint)) {
            String afterEndpoint = fileUrl.substring(fileUrl.indexOf(endpoint) + endpoint.length());
            // 去除开头的 /
            while (afterEndpoint.startsWith("/")) {
                afterEndpoint = afterEndpoint.substring(1);
            }
            // 去除bucket名称（如果还有）
            if (afterEndpoint.contains("/")) {
                return afterEndpoint.substring(afterEndpoint.indexOf("/") + 1);
            }
            return afterEndpoint;
        }

        // 直接返回原URL（假设它就是objectName）
        return fileUrl;
    }
}