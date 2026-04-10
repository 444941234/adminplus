package com.adminplus.converter.dept;

import com.adminplus.pojo.dto.request.DeptCreateRequest;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.utils.XssSanitizer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 部门创建请求转换器
 * 负责将 DeptCreateRequest 转换为 DeptEntity，并处理 XSS 清洗
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Component
public class DeptCreateRequestConverter implements Converter<DeptCreateRequest, DeptEntity> {

    @Override
    public DeptEntity convert(DeptCreateRequest source) {
        DeptEntity entity = new DeptEntity();
        // parentId 是 ID，不需要 XSS 清洗
        entity.setParentId(source.parentId());
        // name, leader 需要 XSS 清洗
        entity.setName(XssSanitizer.sanitizeOrNull(source.name()));
        // code 属于跳过字段，不需要 XSS 清洗
        entity.setCode(source.code());
        entity.setLeader(XssSanitizer.sanitizeOrNull(source.leader()));
        // phone, email 需要 XSS 清洗
        entity.setPhone(XssSanitizer.sanitizeOrNull(source.phone()));
        entity.setEmail(XssSanitizer.sanitizeOrNull(source.email()));
        entity.setSortOrder(source.sortOrder());
        entity.setStatus(source.status());
        return entity;
    }
}