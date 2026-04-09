package com.adminplus.converter.role;

import com.adminplus.pojo.dto.request.RoleCreateRequest;
import com.adminplus.pojo.entity.RoleEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RoleCreateRequestConverter implements Converter<RoleCreateRequest, RoleEntity> {

    @Override
    public RoleEntity convert(RoleCreateRequest source) {
        RoleEntity entity = new RoleEntity();
        entity.setCode(source.code());
        entity.setName(source.name());
        entity.setDescription(source.description());
        entity.setDataScope(source.dataScope());
        entity.setStatus(source.status() != null ? source.status() : 1);
        entity.setSortOrder(source.sortOrder());
        return entity;
    }
}