package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.query.RoleQuery;
import com.adminplus.pojo.dto.request.RoleCreateRequest;
import com.adminplus.pojo.dto.request.RoleUpdateRequest;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.dto.response.RoleResponse;
import com.adminplus.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@RestController
@RequestMapping("/v1/sys/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色增删改查")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "分页查询角色列表")
    @OperationLog(module = "角色管理", operationType = 1, description = "查询角色列表")
    @PreAuthorize("hasAuthority('role:list')")
    public ApiResponse<PageResultResponse<RoleResponse>> getRoleList(RoleQuery query) {
        PageResultResponse<RoleResponse> response = roleService.getRoleList(query);
        return ApiResponse.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有角色列表（用于下拉选择）")
    @PreAuthorize("hasAuthority('role:list')")
    public ApiResponse<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> responses = roleService.getAllRoles();
        return ApiResponse.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询角色")
    @OperationLog(module = "角色管理", operationType = 1, description = "查询角色详情 {#id}")
    @PreAuthorize("hasAuthority('role:query')")
    public ApiResponse<RoleResponse> getRoleById(@PathVariable String id) {
        RoleResponse response = roleService.getRoleById(id);
        return ApiResponse.ok(response);
    }

    @PostMapping
    @Operation(summary = "创建角色")
    @OperationLog(module = "角色管理", operationType = 2, description = "新增角色 {#request.name}")
    @PreAuthorize("hasAuthority('role:add')")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ApiResponse.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    @OperationLog(module = "角色管理", operationType = 3, description = "修改角色 {#id}")
    @PreAuthorize("hasAuthority('role:edit')")
    public ApiResponse<RoleResponse> updateRole(@PathVariable String id, @Valid @RequestBody RoleUpdateRequest request) {
        RoleResponse response = roleService.updateRole(id, request);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @OperationLog(module = "角色管理", operationType = 4, description = "删除角色 {#id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public ApiResponse<Void> deleteRole(@PathVariable String id) {
        roleService.deleteRole(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/menus")
    @Operation(summary = "为角色分配菜单权限")
    @OperationLog(module = "角色管理", operationType = 3, description = "分配角色菜单 {#id}")
    @PreAuthorize("hasAuthority('role:assign')")
    public ApiResponse<Void> assignMenus(
            @PathVariable String id,
            @RequestBody List<String> menuIds
    ) {
        roleService.assignMenus(id, menuIds);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/menus")
    @Operation(summary = "查询角色的菜单ID列表")
    @OperationLog(module = "角色管理", operationType = 1, description = "查询角色菜单 {#id}")
    @PreAuthorize("hasAuthority('role:query')")
    public ApiResponse<List<String>> getRoleMenuIds(@PathVariable String id) {
        List<String> menuIds = roleService.getRoleMenuIds(id);
        return ApiResponse.ok(menuIds);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新角色状态")
    @OperationLog(module = "角色管理", operationType = 3, description = "修改角色状态 {#id}")
    @PreAuthorize("hasAuthority('role:edit')")
    public ApiResponse<Void> updateRoleStatus(
            @PathVariable String id,
            @RequestParam Integer status
    ) {
        roleService.updateRoleStatus(id, status);
        return ApiResponse.ok();
    }
}