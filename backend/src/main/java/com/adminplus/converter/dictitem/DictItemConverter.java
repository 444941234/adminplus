package com.adminplus.converter.dictitem;

import com.adminplus.pojo.dto.response.DictItemResponse;
import com.adminplus.pojo.entity.DictItemEntity;
import com.adminplus.repository.DictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * DictItemEntity → DictItemResponse 转换器
 * <p>
 * 自动查询 dictType
 */
@Component
@RequiredArgsConstructor
public class DictItemConverter implements Converter<DictItemEntity, DictItemResponse> {

    private final DictRepository dictRepository;

    @Override
    public DictItemResponse convert(DictItemEntity source) {
        // 查询字典类型
        String dictType = null;
        if (source.getDictId() != null) {
            dictType = dictRepository.findById(source.getDictId())
                    .map(d -> d.getDictType())
                    .orElse(null);
        }

        String parentId = source.getParent() != null ? source.getParent().getId() : "0";
        return new DictItemResponse(
                source.getId(),
                source.getDictId(),
                dictType,
                parentId,
                source.getLabel(),
                source.getValue(),
                source.getSortOrder(),
                source.getStatus(),
                source.getRemark(),
                null,
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}