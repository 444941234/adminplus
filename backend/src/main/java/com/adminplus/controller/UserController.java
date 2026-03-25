package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.PasswordResetReq;
import com.adminplus.pojo.dto.req.RoleAssignReq;
import com.adminplus.pojo.dto.req.UserCreateReq;
import com.adminplus.pojo.dto.req.UserUpdateReq;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.pojo.dto.resp.UserResp;
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
    public ApiResponse<PageResultResp<UserResp>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deptId
    ) {
        PageResultResp<UserResp> result = userService.getUserList(page, size, keyword, deptId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    @OperationLog(module = "用户管理", operationType = 1, description = "查询用户详情 {#id}")
    @PreAuthorize("hasAuthority('user:query')")
    public ApiResponse<UserResp> getUserById(@PathVariable String id) {
        UserResp user = userService.getUserById(id);
        return ApiResponse.ok(user);
    }

    @PostMapping
    @Operation(summary = "创建用户")
    @OperationLog(module = "用户管理", operationType = 2, description = "新增用户 {#req.username}")
    @PreAuthorize("hasAuthority('user:add')")
    public ApiResponse<UserResp> createUser(@Valid @RequestBody UserCreateReq req) {
        UserResp user = userService.createUser(req);
        return ApiResponse.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @OperationLog(module = "用户管理", operationType = 3, description = "修改用户 {#id}")
    @PreAuthorize("hasAuthority('user:edit')")
    public ApiResponse<UserResp> updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateReq req) {
        UserResp user = userService.updateUser(id, req);
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
            @Valid @RequestBody PasswordResetReq req
    ) {
        userService.resetPassword(id, req.password());
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "为用户分配角色")
    @OperationLog(module = "用户管理", operationType = 3, description = "分配用户角色 {#id}")
    @PreAuthorize("hasAuthority('user:assign')")
    public ApiResponse<Void> assignRoles(
            @PathVariable String id,
            @Valid @RequestBody RoleAssignReq req
    ) {
        userService.assignRoles(id, req.roleIds());
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