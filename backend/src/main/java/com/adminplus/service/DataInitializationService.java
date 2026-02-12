package com.adminplus.service;

import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
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
public class DataInitializationService implements CommandLineRunner {

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
            createDept("1", null, "AdminPlus 总部", "HQ", "张三", "010-12345678", 1, 1),
            createDept("2", "1", "技术研发部", "RD", "李四", "010-12345679", 1, 1),
            createDept("3", "1", "市场运营部", "MK", "王五", "010-12345680", 1, 2),
            createDept("4", "2", "后端开发组", "BE", "赵六", "010-12345681", 1, 1),
            createDept("5", "2", "前端开发组", "FE", "钱七", "010-12345682", 1, 2),
            createDept("6", "3", "市场推广组", "MP", "孙八", "010-12345683", 1, 1),
            createDept("7", "3", "客户服务组", "CS", "周九", "010-12345684", 1, 2)
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
            createRole("1", "ROLE_ADMIN", "超级管理员", "拥有系统所有权限", 1, 1),
            createRole("2", "ROLE_MANAGER", "部门经理", "拥有部门管理权限", 1, 2),
            createRole("3", "ROLE_USER", "普通用户", "拥有基本用户权限", 1, 3),
            createRole("4", "ROLE_DEVELOPER", "开发人员", "拥有开发相关权限", 1, 4),
            createRole("5", "ROLE_OPERATOR", "运营人员", "拥有运营相关权限", 1, 5)
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

        // 创建基础菜单结构
        List<MenuEntity> menus = Arrays.asList(
            // 首页
            createMenu("27", null, 1, "首页", "/dashboard", "Dashboard", "dashboard:view", "HomeFilled", 0, 1, 1),
            
            // 系统管理目录
            createMenu("1", null, 0, "系统管理", "/system", null, null, "Setting", 1, 1, 1),
            
            // 用户管理
            createMenu("2", "1", 1, "用户管理", "/system/user", "system/User", "system:user:list", "User", 1, 1, 1),
            createMenu("3", "2", 2, "新增用户", null, null, "user:add", null, 1, 0, 1),
            createMenu("4", "2", 2, "编辑用户", null, null, "user:edit", null, 2, 0, 1),
            createMenu("5", "2", 2, "删除用户", null, null, "user:delete", null, 3, 0, 1),
            createMenu("6", "2", 2, "分配角色", null, null, "user:assign", null, 4, 0, 1),
            createMenu("7", "2", 2, "重置密码", null, null, "user:reset", null, 5, 0, 1),
            
            // 角色管理
            createMenu("8", "1", 1, "角色管理", "/system/role", "system/Role", "system:role:list", "UserFilled", 2, 1, 1),
            createMenu("9", "8", 2, "新增角色", null, null, "role:add", null, 1, 0, 1),
            createMenu("10", "8", 2, "编辑角色", null, null, "role:edit", null, 2, 0, 1),
            createMenu("11", "8", 2, "删除角色", null, null, "role:delete", null, 3, 0, 1),
            createMenu("12", "8", 2, "分配权限", null, null, "role:assign", null, 4, 0, 1),
            
            // 菜单管理
            createMenu("13", "1", 1, "菜单管理", "/system/menu", "system/Menu", "system:menu:list", "Menu", 3, 1, 1),
            createMenu("14", "13", 2, "新增菜单", null, null, "menu:add", null, 1, 0, 1),
            createMenu("15", "13", 2, "编辑菜单", null, null, "menu:edit", null, 2, 0, 1),
            createMenu("16", "13", 2, "删除菜单", null, null, "menu:delete", null, 3, 0, 1),
            
            // 字典管理
            createMenu("17", "1", 1, "字典管理", "/system/dict", "system/Dict", "system:dict:list", "Document", 4, 1, 1),
            createMenu("18", "17", 2, "新增字典", null, null, "dict:add", null, 1, 0, 1),
            createMenu("19", "17", 2, "编辑字典", null, null, "dict:edit", null, 2, 0, 1),
            createMenu("20", "17", 2, "删除字典", null, null, "dict:delete", null, 3, 0, 1),
            
            // 部门管理
            createMenu("21", "1", 1, "部门管理", "/system/dept", "system/Dept", "system:dept:list", "OfficeBuilding", 5, 1, 1),
            createMenu("22", "21", 2, "新增部门", null, null, "dept:add", null, 1, 0, 1),
            createMenu("23", "21", 2, "编辑部门", null, null, "dept:edit", null, 2, 0, 1),
            createMenu("24", "21", 2, "删除部门", null, null, "dept:delete", null, 3, 0, 1),
            createMenu("25", "21", 2, "查询部门", null, null, "dept:query", null, 4, 0, 1),
            createMenu("26", "21", 2, "部门列表", null, null, "dept:list", null, 5, 0, 1),
            
            // 参数配置
            createMenu("28", "1", 1, "参数配置", "/system/config", "system/Config", "system:config:list", "Tools", 6, 1, 1),
            
            // 数据分析目录
            createMenu("29", null, 0, "数据分析", "/analysis", null, null, "DataLine", 2, 1, 1),
            
            // 数据统计
            createMenu("30", "29", 1, "数据统计", "/analysis/statistics", "analysis/Statistics", "analysis:statistics:view", "TrendCharts", 1, 1, 1),
            
            // 报表管理
            createMenu("31", "29", 1, "报表管理", "/analysis/report", "analysis/Report", "analysis:report:view", "DataAnalysis", 2, 1, 1)
        );

        menuRepository.saveAll(menus);
        log.info("初始化菜单数据完成，共 {} 个菜单", menus.size());
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
            createUser("1", "admin", encryptedPassword, "超级管理员", "admin@adminplus.com", "13800138000", null, 1),
            createUser("2", "manager", encryptedPassword, "部门经理", "manager@adminplus.com", "13800138001", "2", 1),
            createUser("3", "user1", encryptedPassword, "普通用户1", "user1@adminplus.com", "13800138002", "4", 1),
            createUser("4", "user2", encryptedPassword, "普通用户2", "user2@adminplus.com", "13800138003", "5", 1),
            createUser("5", "dev1", encryptedPassword, "开发人员1", "dev1@adminplus.com", "13800138004", "4", 1),
            createUser("6", "dev2", encryptedPassword, "开发人员2", "dev2@adminplus.com", "13800138005", "4", 1),
            createUser("7", "operator1", encryptedPassword, "运营人员1", "operator1@adminplus.com", "13800138006", "6", 1),
            createUser("8", "operator2", encryptedPassword, "运营人员2", "operator2@adminplus.com", "13800138007", "6", 1),
            createUser("9", "cs1", encryptedPassword, "客服人员1", "cs1@adminplus.com", "13800138008", "7", 1),
            createUser("10", "cs2", encryptedPassword, "客服人员2", "cs2@adminplus.com", "13800138009", "7", 1)
        );

        userRepository.saveAll(users);
        log.info("初始化用户数据完成，共 {} 个用户", users.size());
    }

    /**
     * 初始化权限关联
     */
    private void initializePermissions() {
        // 超级管理员拥有所有权限
        List<MenuEntity> allMenus = menuRepository.findAll();
        for (MenuEntity menu : allMenus) {
            if (menu.getType() != 2) { // 只关联非按钮菜单
                roleMenuRepository.save(createRoleMenu("1", menu.getId()));
            }
        }

        // 部门经理权限
        List<String> managerMenuIds = Arrays.asList("27", "2", "3", "4", "5", "6", "7", "21", "22", "23", "24", "25", "26");
        for (String menuId : managerMenuIds) {
            roleMenuRepository.save(createRoleMenu("2", menuId));
        }

        // 开发人员权限
        List<String> developerMenuIds = Arrays.asList("27", "2", "8", "13", "17", "28", "30", "31");
        for (String menuId : developerMenuIds) {
            roleMenuRepository.save(createRoleMenu("4", menuId));
        }

        // 运营人员权限
        List<String> operatorMenuIds = Arrays.asList("27", "30", "31");
        for (String menuId : operatorMenuIds) {
            roleMenuRepository.save(createRoleMenu("5", menuId));
        }

        // 普通用户权限
        roleMenuRepository.save(createRoleMenu("3", "27"));

        // 用户角色关联
        List<UserRoleEntity> userRoles = Arrays.asList(
            createUserRole("1", "1"),  // admin -> 超级管理员
            createUserRole("2", "2"),  // manager -> 部门经理
            createUserRole("3", "3"),  // user1 -> 普通用户
            createUserRole("4", "3"),  // user2 -> 普通用户
            createUserRole("5", "4"),  // dev1 -> 开发人员
            createUserRole("6", "4"),  // dev2 -> 开发人员
            createUserRole("7", "5"),  // operator1 -> 运营人员
            createUserRole("8", "5"),  // operator2 -> 运营人员
            createUserRole("9", "3"),  // cs1 -> 普通用户
            createUserRole("10", "3") // cs2 -> 普通用户
        );

        userRoleRepository.saveAll(userRoles);
        log.info("初始化权限关联完成");
    }

    // 创建实体的辅助方法 - 使用构造函数
    private DeptEntity createDept(String id, String parentId, String name, String code, String leader, String phone, Integer status, Integer sortOrder) {
        DeptEntity dept = new DeptEntity();
        dept.setId(id);
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
        role.setId(id);
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
        menu.setId(id);
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
        user.setId(id);
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
        roleMenu.setId(generateId("RM", roleId, menuId));
        roleMenu.setRoleId(roleId);
        roleMenu.setMenuId(menuId);
        return roleMenu;
    }

    private UserRoleEntity createUserRole(String userId, String roleId) {
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setId(generateId("UR", userId, roleId));
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return userRole;
    }

    // 生成简单的ID
    private String generateId(String prefix, String id1, String id2) {
        return prefix + id1 + "-" + id2;
    }
}