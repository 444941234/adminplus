package com.adminplus.controller;
import com.adminplus.enums.OperationType;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.request.AddSignRequest;
import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.*;
import com.adminplus.service.WorkflowInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流实例控制器
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Slf4j
@RestController
@RequestMapping(value = "/workflow/instances")
@RequiredArgsConstructor
@Tag(name = "工作流实例", description = "工作流实例管理、审批操作")
public class WorkflowInstanceController {

    private final WorkflowInstanceService instanceService;

    @PostMapping("/draft")
    @Operation(summary = "创建工作流草稿")
    @OperationLog(module = "工作流管理", type = OperationType.CREATE, description = "创建工作流草稿 {#request.title}")
    @PreAuthorize("hasAnyAuthority('workflow:draft', 'workflow:create')")
    public ApiResponse<WorkflowInstanceResponse> createDraft(@Valid @RequestBody WorkflowStartRequest request) {
        log.info("创建工作流草稿: title={}", request.title());
        WorkflowInstanceResponse resp = instanceService.createDraft(request);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/{instanceId}/submit")
    @Operation(summary = "提交工作流")
    @OperationLog(module = "工作流管理", type = OperationType.CREATE, description = "提交工作流 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:start', 'workflow:draft', 'workflow:create')")
    public ApiResponse<WorkflowInstanceResponse> submit(
            @PathVariable String instanceId,
            @RequestBody(required = false) WorkflowStartRequest request) {
        log.info("提交工作流: instanceId={}", instanceId);
        WorkflowInstanceResponse response = instanceService.submit(instanceId, request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/start")
    @Operation(summary = "发起并提交工作流")
    @OperationLog(module = "工作流管理", type = OperationType.CREATE, description = "发起工作流 {#request.title}")
    @PreAuthorize("hasAnyAuthority('workflow:start', 'workflow:create')")
    public ApiResponse<WorkflowInstanceResponse> start(@Valid @RequestBody WorkflowStartRequest request) {
        log.info("发起工作流: title={}", request.title());
        WorkflowInstanceResponse response = instanceService.start(request);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{instanceId}")
    @Operation(summary = "查询工作流详情")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<WorkflowDetailResponse> getDetail(@PathVariable String instanceId) {
        WorkflowDetailResponse response = instanceService.getDetail(instanceId);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{instanceId}/draft")
    @Operation(summary = "查询工作流草稿详情")
    @PreAuthorize("hasAnyAuthority('workflow:draft', 'workflow:create')")
    public ApiResponse<WorkflowDraftDetailResponse> getDraftDetail(@PathVariable String instanceId) {
        WorkflowDraftDetailResponse response = instanceService.getDraftDetail(instanceId);
        return ApiResponse.ok(response);
    }

    @PutMapping("/{instanceId}/draft")
    @Operation(summary = "更新工作流草稿")
    @OperationLog(module = "工作流管理", type = OperationType.UPDATE, description = "更新工作流草稿 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:draft', 'workflow:create')")
    public ApiResponse<WorkflowInstanceResponse> updateDraft(
            @PathVariable String instanceId,
            @Valid @RequestBody WorkflowStartRequest request) {
        WorkflowInstanceResponse response = instanceService.updateDraft(instanceId, request);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{instanceId}/draft")
    @Operation(summary = "删除工作流草稿")
    @OperationLog(module = "工作流管理", type = OperationType.DELETE, description = "删除工作流草稿 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:draft', 'workflow:create')")
    public ApiResponse<Void> deleteDraft(@PathVariable String instanceId) {
        instanceService.deleteDraft(instanceId);
        return ApiResponse.ok();
    }

    @GetMapping("/my")
    @Operation(summary = "查询我发起的工作流")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowInstanceResponse>> getMyWorkflows(
            @RequestParam(required = false) String status) {
        List<WorkflowInstanceResponse> responses = instanceService.getMyWorkflows(status);
        return ApiResponse.ok(responses);
    }

    @GetMapping("/pending")
    @Operation(summary = "查询待我审批的工作流")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowInstanceResponse>> getPendingApprovals() {
        List<WorkflowInstanceResponse> responses = instanceService.getPendingApprovals();
        return ApiResponse.ok(responses);
    }

    @GetMapping("/pending/count")
    @Operation(summary = "统计待审批数量")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> countPendingApprovals() {
        long count = instanceService.countPendingApprovals();
        return ApiResponse.ok(count);
    }

    @PostMapping("/{instanceId}/approve")
    @Operation(summary = "同意审批")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "审批成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "工作流不在运行状态或已审批"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无审批权限"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "工作流实例不存在")
    })
    @OperationLog(module = "工作流管理", type = OperationType.UPDATE, description = "同意审批 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:approve')")
    public ApiResponse<WorkflowInstanceResponse> approve(
            @PathVariable String instanceId,
            @Valid @RequestBody ApprovalActionRequest request) {
        log.info("同意审批: instanceId={}", instanceId);
        WorkflowInstanceResponse response = instanceService.approve(instanceId, request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/{instanceId}/reject")
    @Operation(summary = "拒绝审批")
    @OperationLog(module = "工作流管理", type = OperationType.UPDATE, description = "拒绝审批 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:reject', 'workflow:approve')")
    public ApiResponse<WorkflowInstanceResponse> reject(
            @PathVariable String instanceId,
            @Valid @RequestBody ApprovalActionRequest request) {
        log.info("拒绝审批: instanceId={}", instanceId);
        WorkflowInstanceResponse response = instanceService.reject(instanceId, request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/{instanceId}/cancel")
    @Operation(summary = "取消工作流")
    @OperationLog(module = "工作流管理", type = OperationType.DELETE, description = "取消工作流 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:cancel', 'workflow:create')")
    public ApiResponse<Void> cancel(@PathVariable String instanceId) {
        log.info("取消工作流: instanceId={}", instanceId);
        instanceService.cancel(instanceId);
        return ApiResponse.ok();
    }

    @PostMapping("/{instanceId}/withdraw")
    @Operation(summary = "撤回工作流")
    @OperationLog(module = "工作流管理", type = OperationType.UPDATE, description = "撤回工作流 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:withdraw', 'workflow:create')")
    public ApiResponse<Void> withdraw(@PathVariable String instanceId) {
        log.info("撤回工作流: instanceId={}", instanceId);
        instanceService.withdraw(instanceId);
        return ApiResponse.ok();
    }

    @PostMapping("/{instanceId}/rollback")
    @Operation(summary = "回退工作流")
    @OperationLog(module = "工作流管理", type = OperationType.UPDATE, description = "回退工作流 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:rollback', 'workflow:approve')")
    public ApiResponse<WorkflowInstanceResponse> rollback(
            @PathVariable String instanceId,
            @Valid @RequestBody ApprovalActionRequest request) {
        log.info("回退工作流: instanceId={}", instanceId);
        WorkflowInstanceResponse response = instanceService.rollback(instanceId, request);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{instanceId}/rollbackable-nodes")
    @Operation(summary = "获取可回退的节点列表")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowNodeResponse>> getRollbackableNodes(@PathVariable String instanceId) {
        List<WorkflowNodeResponse> responses = instanceService.getRollbackableNodes(instanceId);
        return ApiResponse.ok(responses);
    }

    @GetMapping("/{instanceId}/approvals")
    @Operation(summary = "查询审批记录")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowApprovalResponse>> getApprovals(@PathVariable String instanceId) {
        List<WorkflowApprovalResponse> responses = instanceService.getApprovals(instanceId);
        return ApiResponse.ok(responses);
    }

    @PostMapping("/{instanceId}/add-sign")
    @Operation(summary = "加签/转办")
    @OperationLog(module = "工作流管理", type = OperationType.UPDATE, description = "加签/转办 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:add-sign', 'workflow:approve')")
    public ApiResponse<WorkflowAddSignResponse> addSign(
            @PathVariable String instanceId,
            @Valid @RequestBody AddSignRequest request) {
        log.info("加签/转办: instanceId={}", instanceId);
        WorkflowAddSignResponse response = instanceService.addSign(instanceId, request);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{instanceId}/add-sign-records")
    @Operation(summary = "查询加签记录")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowAddSignResponse>> getAddSignRecords(@PathVariable String instanceId) {
        List<WorkflowAddSignResponse> responses = instanceService.getAddSignRecords(instanceId);
        return ApiResponse.ok(responses);
    }
}
