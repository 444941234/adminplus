package com.adminplus.controller;

import com.adminplus.dto.UserCreateReq;
import com.adminplus.dto.UserUpdateReq;
import com.adminplus.service.UserService;
import com.adminplus.utils.ApiResponse;
import com.adminplus.vo.UserVO;
import com.adminplus.vo.PageResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@RestController
@RequestMapping("/v1/sys/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户增删改查")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "分页查询用户列表")
    @PreAuthorize("hasAuthority('user:query')")
    public ApiResponse<PageResultVO<UserVO>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        PageResultVO<UserVO> result = userService.getUserList(page, size, keyword);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    @PreAuthorize("hasAuthority('user:query')")
    public ApiResponse<UserVO> getUserById(@PathVariable String id) {
        UserVO user = userService.getUserById(id);
        return ApiResponse.ok(user);
    }

    @PostMapping
    @Operation(summary = "创建用户")
    @PreAuthorize("hasAuthority('user:add')")
    public ApiResponse<UserVO> createUser(@Valid @RequestBody UserCreateReq req) {
        UserVO user = userService.createUser(req);
        return ApiResponse.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @PreAuthorize("hasAuthority('user:edit')")
    public ApiResponse<UserVO> updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateReq req) {
        UserVO user = userService.updateUser(id, req);
        return ApiResponse.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('user:delete')")
    public ApiResponse<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态")
    @PreAuthorize("hasAuthority('user:edit')")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable String id,
            @RequestParam Integer status
    ) {
        userService.updateUserStatus(id, status);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "重置用户密码")
    @PreAuthorize("hasAuthority('user:edit')")
    public ApiResponse<Void> resetPassword(
            @PathVariable String id,
            @RequestParam String password
    ) {
        userService.resetPassword(id, password);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "为用户分配角色")
    @PreAuthorize("hasAuthority('user:assign')")
    public ApiResponse<Void> assignRoles(
            @PathVariable String id,
            @RequestBody List<String> roleIds
    ) {
        userService.assignRoles(id, roleIds);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/roles")
    @Operation(summary = "查询用户的角色ID列表")
    @PreAuthorize("hasAuthority('user:query')")
    public ApiResponse<List<String>> getUserRoleIds(@PathVariable String id) {
        List<String> roleIds = userService.getUserRoleIds(id);
        return ApiResponse.ok(roleIds);
    }
}