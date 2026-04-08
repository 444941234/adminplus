package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.query.ConfigQuery;
import com.adminplus.pojo.dto.req.*;
import com.adminplus.pojo.dto.resp.*;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配置控制器
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@RestController
@RequestMapping("/v1/sys/configs")
@RequiredArgsConstructor
@Tag(name = "配置管理", description = "配置增删改查、导入导出、回滚")
public class ConfigController {

    private final ConfigService configService;

    @GetMapping
    @Operation(summary = "分页查询配置列表")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置列表")
    @PreAuthorize("hasAuthority('config:list')")
    public ApiResponse<PageResultResp<ConfigResp>> getConfigList(ConfigQuery req) {
        PageResultResp<ConfigResp> result = configService.getConfigList(req);
        return ApiResponse.ok(result);
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "根据配置组ID查询配置列表")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置组配置 {#groupId}")
    @PreAuthorize("hasAuthority('config:query')")
    public ApiResponse<List<ConfigResp>> getConfigsByGroupId(@PathVariable String groupId) {
        List<ConfigResp> result = configService.getConfigsByGroupId(groupId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/group-code/{groupCode}")
    @Operation(summary = "根据配置组编码查询配置列表")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置组配置 {#groupCode}")
    @PreAuthorize("hasAuthority('config:query')")
    public ApiResponse<List<ConfigResp>> getConfigsByGroupCode(@PathVariable String groupCode) {
        List<ConfigResp> result = configService.getConfigsByGroupCode(groupCode);
        return ApiResponse.ok(result);
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "根据配置键查询")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置键 {#key}")
    @PreAuthorize("hasAuthority('config:query')")
    public ApiResponse<ConfigResp> getConfigByKey(@PathVariable String key) {
        ConfigResp result = configService.getConfigByKey(key);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询配置")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置详情 {#id}")
    @PreAuthorize("hasAuthority('config:query')")
    public ApiResponse<ConfigResp> getConfigById(@PathVariable String id) {
        ConfigResp result = configService.getConfigById(id);
        return ApiResponse.ok(result);
    }

    @PostMapping
    @Operation(summary = "创建配置")
    @OperationLog(module = "配置管理", operationType = 2, description = "新增配置 {#req.name}")
    @PreAuthorize("hasAuthority('config:add')")
    public ApiResponse<ConfigResp> createConfig(@Valid @RequestBody ConfigCreateReq req) {
        ConfigResp result = configService.createConfig(req);
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新配置")
    @OperationLog(module = "配置管理", operationType = 3, description = "修改配置 {#id}")
    @PreAuthorize("hasAuthority('config:edit')")
    public ApiResponse<ConfigResp> updateConfig(
            @PathVariable String id,
            @Valid @RequestBody ConfigUpdateReq req
    ) {
        ConfigResp result = configService.updateConfig(id, req);
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置")
    @OperationLog(module = "配置管理", operationType = 4, description = "删除配置 {#id}")
    @PreAuthorize("hasAuthority('config:delete')")
    public ApiResponse<Void> deleteConfig(@PathVariable String id) {
        configService.deleteConfig(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新配置状态")
    @OperationLog(module = "配置管理", operationType = 3, description = "修改配置状态 {#id}")
    @PreAuthorize("hasAuthority('config:edit')")
    public ApiResponse<Void> updateConfigStatus(
            @PathVariable String id,
            @RequestParam Integer status
    ) {
        configService.updateConfigStatus(id, status);
        return ApiResponse.ok();
    }

    @PostMapping("/batch")
    @Operation(summary = "批量更新配置值")
    @OperationLog(module = "配置管理", operationType = 3, description = "批量更新配置值")
    @PreAuthorize("hasAuthority('config:edit')")
    public ApiResponse<ConfigImportResultResp> batchUpdateConfigs(@Valid @RequestBody ConfigBatchUpdateReq req) {
        ConfigImportResultResp result = configService.batchUpdateConfigs(req);
        return ApiResponse.ok(result);
    }

    @PostMapping("/export")
    @Operation(summary = "导出配置")
    @OperationLog(module = "配置管理", operationType = 5, description = "导出配置")
    @PreAuthorize("hasAuthority('config:export')")
    public ApiResponse<ConfigExportResp> exportConfigs(@RequestBody(required = false) List<String> groupIds) {
        ConfigExportResp result = configService.exportConfigs(groupIds);
        return ApiResponse.ok(result);
    }

    @PostMapping("/import")
    @Operation(summary = "导入配置")
    @OperationLog(module = "配置管理", operationType = 6, description = "导入配置")
    @PreAuthorize("hasAuthority('config:import')")
    public ApiResponse<ConfigImportResultResp> importConfigs(@Valid @RequestBody ConfigImportReq req) {
        ConfigImportResultResp result = configService.importConfigs(req);
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/rollback")
    @Operation(summary = "回滚配置到历史版本")
    @OperationLog(module = "配置管理", operationType = 3, description = "回滚配置 {#id}")
    @PreAuthorize("hasAuthority('config:rollback')")
    public ApiResponse<ConfigResp> rollbackConfig(
            @PathVariable String id,
            @Valid @RequestBody ConfigRollbackReq req
    ) {
        ConfigResp result = configService.rollbackConfig(id, req);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "查询配置历史记录")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置历史 {#id}")
    @PreAuthorize("hasAuthority('config:query')")
    public ApiResponse<List<ConfigHistoryResp>> getConfigHistory(@PathVariable String id) {
        List<ConfigHistoryResp> result = configService.getConfigHistory(id);
        return ApiResponse.ok(result);
    }

    @GetMapping("/effect-info")
    @Operation(summary = "获取配置生效信息")
    @OperationLog(module = "配置管理", operationType = 1, description = "查询配置生效信息")
    @PreAuthorize("hasAuthority('config:query')")
    public ApiResponse<ConfigEffectInfoResp> getConfigEffectInfo() {
        ConfigEffectInfoResp result = configService.getConfigEffectInfo();
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/apply")
    @Operation(summary = "使配置生效（手动生效模式）")
    @OperationLog(module = "配置管理", operationType = 3, description = "手动生效配置 {#id}")
    @PreAuthorize("hasAuthority('config:apply')")
    public ApiResponse<Void> applyConfig(@PathVariable String id) {
        configService.applyConfig(id);
        return ApiResponse.ok();
    }

    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新配置缓存")
    @OperationLog(module = "配置管理", operationType = 3, description = "刷新配置缓存")
    @PreAuthorize("hasAuthority('config:edit')")
    public ApiResponse<Void> refreshConfigCache() {
        configService.refreshConfigCache();
        return ApiResponse.ok();
    }
}
