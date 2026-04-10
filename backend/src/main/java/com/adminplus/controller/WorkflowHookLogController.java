package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.response.WorkflowHookLogResponse;
import com.adminplus.service.WorkflowHookLogService;
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
@RequestMapping(value = "/workflow/hook-logs")
@RequiredArgsConstructor
@Tag(name = "工作流钩子日志", description = "工作流钩子执行日志查询")
public class WorkflowHookLogController {

    private final WorkflowHookLogService hookLogService;

    @GetMapping("/instance/{instanceId}")
    @Operation(summary = "查询工作流实例的所有钩子日志")
    @PreAuthorize("hasAuthority('workflow:hook:log:view')")
    public ApiResponse<List<WorkflowHookLogResponse>> listByInstanceId(@PathVariable String instanceId) {
        log.info("查询工作流钩子日志: instanceId={}", instanceId);
        List<WorkflowHookLogResponse> responses = hookLogService.listByInstanceId(instanceId);
        return ApiResponse.ok(responses);
    }

    @GetMapping("/instance/{instanceId}/{hookPoint}")
    @Operation(summary = "查询工作流实例指定钩子点的日志")
    @PreAuthorize("hasAuthority('workflow:hook:log:view')")
    public ApiResponse<List<WorkflowHookLogResponse>> listByInstanceIdAndHookPoint(
            @PathVariable String instanceId,
            @PathVariable String hookPoint) {
        log.info("查询工作流钩子日志: instanceId={}, hookPoint={}", instanceId, hookPoint);
        List<WorkflowHookLogResponse> logs = hookLogService.listByInstanceIdAndHookPoint(instanceId, hookPoint);
        return ApiResponse.ok(logs);
    }

    @GetMapping("/instance/{instanceId}/node/{nodeId}")
    @Operation(summary = "查询工作流实例指定节点的日志")
    @PreAuthorize("hasAuthority('workflow:hook:log:view')")
    public ApiResponse<List<WorkflowHookLogResponse>> listByInstanceIdAndNodeId(
            @PathVariable String instanceId,
            @PathVariable String nodeId) {
        log.info("查询工作流钩子日志: instanceId={}, nodeId={}", instanceId, nodeId);
        List<WorkflowHookLogResponse> logs = hookLogService.listByInstanceIdAndNodeId(instanceId, nodeId);
        return ApiResponse.ok(logs);
    }

    @GetMapping("/hook/{hookId}")
    @Operation(summary = "查询钩子配置的所有执行日志")
    @PreAuthorize("hasAuthority('workflow:hook:log:view')")
    public ApiResponse<List<WorkflowHookLogResponse>> listByHookId(@PathVariable String hookId) {
        log.info("查询钩子执行日志: hookId={}", hookId);
        List<WorkflowHookLogResponse> logs = hookLogService.listByHookId(hookId);
        return ApiResponse.ok(logs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询钩子日志详情")
    @PreAuthorize("hasAuthority('workflow:hook:log:view')")
    public ApiResponse<WorkflowHookLogResponse> getById(@PathVariable String id) {
        WorkflowHookLogResponse log = hookLogService.getById(id);
        return ApiResponse.ok(log);
    }
}
