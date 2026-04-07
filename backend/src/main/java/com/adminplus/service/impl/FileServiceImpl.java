package com.adminplus.service.impl;

import com.adminplus.common.properties.FileStorageProperties;
import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.entity.FileEntity;
import com.adminplus.repository.FileRepository;
import com.adminplus.service.FileService;
import com.adminplus.service.FileStorageService;
import com.adminplus.utils.FileContentValidator;
import com.adminplus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务实现
 *
 * @author AdminPlus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;
    private final FileStorageProperties fileStorageConfig;

    @Override
    @Transactional
    public FileEntity uploadFile(MultipartFile file, String directory) {
        // 验证文件大小
        int maxSizeMB = fileStorageConfig.getLocal().getMaxSize();
        long maxSizeBytes = maxSizeMB * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new BizException(400, "文件大小超过限制，最大允许 " + maxSizeMB + "MB");
        }

        // 验证文件内容类型（支持图片和常见文档类型）
        if (!FileContentValidator.isAllowedFileType(file, file.getContentType())) {
            throw new BizException("不支持的文件类型");
        }

        // 调用存储服务上传文件
        String fileUrl = fileStorageService.uploadFile(file, directory);

        // 创建文件元数据
        FileEntity fileEntity = new FileEntity();
        fileEntity.setOriginalName(file.getOriginalFilename());
        fileEntity.setFileName(extractFileName(file.getOriginalFilename()));
        fileEntity.setFileExt(extractExtension(file.getOriginalFilename()));
        fileEntity.setFileSize(file.getSize());
        fileEntity.setContentType(file.getContentType());
        fileEntity.setFileUrl(fileUrl);
        fileEntity.setStorageType(fileStorageService.getStorageType());
        fileEntity.setDirectory(directory);
        fileEntity.setStatus(0);

        // 保存到数据库
        fileEntity = fileRepository.save(fileEntity);

        log.info("文件上传成功，ID: {}, URL: {}", fileEntity.getId(), fileUrl);
        return fileEntity;
    }

    @Override
    @Transactional
    public boolean deleteFile(String fileId) {
        FileEntity fileEntity = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BizException("文件不存在"));

        // 调用存储服务删除文件
        boolean deleted = fileStorageService.deleteFile(fileEntity.getFileUrl());

        // 逻辑删除文件记录
        fileEntity.setDeleted(true);
        fileRepository.save(fileEntity);

        log.info("文件删除成功，ID: {}", fileId);
        return deleted;
    }

    @Override
    @Transactional
    public boolean deleteFileWithAuth(String fileId) {
        FileEntity fileEntity = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BizException("文件不存在"));

        // 权限验证：文件所有者或管理员可删除
        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        if (!isAdmin && !fileEntity.getCreateUser().equals(currentUserId)) {
            throw new BizException("无权删除此文件");
        }

        // 调用存储服务删除文件
        boolean deleted = fileStorageService.deleteFile(fileEntity.getFileUrl());

        // 逻辑删除文件记录
        fileEntity.setDeleted(true);
        fileRepository.save(fileEntity);

        log.info("文件删除成功，ID: {}, 操作人: {}", fileId, currentUserId);
        return deleted;
    }

    @Override
    @Transactional(readOnly = true)
    public FileEntity getFileById(String fileId) {
        return fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BizException("文件不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public FileEntity getFileWithAuth(String fileId) {
        FileEntity fileEntity = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BizException("文件不存在"));

        // 权限验证：文件所有者或管理员可查看
        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        if (!isAdmin && !fileEntity.getCreateUser().equals(currentUserId)) {
            throw new BizException("无权查看此文件");
        }

        return fileEntity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileEntity> getUserFiles() {
        String userId = SecurityUtils.getCurrentUserId();
        return fileRepository.findByCreateUserAndDeletedFalseOrderByCreateTimeDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileEntity> getFilesByDirectory(String directory) {
        return fileRepository.findByDirectoryAndDeletedFalseOrderByCreateTimeDesc(directory);
    }

    /**
     * 从文件名中提取名称部分
     */
    private String extractFileName(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return originalName;
        }
        return originalName.substring(0, originalName.lastIndexOf("."));
    }

    /**
     * 从文件名中提取扩展名
     */
    private String extractExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf("."));
    }
}