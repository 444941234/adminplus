package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.MenuBatchDeleteReq;
import com.adminplus.pojo.dto.req.MenuBatchStatusReq;
import com.adminplus.pojo.dto.req.MenuCreateReq;
import com.adminplus.pojo.dto.req.MenuUpdateReq;
import com.adminplus.pojo.dto.resp.MenuResp;
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
    @PreAuthorize("hasAuthority('menu:list')")
    public ApiResponse<List<MenuResp>> getMenuTree() {
        List<MenuResp> menus = menuService.getMenuTree();
        return ApiResponse.ok(menus);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询菜单")
    @PreAuthorize("hasAuthority('menu:query')")
    public ApiResponse<MenuResp> getMenuById(@PathVariable String id) {
        MenuResp menu = menuService.getMenuById(id);
        return ApiResponse.ok(menu);
    }

    @PostMapping
    @Operation(summary = "创建菜单")
    @PreAuthorize("hasAuthority('menu:add')")
    public ApiResponse<MenuResp> createMenu(@Valid @RequestBody MenuCreateReq req) {
        MenuResp menu = menuService.createMenu(req);
        return ApiResponse.ok(menu);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新菜单")
    @PreAuthorize("hasAuthority('menu:edit')")
    public ApiResponse<MenuResp> updateMenu(@PathVariable String id, @Valid @RequestBody MenuUpdateReq req) {
        MenuResp menu = menuService.updateMenu(id, req);
        return ApiResponse.ok(menu);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    @PreAuthorize("hasAuthority('menu:delete')")
    public ApiResponse<Void> deleteMenu(@PathVariable String id) {
        menuService.deleteMenu(id);
        return ApiResponse.ok();
    }

    @PutMapping("/batch/status")
    @Operation(summary = "批量更新菜单状态")
    @PreAuthorize("hasAuthority('menu:edit')")
    public ApiResponse<Void> batchUpdateStatus(@Valid @RequestBody MenuBatchStatusReq req) {
        menuService.batchUpdateStatus(req);
        return ApiResponse.ok();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除菜单")
    @PreAuthorize("hasAuthority('menu:delete')")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody MenuBatchDeleteReq req) {
        menuService.batchDelete(req);
        return ApiResponse.ok();
    }

    @GetMapping("/user/tree")
    @Operation(summary = "获取当前用户的菜单树")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MenuResp>> getUserMenuTree() {
        // 从 SecurityContext 获取当前用户ID
        String userId = SecurityUtils.getCurrentUserId();
        List<MenuResp> menus = menuService.getUserMenuTree(userId);
        return ApiResponse.ok(menus);
    }
}