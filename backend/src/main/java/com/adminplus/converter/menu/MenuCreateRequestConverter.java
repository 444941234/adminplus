package com.adminplus.converter.menu;

import com.adminplus.pojo.dto.request.MenuCreateRequest;
import com.adminplus.pojo.entity.MenuEntity;
import com.adminplus.utils.XssSanitizer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MenuCreateRequestConverter implements Converter<MenuCreateRequest, MenuEntity> {

    @Override
    public MenuEntity convert(MenuCreateRequest source) {
        MenuEntity entity = new MenuEntity();
        // parentId 是 ID，不需要 XSS 清洗
        entity.setParentId(source.parentId());
        entity.setType(source.type());
        // name 需要 XSS 清洗
        entity.setName(XssSanitizer.sanitizeOrNull(source.name()));
        // path 属于跳过字段，不需要 XSS 清洗
        entity.setPath(source.path());
        // component 需要 XSS 清洗
        entity.setComponent(XssSanitizer.sanitizeOrNull(source.component()));
        // permKey 属于跳过字段（key），不需要 XSS 清洗
        entity.setPermKey(source.permKey());
        // icon 需要 XSS 清洗
        entity.setIcon(XssSanitizer.sanitizeOrNull(source.icon()));
        entity.setSortOrder(source.sortOrder());
        entity.setVisible(source.visible());
        entity.setStatus(source.status());
        return entity;
    }
}