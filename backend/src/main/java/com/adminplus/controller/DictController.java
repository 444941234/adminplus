package com.adminplus.controller;
import com.adminplus.enums.OperationType;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.query.DictQuery;
import com.adminplus.pojo.dto.request.DictCreateRequest;
import com.adminplus.pojo.dto.request.DictUpdateRequest;
import com.adminplus.pojo.dto.response.DictItemResponse;
import com.adminplus.pojo.dto.response.DictResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典控制器
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@RestController
@RequestMapping(value = "/sys/dicts")
@RequiredArgsConstructor
@Tag(name = "字典管理", description = "字典增删改查")
public class DictController {

    private final DictService dictService;

    @GetMapping
    @Operation(summary = "分页查询字典列表")
    @OperationLog(module = "字典管理", type = OperationType.QUERY, description = "查询字典列表")
    @PreAuthorize("hasAuthority('dict:list')")
    public ApiResponse<PageResultResponse<DictResponse>> getDictList(DictQuery query) {
        PageResultResponse<DictResponse> response = dictService.getDictList(query);
        return ApiResponse.ok(response);
    }

    @GetMapping("/type/{dictType}")
    @Operation(summary = "根据字典类型查询")
    @OperationLog(module = "字典管理", type = OperationType.QUERY, description = "查询字典类型 {#dictType}")
    @PreAuthorize("hasAuthority('dict:query')")
    public ApiResponse<DictResponse> getDictByType(@PathVariable String dictType) {
        DictResponse response = dictService.getDictByType(dictType);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询字典")
    @OperationLog(module = "字典管理", type = OperationType.QUERY, description = "查询字典详情 {#id}")
    @PreAuthorize("hasAuthority('dict:query')")
    public ApiResponse<DictResponse> getDictById(@PathVariable String id) {
        DictResponse response = dictService.getDictById(id);
        return ApiResponse.ok(response);
    }

    @PostMapping
    @Operation(summary = "创建字典")
    @OperationLog(module = "字典管理", type = OperationType.CREATE, description = "新增字典 {#request.dictName}")
    @PreAuthorize("hasAuthority('dict:add')")
    public ApiResponse<DictResponse> createDict(@Valid @RequestBody DictCreateRequest request) {
        DictResponse response = dictService.createDict(request);
        return ApiResponse.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新字典")
    @OperationLog(module = "字典管理", type = OperationType.UPDATE, description = "修改字典 {#id}")
    @PreAuthorize("hasAuthority('dict:edit')")
    public ApiResponse<DictResponse> updateDict(@PathVariable String id, @Valid @RequestBody DictUpdateRequest request) {
        DictResponse response = dictService.updateDict(id, request);
        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除字典")
    @OperationLog(module = "字典管理", type = OperationType.DELETE, description = "删除字典 {#id}")
    @PreAuthorize("hasAuthority('dict:delete')")
    public ApiResponse<Void> deleteDict(@PathVariable String id) {
        dictService.deleteDict(id);
        return ApiResponse.ok();
    }

    @GetMapping("/type/{dictType}/items")
    @Operation(summary = "根据字典类型查询字典项")
    @PreAuthorize("hasAuthority('dict:query')")
    public ApiResponse<List<DictItemResponse>> getDictItemsByType(@PathVariable String dictType) {
        List<DictItemResponse> responses = dictService.getDictItemsByType(dictType);
        return ApiResponse.ok(responses);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新字典状态")
    @OperationLog(module = "字典管理", type = OperationType.UPDATE, description = "修改字典状态 {#id}")
    @PreAuthorize("hasAuthority('dict:edit')")
    public ApiResponse<Void> updateDictStatus(
            @PathVariable String id,
            @RequestParam Integer status
    ) {
        dictService.updateDictStatus(id, status);
        return ApiResponse.ok();
    }
}