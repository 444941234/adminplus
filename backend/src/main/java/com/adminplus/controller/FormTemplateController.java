package com.adminplus.controller;
import com.adminplus.enums.OperationType;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.exception.BizException;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.request.FormTemplateRequest;
import com.adminplus.pojo.dto.response.FormTemplateResponse;
import com.adminplus.service.FormTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表单模板控制器
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Slf4j
@RestController
@RequestMapping("/v1/form-templates")
@RequiredArgsConstructor
@Tag(name = "表单模板管理", description = "表单模板增删改查")
public class FormTemplateController {

    private final FormTemplateService formTemplateService;

    /**
     * 获取所有表单模板
     */
    @GetMapping
    @Operation(summary = "获取所有表单模板")
    @OperationLog(module = "表单模板管理", type = OperationType.QUERY, description = "查询所有表单模板")
    @PreAuthorize("hasAuthority('workflow:form:list')")
    public ApiResponse<List<FormTemplateResponse>> getAllTemplates() {
        List<FormTemplateResponse> responses = formTemplateService.getAllTemplates();
        return ApiResponse.ok(responses);
    }

    /**
     * 获取启用的表单模板
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('workflow:form:view')")
    public ApiResponse<List<FormTemplateResponse>> getEnabledTemplates() {
        List<FormTemplateResponse> responses = formTemplateService.getEnabledTemplates();
        return ApiResponse.ok(responses);
    }

    /**
     * 根据分类获取表单模板
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('workflow:form:view')")
    public ApiResponse<List<FormTemplateResponse>> getTemplatesByCategory(@PathVariable String category) {
        List<FormTemplateResponse> responses = formTemplateService.getTemplatesByCategory(category);
        return ApiResponse.ok(responses);
    }

    /**
     * 根据ID获取表单模板
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('workflow:form:view')")
    public ApiResponse<FormTemplateResponse> getTemplateById(@PathVariable String id) {
        FormTemplateResponse response = formTemplateService.getTemplateById(id);
        if (response == null) {
            throw new BizException("表单模板不存在");
        }
        return ApiResponse.ok(response);
    }

    /**
     * 根据标识获取表单模板
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('workflow:form:view')")
    public ApiResponse<FormTemplateResponse> getTemplateByCode(@PathVariable String code) {
        FormTemplateResponse response = formTemplateService.getTemplateByCode(code);
        if (response == null) {
            throw new BizException("表单模板不存在");
        }
        return ApiResponse.ok(response);
    }

    /**
     * 创建表单模板
     */
    @PostMapping
    @Operation(summary = "创建表单模板")
    @OperationLog(module = "表单模板管理", type = OperationType.CREATE, description = "创建表单模板 {#request.templateCode()}")
    @PreAuthorize("hasAuthority('workflow:form:create')")
    public ApiResponse<FormTemplateResponse> createTemplate(@Valid @RequestBody FormTemplateRequest request) {

        FormTemplateResponse response = formTemplateService.createTemplate(request);
        return ApiResponse.ok(response);

    }

    /**
     * 更新表单模板
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新表单模板")
    @OperationLog(module = "表单模板管理", type = OperationType.UPDATE, description = "更新表单模板 {#id}")
    @PreAuthorize("hasAuthority('workflow:form:update')")
    public ApiResponse<FormTemplateResponse> updateTemplate(
            @PathVariable String id,
            @Valid @RequestBody FormTemplateRequest request) {
        FormTemplateResponse response = formTemplateService.updateTemplate(id, request);
        return ApiResponse.ok(response);
    }

    /**
     * 删除表单模板
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除表单模板")
    @OperationLog(module = "表单模板管理", type = OperationType.DELETE, description = "删除表单模板 {#id}")
    @PreAuthorize("hasAuthority('workflow:form:delete')")
    public ApiResponse<Void> deleteTemplate(@PathVariable String id) {
        formTemplateService.deleteTemplate(id);
        return ApiResponse.ok();
    }

    /**
     * 检查标识是否存在
     */
    @GetMapping("/exists/{code}")
    @PreAuthorize("hasAuthority('workflow:form:view')")
    public ApiResponse<Boolean> checkCodeExists(@PathVariable String code) {
        boolean exists = formTemplateService.existsByCode(code);
        return ApiResponse.ok(exists);
    }
}
