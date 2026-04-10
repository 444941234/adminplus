package com.adminplus.controller;
import com.adminplus.enums.OperationType;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.request.*;
import com.adminplus.pojo.dto.response.MenuResponse;
import com.adminplus.service.MenuService;
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
 * 菜单控制器
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@RestController
@RequestMapping("/v1/sys/menus")
@RequiredArgsConstructor
@Tag(name = "菜单管理", description = "菜单增删改查")
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @Operation(summary = "查询菜单树形列表")
    @OperationLog(module = "菜单管理", type = OperationType.QUERY, description = "查询菜单树形列表")
    @PreAuthorize("hasAuthority('menu:list')")
    public ApiResponse<List<MenuResponse>> getMenuTree() {
        List<MenuResponse> responses = menuService.getMenuTree();
        return ApiResponse.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询菜单")
    @OperationLog(module = "菜单管理", type = OperationType.QUERY, description = "查询菜单详情 {#id}")
    @PreAuthorize("hasAuthority('menu:query')")
    public ApiResponse<MenuResponse> getMenuById(@PathVariable String id) {
        MenuResponse response = menuService.getMenuById(id);
        return ApiResponse.ok(response);
    }

    @PostMapping
    @Operation(summary = "创建菜单")
    @OperationLog(module = "菜单管理", type = OperationType.CREATE, description = "新增菜单 {#request.menuName}")
    @PreAuthorize("hasAuthority('menu:add')")
    public ApiResponse<MenuResponse> createMenu(@Valid @RequestBody MenuCreateRequest request) {
        MenuResponse response = menuService.createMenu(request);
        return ApiResponse.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新菜单")
    @OperationLog(module = "菜单管理", type = OperationType.UPDATE, description = "修改菜单 {#id}")
    @PreAuthorize("hasAuthority('menu:edit')")
    public ApiResponse<MenuResponse> updateMenu(@PathVariable String id, @Valid @RequestBody MenuUpdateRequest request) {
        MenuResponse response = menuService.updateMenu(id, request);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    @OperationLog(module = "菜单管理", type = OperationType.DELETE, description = "删除菜单 {#id}")
    @PreAuthorize("hasAuthority('menu:delete')")
    public ApiResponse<Void> deleteMenu(@PathVariable String id) {
        menuService.deleteMenu(id);
        return ApiResponse.ok();
    }

    @PutMapping("/batch/status")
    @Operation(summary = "批量更新菜单状态")
    @OperationLog(module = "菜单管理", type = OperationType.UPDATE, description = "批量更新菜单状态")
    @PreAuthorize("hasAuthority('menu:edit')")
    public ApiResponse<Void> batchUpdateStatus(@Valid @RequestBody MenuBatchStatusRequest request) {
        menuService.batchUpdateStatus(request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除菜单")
    @OperationLog(module = "菜单管理", type = OperationType.DELETE, description = "批量删除菜单")
    @PreAuthorize("hasAuthority('menu:delete')")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody MenuBatchDeleteRequest request) {
        menuService.batchDelete(request);
        return ApiResponse.ok();
    }

    @GetMapping("/user/tree")
    @Operation(summary = "获取当前用户的菜单树")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MenuResponse>> getUserMenuTree() {
        // 从 SecurityContext 获取当前用户ID
        String userId = SecurityUtils.getCurrentUserId();
        List<MenuResponse> responses = menuService.getUserMenuTree(userId);
        return ApiResponse.ok(responses);
    }

    @PostMapping("/{id}/copy")
    @Operation(summary = "复制菜单")
    @OperationLog(module = "菜单管理", type = OperationType.CREATE, description = "复制菜单 {#id}")
    @PreAuthorize("hasAuthority('menu:add')")
    public ApiResponse<MenuResponse> copyMenu(
            @PathVariable String id,
            @Valid @RequestBody CopyMenuRequest request) {
        MenuResponse response = menuService.copyMenu(id, request.targetParentId());
        return ApiResponse.ok(response);
    }
}