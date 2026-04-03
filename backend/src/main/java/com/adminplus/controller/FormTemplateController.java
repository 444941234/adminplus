package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.FormTemplateReq;
import com.adminplus.pojo.dto.resp.FormTemplateResp;
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
    @OperationLog(module = "表单模板管理", operationType = 4, description = "查询所有表单模板")
    @PreAuthorize("hasAuthority('workflow:form:list')")
    public ApiResponse<List<FormTemplateResp>> getAllTemplates() {
        List<FormTemplateResp> templates = formTemplateService.getAllTemplates();
        return ApiResponse.ok(templates);
    }

    /**
     * 获取启用的表单模板
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('workflow:form:view')")
    public ApiResponse<List<FormTemplateResp>> getEnabledTemplates() {
        List<FormTemplateResp> templates = formTemplateService.getEnabledTemplates();
        return ApiResponse.ok(templates);
    }

    /**
     * 根据分类获取表单模板
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('workflow:form:view')")
    public ApiResponse<List<FormTemplateResp>> getTemplatesByCategory(@PathVariable String category) {
        List<FormTemplateResp> templates = formTemplateService.getTemplatesByCategory(category);
        return ApiResponse.ok(templates);
    }

    /**
     * 根据ID获取表单模板
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('workflow:form:view')")
    public ApiResponse<FormTemplateResp> getTemplateById(@PathVariable String id) {
        FormTemplateResp template = formTemplateService.getTemplateById(id);
        if (template == null) {
            return ApiResponse.fail(404, "表单模板不存在");
        }
        return ApiResponse.ok(template);
    }

    /**
     * 根据标识获取表单模板
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('workflow:form:view')")
    public ApiResponse<FormTemplateResp> getTemplateByCode(@PathVariable String code) {
        FormTemplateResp template = formTemplateService.getTemplateByCode(code);
        if (template == null) {
            return ApiResponse.fail(404, "表单模板不存在");
        }
        return ApiResponse.ok(template);
    }

    /**
     * 创建表单模板
     */
    @PostMapping
    @Operation(summary = "创建表单模板")
    @OperationLog(module = "表单模板管理", operationType = 1, description = "创建表单模板 {#req.templateCode()}")
    @PreAuthorize("hasAuthority('workflow:form:create')")
    public ApiResponse<FormTemplateResp> createTemplate(@Valid @RequestBody FormTemplateReq req) {
        try {
            FormTemplateResp template = formTemplateService.createTemplate(req);
            return ApiResponse.ok(template);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 更新表单模板
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新表单模板")
    @OperationLog(module = "表单模板管理", operationType = 2, description = "更新表单模板 {#id}")
    @PreAuthorize("hasAuthority('workflow:form:update')")
    public ApiResponse<FormTemplateResp> updateTemplate(
            @PathVariable String id,
            @Valid @RequestBody FormTemplateReq req) {
        try {
            FormTemplateResp template = formTemplateService.updateTemplate(id, req);
            return ApiResponse.ok(template);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(404, e.getMessage());
        }
    }

    /**
     * 删除表单模板
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除表单模板")
    @OperationLog(module = "表单模板管理", operationType = 3, description = "删除表单模板 {#id}")
    @PreAuthorize("hasAuthority('workflow:form:delete')")
    public ApiResponse<Void> deleteTemplate(@PathVariable String id) {
        try {
            formTemplateService.deleteTemplate(id);
            return ApiResponse.ok();
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(404, e.getMessage());
        }
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
