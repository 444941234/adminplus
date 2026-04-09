package com.adminplus.converter.menu;

import com.adminplus.pojo.dto.request.MenuCreateRequest;
import com.adminplus.pojo.entity.MenuEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MenuCreateRequestConverter implements Converter<MenuCreateRequest, MenuEntity> {

    @Override
    public MenuEntity convert(MenuCreateRequest source) {
        MenuEntity entity = new MenuEntity();
        entity.setParentId(source.parentId());
        entity.setType(source.type());
        entity.setName(source.name());
        entity.setPath(source.path());
        entity.setComponent(source.component());
        entity.setPermKey(source.permKey());
        entity.setIcon(source.icon());
        entity.setSortOrder(source.sortOrder());
        entity.setVisible(source.visible());
        entity.setStatus(source.status());
        return entity;
    }
}