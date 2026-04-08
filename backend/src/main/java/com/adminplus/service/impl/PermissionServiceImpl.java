package com.adminplus.service.impl;

import com.adminplus.pojo.dto.resp.PermissionResp;
import com.adminplus.pojo.entity.MenuEntity;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.MenuRepository;
import com.adminplus.repository.RoleMenuRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<String> getUserPermissions(String userId) {
        // 1. 查询用户的角色ID列表
        List<String> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            return List.of();
        }

        // 2. 批量查询这些角色的菜单ID列表（去重）
        Set<String> menuIds = new HashSet<>(roleMenuRepository.findMenuIdsByRoleIds(roleIds));

        if (menuIds.isEmpty()) {
            return List.of();
        }

        // 3. 批量查询菜单的权限标识符（permKey），过滤掉空值
        List<MenuEntity> menus = menuRepository.findAllById(menuIds);
        return menus.stream()
                .filter(menu -> menu.getPermKey() != null && !menu.getPermKey().isBlank())
                .map(MenuEntity::getPermKey)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userRoles", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<String> getUserRoles(String userId) {
        List<String> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            return List.of();
        }

        // 批量查询角色
        List<RoleEntity> roles = roleRepository.findAllById(roleIds);
        return roles.stream()
                .filter(Objects::nonNull)
                .map(RoleEntity::getCode)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserRoleIds(String userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "rolePermissions", key = "#roleId", unless = "#result == null || #result.isEmpty()")
    public List<String> getRolePermissions(String roleId) {
        // 1. 查询角色的菜单ID列表
        List<String> menuIds = roleMenuRepository.findMenuIdByRoleId(roleId);

        if (menuIds.isEmpty()) {
            return List.of();
        }

        // 2. 批量查询菜单的权限标识符（permKey），过滤掉空值
        List<MenuEntity> menus = menuRepository.findAllById(menuIds);
        return menus.stream()
                .filter(menu -> menu.getPermKey() != null && !menu.getPermKey().isBlank())
                .map(MenuEntity::getPermKey)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allPermissions", key = "'list'", unless = "#result == null || #result.isEmpty()")
    public List<PermissionResp> getAllPermissions() {
        List<MenuEntity> menus = menuRepository.findAllByOrderBySortOrderAsc();

        return menus.stream()
                .filter(menu -> menu.getPermKey() != null && !menu.getPermKey().isBlank())
                .map(menu -> {
                    String parentId = menu.getParent() != null ? menu.getParent().getId() : "0";
                    return new PermissionResp(
                            menu.getId(),
                            menu.getPermKey(),
                            menu.getName(),
                            menu.getType(),
                            parentId
                    );
                })
                .toList();
    }
}