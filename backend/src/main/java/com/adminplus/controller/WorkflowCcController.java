package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.response.WorkflowCcResponse;
import com.adminplus.service.WorkflowCcService;
import com.adminplus.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流抄送控制器
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
@RestController
@RequestMapping(value = "/workflow/cc")
@RequiredArgsConstructor
@Tag(name = "工作流抄送", description = "工作流抄送记录管理")
public class WorkflowCcController {

    private final WorkflowCcService ccService;

    @GetMapping("/my")
    @Operation(summary = "获取我的抄送记录")
    @PreAuthorize("hasAnyAuthority('workflow:cc:list')")
    public ApiResponse<List<WorkflowCcResponse>> getMyCcRecords() {
        String userId = SecurityUtils.getCurrentUserId();
        List<WorkflowCcResponse> responses = ccService.getUserCcRecords(userId);
        return ApiResponse.ok(responses);
    }

    @GetMapping("/my/unread")
    @Operation(summary = "获取我的未读抄送记录")
    @PreAuthorize("hasAnyAuthority('workflow:cc:list')")
    public ApiResponse<List<WorkflowCcResponse>> getMyUnreadCcRecords() {
        String userId = SecurityUtils.getCurrentUserId();
        List<WorkflowCcResponse> responses = ccService.getUnreadCcRecords(userId);
        return ApiResponse.ok(responses);
    }

    @GetMapping("/my/unread/count")
    @Operation(summary = "统计我的未读抄送数量")
    @PreAuthorize("hasAnyAuthority('workflow:cc:list')")
    public ApiResponse<Long> countMyUnreadCcRecords() {
        String userId = SecurityUtils.getCurrentUserId();
        long count = ccService.countUnreadCcRecords(userId);
        return ApiResponse.ok(count);
    }

    @GetMapping("/instance/{instanceId}")
    @Operation(summary = "获取工作流实例的抄送记录")
    @PreAuthorize("hasAnyAuthority('workflow:cc:list')")
    public ApiResponse<List<WorkflowCcResponse>> getInstanceCcRecords(@PathVariable String instanceId) {
        List<WorkflowCcResponse> responses = ccService.getInstanceCcRecords(instanceId);
        return ApiResponse.ok(responses);
    }

    @PutMapping("/{ccId}/read")
    @Operation(summary = "标记抄送记录为已读")
    @PreAuthorize("hasAnyAuthority('workflow:cc:read', 'workflow:cc:list')")
    public ApiResponse<Void> markAsRead(@PathVariable String ccId) {
        ccService.markAsRead(ccId);
        return ApiResponse.ok();
    }

    @PutMapping("/read-batch")
    @Operation(summary = "批量标记抄送记录为已读")
    @PreAuthorize("hasAnyAuthority('workflow:cc:read', 'workflow:cc:list')")
    public ApiResponse<Void> markAsReadBatch(@RequestBody List<String> ccIds) {
        ccService.markAsReadBatch(ccIds);
        return ApiResponse.ok();
    }
}
