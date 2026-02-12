package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.DictCreateReq;
import com.adminplus.pojo.dto.req.DictUpdateReq;
import com.adminplus.pojo.dto.resp.DictItemResp;
import com.adminplus.pojo.dto.resp.DictResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
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
@RequestMapping("/v1/sys/dicts")
@RequiredArgsConstructor
@Tag(name = "字典管理", description = "字典增删改查")
public class DictController {

    private final DictService dictService;

    @GetMapping
    @Operation(summary = "分页查询字典列表")
    @PreAuthorize("hasAuthority('dict:list')")
    public ApiResponse<PageResultResp<DictResp>> getDictList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        PageResultResp<DictResp> result = dictService.getDictList(page, size, keyword);
        return ApiResponse.ok(result);
    }

    @GetMapping("/type/{dictType}")
    @Operation(summary = "根据字典类型查询")
    @PreAuthorize("hasAuthority('dict:query')")
    public ApiResponse<DictResp> getDictByType(@PathVariable String dictType) {
        DictResp dict = dictService.getDictByType(dictType);
        return ApiResponse.ok(dict);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询字典")
    @PreAuthorize("hasAuthority('dict:query')")
    public ApiResponse<DictResp> getDictById(@PathVariable String id) {
        DictResp dict = dictService.getDictById(id);
        return ApiResponse.ok(dict);
    }

    @PostMapping
    @Operation(summary = "创建字典")
    @PreAuthorize("hasAuthority('dict:add')")
    public ApiResponse<DictResp> createDict(@Valid @RequestBody DictCreateReq req) {
        DictResp dict = dictService.createDict(req);
        return ApiResponse.ok(dict);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新字典")
    @PreAuthorize("hasAuthority('dict:edit')")
    public ApiResponse<DictResp> updateDict(@PathVariable String id, @Valid @RequestBody DictUpdateReq req) {
        DictResp dict = dictService.updateDict(id, req);
        return ApiResponse.ok(dict);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除字典")
    @PreAuthorize("hasAuthority('dict:delete')")
    public ApiResponse<Void> deleteDict(@PathVariable String id) {
        dictService.deleteDict(id);
        return ApiResponse.ok();
    }

    @GetMapping("/type/{dictType}/items")
    @Operation(summary = "根据字典类型查询字典项")
    @PreAuthorize("hasAuthority('dict:query')")
    public ApiResponse<List<DictItemResp>> getDictItemsByType(@PathVariable String dictType) {
        List<DictItemResp> items = dictService.getDictItemsByType(dictType);
        return ApiResponse.ok(items);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新字典状态")
    @PreAuthorize("hasAuthority('dict:edit')")
    public ApiResponse<Void> updateDictStatus(
            @PathVariable String id,
            @RequestParam Integer status
    ) {
        dictService.updateDictStatus(id, status);
        return ApiResponse.ok();
    }
}