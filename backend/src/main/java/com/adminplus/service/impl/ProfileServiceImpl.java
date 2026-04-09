package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.utils.DictUtils;
import com.adminplus.utils.EntityHelper;
import com.adminplus.pojo.dto.request.PasswordChangeRequest;
import com.adminplus.pojo.dto.request.ProfileUpdateRequest;
import com.adminplus.pojo.dto.request.SettingsUpdateRequest;
import com.adminplus.pojo.dto.response.ActivityItemResponse;
import com.adminplus.pojo.dto.response.ActivityStatsResponse;
import com.adminplus.pojo.dto.response.ProfileResponse;
import com.adminplus.pojo.dto.response.SettingsResponse;
import com.adminplus.pojo.entity.LogEntity;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.repository.LogRepository;
import com.adminplus.repository.ProfileRepository;
import com.adminplus.service.FileService;
import com.adminplus.service.ProfileService;
import com.adminplus.service.VirusScanService;
import com.adminplus.utils.LogMaskingUtils;
import com.adminplus.utils.PasswordUtils;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.XssUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
    private final LogRepository logRepository;
    private final DictUtils dictUtils;
    private final ConversionService conversionService;

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
    public ProfileResponse getCurrentUserProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = EntityHelper.findByIdOrThrow(profileRepository::findById, userId, "用户不存在");

        return conversionService.convert(user, ProfileResponse.class);
    }

    @Override
    @Transactional
    public ProfileResponse updateCurrentProfile(ProfileUpdateRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = EntityHelper.findByIdOrThrow(profileRepository::findById, userId, "用户不存在");

        // 权限检查：确保用户只能修改自己的信息
        // 这里已经通过 SecurityUtils.getCurrentUserId() 获取当前登录用户ID
        // 只有当前登录用户可以修改自己的个人资料

        if (request.nickname() != null) {
            user.setNickname(XssUtils.escapeOrNull(request.nickname()));
        }
        if (request.email() != null) {
            user.setEmail(XssUtils.escapeOrNull(request.email()));
        }
        if (request.phone() != null) {
            user.setPhone(XssUtils.escapeOrNull(request.phone()));
        }
        if (request.avatar() != null) {
            user.setAvatar(request.avatar());
        }

        user = profileRepository.save(user);

        return conversionService.convert(user, ProfileResponse.class);
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        // 验证新密码和确认密码是否一致
        if (!Objects.equals(request.newPassword(), request.confirmPassword())) {
            throw new BizException("新密码和确认密码不一致");
        }

        // 验证新密码不能与原密码相同
        if (Objects.equals(request.oldPassword(), request.newPassword())) {
            throw new BizException("新密码不能与原密码相同");
        }

        // 验证新密码强度
        if (!PasswordUtils.isStrongPassword(request.newPassword())) throw new BizException(PasswordUtils.getErrorMessage(PasswordUtils.getPasswordStrengthHint(request.newPassword())));

        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = EntityHelper.findByIdOrThrow(profileRepository::findById, userId, "用户不存在");

        // 验证原密码
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BizException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        profileRepository.save(user);

        log.info("用户 {} 修改密码成功", LogMaskingUtils.maskUsername(user.getUsername()));
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
        String avatarUrl = fileService.uploadFile(file, "avatars").fileUrl();
        log.info("头像上传成功: {}", avatarUrl);

        return avatarUrl;
    }

    @Override
    @Transactional(readOnly = true)
    public SettingsResponse getSettings() {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = EntityHelper.findByIdOrThrow(profileRepository::findById, userId, "用户不存在");

        // 获取用户设置，如果为 null 则使用默认值
        Map<String, Object> settings = user.getSettings();
        if (settings == null) {
            return new SettingsResponse(true, false, true, "zh-CN");
        }

        return new SettingsResponse(
                getBooleanSetting(settings, "notifications", true),
                getBooleanSetting(settings, "darkMode", false),
                getBooleanSetting(settings, "emailUpdates", true),
                getStringSetting(settings, "language", "zh-CN")
        );
    }

    @Override
    @Transactional
    public SettingsResponse updateSettings(SettingsUpdateRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = EntityHelper.findByIdOrThrow(profileRepository::findById, userId, "用户不存在");

        // 获取当前设置
        Map<String, Object> currentSettings = user.getSettings();
        if (currentSettings == null) {
            currentSettings = new java.util.HashMap<>();
        }

        // 更新设置
        if (request.notifications() != null) {
            currentSettings.put("notifications", request.notifications());
        }
        if (request.darkMode() != null) {
            currentSettings.put("darkMode", request.darkMode());
        }
        if (request.emailUpdates() != null) {
            currentSettings.put("emailUpdates", request.emailUpdates());
        }
        if (request.language() != null) {
            currentSettings.put("language", request.language());
        }

        user.setSettings(currentSettings);
        user = profileRepository.save(user);

        return getSettings();
    }

    /**
     * 从设置 Map 中获取布尔值
     */
    private boolean getBooleanSetting(Map<String, Object> settings, String key, boolean defaultValue) {
        Object value = settings.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    /**
     * 从设置 Map 中获取字符串值
     */
    private String getStringSetting(Map<String, Object> settings, String key, String defaultValue) {
        Object value = settings.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
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

    @Override
    @Transactional(readOnly = true)
    public ActivityStatsResponse getActivityStats() {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = EntityHelper.findByIdOrThrow(profileRepository::findById, userId, "用户不存在");

        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        // 查询最近登录记录
        LogEntity lastLoginLog = logRepository.findLastLoginByUserId(userId);
        String lastLogin = lastLoginLog != null
                ? formatter.format(lastLoginLog.getCreateTime())
                : (user.getUpdateTime() != null ? formatter.format(user.getUpdateTime()) : formatter.format(now));
        String lastLoginIp = lastLoginLog != null && lastLoginLog.getIp() != null
                ? lastLoginLog.getIp()
                : "N/A";

        // 统计活跃天数和操作次数
        long daysActive = logRepository.countDistinctDaysByUserId(userId);
        long totalActions = logRepository.countByUserIdAndDeletedFalse(userId);

        // 查询最近活动记录
        List<LogEntity> recentLogs = logRepository.findTop5ByUserIdAndDeletedFalseOrderByCreateTimeDesc(userId);
        List<ActivityItemResponse> recentActivity = recentLogs.stream()
                .map(log -> new ActivityItemResponse(
                        log.getId(),
                        log.getDescription() != null ? log.getDescription() : log.getModule(),
                        formatter.format(log.getCreateTime()),
                        mapOperationType(log.getOperationType())
                ))
                .collect(Collectors.toList());

        // 如果没有活动记录，返回空列表而不是模拟数据
        if (recentActivity.isEmpty()) {
            recentActivity = List.of();
        }

        return new ActivityStatsResponse(
                (int) daysActive,
                (int) totalActions,
                lastLogin,
                lastLoginIp,
                recentActivity
        );
    }

    /**
     * 将操作类型转换为活动类型字符串
     */
    private String mapOperationType(Integer operationType) {
        if (operationType == null) return "other";
        // 使用字典获取操作类型描述
        String desc = dictUtils.getDictLabel("operation_type", String.valueOf(operationType));
        // 转换为英文活动类型（前端需要）
        return switch (desc) {
            case "新增" -> "create";
            case "修改" -> "update";
            case "删除" -> "delete";
            case "查询" -> "query";
            case "导出" -> "export";
            case "导入" -> "import";
            default -> "other";
        };
    }
}