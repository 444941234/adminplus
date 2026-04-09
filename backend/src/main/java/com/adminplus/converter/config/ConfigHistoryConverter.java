package com.adminplus.converter.config;

import com.adminplus.pojo.dto.response.ConfigHistoryResponse;
import com.adminplus.pojo.entity.ConfigHistoryEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConfigHistoryConverter implements Converter<ConfigHistoryEntity, ConfigHistoryResponse> {

    @Override
    public ConfigHistoryResponse convert(ConfigHistoryEntity source) {
        return new ConfigHistoryResponse(
                source.getId(),
                source.getConfigId(),
                source.getConfigKey(),
                source.getOldValue(),
                source.getNewValue(),
                source.getRemark(),
                null, // operatorName - 从当前用户上下文获取
                source.getCreateTime()
        );
    }
}