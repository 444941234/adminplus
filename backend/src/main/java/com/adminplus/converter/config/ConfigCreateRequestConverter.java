package com.adminplus.converter.config;

import com.adminplus.pojo.dto.request.ConfigCreateRequest;
import com.adminplus.pojo.entity.ConfigEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConfigCreateRequestConverter implements Converter<ConfigCreateRequest, ConfigEntity> {

    @Override
    public ConfigEntity convert(ConfigCreateRequest source) {
        ConfigEntity entity = new ConfigEntity();
        entity.setGroupId(source.groupId());
        entity.setName(source.name());
        entity.setKey(source.key());
        entity.setValue(source.value());
        entity.setValueType(source.valueType());
        entity.setEffectType(source.effectType());
        entity.setDefaultValue(source.defaultValue());
        entity.setDescription(source.description());
        entity.setIsRequired(source.isRequired());
        entity.setValidationRule(source.validationRule());
        entity.setSortOrder(source.sortOrder());
        entity.setStatus(1);
        return entity;
    }
}