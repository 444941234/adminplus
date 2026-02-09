package com.adminplus.controller;

import com.adminplus.security.CustomUserDetails;
import com.adminplus.service.PermissionService;
import com.adminplus.utils.ApiResponse;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.vo.PermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限控制器
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@RestController
@RequestMapping("/v1/sys/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限查询")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/current")
    @Operation(summary = "获取当前用户的权限列表")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<String>> getCurrentUserPermissions() {
        String userId = SecurityUtils.getCurrentUserId();
        List<String> permissions = permissionService.getUserPermissions(userId);
        return ApiResponse.ok(permissions);
    }

    @GetMapping("/current/roles")
    @Operation(summary = "获取当前用户的角色列表")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<String>> getCurrentUserRoles() {
        String userId = SecurityUtils.getCurrentUserId();
        List<String> roles = permissionService.getUserRoles(userId);
        return ApiResponse.ok(roles);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有可用权限（用于分配）")
    @PreAuthorize("hasAuthority('permission:list')")
    public ApiResponse<List<PermissionVO>> getAllPermissions() {
        List<PermissionVO> permissions = permissionService.getAllPermissions();
        return ApiResponse.ok(permissions);
    }
}