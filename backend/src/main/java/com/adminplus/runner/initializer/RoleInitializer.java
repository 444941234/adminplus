package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 角色数据初始化器
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Component
public class RoleInitializer extends AbstractDataInitializer {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        super(() -> roleRepository.count() > 0, "角色");
        this.roleRepository = roleRepository;
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getName() {
        return "角色数据初始化";
    }

    @Override
    protected void doInitialize() {
        List<RoleEntity> roles = Arrays.asList(
                createRole(null, "ROLE_ADMIN", "超级管理员", "拥有系统所有权限", 1, 1),
                createRole(null, "ROLE_MANAGER", "部门经理", "拥有部门管理权限", 1, 2),
                createRole(null, "ROLE_HR", "人事", "拥有人事管理权限", 1, 3),
                createRole(null, "ROLE_USER", "普通用户", "拥有基本用户权限", 1, 4),
                createRole(null, "ROLE_DEVELOPER", "开发人员", "拥有开发相关权限", 1, 5),
                createRole(null, "ROLE_OPERATOR", "运营人员", "拥有运营相关权限", 1, 6),
                createRole(null, "ROLE_LEGAL", "法务", "拥有法务审核权限", 1, 7),
                createRole(null, "ROLE_FINANCE", "财务", "拥有财务审核权限", 1, 8)
        );

        roleRepository.saveAll(roles);
    }

    private RoleEntity createRole(String id, String code, String name, String description, Integer status, Integer sortOrder) {
        RoleEntity role = new RoleEntity();
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        role.setStatus(status);
        role.setSortOrder(sortOrder);
        return role;
    }
}
