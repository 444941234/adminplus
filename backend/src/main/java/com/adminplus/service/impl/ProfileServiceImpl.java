package com.adminplus.service.impl;

import com.adminplus.dto.PasswordChangeReq;
import com.adminplus.dto.ProfileUpdateReq;
import com.adminplus.dto.SettingsUpdateReq;
import com.adminplus.entity.UserEntity;
import com.adminplus.exception.BizException;
import com.adminplus.repository.ProfileRepository;
import com.adminplus.security.CustomUserDetails;
import com.adminplus.service.ProfileService;
import com.adminplus.service.VirusScanService;
import com.adminplus.utils.PasswordUtils;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.XssUtils;
import com.adminplus.vo.ProfileVO;
import com.adminplus.vo.SettingsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 个人中心服务实现
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final VirusScanService virusScanService;

    // 允许的图片格式
    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    };

    // 允许的文件扩展名
    private static final String[] ALLOWED_EXTENSIONS = {
            ".jpg",
            ".jpeg",
            ".png",
            ".gif",
            ".webp"
    };

    // 最大文件大小 2MB
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Override
    @Transactional(readOnly = true)
    public ProfileVO getCurrentUserProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        return new ProfileVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    @Override
    @Transactional
    public ProfileVO updateCurrentProfile(ProfileUpdateReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        // 权限检查：确保用户只能修改自己的信息
        // 这里已经通过 SecurityUtils.getCurrentUserId() 获取当前登录用户ID
        // 只有当前登录用户可以修改自己的个人资料

        if (req.nickname() != null) {
            user.setNickname(XssUtils.escape(req.nickname()));
        }
        if (req.email() != null) {
            user.setEmail(XssUtils.escape(req.email()));
        }
        if (req.phone() != null) {
            user.setPhone(XssUtils.escape(req.phone()));
        }
        if (req.avatar() != null) {
            user.setAvatar(req.avatar());
        }

        user = profileRepository.save(user);

        return new ProfileVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeReq req) {
        // 验证新密码和确认密码是否一致
        if (!Objects.equals(req.newPassword(), req.confirmPassword())) {
            throw new BizException("新密码和确认密码不一致");
        }

        // 验证新密码不能与原密码���同
        if (Objects.equals(req.oldPassword(), req.newPassword())) {
            throw new BizException("新密码不能与原密码相同");
        }

        // 验证新密码强度
        if (!PasswordUtils.isStrongPassword(req.newPassword())) {
            throw new BizException(PasswordUtils.getPasswordStrengthHint(req.newPassword()));
        }

        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        // 验证原密码
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new BizException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        profileRepository.save(user);

        log.info("用户 {} 修改密码成功", maskUsername(user.getUsername()));
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file) {
        // 验证文件
        validateImageFile(file);

        // 病毒扫描
        if (!virusScanService.scanFile(file)) {
            throw new BizException("文件包含病毒，上传被拒绝");
        }

        try {
            // 获取并验证原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new BizException("文件名不能为空");
            }

            // 验证文件名不包含非法字符
            String sanitizedFilename = XssUtils.sanitizeFilename(originalFilename);
            if (!originalFilename.equals(sanitizedFilename)) {
                throw new BizException("文件名包含非法字符");
            }

            // 验证文件扩展名
            if (!XssUtils.isAllowedExtension(originalFilename, ALLOWED_EXTENSIONS)) {
                throw new BizException("不支持的文件格式");
            }

            // 生成唯一文件名（使用 UUID 避免文件名冲突）
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // 按日期创建目录（使用相对路径，防止路径遍历）
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

            // 验证路径安全性
            if (!XssUtils.isSafePath(datePath)) {
                throw new BizException("路径包含非法字符");
            }

            // 使用固定的上传根目录，防止路径遍历
            Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
            Path uploadPath = uploadRoot.resolve("avatars").resolve(datePath).normalize();

            // 确保路径在上传根目录内
            if (!uploadPath.startsWith(uploadRoot)) {
                throw new BizException("非法的文件路径");
            }

            // 创建目录（如果不存在）
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            Path filePath = uploadPath.resolve(filename).normalize();

            // 再次验证文件路径
            if (!filePath.startsWith(uploadPath)) {
                throw new BizException("非法的文件路径");
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回访问URL（相对路径）
            String fileUrl = "/uploads/avatars/" + datePath + "/" + filename;
            log.info("头像上传成功: {}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("头像上传失败", e);
            throw new BizException("头像上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SettingsVO getSettings() {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        // 如果 settings 为 null，返回空 Map 而不是 null
        Map<String, Object> settings = user.getSettings();
        if (settings == null) {
            settings = Map.of();
        }

        return new SettingsVO(settings);
    }

    @Override
    @Transactional
    public SettingsVO updateSettings(SettingsUpdateReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        // 合并设置
        Map<String, Object> currentSettings = user.getSettings();
        if (currentSettings == null) {
            currentSettings = req.settings();
        } else {
            currentSettings.putAll(req.settings());
        }

        user.setSettings(currentSettings);
        user = profileRepository.save(user);

        return new SettingsVO(user.getSettings());
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的文件");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        boolean validType = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                validType = true;
                break;
            }
        }
        if (!validType) {
            throw new BizException("只支持上传 JPG、PNG、GIF、WebP 格式的图片");
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BizException("图片大小不能超过 2MB");
        }
    }

    /**
     * 隐藏用户名敏感信息
     */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "***";
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1);
    }
}