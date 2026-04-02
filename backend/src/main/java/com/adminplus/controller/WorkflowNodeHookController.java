package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.config.SecurityConstants;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.entity.WorkflowNodeHookEntity;
import com.adminplus.repository.WorkflowNodeHookRepository;
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

    private final WorkflowNodeHookRepository hookRepository;

    @PostMapping
    @Operation(summary = "创建钩子配置")
    @OperationLog(module = "工作流管理", operationType = 2, description = "创建钩子配置 {#req.hookName}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.WORKFLOW_HOOK_CREATE + "')")
    public ApiResponse<WorkflowNodeHookEntity> create(@Valid @RequestBody WorkflowNodeHookEntity req) {
        log.info("创建钩子配置: nodeId={}, hookPoint={}", req.getNodeId(), req.getHookPoint());
        WorkflowNodeHookEntity saved = hookRepository.save(req);
        return ApiResponse.ok(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新钩子配置")
    @OperationLog(module = "工作流管理", operationType = 3, description = "更新钩子配置 {#id}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.WORKFLOW_HOOK_UPDATE + "')")
    public ApiResponse<WorkflowNodeHookEntity> update(
            @PathVariable String id,
            @Valid @RequestBody WorkflowNodeHookEntity req) {
        log.info("更新钩子配置: id={}", id);
        req.setId(id);
        WorkflowNodeHookEntity saved = hookRepository.save(req);
        return ApiResponse.ok(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除钩子配置")
    @OperationLog(module = "工作流管理", operationType = 4, description = "删除钩子配置 {#id}")
    @PreAuthorize("hasAuthority('" + SecurityConstants.WORKFLOW_HOOK_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        log.info("删除钩子配置: id={}", id);
        hookRepository.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询钩子配置详情")
    @PreAuthorize("hasAuthority('" + SecurityConstants.WORKFLOW_HOOK_VIEW + "')")
    public ApiResponse<WorkflowNodeHookEntity> getById(@PathVariable String id) {
        return hookRepository.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.fail("钩子配置不存在"));
    }

    @GetMapping("/node/{nodeId}")
    @Operation(summary = "查询节点的所有钩子配置")
    @PreAuthorize("hasAuthority('" + SecurityConstants.WORKFLOW_HOOK_VIEW + "')")
    public ApiResponse<List<WorkflowNodeHookEntity>> listByNodeId(@PathVariable String nodeId) {
        List<WorkflowNodeHookEntity> hooks = hookRepository
                .findByNodeIdAndDeletedFalseOrderByPriorityAsc(nodeId);
        return ApiResponse.ok(hooks);
    }

    @GetMapping("/node/{nodeId}/{hookPoint}")
    @Operation(summary = "查询节点指定钩子点的配置")
    @PreAuthorize("hasAuthority('" + SecurityConstants.WORKFLOW_HOOK_VIEW + "')")
    public ApiResponse<List<WorkflowNodeHookEntity>> listByNodeIdAndHookPoint(
            @PathVariable String nodeId,
            @PathVariable String hookPoint) {
        List<WorkflowNodeHookEntity> hooks = hookRepository
                .findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc(nodeId, hookPoint);
        return ApiResponse.ok(hooks);
    }
}
