package com.adminplus.converter.dict;

import com.adminplus.pojo.dto.response.DictResponse;
import com.adminplus.pojo.entity.DictEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DictConverter implements Converter<DictEntity, DictResponse> {

    @Override
    public DictResponse convert(DictEntity source) {
        return new DictResponse(
                source.getId(),
                source.getDictType(),
                source.getDictName(),
                source.getStatus(),
                source.getRemark(),
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}