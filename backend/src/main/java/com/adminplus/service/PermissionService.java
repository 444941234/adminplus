package com.adminplus.service;

import com.adminplus.pojo.dto.resp.PermissionResp;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public interface PermissionService {

    /**
     * 获取用户的权限标识符列表（如 user:add, user:edit）
     */
    List<String> getUserPermissions(String userId);

    /**
     * 获取用户的角色编码列表（如 ROLE_ADMIN, ROLE_USER）
     */
    List<String> getUserRoles(String userId);

    /**
     * 获取用户的角色ID列表
     */
    List<String> getUserRoleIds(String userId);

    /**
     * 获取角色的权限标识符列表
     */
    List<String> getRolePermissions(String roleId);

    /**
     * 获取所有可用权限（用于分配）
     */
    List<PermissionResp> getAllPermissions();
}