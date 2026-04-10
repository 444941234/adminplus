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
     *
     * @return 当前登录用户的个人信息
     */
    ProfileResponse getCurrentUserProfile();

    /**
     * 更新当前用户信息
     *
     * @param request 用户信息更新请求
     * @return 更新后的用户信息
     */
    ProfileResponse updateCurrentProfile(ProfileUpdateRequest request);

    /**
     * 修改密码
     *
     * @param request 密码修改请求（包含旧密码和新密码）
     * @throws BizException 当旧密码验证失败时抛出
     */
    void changePassword(PasswordChangeRequest request);

    /**
     * 上传头像
     *
     * @param file 头像图片文件
     * @return 头像URL地址
     * @throws BizException 当文件格式不支持或大小超限时抛出
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 获取用户设置
     *
     * @return 用户个人设置信息
     */
    SettingsResponse getSettings();

    /**
     * 更新用户设置
     *
     * @param request 用户设置更新请求
     * @return 更新后的设置信息
     */
    SettingsResponse updateSettings(SettingsUpdateRequest request);

    /**
     * 获取用户活动统计
     *
     * @return 用户活动统计数据（登录次数、操作次数等）
     */
    ActivityStatsResponse getActivityStats();
}