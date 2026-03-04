package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.LogQueryDTO;
import com.adminplus.pojo.dto.resp.LogPageVO;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日志管理控制器
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@RestController
@RequestMapping("/v1/sys/logs")
@RequiredArgsConstructor
@Tag(name = "日志管理", description = "操作日志查询和删除")
public class LogController {

    private final LogService logService;

    @GetMapping
    @Operation(summary = "分页查询日志列表")
    @PreAuthorize("hasAuthority('log:query')")
    public ApiResponse<PageResultResp<LogPageVO>> getLogList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer operationType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        var query = new LogQueryDTO();
        query.setPage(page);
        query.setSize(size);
        query.setUsername(username);
        query.setModule(module);
        query.setOperationType(operationType);
        query.setStatus(status);
        query.setStartTime(startTime);
        query.setEndTime(endTime);

        PageResultResp<LogPageVO> result = logService.findPage(query);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询日志详情")
    @PreAuthorize("hasAuthority('log:query')")
    public ApiResponse<LogPageVO> getLogById(@PathVariable String id) {
        LogPageVO log = logService.findById(id);
        return ApiResponse.ok(log);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除单条日志")
    @OperationLog(module = "日志管理", operationType = 4, description = "删除单条日志 {#id}")
    @PreAuthorize("hasAuthority('log:delete')")
    public ApiResponse<Void> deleteLog(@PathVariable String id) {
        logService.deleteById(id);
        return ApiResponse.ok();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除日志")
    @OperationLog(module = "日志管理", operationType = 4, description = "批量删除日志")
    @PreAuthorize("hasAuthority('log:delete')")
    public ApiResponse<Void> deleteLogsBatch(@RequestBody List<String> ids) {
        logService.deleteByIds(ids);
        return ApiResponse.ok();
    }
}