package com.adminplus.service;

import com.adminplus.pojo.dto.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 *
 * @author AdminPlus
 */
public interface FileService {

    /**
     * 上传文件并保存元数据
     *
     * @param file      要上传的文件
     * @param directory 子目录
     * @return 文件响应对象
     */
    FileResponse uploadFile(MultipartFile file, String directory);

    /**
     * 删除文件（物理删除）
     *
     * @param fileId 文件ID
     * @return 是否删除成功
     */
    boolean deleteFile(String fileId);

    /**
     * 带权限验证的删除文件
     *
     * @param fileId 文件ID
     * @return 是否删除成功
     */
    boolean deleteFileWithAuth(String fileId);

    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件响应对象
     */
    FileResponse getFileById(String fileId);

    /**
     * 带权限验证的获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件响应对象
     */
    FileResponse getFileWithAuth(String fileId);

    /**
     * 获取当前用户上传的文件列表
     *
     * @return 文件列表
     */
    List<FileResponse> getUserFiles();

    /**
     * 根据目录获取文件列表
     *
     * @param directory 目录
     * @return 文件列表
     */
    List<FileResponse> getFilesByDirectory(String directory);
}