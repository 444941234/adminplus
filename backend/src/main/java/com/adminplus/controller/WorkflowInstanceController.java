package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.ApprovalActionReq;
import com.adminplus.pojo.dto.req.WorkflowStartReq;
import com.adminplus.pojo.dto.resp.WorkflowApprovalResp;
import com.adminplus.pojo.dto.resp.WorkflowDetailResp;
import com.adminplus.pojo.dto.resp.WorkflowInstanceResp;
import com.adminplus.service.WorkflowInstanceService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/v1/workflow/instances")
@RequiredArgsConstructor
@Tag(name = "工作流实例", description = "工作流实例管理、审批操作")
public class WorkflowInstanceController {

    private final WorkflowInstanceService instanceService;

    @PostMapping("/draft")
    @Operation(summary = "创建工作流草稿")
    @OperationLog(module = "工作流管理", operationType = 2, description = "创建工作流草稿 {#req.title}")
    @PreAuthorize("hasAuthority('workflow:create')")
    public ApiResponse<WorkflowInstanceResp> createDraft(@Valid @RequestBody WorkflowStartReq req) {
        log.info("创建工作流草稿: title={}", req.title());
        WorkflowInstanceResp resp = instanceService.createDraft(req);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/{instanceId}/submit")
    @Operation(summary = "提交工作流")
    @OperationLog(module = "工作流管理", operationType = 2, description = "提交工作流 {#instanceId}")
    @PreAuthorize("hasAuthority('workflow:create')")
    public ApiResponse<WorkflowInstanceResp> submit(@PathVariable String instanceId) {
        log.info("提交工作流: instanceId={}", instanceId);
        WorkflowInstanceResp resp = instanceService.submit(instanceId);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/start")
    @Operation(summary = "发起并提交工作流")
    @OperationLog(module = "工作流管理", operationType = 2, description = "发起工作流 {#req.title}")
    @PreAuthorize("hasAuthority('workflow:create')")
    public ApiResponse<WorkflowInstanceResp> start(@Valid @RequestBody WorkflowStartReq req) {
        log.info("发起工作流: title={}", req.title());
        WorkflowInstanceResp resp = instanceService.start(req);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/{instanceId}")
    @Operation(summary = "查询工作流详情")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<WorkflowDetailResp> getDetail(@PathVariable String instanceId) {
        WorkflowDetailResp resp = instanceService.getDetail(instanceId);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/my")
    @Operation(summary = "查询我发起的工作流")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowInstanceResp>> getMyWorkflows(
            @RequestParam(required = false) String status) {
        List<WorkflowInstanceResp> resp = instanceService.getMyWorkflows(status);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/pending")
    @Operation(summary = "查询待我审批的工作流")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowInstanceResp>> getPendingApprovals() {
        List<WorkflowInstanceResp> resp = instanceService.getPendingApprovals();
        return ApiResponse.ok(resp);
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
    @OperationLog(module = "工作流管理", operationType = 3, description = "同意审批 {#instanceId}")
    @PreAuthorize("hasAuthority('workflow:approve')")
    public ApiResponse<WorkflowInstanceResp> approve(
            @PathVariable String instanceId,
            @Valid @RequestBody ApprovalActionReq req) {
        log.info("同意审批: instanceId={}", instanceId);
        WorkflowInstanceResp resp = instanceService.approve(instanceId, req);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/{instanceId}/reject")
    @Operation(summary = "拒绝审批")
    @OperationLog(module = "工作流管理", operationType = 3, description = "拒绝审批 {#instanceId}")
    @PreAuthorize("hasAuthority('workflow:approve')")
    public ApiResponse<WorkflowInstanceResp> reject(
            @PathVariable String instanceId,
            @Valid @RequestBody ApprovalActionReq req) {
        log.info("拒绝审批: instanceId={}", instanceId);
        WorkflowInstanceResp resp = instanceService.reject(instanceId, req);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/{instanceId}/cancel")
    @Operation(summary = "取消工作流")
    @OperationLog(module = "工作流管理", operationType = 4, description = "取消工作流 {#instanceId}")
    @PreAuthorize("hasAuthority('workflow:create')")
    public ApiResponse<Void> cancel(@PathVariable String instanceId) {
        log.info("取消工作流: instanceId={}", instanceId);
        instanceService.cancel(instanceId);
        return ApiResponse.ok();
    }

    @PostMapping("/{instanceId}/withdraw")
    @Operation(summary = "撤回工作流")
    @OperationLog(module = "工作流管理", operationType = 3, description = "撤回工作流 {#instanceId}")
    @PreAuthorize("hasAuthority('workflow:create')")
    public ApiResponse<Void> withdraw(@PathVariable String instanceId) {
        log.info("撤回工作流: instanceId={}", instanceId);
        instanceService.withdraw(instanceId);
        return ApiResponse.ok();
    }

    @GetMapping("/{instanceId}/approvals")
    @Operation(summary = "查询审批记录")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowApprovalResp>> getApprovals(@PathVariable String instanceId) {
        List<WorkflowApprovalResp> resp = instanceService.getApprovals(instanceId);
        return ApiResponse.ok(resp);
    }
}
