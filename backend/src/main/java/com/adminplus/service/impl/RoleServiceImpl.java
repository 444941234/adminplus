package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.constants.OperationType;
import com.adminplus.pojo.dto.req.RoleCreateReq;
import com.adminplus.pojo.dto.req.RoleUpdateReq;
import com.adminplus.pojo.dto.resp.RoleResp;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.RoleMenuEntity;
import com.adminplus.repository.RoleMenuRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.LogService;
import com.adminplus.service.RoleService;
import com.adminplus.utils.AssociationDiffHelper;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.XssUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

/**
 * 角色服务实现
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final UserRoleRepository userRoleRepository;
    private final LogService logService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "T(com.adminplus.utils.SecurityUtils).isAdmin() ? 'all' : 'nonAdmin'", unless = "#result == null || #result.isEmpty()")
    public List<RoleResp> getRoleList() {
        List<RoleEntity> roles;

        // 超级管理员可以查看所有角色
        if (SecurityUtils.isAdmin()) {
            roles = roleRepository.findAll();
        } else {
            // 非超级管理员不能查看超级管理员角色（数据库层面过滤）
            roles = roleRepository.findByDeletedFalseAndCodeNot("ROLE_ADMIN");
        }

        return roles.stream().map(this::toResp).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResp getRoleById(String id) {
        var role = EntityHelper.findByIdOrThrow(roleRepository::findById, id, "角色不存在");

        return toResp(role);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userPermissions", "rolePermissions", "roles"}, allEntries = true)
    public RoleResp createRole(RoleCreateReq req) {
        // 检查角色编码是否已存在
        if (roleRepository.existsByCode(req.code())) {
            throw new BizException("角色编码已存在");
        }

        var role = new RoleEntity();
        role.setCode(XssUtils.escape(req.code()));
        role.setName(XssUtils.escape(req.name()));
        role.setDescription(XssUtils.escape(req.description()));
        role.setDataScope(req.dataScope() != null ? req.dataScope() : 1);
        role.setStatus(req.status() != null ? req.status() : 1);
        role.setSortOrder(req.sortOrder());

        role = roleRepository.save(role);

        // 记录审计日志
        logService.log("角色管理", OperationType.CREATE, "创建角色: " + role.getName() + " (" + role.getCode() + ")");

        return toResp(role);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userPermissions", "rolePermissions", "roles"}, allEntries = true)
    public RoleResp updateRole(String id, RoleUpdateReq req) {
        var role = EntityHelper.findByIdOrThrow(roleRepository::findById, id, "角色不存在");

        // 非超级管理员不能修改超级管理员角色
        if ("ROLE_ADMIN".equals(role.getCode()) && !SecurityUtils.isAdmin()) {
            throw new BizException("无权修改超级管理员角色");
        }

        if (req.name() != null) {
            role.setName(XssUtils.escape(req.name()));
        }
        if (req.description() != null) {
            role.setDescription(XssUtils.escape(req.description()));
        }
        if (req.dataScope() != null) {
            role.setDataScope(req.dataScope());
        }
        if (req.status() != null) {
            role.setStatus(req.status());
        }
        if (req.sortOrder() != null) {
            role.setSortOrder(req.sortOrder());
        }

        role = roleRepository.save(role);

        logService.log("角色管理", OperationType.UPDATE, "更新角色: " + role.getName() + " (" + role.getCode() + ")");

        return toResp(role);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userPermissions", "userRoles", "rolePermissions", "roles"}, allEntries = true)
    public void deleteRole(String id) {
        var role = EntityHelper.findByIdOrThrow(roleRepository::findById, id, "角色不存在");

        // 非超级管理员不能删除超级管理员角色
        if ("ROLE_ADMIN".equals(role.getCode()) && !SecurityUtils.isAdmin()) {
            throw new BizException("无权删除超级管理员角色");
        }

        // 不能删除超级管理员角色（即使是超级管理员也不能删除）
        if ("ROLE_ADMIN".equals(role.getCode())) {
            throw new BizException("超级管理员角色不能删除");
        }

        // 检查是否有用户绑定了该角色
        if (userRoleRepository.existsByRoleId(id)) {
            throw new BizException("该角色已分配给用户，无法删除");
        }

        // 删除角色-菜单关联
        roleMenuRepository.deleteByRoleId(id);

        // 逻辑删除（Entity 配置了 @SQLDelete，JPA delete 会触发 UPDATE SET deleted=true）
        roleRepository.delete(role);

        // 记录审计日志
        logService.log("角色管理", OperationType.DELETE, "删除角色: " + role.getName() + " (" + role.getCode() + ")");
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userPermissions", "rolePermissions", "roles"}, allEntries = true)
    public void assignMenus(String roleId, List<String> menuIds) {
        // 检查角色是否存在并获取角色信息（一次查询）
        var role = EntityHelper.findByIdOrThrow(roleRepository::findById, roleId, "角色不存在");

        List<String> safeMenuIds = (menuIds != null) ? menuIds : List.of();

        // diff 精准更新
        var result = AssociationDiffHelper.diffUpdate(
                roleId,
                safeMenuIds,
                rid -> new HashSet<>(roleMenuRepository.findMenuIdByRoleId(rid)),
                roleMenuRepository::deleteByRoleIdAndMenuIdIn,
                (rid, toAdd) -> {
                    List<RoleMenuEntity> list = toAdd.stream().map(menuId -> {
                        var e = new RoleMenuEntity();
                        e.setRoleId(rid);
                        e.setMenuId(menuId);
                        return e;
                    }).toList();
                    roleMenuRepository.saveAll(list);
                }
        );

        // 记录审计日志
        if (result.hasChanges()) {
            logService.log("角色管理", OperationType.UPDATE,
                    "分配菜单权限: " + role.getName() + " -> " + safeMenuIds.size() + " 个菜单"
                            + " (新增" + result.added() + "个, 移除" + result.removed() + "个)");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRoleMenuIds(String roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new BizException("角色不存在");
        }
        return roleMenuRepository.findMenuIdByRoleId(roleId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userPermissions", "userRoles", "rolePermissions", "roles"}, allEntries = true)
    public void updateRoleStatus(String id, Integer status) {
        var role = EntityHelper.findByIdOrThrow(roleRepository::findById, id, "角色不存在");

        // 非超级管理员不能修改超级管理员角色状态
        if ("ROLE_ADMIN".equals(role.getCode()) && !SecurityUtils.isAdmin()) {
            throw new BizException("无权修改超级管理员角色状态");
        }

        role.setStatus(status);
        roleRepository.save(role);

        logService.log("角色管理", OperationType.UPDATE, "更新角色状态: " + role.getName() + " -> " + (status == 1 ? "启用" : "禁用"));
    }

    private RoleResp toResp(RoleEntity role) {
        return new RoleResp(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getDataScope(),
                role.getStatus(),
                role.getSortOrder(),
                role.getCreateTime(),
                role.getUpdateTime()
        );
    }
}