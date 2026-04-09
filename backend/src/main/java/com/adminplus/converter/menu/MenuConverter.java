package com.adminplus.converter.menu;

import com.adminplus.pojo.dto.response.MenuResponse;
import com.adminplus.pojo.entity.MenuEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MenuConverter implements Converter<MenuEntity, MenuResponse> {

    @Override
    public MenuResponse convert(MenuEntity source) {
        return new MenuResponse(
                source.getId(),
                source.getParentId(),
                source.getType(),
                source.getName(),
                source.getPath(),
                source.getComponent(),
                source.getPermKey(),
                source.getIcon(),
                source.getSortOrder(),
                source.getVisible(),
                source.getStatus(),
                null,
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}