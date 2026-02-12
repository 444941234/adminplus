package com.adminplus.service.impl;

import com.adminplus.common.properties.AppProperties;
import com.adminplus.service.VirusScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 病毒扫描服务实现（基于 ClamAV）
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
@Slf4j
@Service
public class VirusScanServiceImpl implements VirusScanService {

    private final AppProperties appProperties;

    public VirusScanServiceImpl(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public boolean scanFile(MultipartFile file) {
        boolean scanEnabled = appProperties.getVirus().getScan().isEnabled();

        // 如果病毒扫描被禁用，直接返回 true
        if (!scanEnabled) {
            log.warn("病毒扫描已禁用，跳过文件扫描: {}", file.getOriginalFilename());
            return true;
        }

        // 检查 ClamAV 服务是否可用
        if (!isServiceAvailable()) {
            log.warn("ClamAV 服务不可用，跳过病毒扫描: {}", file.getOriginalFilename());
            // 根据安全策略，可以选择：
            // 1. 返回 true（允许文件上传，可能有安全风险）
            // 2. 返回 false（拒绝文件上传，更安全）
            // 这里选择返回 true 以避免影响用户体验，但记录警告日志
            return true;
        }

        try {
            // 将文件内容读取到字节数组
            byte[] fileBytes = file.getBytes();

            // 使用 ClamAV 扫描文件
            return scanWithClamAV(fileBytes, file.getOriginalFilename());

        } catch (IOException e) {
            log.error("读取文件失败: {}", file.getOriginalFilename(), e);
            // 扫描失败时拒绝文件上传
            return false;
        }
    }

    @Override
    public boolean isServiceAvailable() {
        boolean scanEnabled = appProperties.getVirus().getScan().isEnabled();
        String clamavHost = appProperties.getVirus().getScan().getClamav().getHost();
        int clamavPort = appProperties.getVirus().getScan().getClamav().getPort();
        int scanTimeout = appProperties.getVirus().getScan().getTimeout();

        if (!scanEnabled) {
            return false;
        }

        try (Socket socket = new Socket(clamavHost, clamavPort)) {
            socket.setSoTimeout(scanTimeout);
            return true;
        } catch (Exception e) {
            log.warn("ClamAV 服务不可用: {}:{}", clamavHost, clamavPort);
            return false;
        }
    }

    /**
     * 使用 ClamAV 扫描文件
     *
     * @param fileBytes 文件字节数组
     * @param filename 文件名
     * @return true 如果文件安全，false 如果包含病毒
     */
    private boolean scanWithClamAV(byte[] fileBytes, String filename) {
        String clamavHost = appProperties.getVirus().getScan().getClamav().getHost();
        int clamavPort = appProperties.getVirus().getScan().getClamav().getPort();
        int scanTimeout = appProperties.getVirus().getScan().getTimeout();

        try (Socket socket = new Socket(clamavHost, clamavPort)) {
            socket.setSoTimeout(scanTimeout);

            // 发送扫描指令
            String command = "zINSTREAM\0";
            socket.getOutputStream().write(command.getBytes());

            // 发送文件内容（分块发送，每个块以 4 字节的长度开头）
            int chunkSize = 4096;
            int offset = 0;
            while (offset < fileBytes.length) {
                int length = Math.min(chunkSize, fileBytes.length - offset);
                byte[] lengthBytes = intToBigEndianBytes(length);
                socket.getOutputStream().write(lengthBytes);
                socket.getOutputStream().write(fileBytes, offset, length);
                offset += length;
            }

            // 发送结束标记（长度为 0 的块）
            socket.getOutputStream().write(new byte[4]);
            socket.getOutputStream().flush();

            // 读取响应
            String response = readResponse(socket);

            // 检查响应
            if (response.startsWith("OK")) {
                log.info("文件扫描通过: {}", filename);
                return true;
            } else if (response.startsWith("FOUND")) {
                log.warn("文件包含病毒: {} - {}", filename, response);
                return false;
            } else {
                log.error("文件扫描失败: {} - {}", filename, response);
                return false;
            }

        } catch (Exception e) {
            log.error("ClamAV 扫描失败: {}", filename, e);
            return false;
        }
    }

    /**
     * 读取 ClamAV 响应
     */
    private String readResponse(Socket socket) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
            // ClamAV 响应以 \0 结尾
            if (buffer[bytesRead - 1] == 0) {
                break;
            }
        }
        // 移除结尾的 \0
        String result = baos.toString(StandardCharsets.ISO_8859_1);
        if (result.endsWith("\0")) {
            result = result.substring(0, result.length() - 1);
        }
        return result.trim();
    }

    /**
     * 将整数转换为 Big Endian 字节数组（4 字节）
     */
    private byte[] intToBigEndianBytes(int value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }
}