package com.adminplus.service.impl;

import com.adminplus.dto.PasswordChangeReq;
import com.adminplus.dto.ProfileUpdateReq;
import com.adminplus.dto.SettingsUpdateReq;
import com.adminplus.entity.UserEntity;
import com.adminplus.exception.BizException;
import com.adminplus.repository.ProfileRepository;
import com.adminplus.security.CustomUserDetails;
import com.adminplus.service.ProfileService;
import com.adminplus.utils.SecurityUtils;
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

    // 允许的图片格式
    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    };

    // 最大文件大小 2MB
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Override
    @Transactional(readOnly = true)
    public ProfileVO getCurrentUserProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
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
        Long userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        if (req.nickname() != null) {
            user.setNickname(req.nickname());
        }
        if (req.email() != null) {
            user.setEmail(req.email());
        }
        if (req.phone() != null) {
            user.setPhone(req.phone());
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

        // 验证新密码不能与原密码相同
        if (Objects.equals(req.oldPassword(), req.newPassword())) {
            throw new BizException("新密码不能与原密码相同");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        // 验证原密码
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new BizException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        profileRepository.save(user);

        log.info("用户 {} 修改密码成功", user.getUsername());
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file) {
        // 验证文件
        validateImageFile(file);

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID() + extension;

            // 按日期创建目录
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadPath = Paths.get("uploads", "avatars", datePath);

            // 创建目录（如果不存在）
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回访问URL
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
        Long userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        return new SettingsVO(user.getSettings());
    }

    @Override
    @Transactional
    public SettingsVO updateSettings(SettingsUpdateReq req) {
        Long userId = SecurityUtils.getCurrentUserId();
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
}