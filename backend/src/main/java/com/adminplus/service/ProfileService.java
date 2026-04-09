package com.adminplus.service;

import com.adminplus.pojo.dto.request.PasswordChangeRequest;
import com.adminplus.pojo.dto.request.ProfileUpdateRequest;
import com.adminplus.pojo.dto.request.SettingsUpdateRequest;
import com.adminplus.pojo.dto.response.ActivityStatsResponse;
import com.adminplus.pojo.dto.response.ProfileResponse;
import com.adminplus.pojo.dto.response.SettingsResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人中心服务接口
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public interface ProfileService {

    /**
     * 获取当前用户信息
     */
    ProfileResponse getCurrentUserProfile();

    /**
     * 更新当前用户信息
     */
    ProfileResponse updateCurrentProfile(ProfileUpdateRequest request);

    /**
     * 修改密码
     */
    void changePassword(PasswordChangeRequest request);

    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 获取用户设置
     */
    SettingsResponse getSettings();

    /**
     * 更新用户设置
     */
    SettingsResponse updateSettings(SettingsUpdateRequest request);

    /**
     * 获取用户活动统计
     */
    ActivityStatsResponse getActivityStats();
}