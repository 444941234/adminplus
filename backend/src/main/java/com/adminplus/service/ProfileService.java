package com.adminplus.service;

import com.adminplus.pojo.dto.req.PasswordChangeReq;
import com.adminplus.pojo.dto.req.ProfileUpdateReq;
import com.adminplus.pojo.dto.req.SettingsUpdateReq;
import com.adminplus.pojo.dto.resp.ProfileResp;
import com.adminplus.pojo.dto.resp.SettingsResp;
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
    ProfileResp getCurrentUserProfile();

    /**
     * 更新当前用户信息
     */
    ProfileResp updateCurrentProfile(ProfileUpdateReq req);

    /**
     * 修改密码
     */
    void changePassword(PasswordChangeReq req);

    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 获取用户设置
     */
    SettingsResp getSettings();

    /**
     * 更新用户设置
     */
    SettingsResp updateSettings(SettingsUpdateReq req);
}