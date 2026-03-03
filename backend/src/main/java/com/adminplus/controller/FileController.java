package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.entity.FileEntity;
import com.adminplus.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件管理控制器
 *
 * @author AdminPlus
 */
@Slf4j
@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、删除、查询")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<FileEntity> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "files") String directory) {
        FileEntity fileEntity = fileService.uploadFile(file, directory);
        return ApiResponse.ok(fileEntity);
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteFile(@PathVariable String fileId) {
        fileService.deleteFile(fileId);
        return ApiResponse.ok();
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "获取文件信息")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<FileEntity> getFile(@PathVariable String fileId) {
        FileEntity fileEntity = fileService.getFileById(fileId);
        return ApiResponse.ok(fileEntity);
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前用户的文件列表")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<FileEntity>> getMyFiles() {
        List<FileEntity> files = fileService.getUserFiles();
        return ApiResponse.ok(files);
    }

    @GetMapping("/directory/{directory}")
    @Operation(summary = "根据目录获取文件列表")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<FileEntity>> getFilesByDirectory(@PathVariable String directory) {
        List<FileEntity> files = fileService.getFilesByDirectory(directory);
        return ApiResponse.ok(files);
    }
}