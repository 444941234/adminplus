package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.req.PasswordChangeReq;
import com.adminplus.pojo.dto.req.ProfileUpdateReq;
import com.adminplus.pojo.dto.req.SettingsUpdateReq;
import com.adminplus.pojo.dto.resp.ActivityItemResp;
import com.adminplus.pojo.dto.resp.ActivityStatsResp;
import com.adminplus.pojo.dto.resp.ProfileResp;
import com.adminplus.pojo.dto.resp.SettingsResp;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.DeptRepository;
import com.adminplus.repository.ProfileRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.FileService;
import com.adminplus.service.ProfileService;
import com.adminplus.service.VirusScanService;
import com.adminplus.utils.MaskingUtils;
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
import java.util.HashMap;
import java.util.Objects;
import java.time.Instant;
import java.time.LocalDateTime;
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
    private final DeptRepository deptRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

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

        // 查询用户角色
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(userId);
        List<String> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
        List<String> roles = roleRepository.findAllById(roleIds).stream()
                .map(RoleEntity::getName)
                .toList();

        // 查询部门名称
        String deptName = null;
        if (user.getDeptId() != null) {
            deptName = deptRepository.findById(user.getDeptId())
                    .map(DeptEntity::getName)
                    .orElse(null);
        }

        return new ProfileResp(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus(),
                deptName,
                roles,
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

        // 重新查询角色和部门
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(userId);
        List<String> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
        List<String> roles = roleRepository.findAllById(roleIds).stream()
                .map(RoleEntity::getName)
                .toList();

        String deptName = null;
        if (user.getDeptId() != null) {
            deptName = deptRepository.findById(user.getDeptId())
                    .map(DeptEntity::getName)
                    .orElse(null);
        }

        return new ProfileResp(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus(),
                deptName,
                roles,
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
        if (!PasswordUtils.isStrongPassword(req.newPassword())) throw new BizException(PasswordUtils.getErrorMessage(PasswordUtils.getPasswordStrengthHint(req.newPassword())));

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

        log.info("用户 {} 修改密码成功", MaskingUtils.maskUsername(user.getUsername()));
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

        // 获取用户设置，如果为 null 则使用默认值
        Map<String, Object> settings = user.getSettings();
        if (settings == null) {
            return new SettingsResp(true, false, true, "zh-CN");
        }

        return new SettingsResp(
                getBooleanSetting(settings, "notifications", true),
                getBooleanSetting(settings, "darkMode", false),
                getBooleanSetting(settings, "emailUpdates", true),
                getStringSetting(settings, "language", "zh-CN")
        );
    }

    @Override
    @Transactional
    public SettingsResp updateSettings(SettingsUpdateReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        // 获取当前设置
        Map<String, Object> currentSettings = user.getSettings();
        if (currentSettings == null) {
            currentSettings = new java.util.HashMap<>();
        }

        // 更新设置
        if (req.notifications() != null) {
            currentSettings.put("notifications", req.notifications());
        }
        if (req.darkMode() != null) {
            currentSettings.put("darkMode", req.darkMode());
        }
        if (req.emailUpdates() != null) {
            currentSettings.put("emailUpdates", req.emailUpdates());
        }
        if (req.language() != null) {
            currentSettings.put("language", req.language());
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
    public ActivityStatsResp getActivityStats() {
        String userId = SecurityUtils.getCurrentUserId();
        UserEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        // 生成模拟数据（实际实现应从审计日志表查询）
        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        // 计算最近登录时间（使用用户最后更新时间作为参考）
        String lastLogin = user.getUpdateTime() != null
                ? formatter.format(user.getUpdateTime())
                : formatter.format(now);

        // 模拟最近登录IP
        String lastLoginIp = "192.168.1.100";

        // 模拟活跃天数
        int daysActive = 45;

        // 模拟总操作次数
        int totalActions = 128;

        // 模拟最近活动记录
        List<ActivityItemResp> recentActivity = List.of(
                new ActivityItemResp(
                        "1",
                        "更新个人信息",
                        formatter.format(now.minusSeconds(3600)),
                        "update"
                ),
                new ActivityItemResp(
                        "2",
                        "创建新用户",
                        formatter.format(now.minusSeconds(86400)),
                        "create"
                ),
                new ActivityItemResp(
                        "3",
                        "系统登录",
                        formatter.format(now.minusSeconds(172800)),
                        "login"
                ),
                new ActivityItemResp(
                        "4",
                        "删除过期数据",
                        formatter.format(now.minusSeconds(259200)),
                        "delete"
                ),
                new ActivityItemResp(
                        "5",
                        "修改系统设置",
                        formatter.format(now.minusSeconds(345600)),
                        "update"
                )
        );

        return new ActivityStatsResp(
                daysActive,
                totalActions,
                lastLogin,
                lastLoginIp,
                recentActivity
        );
    }
}