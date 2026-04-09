package com.adminplus.converter.config;

import com.adminplus.pojo.dto.response.ConfigGroupResponse;
import com.adminplus.pojo.entity.ConfigGroupEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConfigGroupConverter implements Converter<ConfigGroupEntity, ConfigGroupResponse> {

    @Override
    public ConfigGroupResponse convert(ConfigGroupEntity source) {
        return new ConfigGroupResponse(
                source.getId(),
                source.getName(),
                source.getCode(),
                source.getIcon(),
                source.getSortOrder(),
                source.getDescription(),
                source.getStatus(),
                null,
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}