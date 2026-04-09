package com.adminplus.converter.config;

import com.adminplus.pojo.dto.response.ConfigResponse;
import com.adminplus.pojo.entity.ConfigEntity;
import com.adminplus.repository.ConfigGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * ConfigEntity → ConfigResponse 转换器
 * <p>
 * 自动查询 groupName
 */
@Component
@RequiredArgsConstructor
public class ConfigConverter implements Converter<ConfigEntity, ConfigResponse> {

    private final ConfigGroupRepository configGroupRepository;

    @Override
    public ConfigResponse convert(ConfigEntity source) {
        // 查询组名
        String groupName = null;
        if (source.getGroupId() != null) {
            groupName = configGroupRepository.findById(source.getGroupId())
                    .map(g -> g.getName())
                    .orElse(null);
        }

        return new ConfigResponse(
                source.getId(),
                source.getGroupId(),
                groupName,
                source.getName(),
                source.getKey(),
                source.getValue(),
                source.getValueType(),
                source.getEffectType(),
                source.getDefaultValue(),
                source.getDescription(),
                source.getIsRequired(),
                source.getValidationRule(),
                source.getSortOrder(),
                source.getStatus(),
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}