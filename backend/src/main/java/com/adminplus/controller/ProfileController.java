package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.PasswordChangeReq;
import com.adminplus.pojo.dto.req.ProfileUpdateReq;
import com.adminplus.pojo.dto.req.SettingsUpdateReq;
import com.adminplus.pojo.dto.resp.AvatarUploadResp;
import com.adminplus.pojo.dto.resp.ProfileResp;
import com.adminplus.pojo.dto.resp.SettingsResp;
import com.adminplus.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人中心控制器
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@RestController
@RequestMapping("/v1/profile")
@RequiredArgsConstructor
@Tag(name = "个人中心", description = "个人资料、设置、密码管理")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "获取当前用户信息")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProfileResp> getProfile() {
        ProfileResp profile = profileService.getCurrentUserProfile();
        return ApiResponse.ok(profile);
    }

    @PutMapping
    @Operation(summary = "更新当前用户信息")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProfileResp> updateProfile(@Valid @RequestBody ProfileUpdateReq req) {
        ProfileResp profile = profileService.updateCurrentProfile(req);
        return ApiResponse.ok(profile);
    }

    @PostMapping("/password")
    @Operation(summary = "修改密码")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeReq req) {
        profileService.changePassword(req);
        return ApiResponse.ok();
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AvatarUploadResp> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = profileService.uploadAvatar(file);

        // 同时更新用户头像
        ProfileUpdateReq updateReq = new ProfileUpdateReq(null, null, null, avatarUrl);
        profileService.updateCurrentProfile(updateReq);

        return ApiResponse.ok(new AvatarUploadResp(avatarUrl));
    }

    @GetMapping("/settings")
    @Operation(summary = "获取用户设置")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SettingsResp> getSettings() {
        SettingsResp settings = profileService.getSettings();
        return ApiResponse.ok(settings);
    }

    @PutMapping("/settings")
    @Operation(summary = "更新用户设置")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SettingsResp> updateSettings(@Valid @RequestBody SettingsUpdateReq req) {
        SettingsResp settings = profileService.updateSettings(req);
        return ApiResponse.ok(settings);
    }
}