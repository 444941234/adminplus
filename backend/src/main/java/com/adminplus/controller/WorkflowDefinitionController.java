package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.WorkflowDefinitionReq;
import com.adminplus.pojo.dto.req.WorkflowNodeReq;
import com.adminplus.pojo.dto.resp.WorkflowDefinitionResp;
import com.adminplus.pojo.dto.resp.WorkflowNodeResp;
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
    @OperationLog(module = "工作流管理", operationType = 2, description = "创建工作流定义 {#req.definitionName}")
    @PreAuthorize("hasAnyAuthority('workflow:definition:create', 'workflow:create')")
    public ApiResponse<WorkflowDefinitionResp> create(@Valid @RequestBody WorkflowDefinitionReq req) {
        log.info("创建工作流定义: {}", req.definitionName());
        WorkflowDefinitionResp resp = definitionService.create(req);
        return ApiResponse.ok(resp);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新工作流定义")
    @OperationLog(module = "工作流管理", operationType = 3, description = "更新工作流定义 {#id}")
    @PreAuthorize("hasAnyAuthority('workflow:definition:update', 'workflow:update')")
    public ApiResponse<WorkflowDefinitionResp> update(
            @PathVariable String id,
            @Valid @RequestBody WorkflowDefinitionReq req) {
        log.info("更新工作流定义: id={}", id);
        WorkflowDefinitionResp resp = definitionService.update(id, req);
        return ApiResponse.ok(resp);
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
    public ApiResponse<WorkflowDefinitionResp> getById(@PathVariable String id) {
        WorkflowDefinitionResp resp = definitionService.getById(id);
        return ApiResponse.ok(resp);
    }

    @GetMapping
    @Operation(summary = "查询所有工作流定义")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowDefinitionResp>> listAll() {
        List<WorkflowDefinitionResp> resp = definitionService.listAll();
        return ApiResponse.ok(resp);
    }

    @GetMapping("/enabled")
    @Operation(summary = "查询启用的工作流定义")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowDefinitionResp>> listEnabled() {
        List<WorkflowDefinitionResp> resp = definitionService.listEnabled();
        return ApiResponse.ok(resp);
    }

    @PostMapping("/{definitionId}/nodes")
    @Operation(summary = "添加工作流节点")
    @OperationLog(module = "工作流管理", operationType = 2, description = "添加工作流节点 {#req.nodeName}")
    @PreAuthorize("hasAnyAuthority('workflow:definition:update', 'workflow:update')")
    public ApiResponse<WorkflowNodeResp> addNode(
            @PathVariable String definitionId,
            @Valid @RequestBody WorkflowNodeReq req) {
        log.info("添加工作流节点: definitionId={}, nodeName={}", definitionId, req.nodeName());
        WorkflowNodeResp resp = definitionService.addNode(definitionId, req);
        return ApiResponse.ok(resp);
    }

    @PutMapping("/nodes/{nodeId}")
    @Operation(summary = "更新工作流节点")
    @OperationLog(module = "工作流管理", operationType = 3, description = "更新工作流节点 {#nodeId}")
    @PreAuthorize("hasAnyAuthority('workflow:definition:update', 'workflow:update')")
    public ApiResponse<WorkflowNodeResp> updateNode(
            @PathVariable String nodeId,
            @Valid @RequestBody WorkflowNodeReq req) {
        log.info("更新工作流节点: nodeId={}", nodeId);
        WorkflowNodeResp resp = definitionService.updateNode(nodeId, req);
        return ApiResponse.ok(resp);
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
    public ApiResponse<List<WorkflowNodeResp>> listNodes(@PathVariable String definitionId) {
        List<WorkflowNodeResp> resp = definitionService.listNodes(definitionId);
        return ApiResponse.ok(resp);
    }
}
