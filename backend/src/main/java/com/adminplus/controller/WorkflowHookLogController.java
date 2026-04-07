package com.adminplus.controller;

import com.adminplus.common.constant.WorkflowExtensionPermissions;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.entity.WorkflowHookLogEntity;
import com.adminplus.repository.WorkflowHookLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流钩子执行日志控制器
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Slf4j
@RestController
@RequestMapping("/v1/workflow/hook-logs")
@RequiredArgsConstructor
@Tag(name = "工作流钩子日志", description = "工作流钩子执行日志查询")
public class WorkflowHookLogController {

    private final WorkflowHookLogRepository hookLogRepository;

    @GetMapping("/instance/{instanceId}")
    @Operation(summary = "查询工作流实例的所有钩子日志")
    @PreAuthorize("hasAuthority('" + WorkflowExtensionPermissions.HOOK_LOG_VIEW + "')")
    public ApiResponse<List<WorkflowHookLogEntity>> listByInstanceId(@PathVariable String instanceId) {
        log.info("查询工作流钩子日志: instanceId={}", instanceId);
        List<WorkflowHookLogEntity> logs = hookLogRepository
                .findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc(instanceId);
        return ApiResponse.ok(logs);
    }

    @GetMapping("/instance/{instanceId}/{hookPoint}")
    @Operation(summary = "查询工作流实例指定钩子点的日志")
    @PreAuthorize("hasAuthority('" + WorkflowExtensionPermissions.HOOK_LOG_VIEW + "')")
    public ApiResponse<List<WorkflowHookLogEntity>> listByInstanceIdAndHookPoint(
            @PathVariable String instanceId,
            @PathVariable String hookPoint) {
        log.info("查询工作流钩子日志: instanceId={}, hookPoint={}", instanceId, hookPoint);
        List<WorkflowHookLogEntity> logs = hookLogRepository
                .findByInstanceIdAndHookPointAndDeletedFalseOrderByCreateTimeDesc(instanceId, hookPoint);
        return ApiResponse.ok(logs);
    }

    @GetMapping("/instance/{instanceId}/node/{nodeId}")
    @Operation(summary = "查询工作流实例指定节点的日志")
    @PreAuthorize("hasAuthority('" + WorkflowExtensionPermissions.HOOK_LOG_VIEW + "')")
    public ApiResponse<List<WorkflowHookLogEntity>> listByInstanceIdAndNodeId(
            @PathVariable String instanceId,
            @PathVariable String nodeId) {
        log.info("查询工作流钩子日志: instanceId={}, nodeId={}", instanceId, nodeId);
        List<WorkflowHookLogEntity> logs = hookLogRepository
                .findByInstanceIdAndNodeIdAndDeletedFalseOrderByCreateTimeDesc(instanceId, nodeId);
        return ApiResponse.ok(logs);
    }

    @GetMapping("/hook/{hookId}")
    @Operation(summary = "查询钩子配置的所有执行日志")
    @PreAuthorize("hasAuthority('" + WorkflowExtensionPermissions.HOOK_LOG_VIEW + "')")
    public ApiResponse<List<WorkflowHookLogEntity>> listByHookId(@PathVariable String hookId) {
        log.info("查询钩子执行日志: hookId={}", hookId);
        List<WorkflowHookLogEntity> logs = hookLogRepository
                .findByHookIdAndDeletedFalseOrderByCreateTimeDesc(hookId);
        return ApiResponse.ok(logs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询钩子日志详情")
    @PreAuthorize("hasAuthority('" + WorkflowExtensionPermissions.HOOK_LOG_VIEW + "')")
    public ApiResponse<WorkflowHookLogEntity> getById(@PathVariable String id) {
        return hookLogRepository.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.fail("钩子日志不存在"));
    }
}
