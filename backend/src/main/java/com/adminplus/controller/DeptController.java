package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.request.DeptCreateRequest;
import com.adminplus.pojo.dto.request.DeptUpdateRequest;
import com.adminplus.pojo.dto.response.DeptResponse;
import com.adminplus.service.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门控制器
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
@Slf4j
@RestController
@RequestMapping("/v1/sys/depts")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门增删改查")
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/tree")
    @Operation(summary = "查询部门树形列表")
    @OperationLog(module = "部门管理", operationType = 1, description = "查询部门树形列表")
    @PreAuthorize("hasAuthority('dept:list')")
    public ApiResponse<List<DeptResponse>> getDeptTree() {
        List<DeptResponse> responses = deptService.getDeptTree();
        return ApiResponse.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询部门")
    @OperationLog(module = "部门管理", operationType = 1, description = "查询部门详情 {#id}")
    @PreAuthorize("hasAuthority('dept:query')")
    public ApiResponse<DeptResponse> getDeptById(@PathVariable String id) {
        DeptResponse response = deptService.getDeptById(id);
        return ApiResponse.ok(response);
    }

    @PostMapping
    @Operation(summary = "创建部门")
    @OperationLog(module = "部门管理", operationType = 2, description = "新增部门 {#request.name}")
    @PreAuthorize("hasAuthority('dept:add')")
    public ApiResponse<DeptResponse> createDept(@Valid @RequestBody DeptCreateRequest request) {
        DeptResponse response = deptService.createDept(request);
        return ApiResponse.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门")
    @OperationLog(module = "部门管理", operationType = 3, description = "修改部门 {#id}")
    @PreAuthorize("hasAuthority('dept:edit')")
    public ApiResponse<DeptResponse> updateDept(@PathVariable String id, @Valid @RequestBody DeptUpdateRequest request) {
        DeptResponse response = deptService.updateDept(id, request);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    @OperationLog(module = "部门管理", operationType = 4, description = "删除部门 {#id}")
    @PreAuthorize("hasAuthority('dept:delete')")
    public ApiResponse<Void> deleteDept(@PathVariable String id) {
        deptService.deleteDept(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新部门状态")
    @OperationLog(module = "部门管理", operationType = 3, description = "修改部门状态 {#id}")
    @PreAuthorize("hasAuthority('dept:edit')")
    public ApiResponse<Void> updateDeptStatus(
            @PathVariable String id,
            @RequestParam Integer status
    ) {
        deptService.updateDeptStatus(id, status);
        return ApiResponse.ok();
    }
}