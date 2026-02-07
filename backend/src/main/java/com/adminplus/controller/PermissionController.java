package com.adminplus.controller;

import com.adminplus.entity.UserEntity;
import com.adminplus.repository.UserRepository;
import com.adminplus.security.CustomUserDetails;
import com.adminplus.service.PermissionService;
import com.adminplus.utils.ApiResponse;
import com.adminplus.vo.PermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping("/sys/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限查询")
public class PermissionController {

    private final PermissionService permissionService;
    private final UserRepository userRepository;

    /**
     * 从 Authentication 中获取用户 ID
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        } else if (principal instanceof Jwt jwt) {
            String username = jwt.getSubject();
            return userRepository.findByUsername(username)
                    .map(UserEntity::getId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        } else {
            throw new IllegalArgumentException("Unsupported principal type: " + principal.getClass());
        }
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前用户的权限列表")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<String>> getCurrentUserPermissions(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<String> permissions = permissionService.getUserPermissions(userId);
        return ApiResponse.ok(permissions);
    }

    @GetMapping("/current/roles")
    @Operation(summary = "获取当前用户的角色列表")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<String>> getCurrentUserRoles(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
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