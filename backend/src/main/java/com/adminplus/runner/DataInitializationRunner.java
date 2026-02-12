package com.adminplus.runner;

import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
import com.adminplus.utils.IdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 数据初始化服务
 * 在应用启动时自动执行基础数据初始化
 * 
 * @author AdminPlus
 * @since 2026-02-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitializationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final DeptRepository deptRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("开始执行数据初始化...");
        
        try {
            // 检查是否已经初始化过
            if (isAlreadyInitialized()) {
                log.info("数据已经初始化过，跳过初始化过程");
                return;
            }

            // 初始化部门数据
            initializeDepartments();

            // 初始化角色数据
            initializeRoles();

            // 初始化菜单数据
            initializeMenus();

            // 初始化用户数据
            initializeUsers();

            // 初始化权限关联
            initializePermissions();

            log.info("数据初始化完成！");
            
        } catch (Exception e) {
            log.error("数据初始化失败", e);
            throw e;
        }
    }

    /**
     * 检查是否已经初始化过
     */
    private boolean isAlreadyInitialized() {
        return userRepository.count() > 0 && roleRepository.count() > 0;
    }

    /**
     * 初始化部门数据
     */
    private void initializeDepartments() {
        if (deptRepository.count() > 0) {
            log.info("部门数据已存在，跳过初始化");
            return;
        }

        List<DeptEntity> depts = Arrays.asList(
                createDept(null, null, "AdminPlus 总部", "HQ", "张三", "010-12345678", 1, 1),
                createDept(null, "1", "技术研发部", "RD", "李四", "010-12345679", 1, 1),
                createDept(null, "1", "市场运营部", "MK", "王五", "010-12345680", 1, 2),
                createDept(null, "2", "后端开发组", "BE", "赵六", "010-12345681", 1, 1),
                createDept(null, "2", "前端开发组", "FE", "钱七", "010-12345682", 1, 2),
                createDept(null, "3", "市场推广组", "MP", "孙八", "010-12345683", 1, 1),
                createDept(null, "3", "客户服务组", "CS", "周九", "010-12345684", 1, 2)
        );

        deptRepository.saveAll(depts);
        log.info("初始化部门数据完成，共 {} 个部门", depts.size());
    }

    /**
     * 初始化角色数据
     */
    private void initializeRoles() {
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

    /**
     * 初始化菜单数据
     */
    private void initializeMenus() {
        if (menuRepository.count() > 0) {
            log.info("菜单数据已存在，跳过初始化");
            return;
        }

        // 先创建顶级菜单（目录和页面）
        MenuEntity dashboard = createMenu(null, null, 1, "首页", "/dashboard", "Dashboard", "dashboard:view", "HomeFilled", 0, 1, 1);
        MenuEntity system = createMenu(null, null, 0, "系统管理", "/system", null, null, "Setting", 1, 1, 1);
        MenuEntity analysis = createMenu(null, null, 0, "数据分析", "/analysis", null, null, "DataLine", 2, 1, 1);

        // 保存顶级菜单以获取ID
        menuRepository.save(dashboard);
        menuRepository.save(system);
        menuRepository.save(analysis);

        // 创建系统管理下的子菜单
        MenuEntity user = createMenu(null, system.getId(), 1, "用户管理", "/system/user", "system/User", "system:user:list", "User", 1, 1, 1);
        MenuEntity role = createMenu(null, system.getId(), 1, "角色管理", "/system/role", "system/Role", "system:role:list", "UserFilled", 2, 1, 1);
        MenuEntity menu = createMenu(null, system.getId(), 1, "菜单管理", "/system/menu", "system/Menu", "system:menu:list", "Menu", 3, 1, 1);
        MenuEntity dict = createMenu(null, system.getId(), 1, "字典管理", "/system/dict", "system/Dict", "system:dict:list", "Document", 4, 1, 1);
        MenuEntity dept = createMenu(null, system.getId(), 1, "部门管理", "/system/dept", "system/Dept", "system:dept:list", "OfficeBuilding", 5, 1, 1);
        MenuEntity config = createMenu(null, system.getId(), 1, "参数配置", "/system/config", "system/Config", "system:config:list", "Tools", 6, 1, 1);

        // 保存二级菜单
        menuRepository.save(user);
        menuRepository.save(role);
        menuRepository.save(menu);
        menuRepository.save(dict);
        menuRepository.save(dept);
        menuRepository.save(config);

        // 创建用户管理的按钮权限
        menuRepository.save(createMenu(null, user.getId(), 2, "新增用户", null, null, "user:add", null, 1, 0, 1));
        menuRepository.save(createMenu(null, user.getId(), 2, "编辑用户", null, null, "user:edit", null, 2, 0, 1));
        menuRepository.save(createMenu(null, user.getId(), 2, "删除用户", null, null, "user:delete", null, 3, 0, 1));
        menuRepository.save(createMenu(null, user.getId(), 2, "分配角色", null, null, "user:assign", null, 4, 0, 1));
        menuRepository.save(createMenu(null, user.getId(), 2, "重置密码", null, null, "user:reset", null, 5, 0, 1));

        // 创建角色管理的按钮权限
        menuRepository.save(createMenu(null, role.getId(), 2, "新增角色", null, null, "role:add", null, 1, 0, 1));
        menuRepository.save(createMenu(null, role.getId(), 2, "编辑角色", null, null, "role:edit", null, 2, 0, 1));
        menuRepository.save(createMenu(null, role.getId(), 2, "删除角色", null, null, "role:delete", null, 3, 0, 1));
        menuRepository.save(createMenu(null, role.getId(), 2, "分配权限", null, null, "role:assign", null, 4, 0, 1));

        // 创建菜单管理的按钮权限
        menuRepository.save(createMenu(null, menu.getId(), 2, "新增菜单", null, null, "menu:add", null, 1, 0, 1));
        menuRepository.save(createMenu(null, menu.getId(), 2, "编辑菜单", null, null, "menu:edit", null, 2, 0, 1));
        menuRepository.save(createMenu(null, menu.getId(), 2, "删除菜单", null, null, "menu:delete", null, 3, 0, 1));

        // 创建字典管理的按钮权限
        menuRepository.save(createMenu(null, dict.getId(), 2, "新增字典", null, null, "dict:add", null, 1, 0, 1));
        menuRepository.save(createMenu(null, dict.getId(), 2, "编辑字典", null, null, "dict:edit", null, 2, 0, 1));
        menuRepository.save(createMenu(null, dict.getId(), 2, "删除字典", null, null, "dict:delete", null, 3, 0, 1));

        // 创建部门管理的按钮权限
        menuRepository.save(createMenu(null, dept.getId(), 2, "新增部门", null, null, "dept:add", null, 1, 0, 1));
        menuRepository.save(createMenu(null, dept.getId(), 2, "编辑部门", null, null, "dept:edit", null, 2, 0, 1));
        menuRepository.save(createMenu(null, dept.getId(), 2, "删除部门", null, null, "dept:delete", null, 3, 0, 1));
        menuRepository.save(createMenu(null, dept.getId(), 2, "查询部门", null, null, "dept:query", null, 4, 0, 1));
        menuRepository.save(createMenu(null, dept.getId(), 2, "部门列表", null, null, "dept:list", null, 5, 0, 1));

        // 创建数据分析下的子菜单
        MenuEntity statistics = createMenu(null, analysis.getId(), 1, "数据统计", "/analysis/statistics", "analysis/Statistics", "analysis:statistics:view", "TrendCharts", 1, 1, 1);
        MenuEntity report = createMenu(null, analysis.getId(), 1, "报表管理", "/analysis/report", "analysis/Report", "analysis:report:view", "DataAnalysis", 2, 1, 1);

        menuRepository.save(statistics);
        menuRepository.save(report);

        long menuCount = menuRepository.count();
        log.info("初始化菜单数据完成，共 {} 个菜单", menuCount);
    }

    /**
     * 初始化用户数据
     */
    private void initializeUsers() {
        if (userRepository.count() > 0) {
            log.info("用户数据已存在，跳过初始化");
            return;
        }

        // 使用系统的PasswordEncoder动态生成密码（123456）
        String encryptedPassword = passwordEncoder.encode("123456");
        
        List<UserEntity> users = Arrays.asList(
                createUser(null, "admin", encryptedPassword, "超级管理员", "admin@adminplus.com", "13800138000", null, 1),
                createUser(null, "manager", encryptedPassword, "部门经理", "manager@adminplus.com", "13800138001", null, 1),
                createUser(null, "user1", encryptedPassword, "普通用户1", "user1@adminplus.com", "13800138002", null, 1),
                createUser(null, "user2", encryptedPassword, "普通用户2", "user2@adminplus.com", "13800138003", null, 1),
                createUser(null, "dev1", encryptedPassword, "开发人员1", "dev1@adminplus.com", "13800138004", null, 1),
                createUser(null, "dev2", encryptedPassword, "开发人员2", "dev2@adminplus.com", "13800138005", null, 1),
                createUser(null, "operator1", encryptedPassword, "运营人员1", "operator1@adminplus.com", "13800138006", null, 1),
                createUser(null, "operator2", encryptedPassword, "运营人员2", "operator2@adminplus.com", "13800138007", null, 1),
                createUser(null, "cs1", encryptedPassword, "客服人员1", "cs1@adminplus.com", "13800138008", null, 1),
                createUser(null, "cs2", encryptedPassword, "客服人员2", "cs2@adminplus.com", "13800138009", null, 1)
        );

        userRepository.saveAll(users);
        log.info("初始化用户数据完成，共 {} 个用户", users.size());
    }

    /**
     * 初始化权限关联
     */
    private void initializePermissions() {
        // 先获取所有角色和菜单
        List<RoleEntity> roles = roleRepository.findAll();
        List<MenuEntity> menus = menuRepository.findAll();
        List<UserEntity> users = userRepository.findAll();

        // 创建角色ID到角色的映射
        java.util.Map<String, RoleEntity> roleMap = new java.util.HashMap<>();
        for (RoleEntity role : roles) {
            roleMap.put(role.getCode(), role);
        }

        // 创建菜单名称到菜单的映射
        java.util.Map<String, MenuEntity> menuMap = new java.util.HashMap<>();
        for (MenuEntity menu : menus) {
            menuMap.put(menu.getName(), menu);
        }

        // 创建用户名到用户的映射
        java.util.Map<String, UserEntity> userMap = new java.util.HashMap<>();
        for (UserEntity user : users) {
            userMap.put(user.getUsername(), user);
        }

        // 超级管理员拥有所有权限
        RoleEntity adminRole = roleMap.get("ROLE_ADMIN");
        if (adminRole != null) {
            for (MenuEntity menu : menus) {
                if (menu.getType() != 2) { // 只关联非按钮菜单
                    roleMenuRepository.save(createRoleMenu(adminRole.getId(), menu.getId()));
                }
            }
        }

        // 部门经理权限
        RoleEntity managerRole = roleMap.get("ROLE_MANAGER");
        if (managerRole != null) {
            List<String> managerMenuNames = Arrays.asList("首页", "用户管理", "新增用户", "编辑用户", "删除用户", "分配角色", "重置密码",
                    "部门管理", "新增部门", "编辑部门", "删除部门", "查询部门", "部门列表");
            for (String menuName : managerMenuNames) {
                MenuEntity menu = menuMap.get(menuName);
                if (menu != null) {
                    roleMenuRepository.save(createRoleMenu(managerRole.getId(), menu.getId()));
                }
            }
        }

        // 开发人员权限
        RoleEntity developerRole = roleMap.get("ROLE_DEVELOPER");
        if (developerRole != null) {
            List<String> developerMenuNames = Arrays.asList("首页", "用户管理", "角色管理", "菜单管理", "字典管理", "参数配置", "数据统计", "报表管理");
            for (String menuName : developerMenuNames) {
                MenuEntity menu = menuMap.get(menuName);
                if (menu != null) {
                    roleMenuRepository.save(createRoleMenu(developerRole.getId(), menu.getId()));
                }
            }
        }

        // 运营人员权限
        RoleEntity operatorRole = roleMap.get("ROLE_OPERATOR");
        if (operatorRole != null) {
            List<String> operatorMenuNames = Arrays.asList("首页", "数据统计", "报表管理");
            for (String menuName : operatorMenuNames) {
                MenuEntity menu = menuMap.get(menuName);
                if (menu != null) {
                    roleMenuRepository.save(createRoleMenu(operatorRole.getId(), menu.getId()));
                }
            }
        }

        // 普通用户权限
        RoleEntity userRole = roleMap.get("ROLE_USER");
        if (userRole != null) {
            MenuEntity homeMenu = menuMap.get("首页");
            if (homeMenu != null) {
                roleMenuRepository.save(createRoleMenu(userRole.getId(), homeMenu.getId()));
            }
        }

        // 用户角色关联
        if (adminRole != null && userMap.get("admin") != null) {
            userRoleRepository.save(createUserRole(userMap.get("admin").getId(), adminRole.getId()));
        }
        if (managerRole != null && userMap.get("manager") != null) {
            userRoleRepository.save(createUserRole(userMap.get("manager").getId(), managerRole.getId()));
        }
        if (userRole != null) {
            UserEntity user1 = userMap.get("user1");
            UserEntity user2 = userMap.get("user2");
            UserEntity cs1 = userMap.get("cs1");
            UserEntity cs2 = userMap.get("cs2");

            if (user1 != null) userRoleRepository.save(createUserRole(user1.getId(), userRole.getId()));
            if (user2 != null) userRoleRepository.save(createUserRole(user2.getId(), userRole.getId()));
            if (cs1 != null) userRoleRepository.save(createUserRole(cs1.getId(), userRole.getId()));
            if (cs2 != null) userRoleRepository.save(createUserRole(cs2.getId(), userRole.getId()));
        }
        if (developerRole != null) {
            UserEntity dev1 = userMap.get("dev1");
            UserEntity dev2 = userMap.get("dev2");

            if (dev1 != null) userRoleRepository.save(createUserRole(dev1.getId(), developerRole.getId()));
            if (dev2 != null) userRoleRepository.save(createUserRole(dev2.getId(), developerRole.getId()));
        }
        if (operatorRole != null) {
            UserEntity op1 = userMap.get("operator1");
            UserEntity op2 = userMap.get("operator2");

            if (op1 != null) userRoleRepository.save(createUserRole(op1.getId(), operatorRole.getId()));
            if (op2 != null) userRoleRepository.save(createUserRole(op2.getId(), operatorRole.getId()));
        }

        log.info("初始化权限关联完成");
    }

    // 创建实体的辅助方法 - 使用构造函数
    private DeptEntity createDept(String id, String parentId, String name, String code, String leader, String phone, Integer status, Integer sortOrder) {
        DeptEntity dept = new DeptEntity();
        // ID由BaseEntity的@PrePersist自动设置
        dept.setParentId(parentId);
        dept.setName(name);
        dept.setCode(code);
        dept.setLeader(leader);
        dept.setPhone(phone);
        dept.setStatus(status);
        dept.setSortOrder(sortOrder);
        dept.setCreateUser("system");
        dept.setUpdateUser("system");
        return dept;
    }

    private RoleEntity createRole(String id, String code, String name, String description, Integer status, Integer sortOrder) {
        RoleEntity role = new RoleEntity();
        // ID由BaseEntity的@PrePersist自动设置
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        role.setStatus(status);
        role.setSortOrder(sortOrder);
        role.setCreateUser("system");
        role.setUpdateUser("system");
        return role;
    }

    private MenuEntity createMenu(String id, String parentId, Integer type, String name, String path, String component, String permKey, String icon, Integer sortOrder, Integer visible, Integer status) {
        MenuEntity menu = new MenuEntity();
        // ID由BaseEntity的@PrePersist自动设置
        menu.setParentId(parentId);
        menu.setType(type);
        menu.setName(name);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setPermKey(permKey);
        menu.setIcon(icon);
        menu.setSortOrder(sortOrder);
        menu.setVisible(visible);
        menu.setStatus(status);
        menu.setCreateUser("system");
        menu.setUpdateUser("system");
        return menu;
    }

    private UserEntity createUser(String id, String username, String password, String nickname, String email, String phone, String deptId, Integer status) {
        UserEntity user = new UserEntity();
        // ID由BaseEntity的@PrePersist自动设置
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        // user.setDeptId(deptId); // 暂时注释，UserEntity 暂无 deptId 字段
        user.setStatus(status);
        user.setCreateUser("system");
        user.setUpdateUser("system");
        return user;
    }

    private RoleMenuEntity createRoleMenu(String roleId, String menuId) {
        RoleMenuEntity roleMenu = new RoleMenuEntity();
        roleMenu.setId(IdUtils.nextIdStr()); // 使用雪花ID
        roleMenu.setRoleId(roleId);
        roleMenu.setMenuId(menuId);
        return roleMenu;
    }

    private UserRoleEntity createUserRole(String userId, String roleId) {
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setId(IdUtils.nextIdStr()); // 使用雪花ID
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return userRole;
    }

    // 已移除generateId方法，改用雪花ID
}