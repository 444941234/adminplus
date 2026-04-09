package com.adminplus.converter.permission;

import com.adminplus.pojo.dto.response.PermissionResponse;
import com.adminplus.pojo.entity.MenuEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * MenuEntity → PermissionResponse 转换器
 * <p>
 * 用于转换具有权限标识的菜单为权限响应对象
 */
@Component
public class PermissionConverter implements Converter<MenuEntity, PermissionResponse> {

    @Override
    public PermissionResponse convert(MenuEntity source) {
        String parentId = source.getParent() != null ? source.getParent().getId() : "0";
        return new PermissionResponse(
                source.getId(),
                source.getPermKey(),
                source.getName(),
                source.getType(),
                parentId
        );
    }
}