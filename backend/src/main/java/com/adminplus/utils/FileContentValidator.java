package com.adminplus.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件内容验证工具类
 * 通过 Magic Number 验证文件实际类型
 *
 * @author AdminPlus
 */
public class FileContentValidator {

    // Magic Number 映射
    private static final Map<String, byte[]> MAGIC_NUMBERS = new HashMap<>();

    static {
        // JPEG
        MAGIC_NUMBERS.put("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        // PNG
        MAGIC_NUMBERS.put("image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        // GIF
        MAGIC_NUMBERS.put("image/gif", new byte[]{'G', 'I', 'F', '8'});
        // WebP
        MAGIC_NUMBERS.put("image/webp", new byte[]{'R', 'I', 'F', 'F'});
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
            // 未知的 MIME 类型，跳过验证
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
        if (contentType == null || !contentType.startsWith("image/")) {
            return false;
        }

        // 检查是否为允许的图片类型
        String[] allowedTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"};
        boolean isAllowedType = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isAllowedType = true;
                break;
            }
        }

        if (!isAllowedType) {
            return false;
        }

        // 验证文件内容
        return validateContentType(file, contentType);
    }
}