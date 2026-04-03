package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.AddSignReq;
import com.adminplus.pojo.dto.req.ApprovalActionReq;
import com.adminplus.pojo.dto.req.WorkflowStartReq;
import com.adminplus.pojo.dto.resp.WorkflowAddSignResp;
import com.adminplus.pojo.dto.resp.WorkflowApprovalResp;
import com.adminplus.pojo.dto.resp.WorkflowDetailResp;
import com.adminplus.pojo.dto.resp.WorkflowDraftDetailResp;
import com.adminplus.pojo.dto.resp.WorkflowInstanceResp;
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
@RequestMapping("/v1/workflow/instances")
@RequiredArgsConstructor
@Tag(name = "工作流实例", description = "工作流实例管理、审批操作")
public class WorkflowInstanceController {

    private final WorkflowInstanceService instanceService;

    @PostMapping("/draft")
    @Operation(summary = "创建工作流草稿")
    @OperationLog(module = "工作流管理", operationType = 2, description = "创建工作流草稿 {#req.title}")
    @PreAuthorize("hasAnyAuthority('workflow:draft', 'workflow:create')")
    public ApiResponse<WorkflowInstanceResp> createDraft(@Valid @RequestBody WorkflowStartReq req) {
        log.info("创建工作流草稿: title={}", req.title());
        WorkflowInstanceResp resp = instanceService.createDraft(req);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/{instanceId}/submit")
    @Operation(summary = "提交工作流")
    @OperationLog(module = "工作流管理", operationType = 2, description = "提交工作流 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:start', 'workflow:draft', 'workflow:create')")
    public ApiResponse<WorkflowInstanceResp> submit(
            @PathVariable String instanceId,
            @RequestBody(required = false) WorkflowStartReq req) {
        log.info("提交工作流: instanceId={}", instanceId);
        WorkflowInstanceResp resp = instanceService.submit(instanceId, req);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/start")
    @Operation(summary = "发起并提交工作流")
    @OperationLog(module = "工作流管理", operationType = 2, description = "发起工作流 {#req.title}")
    @PreAuthorize("hasAnyAuthority('workflow:start', 'workflow:create')")
    public ApiResponse<WorkflowInstanceResp> start(@Valid @RequestBody WorkflowStartReq req) {
        log.info("发起工作流: title={}", req.title());
        WorkflowInstanceResp resp = instanceService.start(req);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/{instanceId}")
    @Operation(summary = "查询工作流详情")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权限查看该工作流"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "工作流实例不存在")
    })
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<WorkflowDetailResp> getDetail(@PathVariable String instanceId) {
        WorkflowDetailResp resp = instanceService.getDetail(instanceId);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/{instanceId}/draft")
    @Operation(summary = "查询工作流草稿详情")
    @PreAuthorize("hasAnyAuthority('workflow:draft', 'workflow:create')")
    public ApiResponse<WorkflowDraftDetailResp> getDraftDetail(@PathVariable String instanceId) {
        WorkflowDraftDetailResp resp = instanceService.getDraftDetail(instanceId);
        return ApiResponse.ok(resp);
    }

    @PutMapping("/{instanceId}/draft")
    @Operation(summary = "更新工作流草稿")
    @OperationLog(module = "工作流管理", operationType = 3, description = "更新工作流草稿 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:draft', 'workflow:create')")
    public ApiResponse<WorkflowInstanceResp> updateDraft(
            @PathVariable String instanceId,
            @Valid @RequestBody WorkflowStartReq req) {
        WorkflowInstanceResp resp = instanceService.updateDraft(instanceId, req);
        return ApiResponse.ok(resp);
    }

    @DeleteMapping("/{instanceId}/draft")
    @Operation(summary = "删除工作流草稿")
    @OperationLog(module = "工作流管理", operationType = 4, description = "删除工作流草稿 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:draft', 'workflow:create')")
    public ApiResponse<Void> deleteDraft(@PathVariable String instanceId) {
        instanceService.deleteDraft(instanceId);
        return ApiResponse.ok();
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
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "审批成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "工作流不在运行状态或已审批"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无审批权限"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "工作流实例不存在")
    })
    @OperationLog(module = "工作流管理", operationType = 3, description = "同意审批 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:approve')")
    public ApiResponse<WorkflowInstanceResp> approve(
            @PathVariable String instanceId,
            @Valid @RequestBody ApprovalActionReq req) {
        log.info("同意审批: instanceId={}", instanceId);
        WorkflowInstanceResp resp = instanceService.approve(instanceId, req);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/{instanceId}/reject")
    @Operation(summary = "拒绝审批")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "拒绝成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "工作流不在运行状态或已审批"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无审批权限"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "工作流实例不存在")
    })
    @OperationLog(module = "工作流管理", operationType = 3, description = "拒绝审批 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:reject', 'workflow:approve')")
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
    @PreAuthorize("hasAnyAuthority('workflow:cancel', 'workflow:create')")
    public ApiResponse<Void> cancel(@PathVariable String instanceId) {
        log.info("取消工作流: instanceId={}", instanceId);
        instanceService.cancel(instanceId);
        return ApiResponse.ok();
    }

    @PostMapping("/{instanceId}/withdraw")
    @Operation(summary = "撤回工作流")
    @OperationLog(module = "工作流管理", operationType = 3, description = "撤回工作流 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:withdraw', 'workflow:create')")
    public ApiResponse<Void> withdraw(@PathVariable String instanceId) {
        log.info("撤回工作流: instanceId={}", instanceId);
        instanceService.withdraw(instanceId);
        return ApiResponse.ok();
    }

    @PostMapping("/{instanceId}/rollback")
    @Operation(summary = "回退工作流")
    @OperationLog(module = "工作流管理", operationType = 3, description = "回退工作流 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:rollback', 'workflow:approve')")
    public ApiResponse<WorkflowInstanceResp> rollback(
            @PathVariable String instanceId,
            @Valid @RequestBody ApprovalActionReq req) {
        log.info("回退工作流: instanceId={}", instanceId);
        WorkflowInstanceResp resp = instanceService.rollback(instanceId, req);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/{instanceId}/rollbackable-nodes")
    @Operation(summary = "获取可回退的节点列表")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<com.adminplus.pojo.dto.resp.WorkflowNodeResp>> getRollbackableNodes(@PathVariable String instanceId) {
        List<com.adminplus.pojo.dto.resp.WorkflowNodeResp> resp = instanceService.getRollbackableNodes(instanceId);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/{instanceId}/approvals")
    @Operation(summary = "查询审批记录")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowApprovalResp>> getApprovals(@PathVariable String instanceId) {
        List<WorkflowApprovalResp> resp = instanceService.getApprovals(instanceId);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/{instanceId}/add-sign")
    @Operation(summary = "加签/转办")
    @OperationLog(module = "工作流管理", operationType = 3, description = "加签/转办 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:add-sign', 'workflow:approve')")
    public ApiResponse<WorkflowAddSignResp> addSign(
            @PathVariable String instanceId,
            @Valid @RequestBody AddSignReq req) {
        log.info("加签/转办: instanceId={}", instanceId);
        WorkflowAddSignResp resp = instanceService.addSign(instanceId, req);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/{instanceId}/add-sign-records")
    @Operation(summary = "查询加签记录")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WorkflowAddSignResp>> getAddSignRecords(@PathVariable String instanceId) {
        List<WorkflowAddSignResp> resp = instanceService.getAddSignRecords(instanceId);
        return ApiResponse.ok(resp);
    }
}
