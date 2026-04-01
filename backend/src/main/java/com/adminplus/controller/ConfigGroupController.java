package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.ConfigGroupCreateReq;
import com.adminplus.pojo.dto.req.ConfigGroupUpdateReq;
import com.adminplus.pojo.dto.resp.ConfigGroupResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.ConfigGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配置组控制器
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@RestController
@RequestMapping("/v1/sys/config-groups")
@RequiredArgsConstructor
@Tag(name = "配置组管理", description = "配置组增删改查")
public class ConfigGroupController {

    private final ConfigGroupService configGroupService;

    @GetMapping
    @Operation(summary = "分页查询配置组列表")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置组列表")
    @PreAuthorize("hasAuthority('config:group:list')")
    public ApiResponse<PageResultResp<ConfigGroupResp>> getConfigGroupList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        PageResultResp<ConfigGroupResp> result = configGroupService.getConfigGroupList(page, size, keyword);
        return ApiResponse.ok(result);
    }

    @GetMapping("/active")
    @Operation(summary = "查询所有启用的配置组")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询启用的配置组")
    @PreAuthorize("hasAuthority('config:group:query')")
    public ApiResponse<List<ConfigGroupResp>> getActiveConfigGroups() {
        List<ConfigGroupResp> result = configGroupService.getActiveConfigGroups();
        return ApiResponse.ok(result);
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有配置组（包括禁用的）")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询所有配置组")
    @PreAuthorize("hasAuthority('config:group:query')")
    public ApiResponse<List<ConfigGroupResp>> getAllConfigGroups() {
        List<ConfigGroupResp> result = configGroupService.getAllConfigGroups();
        return ApiResponse.ok(result);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码查询配置组")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置组编码 {#code}")
    @PreAuthorize("hasAuthority('config:group:query')")
    public ApiResponse<ConfigGroupResp> getConfigGroupByCode(@PathVariable String code) {
        ConfigGroupResp result = configGroupService.getConfigGroupByCode(code);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询配置组")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置组详情 {#id}")
    @PreAuthorize("hasAuthority('config:group:query')")
    public ApiResponse<ConfigGroupResp> getConfigGroupById(@PathVariable String id) {
        ConfigGroupResp result = configGroupService.getConfigGroupById(id);
        return ApiResponse.ok(result);
    }

    @PostMapping
    @Operation(summary = "创建配置组")
    @OperationLog(module = "配置管理", operationType = 2, description = "新增配置组 {#req.name}")
    @PreAuthorize("hasAuthority('config:group:add')")
    public ApiResponse<ConfigGroupResp> createConfigGroup(@Valid @RequestBody ConfigGroupCreateReq req) {
        ConfigGroupResp result = configGroupService.createConfigGroup(req);
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新配置组")
    @OperationLog(module = "配置管理", operationType = 3, description = "修改配置组 {#id}")
    @PreAuthorize("hasAuthority('config:group:edit')")
    public ApiResponse<ConfigGroupResp> updateConfigGroup(
            @PathVariable String id,
            @Valid @RequestBody ConfigGroupUpdateReq req
    ) {
        ConfigGroupResp result = configGroupService.updateConfigGroup(id, req);
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置组")
    @OperationLog(module = "配置管理", operationType = 4, description = "删除配置组 {#id}")
    @PreAuthorize("hasAuthority('config:group:delete')")
    public ApiResponse<Void> deleteConfigGroup(@PathVariable String id) {
        configGroupService.deleteConfigGroup(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新配置组状态")
    @OperationLog(module = "配置管理", operationType = 3, description = "修改配置组状态 {#id}")
    @PreAuthorize("hasAuthority('config:group:edit')")
    public ApiResponse<Void> updateConfigGroupStatus(
            @PathVariable String id,
            @RequestParam Integer status
    ) {
        configGroupService.updateConfigGroupStatus(id, status);
        return ApiResponse.ok();
    }
}
