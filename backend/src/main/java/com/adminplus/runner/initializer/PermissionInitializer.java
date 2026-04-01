package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
import com.adminplus.utils.IdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限关联初始化器
 * 负责初始化角色-菜单、用户-角色关联
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInitializer implements DataInitializer {

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public int getOrder() {
        return 50;
    }

    @Override
    public String getName() {
        return "权限关联初始化器";
    }

    @Override
    public void initialize() {
        log.info("开始初始化权限关联...");

        List<RoleEntity> roles = roleRepository.findAll();
        List<MenuEntity> menus = menuRepository.findAll();
        List<UserEntity> users = userRepository.findAll();
        Set<String> existingRoleMenuKeys = roleMenuRepository.findAll().stream()
                .map(item -> item.getRoleId() + ":" + item.getMenuId())
                .collect(Collectors.toSet());
        Set<String> existingUserRoleKeys = userRoleRepository.findAll().stream()
                .map(item -> item.getUserId() + ":" + item.getRoleId())
                .collect(Collectors.toSet());

        Map<String, RoleEntity> roleMap = roles.stream()
                .collect(Collectors.toMap(RoleEntity::getCode, r -> r));
        Map<String, MenuEntity> menuById = menus.stream()
                .collect(Collectors.toMap(MenuEntity::getId, m -> m));
        Map<String, MenuEntity> menuByName = menus.stream()
                .collect(Collectors.toMap(MenuEntity::getName, m -> m, (existing, replacement) -> existing));
        Map<String, UserEntity> userMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getUsername, u -> u));

        // 初始化角色-菜单权限
        initializeRoleMenus(roleMap, menuById, menuByName, existingRoleMenuKeys);

        // 初始化用户-角色关联
        initializeUserRoles(roleMap, userMap, existingUserRoleKeys);

        log.info("权限关联初始化完成");
    }

    private void initializeRoleMenus(Map<String, RoleEntity> roleMap, Map<String, MenuEntity> menuById,
                                     Map<String, MenuEntity> menuByName, Set<String> existingKeys) {
        // 超级管理员拥有所有权限
        RoleEntity adminRole = roleMap.get("ROLE_ADMIN");
        if (adminRole != null) {
            menuById.values().forEach(menu -> saveRoleMenuIfAbsent(adminRole, menu, existingKeys));
        }

        // 部门经理权限
        RoleEntity managerRole = roleMap.get("ROLE_MANAGER");
        if (managerRole != null) {
            assignMenusToRole(managerRole, menuByName, existingKeys, Arrays.asList(
                "首页", "用户管理", "新增用户", "编辑用户", "删除用户", "分配角色", "重置密码",
                "部门管理", "新增部门", "编辑部门", "删除部门", "查询部门", "部门列表",
                "工作流管理", "流程模板", "我的流程", "待我审批", "抄送我的", "催办中心",
                "发起流程", "保存草稿", "创建", "更新", "删除", "撤回流程", "取消流程",
                "审批通过", "审批驳回", "回退流程", "加签转办", "查看", "查看", "催办",
                "文件管理", "上传文件", "删除文件"
            ));
        }

        // 开发人员权限
        RoleEntity developerRole = roleMap.get("ROLE_DEVELOPER");
        if (developerRole != null) {
            assignMenusToRole(developerRole, menuByName, existingKeys, Arrays.asList(
                "首页", "用户管理", "角色管理", "菜单管理", "字典管理", "参数配置",
                "新增配置", "编辑配置", "删除配置", "导出配置", "导入配置", "刷新缓存",
                "字典项列表", "新增字典项", "编辑字典项", "删除字典项",
                "数据统计", "报表管理", "工作流管理", "流程模板", "流程设计", "我的流程", "抄送我的", "催办中心",
                "发起流程", "保存草稿", "创建", "更新", "删除",
                "创建", "撤回流程", "取消流程", "查看", "查看", "催办",
                "保存", "发布", "删除",
                "文件管理", "上传文件", "删除文件"
            ));
        }

        // 运营人员权限
        RoleEntity operatorRole = roleMap.get("ROLE_OPERATOR");
        if (operatorRole != null) {
            assignMenusToRole(operatorRole, menuByName, existingKeys, Arrays.asList(
                "首页", "数据统计", "报表管理",
                "工作流管理", "流程模板", "我的流程", "抄送我的", "催办中心",
                "发起流程", "保存草稿", "创建", "撤回流程", "取消流程", "查看", "查看", "催办",
                "文件管理", "上传文件", "删除文件"
            ));
        }

        // 普通用户权限
        RoleEntity userRole = roleMap.get("ROLE_USER");
        if (userRole != null) {
            assignMenusToRole(userRole, menuByName, existingKeys, Arrays.asList(
                "首页", "工作流管理", "流程模板", "我的流程", "抄送我的", "催办中心",
                "发起流程", "保存草稿", "创建", "撤回流程", "取消流程", "查看", "查看", "催办",
                "文件管理", "上传文件", "删除文件"
            ));
        }
    }

    private void assignMenusToRole(RoleEntity role, Map<String, MenuEntity> menuByName,
                                   Set<String> existingKeys, List<String> menuNames) {
        menuNames.stream()
                .map(menuByName::get)
                .filter(Objects::nonNull)
                .forEach(menu -> saveRoleMenuIfAbsent(role, menu, existingKeys));
    }

    private void initializeUserRoles(Map<String, RoleEntity> roleMap, Map<String, UserEntity> userMap,
                                     Set<String> existingKeys) {
        RoleEntity adminRole = roleMap.get("ROLE_ADMIN");
        RoleEntity managerRole = roleMap.get("ROLE_MANAGER");
        RoleEntity userRole = roleMap.get("ROLE_USER");
        RoleEntity developerRole = roleMap.get("ROLE_DEVELOPER");
        RoleEntity operatorRole = roleMap.get("ROLE_OPERATOR");

        if (adminRole != null) {
            saveUserRoleIfAbsent(userMap.get("admin"), adminRole, existingKeys);
        }
        if (managerRole != null) {
            saveUserRoleIfAbsent(userMap.get("manager"), managerRole, existingKeys);
        }
        if (userRole != null) {
            saveUserRoleIfAbsent(userMap.get("user1"), userRole, existingKeys);
            saveUserRoleIfAbsent(userMap.get("user2"), userRole, existingKeys);
            saveUserRoleIfAbsent(userMap.get("cs1"), userRole, existingKeys);
            saveUserRoleIfAbsent(userMap.get("cs2"), userRole, existingKeys);
        }
        if (developerRole != null) {
            saveUserRoleIfAbsent(userMap.get("dev1"), developerRole, existingKeys);
            saveUserRoleIfAbsent(userMap.get("dev2"), developerRole, existingKeys);
        }
        if (operatorRole != null) {
            saveUserRoleIfAbsent(userMap.get("operator1"), operatorRole, existingKeys);
            saveUserRoleIfAbsent(userMap.get("operator2"), operatorRole, existingKeys);
        }
    }

    private void saveRoleMenuIfAbsent(RoleEntity role, MenuEntity menu, Set<String> existingKeys) {
        String key = role.getId() + ":" + menu.getId();
        if (existingKeys.add(key)) {
            roleMenuRepository.save(createRoleMenu(role.getId(), menu.getId()));
            // 记录新添加的权限关联（用于调试）
            if (menu.getPermKey() != null && menu.getPermKey().startsWith("config")) {
                log.info("✅ 新增权限关联: role={} <- menu={}, permKey={}",
                        role.getCode(), menu.getName(), menu.getPermKey());
            }
        }
    }

    private void saveUserRoleIfAbsent(UserEntity user, RoleEntity role, Set<String> existingKeys) {
        if (user == null) return;
        String key = user.getId() + ":" + role.getId();
        if (existingKeys.add(key)) {
            userRoleRepository.save(createUserRole(user.getId(), role.getId()));
        }
    }

    private RoleMenuEntity createRoleMenu(String roleId, String menuId) {
        RoleMenuEntity roleMenu = new RoleMenuEntity();
        roleMenu.setId(IdUtils.nextIdStr());
        roleMenu.setRoleId(roleId);
        roleMenu.setMenuId(menuId);
        return roleMenu;
    }

    private UserRoleEntity createUserRole(String userId, String roleId) {
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setId(IdUtils.nextIdStr());
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return userRole;
    }
}
