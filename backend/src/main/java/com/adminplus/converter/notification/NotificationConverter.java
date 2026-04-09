package com.adminplus.converter.notification;

import com.adminplus.pojo.dto.response.NotificationResponse;
import com.adminplus.pojo.entity.NotificationEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class NotificationConverter implements Converter<NotificationEntity, NotificationResponse> {

    @Override
    public NotificationResponse convert(NotificationEntity source) {
        NotificationResponse resp = new NotificationResponse();
        resp.setId(source.getId());
        resp.setType(source.getType());
        resp.setRecipientId(source.getRecipientId());
        resp.setTitle(source.getTitle());
        resp.setContent(source.getContent());
        resp.setRelatedId(source.getRelatedId());
        resp.setRelatedType(source.getRelatedType());
        resp.setStatus(source.getStatus());
        resp.setCreateTime(source.getCreateTime() != null ? source.getCreateTime().toString() : null);
        resp.setUpdateTime(source.getUpdateTime() != null ? source.getUpdateTime().toString() : null);
        return resp;
    }
}