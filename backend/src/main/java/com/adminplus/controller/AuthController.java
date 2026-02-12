package com.adminplus.controller;

import com.adminplus.dto.UserLoginReq;
import com.adminplus.security.AppUserDetails;
import com.adminplus.service.AuthService;
import com.adminplus.utils.ApiResponse;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.vo.LoginResp;
import com.adminplus.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    public ApiResponse<LoginResp> login(@Valid @RequestBody UserLoginReq req) {
        log.info("用户登录: {}", req.username());
        LoginResp resp = authService.login(req);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取当前用户信息")
    public ApiResponse<UserVO> getCurrentUser(Authentication authentication) {
        // 使用 SecurityUtils 获取当前用户名，支持 JWT 和 Session 认证
        String username = SecurityUtils.getCurrentUsername();
        UserVO userVO = authService.getCurrentUser(username);
        return ApiResponse.ok(userVO);
    }

    @GetMapping("/permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取当前用户的权限列表")
    public ApiResponse<List<String>> getCurrentUserPermissions(Authentication authentication) {
        // 使用 SecurityUtils 获取当前用户名，支持 JWT 和 Session 认证
        String username = SecurityUtils.getCurrentUsername();
        List<String> permissions = authService.getCurrentUserPermissions(username);
        return ApiResponse.ok(permissions);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Access Token")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> refreshAccessToken(@RequestBody RefreshTokenReq req) {
        String newToken = authService.refreshAccessToken(req.refreshToken());
        return ApiResponse.ok(newToken);
    }

    /**
     * 刷新 Token 请求
     */
    public record RefreshTokenReq(String refreshToken) {
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