package com.adminplus.converter.config;

import com.adminplus.pojo.dto.request.ConfigGroupCreateRequest;
import com.adminplus.pojo.entity.ConfigGroupEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConfigGroupCreateRequestConverter implements Converter<ConfigGroupCreateRequest, ConfigGroupEntity> {

    @Override
    public ConfigGroupEntity convert(ConfigGroupCreateRequest source) {
        ConfigGroupEntity entity = new ConfigGroupEntity();
        entity.setName(source.name());
        entity.setCode(source.code());
        entity.setIcon(source.icon());
        entity.setSortOrder(source.sortOrder());
        entity.setDescription(source.description());
        return entity;
    }
}