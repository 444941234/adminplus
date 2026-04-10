package com.adminplus.controller;

import com.adminplus.common.annotation.LoginLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.request.RefreshTokenRequest;
import com.adminplus.pojo.dto.request.UserLoginRequest;
import com.adminplus.pojo.dto.response.LoginResponse;
import com.adminplus.pojo.dto.response.UserResponse;
import com.adminplus.service.AuthService;
import com.adminplus.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、登出、获取当前用户信息")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @LoginLog(type = 1, description = "用户登录")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取当前用户信息")
    public ApiResponse<UserResponse> getCurrentUser() {
        String userId = SecurityUtils.getCurrentUserId();
        UserResponse userResponse = authService.getUserById(userId);
        return ApiResponse.ok(userResponse);
    }

    @GetMapping("/permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取当前用户的权限列表")
    public ApiResponse<List<String>> getCurrentUserPermissions() {
        String userId = SecurityUtils.getCurrentUserId();
        List<String> permissions = authService.getUserPermissions(userId);
        return ApiResponse.ok(permissions);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    @PreAuthorize("isAuthenticated()")
    @LoginLog(type = 2, description = "用户登出")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Access Token")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        String newToken = authService.refreshAccessToken(request.refreshToken());
        return ApiResponse.ok(newToken);
    }
}