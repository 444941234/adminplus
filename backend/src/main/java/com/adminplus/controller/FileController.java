package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.exception.BizException;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.response.FileResponse;
import com.adminplus.service.FileService;
import com.adminplus.utils.XssUtils;
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
    @OperationLog(module = "文件管理", operationType = 2, description = "上传文件 {#file.originalFilename}")
    @PreAuthorize("hasAuthority('file:upload')")
    public ApiResponse<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "files") String directory) {
        // 验证目录名称安全性，防止路径遍历攻击
        if (!XssUtils.isSafePath(directory)) {
            throw new BizException(400, "目录名称不合法，不能包含特殊字符或路径遍历符");
        }
        FileResponse fileResponse = fileService.uploadFile(file, directory);
        return ApiResponse.ok(fileResponse);
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件")
    @OperationLog(module = "文件管理", operationType = 4, description = "删除文件 {#fileId}")
    @PreAuthorize("hasAuthority('file:delete')")
    public ApiResponse<Void> deleteFile(@PathVariable String fileId) {
        fileService.deleteFileWithAuth(fileId);
        return ApiResponse.ok();
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "获取文件信息")
    @PreAuthorize("hasAuthority('file:list')")
    public ApiResponse<FileResponse> getFile(@PathVariable String fileId) {
        FileResponse fileResponse = fileService.getFileWithAuth(fileId);
        return ApiResponse.ok(fileResponse);
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前用户的文件列表")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<FileResponse>> getMyFiles() {
        List<FileResponse> files = fileService.getUserFiles();
        return ApiResponse.ok(files);
    }

    @GetMapping("/directory/{directory}")
    @Operation(summary = "根据目录获取文件列表")
    @PreAuthorize("hasAuthority('file:list')")
    public ApiResponse<List<FileResponse>> getFilesByDirectory(@PathVariable String directory) {
        List<FileResponse> files = fileService.getFilesByDirectory(directory);
        return ApiResponse.ok(files);
    }
}