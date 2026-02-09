package com.adminplus.listener;

import com.adminplus.entity.BaseEntity;
import com.adminplus.util.SnowflakeIdGenerator;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 实体审计监听器
 * 自动设置ID、创建人、更新人、创建时间、更新时间
 * 
 * @author AdminPlus
 * @since 2026-02-09
 */
@Component
public class EntityAuditListener {
    
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;
    
    /**
     * 在实体保存前自动设置字段
     */
    @PrePersist
    public void prePersist(BaseEntity entity) {
        // 设置雪花ID
        if (entity.getId() == null) {
            entity.setId(snowflakeIdGenerator.nextId());
        }
        
        // 设置创建时间和更新时间
        Instant now = Instant.now();
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(now);
        }
        if (entity.getUpdateTime() == null) {
            entity.setUpdateTime(now);
        }
        
        // 设置创建人和更新人
        String currentUser = getCurrentUsername();
        if (entity.getCreateUser() == null) {
            entity.setCreateUser(currentUser);
        }
        if (entity.getUpdateUser() == null) {
            entity.setUpdateUser(currentUser);
        }
        
        // 设置删除标记
        if (entity.getDeleted() == null) {
            entity.setDeleted(false);
        }
    }
    
    /**
     * 在实体更新前自动设置字段
     */
    @PreUpdate
    public void preUpdate(BaseEntity entity) {
        // 设置更新时间
        entity.setUpdateTime(Instant.now());
        
        // 设置更新人
        entity.setUpdateUser(getCurrentUsername());
    }
    
    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "system"; // 默认系统用户
    }
}