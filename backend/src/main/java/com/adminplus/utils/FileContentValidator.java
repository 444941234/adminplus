package com.adminplus.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 文件内容验证工具类
 * 通过 Magic Number 验证文件实际类型
 *
 * @author AdminPlus
 */
public class FileContentValidator {

    // Magic Number 映射
    private static final Map<String, byte[]> MAGIC_NUMBERS = new HashMap<>();

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
            "application/json"
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
        // JPEG
        MAGIC_NUMBERS.put("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        // PNG
        MAGIC_NUMBERS.put("image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        // GIF
        MAGIC_NUMBERS.put("image/gif", new byte[]{'G', 'I', 'F', '8'});
        // WebP
        MAGIC_NUMBERS.put("image/webp", new byte[]{'R', 'I', 'F', 'F'});
        // PDF
        MAGIC_NUMBERS.put("application/pdf", new byte[]{'%', 'P', 'D', 'F'});
        // ZIP (also used for DOCX, XLSX, PPTX)
        MAGIC_NUMBERS.put("application/zip", new byte[]{0x50, 0x4B});
        MAGIC_NUMBERS.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", new byte[]{0x50, 0x4B});
        MAGIC_NUMBERS.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{0x50, 0x4B});
        MAGIC_NUMBERS.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", new byte[]{0x50, 0x4B});

        ALLOWED_ALL_TYPES = new java.util.HashSet<>();
        ALLOWED_ALL_TYPES.addAll(ALLOWED_IMAGE_TYPES);
        ALLOWED_ALL_TYPES.addAll(ALLOWED_DOCUMENT_TYPES);
    }

    // 读取的字节数
    private static final int MAGIC_NUMBER_LENGTH = 4;

    /**
     * 验证文件内容类型是否与声明的 MIME 类型匹配
     *
     * @param file        上传的文件
     * @param contentType 声明的 MIME 类型
     * @return 是否匹配
     */
    public static boolean validateContentType(MultipartFile file, String contentType) {
        if (file == null || contentType == null) {
            return false;
        }

        byte[] magicBytes = MAGIC_NUMBERS.get(contentType);
        if (magicBytes == null) {
            // 未知的 MIME 类型，跳过 magic number 验证，只检查扩展名
            return true;
        }

        try (InputStream is = file.getInputStream()) {
            byte[] fileBytes = new byte[MAGIC_NUMBER_LENGTH];
            int read = is.read(fileBytes);
            if (read < magicBytes.length) {
                return false;
            }

            // 检查 magic number
            for (int i = 0; i < magicBytes.length; i++) {
                if (fileBytes[i] != magicBytes[i]) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 验证文件是否为允许的图片类型
     *
     * @param file        上传的文件
     * @param contentType 声明的 MIME 类型
     * @return 是否为允许的图片类型
     */
    public static boolean isAllowedImageType(MultipartFile file, String contentType) {
        // 先检查 MIME 类型
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return false;
        }

        // 验证文件内容
        return validateContentType(file, contentType);
    }

    /**
     * 验证文件是否为允许的类型（图片 + 文档）
     *
     * @param file        上传的文件
     * @param contentType 声明的 MIME 类型
     * @return 是否为允许的类型
     */
    public static boolean isAllowedFileType(MultipartFile file, String contentType) {
        if (contentType == null) {
            return false;
        }

        // 检查是否为允许的类型
        if (!ALLOWED_ALL_TYPES.contains(contentType)) {
            return false;
        }

        // 验证文件内容（magic number）
        return validateContentType(file, contentType);
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
                Map.entry("application/zip", Set.of("zip")),
                Map.entry("application/json", Set.of("json"))
        );

        Set<String> allowedExtensions = extensionMap.get(contentType);
        return allowedExtensions != null && allowedExtensions.contains(extension);
    }
}