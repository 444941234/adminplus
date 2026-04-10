package com.adminplus.controller;
import com.adminplus.enums.OperationType;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.query.LogQuery;
import com.adminplus.pojo.dto.response.LogPageResponse;
import com.adminplus.pojo.dto.response.LogStatisticsResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.service.LogExportService;
import com.adminplus.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 日志管理控制器
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@RestController
@RequestMapping(value = "/sys/logs")
@RequiredArgsConstructor
@Tag(name = "日志管理", description = "操作日志查询和删除")
public class LogController {

    private final LogService logService;
    private final LogExportService logExportService;

    @GetMapping
    @Operation(summary = "分页查询日志列表")
    @PreAuthorize("hasAuthority('log:query')")
    public ApiResponse<PageResultResponse<LogPageResponse>> getLogList(LogQuery query) {
        PageResultResponse<LogPageResponse> response = logService.getLogList(query);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询日志详情")
    @PreAuthorize("hasAuthority('log:query')")
    public ApiResponse<LogPageResponse> getLogById(@PathVariable String id) {
        LogPageResponse response = logService.getLogById(id);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除单条日志")
    @OperationLog(module = "日志管理", type = OperationType.DELETE, description = "删除单条日志 {#id}")
    @PreAuthorize("hasAuthority('log:delete')")
    public ApiResponse<Void> deleteLog(@PathVariable String id) {
        logService.deleteById(id);
        return ApiResponse.ok();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除日志")
    @OperationLog(module = "日志管理", type = OperationType.DELETE, description = "批量删除日志")
    @PreAuthorize("hasAuthority('log:delete')")
    public ApiResponse<Void> deleteLogsBatch(@RequestBody List<String> ids) {
        logService.deleteByIds(ids);
        return ApiResponse.ok();
    }

    @DeleteMapping("/condition")
    @Operation(summary = "根据条件删除日志")
    @OperationLog(module = "日志管理", type = OperationType.DELETE, description = "根据条件删除日志")
    @PreAuthorize("hasAuthority('log:delete')")
    public ApiResponse<Integer> deleteLogsByCondition(@Valid @RequestBody LogQuery query) {
        Integer count = logService.deleteByCondition(query);
        return ApiResponse.ok(count);
    }

    @PostMapping("/cleanup")
    @Operation(summary = "清理过期日志")
    @OperationLog(module = "日志管理", type = OperationType.DELETE, description = "清理过期日志")
    @PreAuthorize("hasAuthority('log:delete')")
    public ApiResponse<Integer> cleanupExpiredLogs() {
        Integer count = logService.cleanupExpiredLogs();
        return ApiResponse.ok(count);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取日志统计")
    @PreAuthorize("hasAuthority('log:query')")
    public ApiResponse<LogStatisticsResponse> getStatistics() {
        LogStatisticsResponse response = logService.getStatistics();
        return ApiResponse.ok(response);
    }

    @GetMapping("/export/excel")
    @Operation(summary = "导出日志为Excel")
    @OperationLog(module = "日志管理", type = OperationType.EXPORT, description = "导出日志为Excel")
    @PreAuthorize("hasAuthority('log:export')")
    public ResponseEntity<byte[]> exportToExcel(LogQuery query) throws IOException {
        return logExportService.exportToExcel(query);
    }

    @GetMapping("/export/csv")
    @Operation(summary = "导出日志为CSV")
    @OperationLog(module = "日志管理", type = OperationType.EXPORT, description = "导出日志为CSV")
    @PreAuthorize("hasAuthority('log:export')")
    public ResponseEntity<byte[]> exportToCsv(LogQuery query) throws IOException {
        return logExportService.exportToCsv(query);
    }
}
