package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.request.PasswordChangeRequest;
import com.adminplus.pojo.dto.request.ProfileUpdateRequest;
import com.adminplus.pojo.dto.request.SettingsUpdateRequest;
import com.adminplus.pojo.dto.response.ActivityStatsResponse;
import com.adminplus.pojo.dto.response.AvatarUploadResponse;
import com.adminplus.pojo.dto.response.ProfileResponse;
import com.adminplus.pojo.dto.response.SettingsResponse;
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
    public ApiResponse<ProfileResponse> getProfile() {
        ProfileResponse profile = profileService.getCurrentUserProfile();
        return ApiResponse.ok(profile);
    }

    @PutMapping
    @Operation(summary = "更新当前用户信息")
    @OperationLog(module = "个人中心", operationType = 3, description = "更新个人信息")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProfileResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        ProfileResponse response = profileService.updateCurrentProfile(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/password")
    @Operation(summary = "修改密码")
    @OperationLog(module = "个人中心", operationType = 3, description = "修改密码")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        profileService.changePassword(request);
        return ApiResponse.ok();
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像")
    @OperationLog(module = "个人中心", operationType = 3, description = "上传头像")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AvatarUploadResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = profileService.uploadAvatar(file);
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(null, null, null, avatarUrl);
        profileService.updateCurrentProfile(updateRequest);
        return ApiResponse.ok(new AvatarUploadResponse(avatarUrl));
    }

    @GetMapping("/settings")
    @Operation(summary = "获取用户设置")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SettingsResponse> getSettings() {
        SettingsResponse settings = profileService.getSettings();
        return ApiResponse.ok(settings);
    }

    @PutMapping("/settings")
    @Operation(summary = "更新用户设置")
    @OperationLog(module = "个人中心", operationType = 3, description = "更新用户设置")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SettingsResponse> updateSettings(@Valid @RequestBody SettingsUpdateRequest req) {
        SettingsResponse settings = profileService.updateSettings(req);
        return ApiResponse.ok(settings);
    }

    @GetMapping("/activity")
    @Operation(summary = "获取用户活动统计")
    @OperationLog(module = "个人中心", operationType = 1, description = "查看活动统计")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ActivityStatsResponse> getActivityStats() {
        ActivityStatsResponse response = profileService.getActivityStats();
        return ApiResponse.ok(response);
    }
}