package com.adminplus.service.workflow.impl;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import com.adminplus.enums.WorkflowStatus;
import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.WorkflowDraftDetailResponse;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.WorkflowDefinitionEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.WorkflowDefinitionRepository;
import com.adminplus.repository.WorkflowInstanceRepository;
import com.adminplus.service.workflow.WorkflowDraftService;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.ServiceAssert;
import com.adminplus.utils.XssUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

/**
 * 工作流草稿服务实现
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowDraftServiceImpl implements WorkflowDraftService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    private final UserRepository userRepository;
    private final JsonMapper objectMapper;
    private final ConversionService conversionService;

    @Override
    @Transactional
    public WorkflowInstanceResponse createDraft(WorkflowStartRequest request) {
        String userId = getCurrentUserId();
        log.info("创建工作流草稿: userId={}, definitionId={}, title={}", userId, request.definitionId(), request.title());

        WorkflowDefinitionEntity definition = EntityHelper.findByIdOrThrow(
            definitionRepository::findById, request.definitionId(), "工作流定义不存在");

        UserEntity user = EntityHelper.findByIdOrThrow(
            userRepository::findById, userId, "用户不存在");

        WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        instance.setDefinitionId(request.definitionId());
        instance.setDefinitionName(definition.getDefinitionName());
        instance.setUserId(userId);
        instance.setUserName(user.getNickname());
        instance.setDeptId(user.getDeptId());
        instance.setTitle(XssUtils.escape(request.title()));
        instance.setBusinessData(serializeFormData(request.formData()));
        instance.setStatus(WorkflowStatus.DRAFT.getCode());
        instance.setRemark(XssUtils.escape(request.remark()));

        instance = instanceRepository.save(instance);

        log.info("工作流草稿创建成功: id={}", instance.getId());
        return toInstanceResponse(instance);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowDraftDetailResponse getDraftDetail(String instanceId) {
        String userId = getCurrentUserId();
        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以查看草稿");
        ServiceAssert.isTrue(instance.isDraft(), "当前流程不是草稿状态");

        WorkflowDefinitionEntity definition = EntityHelper.findByIdOrThrow(
            definitionRepository::findById, instance.getDefinitionId(), "工作流定义不存在");

        return new WorkflowDraftDetailResponse(
                toInstanceResponse(instance),
                definition.getFormConfig(),
                deserializeFormData(instance.getBusinessData())
        );
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse updateDraft(String instanceId, WorkflowStartRequest request) {
        String userId = getCurrentUserId();
        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以更新草稿");
        ServiceAssert.isTrue(instance.isDraft(), "只有草稿状态可以更新");

        applyDraftChanges(instance, request);
        WorkflowInstanceEntity saved = instanceRepository.save(instance);
        return toInstanceResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDraft(String instanceId) {
        String userId = getCurrentUserId();
        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以删除草稿");
        ServiceAssert.isTrue(instance.isDraft(), "只有草稿状态可以删除");

        instanceRepository.delete(instance);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    /**
     * 应用草稿更改
     */
    private void applyDraftChanges(WorkflowInstanceEntity instance, WorkflowStartRequest req) {
        if (!instance.getDefinitionId().equals(req.definitionId())) {
            WorkflowDefinitionEntity definition = EntityHelper.findByIdOrThrow(
                definitionRepository::findById, req.definitionId(), "工作流定义不存在");
            instance.setDefinitionId(definition.getId());
            instance.setDefinitionName(definition.getDefinitionName());
        }

        instance.setTitle(XssUtils.escape(req.title()));
        instance.setBusinessData(serializeFormData(req.formData()));
        instance.setRemark(XssUtils.escape(req.remark()));
    }

    /**
     * 序列化表单数据
     */
    private String serializeFormData(Map<String, Object> formData) {
        try {
            return objectMapper.writeValueAsString(formData == null ? Collections.emptyMap() : formData);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("表单数据格式不正确", e);
        }
    }

    /**
     * 反序列化表单数据
     */
    private Map<String, Object> deserializeFormData(String businessData) {
        if (businessData == null || businessData.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(businessData, new TypeReference<>() {});
        } catch (JacksonException e) {
            throw new IllegalArgumentException("业务表单数据解析失败", e);
        }
    }

    /**
     * 转换为响应对象
     */
    private WorkflowInstanceResponse toInstanceResponse(WorkflowInstanceEntity entity) {
        return conversionService.convert(entity, WorkflowInstanceResponse.class);
    }
}