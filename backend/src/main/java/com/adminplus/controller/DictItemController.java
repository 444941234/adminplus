package com.adminplus.controller;
import com.adminplus.enums.OperationType;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.request.DictItemCreateRequest;
import com.adminplus.pojo.dto.request.DictItemUpdateRequest;
import com.adminplus.pojo.dto.response.DictItemResponse;
import com.adminplus.service.DictItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典项控制器
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@RestController
@RequestMapping(value = "/sys/dicts/{dictId}/items")
@RequiredArgsConstructor
@Tag(name = "字典项管理", description = "字典项增删改查")
public class DictItemController {

    private final DictItemService dictItemService;

    @GetMapping
    @Operation(summary = "查询字典项列表")
    @OperationLog(module = "字典项管理", type = OperationType.QUERY, description = "查询字典项列表 {#dictId}")
    @PreAuthorize("hasAuthority('dictitem:list')")
    public ApiResponse<List<DictItemResponse>> getDictItems(@PathVariable String dictId) {
        List<DictItemResponse> items = dictItemService.getDictItemsByDictId(dictId);
        return ApiResponse.ok(items);
    }

    @GetMapping("/tree")
    @Operation(summary = "查询字典项树形结构")
    @PreAuthorize("hasAuthority('dictitem:list')")
    public ApiResponse<List<DictItemResponse>> getDictItemTree(@PathVariable String dictId) {
        List<DictItemResponse> tree = dictItemService.getDictItemTreeByDictId(dictId);
        return ApiResponse.ok(tree);
    }

    @GetMapping("/type/{dictType}")
    @Operation(summary = "根据字典类型查询字典项")
    @PreAuthorize("hasAuthority('dictitem:query')")
    public ApiResponse<List<DictItemResponse>> getDictItemsByType(@PathVariable String dictType) {
        List<DictItemResponse> items = dictItemService.getDictItemsByType(dictType);
        return ApiResponse.ok(items);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询字典项")
    @OperationLog(module = "字典项管理", type = OperationType.QUERY, description = "查询字典项详情 {#id}")
    @PreAuthorize("hasAuthority('dictitem:query')")
    public ApiResponse<DictItemResponse> getDictItemById(@PathVariable String id) {
        DictItemResponse item = dictItemService.getDictItemById(id);
        return ApiResponse.ok(item);
    }

    @PostMapping
    @Operation(summary = "创建字典项")
    @OperationLog(module = "字典项管理", type = OperationType.CREATE, description = "新增字典项 {#request.label}")
    @PreAuthorize("hasAuthority('dictitem:add')")
    public ApiResponse<DictItemResponse> createDictItem(@PathVariable String dictId, @Valid @RequestBody DictItemCreateRequest request) {
        DictItemResponse item = dictItemService.createDictItem(request);
        return ApiResponse.ok(item);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新字典项")
    @OperationLog(module = "字典项管理", type = OperationType.UPDATE, description = "修改字典项 {#id}")
    @PreAuthorize("hasAuthority('dictitem:edit')")
    public ApiResponse<DictItemResponse> updateDictItem(@PathVariable String id, @Valid @RequestBody DictItemUpdateRequest request) {
        DictItemResponse item = dictItemService.updateDictItem(id, request);
        return ApiResponse.ok(item);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除字典项")
    @OperationLog(module = "字典项管理", type = OperationType.DELETE, description = "删除字典项 {#id}")
    @PreAuthorize("hasAuthority('dictitem:delete')")
    public ApiResponse<Void> deleteDictItem(@PathVariable String id) {
        dictItemService.deleteDictItem(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新字典项状态")
    @OperationLog(module = "字典项管理", type = OperationType.UPDATE, description = "修改字典项状态 {#id}")
    @PreAuthorize("hasAuthority('dictitem:edit')")
    public ApiResponse<Void> updateDictItemStatus(
            @PathVariable String id,
            @RequestParam Integer status
    ) {
        dictItemService.updateDictItemStatus(id, status);
        return ApiResponse.ok();
    }
}