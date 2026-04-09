package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.query.UserQuery;
import com.adminplus.pojo.dto.request.PasswordResetRequest;
import com.adminplus.pojo.dto.request.RoleAssignRequest;
import com.adminplus.pojo.dto.request.UserCreateRequest;
import com.adminplus.pojo.dto.request.UserUpdateRequest;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.dto.response.UserResponse;
import com.adminplus.service.UserService;
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
    @OperationLog(module = "用户管理", operationType = 1, description = "查询用户列表")
    @PreAuthorize("hasAuthority('user:query')")
    public ApiResponse<PageResultResponse<UserResponse>> getUserList(UserQuery req) {
        PageResultResponse<UserResponse> result = userService.getUserList(req);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    @OperationLog(module = "用户管理", operationType = 1, description = "查询用户详情 {#id}")
    @PreAuthorize("hasAuthority('user:query')")
    public ApiResponse<UserResponse> getUserById(@PathVariable String id) {
        UserResponse user = userService.getUserById(id);
        return ApiResponse.ok(user);
    }

    @PostMapping
    @Operation(summary = "创建用户")
    @OperationLog(module = "用户管理", operationType = 2, description = "新增用户 {#request.username}")
    @PreAuthorize("hasAuthority('user:add')")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse user = userService.createUser(request);
        return ApiResponse.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @OperationLog(module = "用户管理", operationType = 3, description = "修改用户 {#id}")
    @PreAuthorize("hasAuthority('user:edit')")
    public ApiResponse<UserResponse> updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ApiResponse.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @OperationLog(module = "用户管理", operationType = 4, description = "删除用户 {#id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ApiResponse<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态")
    @OperationLog(module = "用户管理", operationType = 3, description = "修改用户状态 {#id}")
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
    @OperationLog(module = "用户管理", operationType = 3, description = "重置用户密码 {#id}")
    @PreAuthorize("hasAuthority('user:edit')")
    public ApiResponse<Void> resetPassword(
            @PathVariable String id,
            @Valid @RequestBody PasswordResetRequest request
    ) {
        userService.resetPassword(id, request.password());
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "为用户分配角色")
    @OperationLog(module = "用户管理", operationType = 3, description = "分配用户角色 {#id}")
    @PreAuthorize("hasAuthority('user:assign')")
    public ApiResponse<Void> assignRoles(
            @PathVariable String id,
            @Valid @RequestBody RoleAssignRequest request
    ) {
        userService.assignRoles(id, request.roleIds());
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/roles")
    @Operation(summary = "查询用户的角色ID列表")
    @OperationLog(module = "用户管理", operationType = 1, description = "查询用户角色 {#id}")
    @PreAuthorize("hasAuthority('user:query')")
    public ApiResponse<List<String>> getUserRoleIds(@PathVariable String id) {
        List<String> roleIds = userService.getUserRoleIds(id);
        return ApiResponse.ok(roleIds);
    }
}