package com.adminplus.controller;

import com.adminplus.dto.DictItemCreateReq;
import com.adminplus.dto.DictItemUpdateReq;
import com.adminplus.service.DictItemService;
import com.adminplus.utils.ApiResponse;
import com.adminplus.vo.DictItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 字典项控制器
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@RestController
@RequestMapping("/v1/sys/dicts/{dictId}/items")
@RequiredArgsConstructor
@Tag(name = "字典项管理", description = "字典项增删改查")
public class DictItemController {

    private final DictItemService dictItemService;

    @GetMapping
    @Operation(summary = "查询字典项列表")
    @PreAuthorize("hasAuthority('dictitem:list')")
    public ApiResponse<Map<String, Object>> getDictItems(@PathVariable String dictId) {
        List<DictItemVO> items = dictItemService.getDictItemsByDictId(dictId);
        return ApiResponse.ok(Map.of(
                "records", items,
                "total", items.size()
        ));
    }

    @GetMapping("/tree")
    @Operation(summary = "查询字典项树形结构")
    @PreAuthorize("hasAuthority('dictitem:list')")
    public ApiResponse<List<DictItemVO>> getDictItemTree(@PathVariable String dictId) {
        List<DictItemVO> tree = dictItemService.getDictItemTreeByDictId(dictId);
        return ApiResponse.ok(tree);
    }

    @GetMapping("/type/{dictType}")
    @Operation(summary = "根据字典类型查询字典项")
    @PreAuthorize("hasAuthority('dictitem:query')")
    public ApiResponse<List<DictItemVO>> getDictItemsByType(@PathVariable String dictType) {
        List<DictItemVO> items = dictItemService.getDictItemsByType(dictType);
        return ApiResponse.ok(items);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询字典项")
    @PreAuthorize("hasAuthority('dictitem:query')")
    public ApiResponse<DictItemVO> getDictItemById(@PathVariable String id) {
        DictItemVO item = dictItemService.getDictItemById(id);
        return ApiResponse.ok(item);
    }

    @PostMapping
    @Operation(summary = "创建字典项")
    @PreAuthorize("hasAuthority('dictitem:add')")
    public ApiResponse<DictItemVO> createDictItem(@PathVariable String dictId, @Valid @RequestBody DictItemCreateReq req) {
        DictItemVO item = dictItemService.createDictItem(req);
        return ApiResponse.ok(item);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新字典项")
    @PreAuthorize("hasAuthority('dictitem:edit')")
    public ApiResponse<DictItemVO> updateDictItem(@PathVariable String id, @Valid @RequestBody DictItemUpdateReq req) {
        DictItemVO item = dictItemService.updateDictItem(id, req);
        return ApiResponse.ok(item);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除字典项")
    @PreAuthorize("hasAuthority('dictitem:delete')")
    public ApiResponse<Void> deleteDictItem(@PathVariable String id) {
        dictItemService.deleteDictItem(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新字典项状态")
    @PreAuthorize("hasAuthority('dictitem:edit')")
    public ApiResponse<Void> updateDictItemStatus(
            @PathVariable String id,
            @RequestParam Integer status
    ) {
        dictItemService.updateDictItemStatus(id, status);
        return ApiResponse.ok();
    }
}