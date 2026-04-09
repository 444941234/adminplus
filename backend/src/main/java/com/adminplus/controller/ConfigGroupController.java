package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.query.ConfigGroupQuery;
import com.adminplus.pojo.dto.request.ConfigGroupCreateRequest;
import com.adminplus.pojo.dto.request.ConfigGroupUpdateRequest;
import com.adminplus.pojo.dto.response.ConfigGroupResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
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
    public ApiResponse<PageResultResponse<ConfigGroupResponse>> getConfigGroupList(ConfigGroupQuery query) {
        PageResultResponse<ConfigGroupResponse> response = configGroupService.getConfigGroupList(query);
        return ApiResponse.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "查询所有启用的配置组")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询启用的配置组")
    @PreAuthorize("hasAuthority('config:group:query')")
    public ApiResponse<List<ConfigGroupResponse>> getActiveConfigGroups() {
        List<ConfigGroupResponse> responses = configGroupService.getActiveConfigGroups();
        return ApiResponse.ok(responses);
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有配置组（包括禁用的）")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询所有配置组")
    @PreAuthorize("hasAuthority('config:group:query')")
    public ApiResponse<List<ConfigGroupResponse>> getAllConfigGroups() {
        List<ConfigGroupResponse> responses = configGroupService.getAllConfigGroups();
        return ApiResponse.ok(responses);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码查询配置组")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置组编码 {#code}")
    @PreAuthorize("hasAuthority('config:group:query')")
    public ApiResponse<ConfigGroupResponse> getConfigGroupByCode(@PathVariable String code) {
        ConfigGroupResponse response = configGroupService.getConfigGroupByCode(code);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询配置组")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置组详情 {#id}")
    @PreAuthorize("hasAuthority('config:group:query')")
    public ApiResponse<ConfigGroupResponse> getConfigGroupById(@PathVariable String id) {
        ConfigGroupResponse response = configGroupService.getConfigGroupById(id);
        return ApiResponse.ok(response);
    }

    @PostMapping
    @Operation(summary = "创建配置组")
    @OperationLog(module = "配置管理", operationType = 2, description = "新增配置组 {#request.name}")
    @PreAuthorize("hasAuthority('config:group:add')")
    public ApiResponse<ConfigGroupResponse> createConfigGroup(@Valid @RequestBody ConfigGroupCreateRequest request) {
        ConfigGroupResponse response = configGroupService.createConfigGroup(request);
        return ApiResponse.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新配置组")
    @OperationLog(module = "配置管理", operationType = 3, description = "修改配置组 {#id}")
    @PreAuthorize("hasAuthority('config:group:edit')")
    public ApiResponse<ConfigGroupResponse> updateConfigGroup(
            @PathVariable String id,
            @Valid @RequestBody ConfigGroupUpdateRequest request
    ) {
        ConfigGroupResponse response = configGroupService.updateConfigGroup(id, request);
        return ApiResponse.ok(response);
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
