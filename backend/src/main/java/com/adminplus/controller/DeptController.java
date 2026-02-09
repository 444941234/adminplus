package com.adminplus.controller;

import com.adminplus.dto.DeptCreateReq;
import com.adminplus.dto.DeptUpdateReq;
import com.adminplus.service.DeptService;
import com.adminplus.utils.ApiResponse;
import com.adminplus.vo.DeptVO;
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
    @PreAuthorize("hasAuthority('dept:list')")
    public ApiResponse<List<DeptVO>> getDeptTree() {
        List<DeptVO> depts = deptService.getDeptTree();
        return ApiResponse.ok(depts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询部门")
    @PreAuthorize("hasAuthority('dept:query')")
    public ApiResponse<DeptVO> getDeptById(@PathVariable String id) {
        DeptVO dept = deptService.getDeptById(id);
        return ApiResponse.ok(dept);
    }

    @PostMapping
    @Operation(summary = "创建部门")
    @PreAuthorize("hasAuthority('dept:add')")
    public ApiResponse<DeptVO> createDept(@Valid @RequestBody DeptCreateReq req) {
        DeptVO dept = deptService.createDept(req);
        return ApiResponse.ok(dept);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门")
    @PreAuthorize("hasAuthority('dept:edit')")
    public ApiResponse<DeptVO> updateDept(@PathVariable String id, @Valid @RequestBody DeptUpdateReq req) {
        DeptVO dept = deptService.updateDept(id, req);
        return ApiResponse.ok(dept);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    @PreAuthorize("hasAuthority('dept:delete')")
    public ApiResponse<Void> deleteDept(@PathVariable String id) {
        deptService.deleteDept(id);
        return ApiResponse.ok();
    }
}