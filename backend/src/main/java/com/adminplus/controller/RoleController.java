package com.adminplus.controller;

import com.adminplus.dto.RoleCreateReq;
import com.adminplus.dto.RoleUpdateReq;
import com.adminplus.service.RoleService;
import com.adminplus.utils.ApiResponse;
import com.adminplus.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @Operation(summary = "查询角色列表")
    @PreAuthorize("hasAuthority('role:list')")
    public ApiResponse<Map<String, Object>> getRoleList() {
        List<RoleVO> roles = roleService.getRoleList();
        return ApiResponse.ok(Map.of(
                "records", roles,
                "total", roles.size()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询角色")
    @PreAuthorize("hasAuthority('role:query')")
    public ApiResponse<RoleVO> getRoleById(@PathVariable String id) {
        RoleVO role = roleService.getRoleById(id);
        return ApiResponse.ok(role);
    }

    @PostMapping
    @Operation(summary = "创建角色")
    @PreAuthorize("hasAuthority('role:add')")
    public ApiResponse<RoleVO> createRole(@Valid @RequestBody RoleCreateReq req) {
        RoleVO role = roleService.createRole(req);
        return ApiResponse.ok(role);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    @PreAuthorize("hasAuthority('role:edit')")
    public ApiResponse<RoleVO> updateRole(@PathVariable String id, @Valid @RequestBody RoleUpdateReq req) {
        RoleVO role = roleService.updateRole(id, req);
        return ApiResponse.ok(role);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('role:delete')")
    public ApiResponse<Void> deleteRole(@PathVariable String id) {
        roleService.deleteRole(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/menus")
    @Operation(summary = "为角色分配菜单权限")
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
    @PreAuthorize("hasAuthority('role:query')")
    public ApiResponse<List<String>> getRoleMenuIds(@PathVariable String id) {
        List<String> menuIds = roleService.getRoleMenuIds(id);
        return ApiResponse.ok(menuIds);
    }
}