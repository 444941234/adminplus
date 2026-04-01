package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 角色数据初始化器
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleInitializer implements DataInitializer {

    private final RoleRepository roleRepository;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getName() {
        return "角色数据初始化";
    }

    @Override
    @Transactional
    public void initialize() {
        if (roleRepository.count() > 0) {
            log.info("角色数据已存在，跳过初始化");
            return;
        }

        List<RoleEntity> roles = Arrays.asList(
                createRole(null, "ROLE_ADMIN", "超级管理员", "拥有系统所有权限", 1, 1),
                createRole(null, "ROLE_MANAGER", "部门经理", "拥有部门管理权限", 1, 2),
                createRole(null, "ROLE_USER", "普通用户", "拥有基本用户权限", 1, 3),
                createRole(null, "ROLE_DEVELOPER", "开发人员", "拥有开发相关权限", 1, 4),
                createRole(null, "ROLE_OPERATOR", "运营人员", "拥有运营相关权限", 1, 5)
        );

        roleRepository.saveAll(roles);
        log.info("初始化角色数据完成，共 {} 个角色", roles.size());
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