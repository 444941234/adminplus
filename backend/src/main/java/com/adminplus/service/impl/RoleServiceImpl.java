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
import com.adminplus.service.LogService;
import com.adminplus.service.RoleService;
import com.adminplus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final LogService logService;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResp> getRoleList() {
        List<RoleEntity> roles;

        // 超级管理员可以查看所有角色
        if (SecurityUtils.isAdmin()) {
            roles = roleRepository.findAll();
        } else {
            // 非超级管理员不能查看超级管理员角色
            roles = roleRepository.findAll().stream()
                    .filter(role -> !"ROLE_ADMIN".equals(role.getCode()))
                    .toList();
        }

        return roles.stream().map(role -> new RoleResp(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getDataScope(),
                role.getStatus(),
                role.getSortOrder(),
                role.getCreateTime(),
                role.getUpdateTime()
        )).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResp getRoleById(String id) {
        var role = roleRepository.findById(id)
                .orElseThrow(() -> new BizException("角色不存在"));

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

    @Override
    @Transactional
    public RoleResp createRole(RoleCreateReq req) {
        // 检查角色编码是否已存在
        if (roleRepository.existsByCode(req.code())) {
            throw new BizException("角色编码已存在");
        }

        var role = new RoleEntity();
        role.setCode(req.code());
        role.setName(req.name());
        role.setDescription(req.description());
        role.setDataScope(req.dataScope() != null ? req.dataScope() : 1);
        role.setStatus(req.status() != null ? req.status() : 1);
        role.setSortOrder(req.sortOrder());

        role = roleRepository.save(role);

        // 记录审计日志
        logService.log("角色管理", OperationType.CREATE, "创建角色: " + role.getName() + " (" + role.getCode() + ")");

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

    @Override
    @Transactional
    public RoleResp updateRole(String id, RoleUpdateReq req) {
        var role = roleRepository.findById(id)
                .orElseThrow(() -> new BizException("角色不存在"));

        // 非超级管理员不能修改超级管理员角色
        if ("ROLE_ADMIN".equals(role.getCode()) && !SecurityUtils.isAdmin()) {
            throw new BizException("无权修改超级管理员角色");
        }

        if (req.name() != null) {
            role.setName(req.name());
        }
        if (req.description() != null) {
            role.setDescription(req.description());
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

    @Override
    @Transactional
    public void deleteRole(String id) {
        var role = roleRepository.findById(id)
                .orElseThrow(() -> new BizException("角色不存在"));

        // 非超级管理员不能删除超级管理员角色
        if ("ROLE_ADMIN".equals(role.getCode()) && !SecurityUtils.isAdmin()) {
            throw new BizException("无权删除超级管理员角色");
        }

        // 不能删除超级管理员角色（即使是超级管理员也不能删除）
        if ("ROLE_ADMIN".equals(role.getCode())) {
            throw new BizException("超级管理员角色不能删除");
        }

        // 删除角色-菜单关联
        roleMenuRepository.deleteByRoleId(id);

        // 删除角色（逻辑删除或物理删除，这里使用物理删除）
        roleRepository.delete(role);

        // 记录审计日志
        logService.log("角色管理", OperationType.DELETE, "删除角色: " + role.getName() + " (" + role.getCode() + ")");
    }

    @Override
    @Transactional
    public void assignMenus(String roleId, List<String> menuIds) {
        // 检查角色是否存在
        if (!roleRepository.existsById(roleId)) {
            throw new BizException("角色不存在");
        }

        // 删除原有的角色-菜单关联
        roleMenuRepository.deleteByRoleId(roleId);

        // 添加新的角色-菜单关联
        if (menuIds != null && !menuIds.isEmpty()) {
            List<RoleMenuEntity> roleMenus = menuIds.stream().map(menuId -> {
                var roleMenu = new RoleMenuEntity();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                return roleMenu;
            }).toList();
            roleMenuRepository.saveAll(roleMenus);
        }

        // 记录审计日志
        var role = roleRepository.findById(roleId).orElse(null);
        if (role != null) {
            logService.log("角色管理", OperationType.UPDATE, "分配菜单权限: " + role.getName() + " -> " + menuIds.size() + " 个菜单");
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
}