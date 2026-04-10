package com.adminplus.controller;
import com.adminplus.enums.OperationType;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.response.WorkflowNodeHookResponse;
import com.adminplus.pojo.dto.workflow.hook.WorkflowNodeHookRequest;
import com.adminplus.service.WorkflowNodeHookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流节点钩子配置控制器
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Slf4j
@RestController
@RequestMapping("/v1/workflow/hooks")
@RequiredArgsConstructor
@Tag(name = "工作流钩子", description = "工作流节点钩子配置管理")
public class WorkflowNodeHookController {

    private final WorkflowNodeHookService hookService;

    @PostMapping
    @Operation(summary = "创建钩子配置")
    @OperationLog(module = "工作流管理", type = OperationType.CREATE, description = "创建钩子配置 {#req.hookName}")
    @PreAuthorize("hasAuthority('workflow:hook:create')")
    public ApiResponse<WorkflowNodeHookResponse> create(@Valid @RequestBody WorkflowNodeHookRequest req) {
        log.info("创建钩子配置: nodeId={}, hookPoint={}", req.nodeId(), req.hookPoint());
        WorkflowNodeHookResponse response = hookService.create(req);
        return ApiResponse.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新钩子配置")
    @OperationLog(module = "工作流管理", type = OperationType.UPDATE, description = "更新钩子配置 {#id}")
    @PreAuthorize("hasAuthority('workflow:hook:update')")
    public ApiResponse<WorkflowNodeHookResponse> update(
            @PathVariable String id,
            @Valid @RequestBody WorkflowNodeHookRequest req) {
        log.info("更新钩子配置: id={}", id);
        WorkflowNodeHookResponse response = hookService.update(id, req);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除钩子配置")
    @OperationLog(module = "工作流管理", type = OperationType.DELETE, description = "删除钩子配置 {#id}")
    @PreAuthorize("hasAuthority('workflow:hook:delete')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        log.info("删除钩子配置: id={}", id);
        hookService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询钩子配置详情")
    @PreAuthorize("hasAuthority('workflow:hook:view')")
    public ApiResponse<WorkflowNodeHookResponse> getById(@PathVariable String id) {
        WorkflowNodeHookResponse response = hookService.getById(id);
        return ApiResponse.ok(response);
    }

    @GetMapping("/node/{nodeId}")
    @Operation(summary = "查询节点的所有钩子配置")
    @PreAuthorize("hasAuthority('workflow:hook:view')")
    public ApiResponse<List<WorkflowNodeHookResponse>> listByNodeId(@PathVariable String nodeId) {
        List<WorkflowNodeHookResponse> hooks = hookService.listByNodeId(nodeId);
        return ApiResponse.ok(hooks);
    }

    @GetMapping("/node/{nodeId}/{hookPoint}")
    @Operation(summary = "查询节点指定钩子点的配置")
    @PreAuthorize("hasAuthority('workflow:hook:view')")
    public ApiResponse<List<WorkflowNodeHookResponse>> listByNodeIdAndHookPoint(
            @PathVariable String nodeId,
            @PathVariable String hookPoint) {
        List<WorkflowNodeHookResponse> hooks = hookService.listByNodeIdAndHookPoint(nodeId, hookPoint);
        return ApiResponse.ok(hooks);
    }
}
