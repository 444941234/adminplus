package com.adminplus.service;

import com.adminplus.dto.PasswordChangeReq;
import com.adminplus.dto.ProfileUpdateReq;
import com.adminplus.dto.SettingsUpdateReq;
import com.adminplus.vo.ProfileVO;
import com.adminplus.vo.SettingsVO;
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
    ProfileVO getCurrentUserProfile();

    /**
     * 更新当前用户信息
     */
    ProfileVO updateCurrentProfile(ProfileUpdateReq req);

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
    SettingsVO getSettings();

    /**
     * 更新用户设置
     */
    SettingsVO updateSettings(SettingsUpdateReq req);
}