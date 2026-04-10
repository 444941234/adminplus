package com.adminplus.controller;
import com.adminplus.enums.OperationType;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.request.UrgeActionRequest;
import com.adminplus.pojo.dto.response.WorkflowUrgeResponse;
import com.adminplus.service.WorkflowUrgeService;
import com.adminplus.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流催办控制器
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
@RestController
@RequestMapping("/v1/workflow/urge")
@RequiredArgsConstructor
@Tag(name = "工作流催办", description = "工作流催办管理")
public class WorkflowUrgeController {

    private final WorkflowUrgeService urgeService;

    @PostMapping("/{instanceId}")
    @Operation(summary = "催办工作流")
    @OperationLog(module = "工作流管理", type = OperationType.UPDATE, description = "催办工作流 {#instanceId}")
    @PreAuthorize("hasAnyAuthority('workflow:urge', 'workflow:create')")
    public ApiResponse<Void> urgeWorkflow(
            @PathVariable String instanceId,
            @Valid @RequestBody UrgeActionRequest request) {
        log.info("催办工作流: instanceId={}", instanceId);
        urgeService.urgeWorkflow(instanceId, request);
        return ApiResponse.ok();
    }

    @GetMapping("/received")
    @Operation(summary = "获取收到的催办记录")
    @PreAuthorize("hasAnyAuthority('workflow:urge:list', 'workflow:urge')")
    public ApiResponse<List<WorkflowUrgeResponse>> getReceivedUrgeRecords() {
        String userId = SecurityUtils.getCurrentUserId();
        List<WorkflowUrgeResponse> responses = urgeService.getReceivedUrgeRecords(userId);
        return ApiResponse.ok(responses);
    }

    @GetMapping("/sent")
    @Operation(summary = "获取发送的催办记录")
    @PreAuthorize("hasAnyAuthority('workflow:urge:list', 'workflow:urge')")
    public ApiResponse<List<WorkflowUrgeResponse>> getSentUrgeRecords() {
        String userId = SecurityUtils.getCurrentUserId();
        List<WorkflowUrgeResponse> responses = urgeService.getSentUrgeRecords(userId);
        return ApiResponse.ok(responses);
    }

    @GetMapping("/unread")
    @Operation(summary = "获取未读催办记录")
    @PreAuthorize("hasAnyAuthority('workflow:urge:list', 'workflow:urge')")
    public ApiResponse<List<WorkflowUrgeResponse>> getUnreadUrgeRecords() {
        String userId = SecurityUtils.getCurrentUserId();
        List<WorkflowUrgeResponse> responses = urgeService.getUnreadUrgeRecords(userId);
        return ApiResponse.ok(responses);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "统计未读催办数量")
    @PreAuthorize("hasAnyAuthority('workflow:urge:list', 'workflow:urge')")
    public ApiResponse<Long> countUnreadUrgeRecords() {
        String userId = SecurityUtils.getCurrentUserId();
        long count = urgeService.countUnreadUrgeRecords(userId);
        return ApiResponse.ok(count);
    }

    @GetMapping("/instance/{instanceId}")
    @Operation(summary = "获取工作流实例的催办记录")
    @PreAuthorize("hasAnyAuthority('workflow:urge:list', 'workflow:urge')")
    public ApiResponse<List<WorkflowUrgeResponse>> getInstanceUrgeRecords(@PathVariable String instanceId) {
        List<WorkflowUrgeResponse> responses = urgeService.getInstanceUrgeRecords(instanceId);
        return ApiResponse.ok(responses);
    }

    @PutMapping("/{urgeId}/read")
    @Operation(summary = "标记催办记录为已读")
    @PreAuthorize("hasAnyAuthority('workflow:urge:read', 'workflow:urge:list', 'workflow:urge')")
    public ApiResponse<Void> markAsRead(@PathVariable String urgeId) {
        urgeService.markAsRead(urgeId);
        return ApiResponse.ok();
    }

    @PutMapping("/read-batch")
    @Operation(summary = "批量标记催办记录为已读")
    @PreAuthorize("hasAnyAuthority('workflow:urge:read', 'workflow:urge:list', 'workflow:urge')")
    public ApiResponse<Void> markAsReadBatch(@RequestBody List<String> urgeIds) {
        urgeService.markAsReadBatch(urgeIds);
        return ApiResponse.ok();
    }
}
