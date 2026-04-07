package com.adminplus.common.properties;

import com.adminplus.constants.StorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件存储配置属性类
 *
 * @author AdminPlus
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.file-storage")
public class FileStorageProperties {

    /**
     * 存储类型: LOCAL 或 MINIO
     */
    private StorageType type = StorageType.LOCAL;

    /**
     * 本地存储配置
     */
    private LocalConfig local = new LocalConfig();

    /**
     * MinIO 存储配置
     */
    private MinioConfig minio = new MinioConfig();

    @Data
    public static class LocalConfig {
        /**
         * 本地存储根目录
         */
        private String basePath = "uploads";

        /**
         * 访问URL前缀
         */
        private String accessPrefix = "/uploads";

        /**
         * 允许的文件类型
         */
        private List<String> allowedTypes = new ArrayList<>(List.of(
                "image/jpeg",
                "image/png",
                "image/gif",
                "image/webp"
        ));

        /**
         * 最大文件大小 (MB)
         */
        private int maxSize = 10;
    }

    @Data
    public static class MinioConfig {
        /**
         * MinIO服务器地址
         */
        private String endpoint = "http://localhost:9000";

        /**
         * 访问Key
         */
        private String accessKey = "";

        /**
         * 密钥
         */
        private String secretKey = "";

        /**
         * 存储桶名称
         */
        private String bucketName = "adminplus";

        /**
         * 访问域名(用于生成访问URL)
         */
        private String accessDomain = "";

        /**
         * 是否使用SSL
         */
        private boolean secure = false;
    }
}
