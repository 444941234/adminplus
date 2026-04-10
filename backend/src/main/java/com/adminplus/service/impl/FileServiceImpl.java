package com.adminplus.service.impl;

import com.adminplus.common.properties.FileStorageProperties;
import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.response.FileResponse;
import com.adminplus.pojo.entity.FileEntity;
import com.adminplus.repository.FileRepository;
import com.adminplus.service.FileService;
import com.adminplus.service.FileStorageService;
import com.adminplus.utils.FileContentValidator;
import com.adminplus.utils.ServiceAssert;
import com.adminplus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
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
    private final ConversionService conversionService;

    @Override
    @Transactional
    public FileResponse uploadFile(MultipartFile file, String directory) {
        int maxSizeMB = fileStorageConfig.getLocal().getMaxSize();
        long maxSizeBytes = maxSizeMB * 1024L * 1024L;
        ServiceAssert.isTrue(file.getSize() <= maxSizeBytes, 400, "文件大小超过限制，最大允许 " + maxSizeMB + "MB");

        ServiceAssert.isTrue(FileContentValidator.isAllowedFileType(file, file.getContentType()), "不支持的文件类型");

        String fileUrl = fileStorageService.uploadFile(file, directory);

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

        fileEntity = fileRepository.save(fileEntity);

        log.info("文件上传成功，ID: {}, URL: {}", fileEntity.getId(), fileUrl);
        return conversionService.convert(fileEntity, FileResponse.class);
    }

    @Override
    @Transactional
    public boolean deleteFile(String fileId) {
        FileEntity fileEntity = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BizException("文件不存在"));

        boolean deleted = fileStorageService.deleteFile(fileEntity.getFileUrl());

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

        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        ServiceAssert.isTrue(isAdmin || fileEntity.getCreateUser().equals(currentUserId), "无权删除此文件");

        boolean deleted = fileStorageService.deleteFile(fileEntity.getFileUrl());

        fileEntity.setDeleted(true);
        fileRepository.save(fileEntity);

        log.info("文件删除成功，ID: {}, 操作人: {}", fileId, currentUserId);
        return deleted;
    }

    @Override
    @Transactional(readOnly = true)
    public FileResponse getFileById(String fileId) {
        FileEntity entity = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BizException("文件不存在"));
        return conversionService.convert(entity, FileResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public FileResponse getFileWithAuth(String fileId) {
        FileEntity fileEntity = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BizException("文件不存在"));

        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        ServiceAssert.isTrue(isAdmin || fileEntity.getCreateUser().equals(currentUserId), "无权查看此文件");

        return conversionService.convert(fileEntity, FileResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileResponse> getUserFiles() {
        String userId = SecurityUtils.getCurrentUserId();
        return fileRepository.findByCreateUserAndDeletedFalseOrderByCreateTimeDesc(userId)
                .stream()
                .map(e -> conversionService.convert(e, FileResponse.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileResponse> getFilesByDirectory(String directory) {
        return fileRepository.findByDirectoryAndDeletedFalseOrderByCreateTimeDesc(directory)
                .stream()
                .map(e -> conversionService.convert(e, FileResponse.class))
                .toList();
    }

    private String extractFileName(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return originalName;
        }
        return originalName.substring(0, originalName.lastIndexOf("."));
    }

    private String extractExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf("."));
    }
}