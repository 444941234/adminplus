package com.adminplus.service.impl;

import com.adminplus.pojo.dto.response.PermissionResponse;
import com.adminplus.pojo.entity.MenuEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.MenuRepository;
import com.adminplus.repository.RoleMenuRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限服务实现
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UserRoleRepository userRoleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final MenuRepository menuRepository;
    private final RoleRepository roleRepository;
    private final ConversionService conversionService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<String> getUserPermissions(String userId) {
        // 直接通过 userId 查询权限（三表关联查询优化）
        return menuRepository.findPermKeysByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userRoles", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<String> getUserRoles(String userId) {
        return roleRepository.findActiveRoleCodesByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserRoleIds(String userId) {
        return roleRepository.findActiveRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "rolePermissions", key = "#roleId", unless = "#result == null || #result.isEmpty()")
    public List<String> getRolePermissions(String roleId) {
        List<String> menuIds = roleMenuRepository.findMenuIdByRoleId(roleId);
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return menuRepository.findPermKeysByMenuIds(menuIds);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allPermissions", key = "'list'", unless = "#result == null || #result.isEmpty()")
    public List<PermissionResponse> getAllPermissions() {
        List<MenuEntity> menus = menuRepository.findAllByOrderBySortOrderAsc();

        return menus.stream()
                .filter(menu -> menu.getPermKey() != null && !menu.getPermKey().isBlank())
                .map(menu -> conversionService.convert(menu, PermissionResponse.class))
                .toList();
    }
}