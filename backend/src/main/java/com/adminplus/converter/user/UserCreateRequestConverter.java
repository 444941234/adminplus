package com.adminplus.converter.user;

import com.adminplus.pojo.dto.request.UserCreateRequest;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.utils.XssSanitizer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 用户创建请求转换器
 * 负责将 UserCreateRequest 转换为 UserEntity，并处理 XSS 清洗
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Component
public class UserCreateRequestConverter implements Converter<UserCreateRequest, UserEntity> {

    @Override
    public UserEntity convert(UserCreateRequest source) {
        UserEntity entity = new UserEntity();
        // username 不需要 XSS 清洗（属于跳过字段）
        entity.setUsername(source.username());
        // nickname, email, phone 需要 XSS 清洗
        entity.setNickname(XssSanitizer.sanitizeOrNull(source.nickname()));
        entity.setEmail(XssSanitizer.sanitizeOrNull(source.email()));
        entity.setPhone(XssSanitizer.sanitizeOrNull(source.phone()));
        // avatar 是 URL，不需要 XSS 清洗
        entity.setAvatar(source.avatar());
        // deptId 是 ID，不需要 XSS 清洗
        entity.setDeptId(source.deptId());
        // 密码和状态在 Service 中处理
        return entity;
    }
}