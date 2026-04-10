package com.adminplus.converter.role;

import com.adminplus.pojo.dto.request.RoleCreateRequest;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.utils.XssSanitizer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RoleCreateRequestConverter implements Converter<RoleCreateRequest, RoleEntity> {

    @Override
    public RoleEntity convert(RoleCreateRequest source) {
        RoleEntity entity = new RoleEntity();
        // code 不需要 XSS 清洗（属于跳过字段）
        entity.setCode(source.code());
        // name 和 description 需要 XSS 清洗
        entity.setName(XssSanitizer.sanitizeOrNull(source.name()));
        entity.setDescription(XssSanitizer.sanitizeOrNull(source.description()));
        entity.setDataScope(source.dataScope());
        entity.setStatus(source.status() != null ? source.status() : 1);
        entity.setSortOrder(source.sortOrder());
        return entity;
    }
}