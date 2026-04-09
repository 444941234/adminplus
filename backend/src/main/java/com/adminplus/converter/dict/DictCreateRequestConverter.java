package com.adminplus.converter.dict;

import com.adminplus.pojo.dto.request.DictCreateRequest;
import com.adminplus.pojo.entity.DictEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DictCreateRequestConverter implements Converter<DictCreateRequest, DictEntity> {

    @Override
    public DictEntity convert(DictCreateRequest source) {
        DictEntity entity = new DictEntity();
        entity.setDictType(source.dictType());
        entity.setDictName(source.dictName());
        entity.setRemark(source.remark());
        entity.setStatus(1);
        return entity;
    }
}