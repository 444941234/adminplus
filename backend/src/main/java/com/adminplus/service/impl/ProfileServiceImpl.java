package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.req.PasswordChangeReq;
import com.adminplus.pojo.dto.req.ProfileUpdateReq;
import com.adminplus.pojo.dto.req.SettingsUpdateReq;
import com.adminplus.pojo.dto.resp.ProfileResp;
import com.adminplus.pojo.dto.resp.SettingsResp;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.repository.ProfileRepository;
import com.adminplus.service.FileService;
import com.adminplus.service.ProfileService;
import com.adminplus.service.VirusScanService;
import com.adminplus.utils.PasswordUtils;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.XssUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;

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
    private final FileService fileService;

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
    public ProfileResp getCurrentUserProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        return new ProfileResp(
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
    public ProfileResp updateCurrentProfile(ProfileUpdateReq req) {
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

        return new ProfileResp(
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

        // 验证新密码强度
        if (!PasswordUtils.isStrongPassword(req.newPassword())) {
            int errorCode = PasswordUtils.getPasswordStrengthHint(req.newPassword());
            throw new BizException(PasswordUtils.getErrorMessage(errorCode));
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

        // 使用统一的文件服务上传（包含数据库记录）
        String avatarUrl = fileService.uploadFile(file, "avatars").getFileUrl();
        log.info("头像上传成功: {}", avatarUrl);

        return avatarUrl;
    }

    @Override
    @Transactional(readOnly = true)
    public SettingsResp getSettings() {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        // 如果 settings 为 null，返回空 Map 而不是 null
        Map<String, Object> settings = user.getSettings();
        if (settings == null) {
            settings = Map.of();
        }

        return new SettingsResp(settings);
    }

    @Override
    @Transactional
    public SettingsResp updateSettings(SettingsUpdateReq req) {
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

        return new SettingsResp(user.getSettings());
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