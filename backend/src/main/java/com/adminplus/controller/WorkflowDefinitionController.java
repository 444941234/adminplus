package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.request.WorkflowDefinitionRequest;
import com.adminplus.pojo.dto.request.WorkflowNodeRequest;
import com.adminplus.pojo.dto.response.WorkflowDefinitionResponse;
import com.adminplus.pojo.dto.response.WorkflowNodeResponse;
import com.adminplus.service.WorkflowDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流定义控制器
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Slf4j
@RestController
@RequestMapping("/v1/workflow/definitions")
@RequiredArgsConstructor
@Tag(name = "工作流定义", description = "工作流模板管理")
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService definitionService;

    @PostMapping
    @Operation(summary = "创建工作流定义")
    @OperationLog(module = "工作流管理", operationType = 2, description = "创建工作流定义 {#request.definitionName}")
    @PreAuthorize("hasAnyAuthority('workflow:definition:create', 'workflow:create')")
    public ApiResponse<WorkflowDefinitionResponse> create(@Valid @RequestBody WorkflowDefinitionRequest request) {
        log.info("创建工作流定义: {}", request.definitionName());
        WorkflowDefinitionResponse resp = definitionService.create(request);
        return ApiResponse.ok(resp);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新工作流定义")
    @OperationLog(module = "工作流管理", operationType = 3, description = "更新工作流定义 {#id}")
    @PreAuthorize("hasAnyAuthority('workflow:definition:update', 'workflow:update')")
    public ApiResponse<WorkflowDefinitionResponse> update(
            @PathVariable String id,
            @Valid @RequestBody WorkflowDefinitionRequest request) {
        log.info("更新工作流定义: id={}", id);
        WorkflowDefinitionResponse response = definitionService.update(id, request);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除工作流定义")
    @OperationLog(module = "工作流管理", operationType = 4, description = "删除工作流定义 {#id}")
    @PreAuthorize("hasAnyAuthority('workflow:definition:delete', 'workflow:delete')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        log.info("删除工作流定义: id={}", id);
        definitionService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询工作流定义详情")
    @OperationLog(module = "工作流管理", operationType = 1, description = "查询工作流定义详情 {#id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<WorkflowDefinitionResponse> getById(@PathVariable String id) {
        WorkflowDefinitionResponse response = definitionService.getById(id);
        return ApiResponse.ok(response);
    }

    @GetMapping
    @Operation(summary = "查询所有工作流定义")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowDefinitionResponse>> listAll() {
        List<WorkflowDefinitionResponse> responses = definitionService.listAll();
        return ApiResponse.ok(responses);
    }

    @GetMapping("/enabled")
    @Operation(summary = "查询启用的工作流定义")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowDefinitionResponse>> listEnabled() {
        List<WorkflowDefinitionResponse> responses = definitionService.listEnabled();
        return ApiResponse.ok(responses);
    }

    @PostMapping("/{definitionId}/nodes")
    @Operation(summary = "添加工作流节点")
    @OperationLog(module = "工作流管理", operationType = 2, description = "添加工作流节点 {#request.nodeName}")
    @PreAuthorize("hasAnyAuthority('workflow:definition:update', 'workflow:update')")
    public ApiResponse<WorkflowNodeResponse> addNode(
            @PathVariable String definitionId,
            @Valid @RequestBody WorkflowNodeRequest request) {
        log.info("添加工作流节点: definitionId={}, nodeName={}", definitionId, request.nodeName());
        WorkflowNodeResponse resp = definitionService.addNode(definitionId, request);
        return ApiResponse.ok(resp);
    }

    @PutMapping("/nodes/{nodeId}")
    @Operation(summary = "更新工作流节点")
    @OperationLog(module = "工作流管理", operationType = 3, description = "更新工作流节点 {#nodeId}")
    @PreAuthorize("hasAnyAuthority('workflow:definition:update', 'workflow:update')")
    public ApiResponse<WorkflowNodeResponse> updateNode(
            @PathVariable String nodeId,
            @Valid @RequestBody WorkflowNodeRequest request) {
        log.info("更新工作流节点: nodeId={}", nodeId);
        WorkflowNodeResponse response = definitionService.updateNode(nodeId, request);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/nodes/{nodeId}")
    @Operation(summary = "删除工作流节点")
    @OperationLog(module = "工作流管理", operationType = 4, description = "删除工作流节点 {#nodeId}")
    @PreAuthorize("hasAnyAuthority('workflow:definition:update', 'workflow:update')")
    public ApiResponse<Void> deleteNode(@PathVariable String nodeId) {
        log.info("删除工作流节点: nodeId={}", nodeId);
        definitionService.deleteNode(nodeId);
        return ApiResponse.ok();
    }

    @GetMapping("/{definitionId}/nodes")
    @Operation(summary = "查询工作流的所有节点")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowNodeResponse>> listNodes(@PathVariable String definitionId) {
        log.info("查询工作流节点: definitionId={}", definitionId);
        List<WorkflowNodeResponse> resp = definitionService.listNodes(definitionId);
        log.info("查询工作流节点结果: definitionId={}, 节点数={}", definitionId, resp.size());
        return ApiResponse.ok(resp);
    }
}
