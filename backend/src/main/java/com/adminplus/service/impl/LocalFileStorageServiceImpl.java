package com.adminplus.service.impl;

import com.adminplus.common.properties.FileStorageProperties;
import com.adminplus.enums.StorageType;
import com.adminplus.service.FileStorageService;
import com.adminplus.utils.FileContentValidator;
import com.adminplus.utils.XssUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件存储服务实现
 *
 * @author AdminPlus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final FileStorageProperties config;

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

            // 按日期创建目录（使用相对路径，防止路径遍历）
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

            // 验证路径安全性
            if (!XssUtils.isSafePath(datePath)) {
                throw new IllegalArgumentException("路径包含非法字符");
            }

            // 使用固定的上传根目录，防止路径遍历
            Path uploadRoot = Paths.get(config.getLocal().getBasePath()).toAbsolutePath().normalize();
            Path uploadPath = uploadRoot.resolve(directory).resolve(datePath).normalize();

            // 确保路径在上传根目录内
            if (!uploadPath.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("非法的文件路径");
            }

            // 创建目录（如果不存在）
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            Path filePath = uploadPath.resolve(filename).normalize();

            // 再次验证文件路径
            if (!filePath.startsWith(uploadPath)) {
                throw new IllegalArgumentException("非法的文件路径");
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回访问URL（相对路径）
            String objectName = directory + "/" + datePath + "/" + filename;
            String fileUrl = config.getLocal().getAccessPrefix() + "/" + objectName;
            log.info("文件上传成功(本地): {}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("文件上传失败(本地)", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL提取相对路径
            String prefix = config.getLocal().getAccessPrefix();
            String relativePath = fileUrl;
            if (fileUrl.startsWith(prefix)) {
                relativePath = fileUrl.substring(prefix.length());
            }
            // 移除开头的 /
            while (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }

            Path filePath = Paths.get(config.getLocal().getBasePath(), relativePath).normalize();

            // 确保路径在上传根目录内
            Path uploadRoot = Paths.get(config.getLocal().getBasePath()).toAbsolutePath().normalize();
            if (!filePath.startsWith(uploadRoot)) {
                log.warn("非法的文件路径: {}", fileUrl);
                return false;
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功(本地): {}", fileUrl);
                return true;
            }
            log.warn("文件不存在: {}", fileUrl);
            return false;
        } catch (IOException e) {
            log.error("文件删除失败(本地): {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public String getAccessUrl(String objectName) {
        return config.getLocal().getAccessPrefix() + "/" + objectName;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.LOCAL;
    }
}