package com.adminplus.converter.role;

import com.adminplus.pojo.dto.response.RoleResponse;
import com.adminplus.pojo.entity.RoleEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter implements Converter<RoleEntity, RoleResponse> {

    @Override
    public RoleResponse convert(RoleEntity source) {
        return new RoleResponse(
                source.getId(),
                source.getCode(),
                source.getName(),
                source.getDescription(),
                source.getDataScope(),
                source.getStatus(),
                source.getSortOrder(),
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}