package com.adminplus.utils;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 文件内容验证工具类
 * 使用 Apache Tika 验证文件实际类型
 *
 * @author AdminPlus
 */
public class FileContentValidator {

    private static final Tika TIKA = new Tika();

    // 允许的图片类型
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    // 允许的文档类型
    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/csv",
            "application/zip",
            "application/x-rar-compressed",
            "application/json",
            // Tika 可能返回的其他 MIME 类型变体
            "application/vnd.fdf", // PDF 相关
            "application/x-pdf"
    );

    // 允许的文件扩展名（用于存储服务验证）
    public static final String[] ALLOWED_EXTENSIONS = {
            // 图片
            ".jpg", ".jpeg", ".png", ".gif", ".webp",
            // 文档
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            // 文本
            ".txt", ".csv", ".json",
            // 压缩
            ".zip", ".rar"
    };

    // 允许的所有类型（图片 + 文档）
    private static final Set<String> ALLOWED_ALL_TYPES;

    static {
        ALLOWED_ALL_TYPES = new HashSet<>();
        ALLOWED_ALL_TYPES.addAll(ALLOWED_IMAGE_TYPES);
        ALLOWED_ALL_TYPES.addAll(ALLOWED_DOCUMENT_TYPES);
    }

    /**
     * 使用 Tika 检测文件的实际 MIME 类型
     *
     * @param file 上传的文件
     * @return 检测到的 MIME 类型，如果文件为空则返回 null
     */
    public static String detectMimeType(MultipartFile file) {
        // 先检查文件是否为空
        if (file == null || file.isEmpty()) {
            return null;
        }
        try (InputStream is = file.getInputStream()) {
            return TIKA.detect(is, file.getOriginalFilename());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 验证文件是否为允许的图片类型
     *
     * @param file        上传的文件
     * @param contentType 声明的 MIME 类型（可选，用于参考）
     * @return 是否为允许的图片类型
     */
    public static boolean isAllowedImageType(MultipartFile file, String contentType) {
        // 使用 Tika 检测实际类型
        String detectedType = detectMimeType(file);
        if (detectedType == null) {
            return false;
        }

        // 检查检测到的类型是否在允许列表中
        return ALLOWED_IMAGE_TYPES.contains(detectedType);
    }

    /**
     * 验证文件是否为允许的类型（图片 + 文档）
     *
     * @param file        上传的文件
     * @param contentType 声明的 MIME 类型（可选，用于参考）
     * @return 是否为允许的类型
     */
    public static boolean isAllowedFileType(MultipartFile file, String contentType) {
        // 使用 Tika 检测实际类型
        String detectedType = detectMimeType(file);
        if (detectedType == null) {
            return false;
        }

        // Tika 对于 Office 文档可能返回 application/zip 或更具体的类型
        // 对于 PDF 可能返回 application/pdf 或 application/vnd.fdf 等
        // 需要检查检测到的类型是否在允许列表中，或者是否是相关变体

        // 检查检测到的类型是否在允许列表中
        if (ALLOWED_ALL_TYPES.contains(detectedType)) {
            return true;
        }

        // 特殊处理：Office 文档（DOCX, XLSX, PPTX）实际上是 ZIP 格式
        // 如果检测到 application/zip，需要检查文件名扩展名
        if ("application/zip".equals(detectedType)) {
            String filename = file.getOriginalFilename();
            if (filename != null) {
                String ext = filename.toLowerCase();
                if (ext.endsWith(".docx") || ext.endsWith(".xlsx") || ext.endsWith(".pptx") || ext.endsWith(".zip")) {
                    return true;
                }
            }
        }

        // 特殊处理：PDF 相关类型
        if (detectedType.startsWith("application/") && detectedType.contains("pdf")) {
            return true;
        }

        // 特殊处理：Office 旧版格式（DOC, XLS, PPT）
        if ("application/msword".equals(detectedType) ||
            "application/vnd.ms-excel".equals(detectedType) ||
            "application/vnd.ms-powerpoint".equals(detectedType)) {
            return true;
        }

        return false;
    }

    /**
     * 验证文件扩展名是否与 MIME 类型匹配
     *
     * @param filename    文件名
     * @param contentType MIME 类型
     * @return 是否匹配
     */
    public static boolean validateExtension(String filename, String contentType) {
        if (filename == null || contentType == null) {
            return false;
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        Map<String, Set<String>> extensionMap = Map.ofEntries(
                Map.entry("image/jpeg", Set.of("jpg", "jpeg")),
                Map.entry("image/png", Set.of("png")),
                Map.entry("image/gif", Set.of("gif")),
                Map.entry("image/webp", Set.of("webp")),
                Map.entry("application/pdf", Set.of("pdf")),
                Map.entry("application/msword", Set.of("doc")),
                Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Set.of("docx")),
                Map.entry("application/vnd.ms-excel", Set.of("xls")),
                Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Set.of("xlsx")),
                Map.entry("application/vnd.ms-powerpoint", Set.of("ppt")),
                Map.entry("application/vnd.openxmlformats-officedocument.presentationml.presentation", Set.of("pptx")),
                Map.entry("text/plain", Set.of("txt")),
                Map.entry("text/csv", Set.of("csv")),
                Map.entry("application/zip", Set.of("zip", "docx", "xlsx", "pptx")),
                Map.entry("application/json", Set.of("json"))
        );

        Set<String> allowedExtensions = extensionMap.get(contentType);
        return allowedExtensions != null && allowedExtensions.contains(extension);
    }
}