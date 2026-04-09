package com.adminplus.converter.dept;

import com.adminplus.pojo.dto.response.DeptResponse;
import com.adminplus.pojo.entity.DeptEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DeptConverter implements Converter<DeptEntity, DeptResponse> {

    @Override
    public DeptResponse convert(DeptEntity source) {
        String parentId = source.getParent() != null ? source.getParent().getId() : "0";
        return new DeptResponse(
                source.getId(),
                parentId,
                source.getName(),
                source.getCode(),
                source.getLeader(),
                source.getPhone(),
                source.getEmail(),
                source.getSortOrder(),
                source.getStatus(),
                null, // children - 由调用方构建树形结构
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}