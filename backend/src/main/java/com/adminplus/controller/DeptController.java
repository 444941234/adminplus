package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.DeptCreateReq;
import com.adminplus.pojo.dto.req.DeptUpdateReq;
import com.adminplus.pojo.dto.resp.DeptResp;
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
    public ApiResponse<List<DeptResp>> getDeptTree() {
        List<DeptResp> depts = deptService.getDeptTree();
        return ApiResponse.ok(depts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询部门")
    @OperationLog(module = "部门管理", operationType = 1, description = "查询部门详情 {#id}")
    @PreAuthorize("hasAuthority('dept:query')")
    public ApiResponse<DeptResp> getDeptById(@PathVariable String id) {
        DeptResp dept = deptService.getDeptById(id);
        return ApiResponse.ok(dept);
    }

    @PostMapping
    @Operation(summary = "创建部门")
    @OperationLog(module = "部门管理", operationType = 2, description = "新增部门 {#req.name}")
    @PreAuthorize("hasAuthority('dept:add')")
    public ApiResponse<DeptResp> createDept(@Valid @RequestBody DeptCreateReq req) {
        DeptResp dept = deptService.createDept(req);
        return ApiResponse.ok(dept);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门")
    @OperationLog(module = "部门管理", operationType = 3, description = "修改部门 {#id}")
    @PreAuthorize("hasAuthority('dept:edit')")
    public ApiResponse<DeptResp> updateDept(@PathVariable String id, @Valid @RequestBody DeptUpdateReq req) {
        DeptResp dept = deptService.updateDept(id, req);
        return ApiResponse.ok(dept);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    @OperationLog(module = "部门管理", operationType = 4, description = "删除部门 {#id}")
    @PreAuthorize("hasAuthority('dept:delete')")
    public ApiResponse<Void> deleteDept(@PathVariable String id) {
        deptService.deleteDept(id);
        return ApiResponse.ok();
    }
}