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

import java.util.*;

import java.util.*;
import java.util.stream.Collectors;

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
    private final DictRepository dictRepository;
    private final DictItemRepository dictItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("开始执行数据初始化...");

        try {
            // 初始化部门数据（每个模块单独判断）
            initializeDepartments();

            // 初始化角色数据
            initializeRoles();

            // 初始化菜单数据
            initializeMenus();

            // 初始化用户数据
            initializeUsers();

            // 初始化权限关联
            initializePermissions();

            // 初始化字典数据
            initializeDicts();

            log.info("数据初始化完成！");

        } catch (Exception e) {
            log.error("数据初始化失败", e);
            throw e;
        }
    }

    /**
     * 检查是否已经初始化过（保留方法但总是返回false，由各模块单独判断）
     */
    private boolean isAlreadyInitialized() {
        return false;
    }

    /**
     * 初始化部门数据
     */
    private void initializeDepartments() {
        if (deptRepository.count() > 0) {
            log.info("部门数据已存在，跳过初始化");
            return;
        }

        // 创建部门数据（临时 ID 用于建立关系）
        var deptData = Arrays.asList(
                new Object[]{"1", null, "AdminPlus 总部", "HQ", "张三", "010-12345678", 1, 1},
                new Object[]{"2", "1", "技术研发部", "RD", "李四", "010-12345679", 1, 1},
                new Object[]{"3", "1", "市场运营部", "MK", "王五", "010-12345680", 1, 2},
                new Object[]{"4", "2", "后端开发组", "BE", "赵六", "010-12345681", 1, 1},
                new Object[]{"5", "2", "前端开发组", "FE", "钱七", "010-12345682", 1, 2},
                new Object[]{"6", "3", "市场推广组", "MP", "孙八", "010-12345683", 1, 1},
                new Object[]{"7", "3", "客户服务组", "CS", "周九", "010-12345684", 1, 2}
        );

        // 先创建所有部门
        List<DeptEntity> depts = deptData.stream()
                .map(d -> createDept((String) d[0], (String) d[1], (String) d[2], (String) d[3], (String) d[4], (String) d[5], (Integer) d[6], (Integer) d[7]))
                .toList();

        deptRepository.saveAll(depts);

        // 建立父子关系
        Map<String, DeptEntity> deptMap = depts.stream()
                .collect(Collectors.toMap(
                        dept -> getTempIdFromCode(dept.getCode()),
                        dept -> dept
                ));

        for (var d : deptData) {
            String tempId = (String) d[0];
            String parentId = (String) d[1];
            if (parentId != null && !parentId.isEmpty()) {
                DeptEntity child = deptMap.get(tempId);
                DeptEntity parent = deptMap.get(parentId);
                if (child != null && parent != null) {
                    child.setParent(parent);
                    String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
                    child.setAncestors(parentAncestors + parent.getId() + ",");
                }
            }
        }

        deptRepository.saveAll(depts);
        log.info("初始化部门数据完成，共 {} 个部门", depts.size());
    }

    private String getTempIdFromCode(String code) {
        return switch (code) {
            case "HQ" -> "1";
            case "RD" -> "2";
            case "MK" -> "3";
            case "BE" -> "4";
            case "FE" -> "5";
            case "MP" -> "6";
            case "CS" -> "7";
            default -> code;
        };
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

        // 菜单数据：[临时ID, 父临时ID, 类型, 名称, 路径, 组件, 权限, 图标, 排序, 可见, 状态]
        List<Object[]> menuData = new ArrayList<>();

        // 顶级菜单
        menuData.add(new Object[]{"M1", null, 1, "首页", "/dashboard", "Dashboard", "dashboard:view", "HomeFilled", 0, 1, 1});
        menuData.add(new Object[]{"M2", null, 0, "系统管理", "/system", null, null, "Setting", 1, 1, 1});
        menuData.add(new Object[]{"M3", null, 0, "数据分析", "/analysis", null, null, "DataLine", 2, 1, 1});

        // 系统管理子菜单
        menuData.add(new Object[]{"M21", "M2", 1, "用户管理", "/system/user", "system/User", "system:user:list", "User", 1, 1, 1});
        menuData.add(new Object[]{"M22", "M2", 1, "角色管理", "/system/role", "system/Role", "system:role:list", "UserFilled", 2, 1, 1});
        menuData.add(new Object[]{"M23", "M2", 1, "菜单管理", "/system/menu", "system/Menu", "system:menu:list", "Menu", 3, 1, 1});
        menuData.add(new Object[]{"M24", "M2", 1, "字典管理", "/system/dict", "system/Dict", "system:dict:list", "Document", 4, 1, 1});
        menuData.add(new Object[]{"M25", "M2", 1, "部门管理", "/system/dept", "system/Dept", "system:dept:list", "OfficeBuilding", 5, 1, 1});
        menuData.add(new Object[]{"M26", "M2", 1, "日志管理", "/system/log", "system/Log", "system:log:list", "DocumentCopy", 6, 1, 1});
        menuData.add(new Object[]{"M27", "M2", 1, "参数配置", "/system/config", "system/Config", "system:config:list", "Tools", 7, 1, 1});

        // 用户管理按钮权限
        menuData.add(new Object[]{"M211", "M21", 2, "新增用户", null, null, "user:add", null, 1, 0, 1});
        menuData.add(new Object[]{"M212", "M21", 2, "编辑用户", null, null, "user:edit", null, 2, 0, 1});
        menuData.add(new Object[]{"M213", "M21", 2, "删除用户", null, null, "user:delete", null, 3, 0, 1});
        menuData.add(new Object[]{"M214", "M21", 2, "分配角色", null, null, "user:assign", null, 4, 0, 1});
        menuData.add(new Object[]{"M215", "M21", 2, "重置密码", null, null, "user:reset", null, 5, 0, 1});

        // 角色管理按钮权限
        menuData.add(new Object[]{"M221", "M22", 2, "新增角色", null, null, "role:add", null, 1, 0, 1});
        menuData.add(new Object[]{"M222", "M22", 2, "编辑角色", null, null, "role:edit", null, 2, 0, 1});
        menuData.add(new Object[]{"M223", "M22", 2, "删除角色", null, null, "role:delete", null, 3, 0, 1});
        menuData.add(new Object[]{"M224", "M22", 2, "分配权限", null, null, "role:assign", null, 4, 0, 1});

        // 菜单管理按钮权限
        menuData.add(new Object[]{"M231", "M23", 2, "新增菜单", null, null, "menu:add", null, 1, 0, 1});
        menuData.add(new Object[]{"M232", "M23", 2, "编辑菜单", null, null, "menu:edit", null, 2, 0, 1});
        menuData.add(new Object[]{"M233", "M23", 2, "删除菜单", null, null, "menu:delete", null, 3, 0, 1});

        // 字典管理按钮权限
        menuData.add(new Object[]{"M241", "M24", 2, "新增字典", null, null, "dict:add", null, 1, 0, 1});
        menuData.add(new Object[]{"M242", "M24", 2, "编辑字典", null, null, "dict:edit", null, 2, 0, 1});
        menuData.add(new Object[]{"M243", "M24", 2, "删除字典", null, null, "dict:delete", null, 3, 0, 1});

        // 部门管理按钮权限
        menuData.add(new Object[]{"M251", "M25", 2, "新增部门", null, null, "dept:add", null, 1, 0, 1});
        menuData.add(new Object[]{"M252", "M25", 2, "编辑部门", null, null, "dept:edit", null, 2, 0, 1});
        menuData.add(new Object[]{"M253", "M25", 2, "删除部门", null, null, "dept:delete", null, 3, 0, 1});
        menuData.add(new Object[]{"M254", "M25", 2, "查询部门", null, null, "dept:query", null, 4, 0, 1});
        menuData.add(new Object[]{"M255", "M25", 2, "部门列表", null, null, "dept:list", null, 5, 0, 1});

        // 日志管理按钮权限
        menuData.add(new Object[]{"M261", "M26", 2, "查询日志", null, null, "log:query", null, 1, 0, 1});
        menuData.add(new Object[]{"M262", "M26", 2, "删除日志", null, null, "log:delete", null, 2, 0, 1});
        menuData.add(new Object[]{"M263", "M26", 2, "导出日志", null, null, "log:export", null, 3, 0, 1});

        // 数据分析子菜单
        menuData.add(new Object[]{"M31", "M3", 1, "数据统计", "/analysis/statistics", "analysis/Statistics", "analysis:statistics:view", "TrendCharts", 1, 1, 1});
        menuData.add(new Object[]{"M32", "M3", 1, "报表管理", "/analysis/report", "analysis/Report", "analysis:report:view", "DataAnalysis", 2, 1, 1});

        // 创建所有菜单
        List<MenuEntity> menus = menuData.stream()
                .map(d -> createMenu(
                        (String) d[0], (String) d[1], (Integer) d[2], (String) d[3], (String) d[4],
                        (String) d[5], (String) d[6], (String) d[7], (Integer) d[8], (Integer) d[9], (Integer) d[10]
                ))
                .toList();

        menuRepository.saveAll(menus);

        // 建立父子关系
        Map<String, MenuEntity> menuMap = new HashMap<>();
        for (int i = 0; i < menus.size(); i++) {
            menuMap.put((String) menuData.get(i)[0], menus.get(i));
        }

        for (var d : menuData) {
            String tempId = (String) d[0];
            String parentId = (String) d[1];
            if (parentId != null && !parentId.isEmpty()) {
                MenuEntity child = menuMap.get(tempId);
                MenuEntity parent = menuMap.get(parentId);
                if (child != null && parent != null) {
                    child.setParent(parent);
                    String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
                    child.setAncestors(parentAncestors + parent.getId() + ",");
                }
            }
        }

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

        // 先获取部门数据
        List<DeptEntity> depts = deptRepository.findAll();
        java.util.Map<String, String> deptCodeMap = new java.util.HashMap<>();
        for (DeptEntity dept : depts) {
            deptCodeMap.put(dept.getCode(), dept.getId());
        }

        // 使用系统的PasswordEncoder动态生成密码（admin123）
        String encryptedPassword = passwordEncoder.encode("admin123");

        // 总部
        String hqId = deptCodeMap.getOrDefault("HQ", null);
        // 技术研发部
        String rdId = deptCodeMap.getOrDefault("RD", null);
        // 市场运营部
        String mkId = deptCodeMap.getOrDefault("MK", null);
        // 后端开发组
        String beId = deptCodeMap.getOrDefault("BE", null);
        // 前端开发组
        String feId = deptCodeMap.getOrDefault("FE", null);
        // 市场推广组
        String mpId = deptCodeMap.getOrDefault("MP", null);
        // 客户服务组
        String csId = deptCodeMap.getOrDefault("CS", null);

        List<UserEntity> users = Arrays.asList(
                createUser(null, "admin", encryptedPassword, "超级管理员", "admin@adminplus.com", "13800138000", hqId, 1),
                createUser(null, "manager", encryptedPassword, "部门经理", "manager@adminplus.com", "13800138001", hqId, 1),
                createUser(null, "user1", encryptedPassword, "普通用户1", "user1@adminplus.com", "13800138002", rdId, 1),
                createUser(null, "user2", encryptedPassword, "普通用户2", "user2@adminplus.com", "13800138003", mkId, 1),
                createUser(null, "dev1", encryptedPassword, "开发人员1", "dev1@adminplus.com", "13800138004", beId, 1),
                createUser(null, "dev2", encryptedPassword, "开发人员2", "dev2@adminplus.com", "13800138005", feId, 1),
                createUser(null, "operator1", encryptedPassword, "运营人员1", "operator1@adminplus.com", "13800138006", mpId, 1),
                createUser(null, "operator2", encryptedPassword, "运营人员2", "operator2@adminplus.com", "13800138007", mkId, 1),
                createUser(null, "cs1", encryptedPassword, "客服人员1", "cs1@adminplus.com", "13800138008", csId, 1),
                createUser(null, "cs2", encryptedPassword, "客服人员2", "cs2@adminplus.com", "13800138009", csId, 1)
        );

        userRepository.saveAll(users);
        log.info("初始化用户数据完成，共 {} 个用户", users.size());
    }

    /**
     * 初始化权限关联
     */
    private void initializePermissions() {
        if (roleMenuRepository.count() > 0) {
            log.info("权限关联数据已存在，跳过初始化");
            return;
        }

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
                // 关联所有菜单，包括按钮权限
                roleMenuRepository.save(createRoleMenu(adminRole.getId(), menu.getId()));
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
    private DeptEntity createDept(String tempId, String parentId, String name, String code, String leader, String phone, Integer status, Integer sortOrder) {
        DeptEntity dept = new DeptEntity();
        dept.setName(name);
        dept.setCode(code);
        dept.setLeader(leader);
        dept.setPhone(phone);
        dept.setStatus(status);
        dept.setSortOrder(sortOrder);
        dept.setCreateUser("system");
        dept.setUpdateUser("system");
        // parent 关系在保存后设置
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

    private MenuEntity createMenu(String tempId, String parentId, Integer type, String name, String path, String component, String permKey, String icon, Integer sortOrder, Integer visible, Integer status) {
        MenuEntity menu = new MenuEntity();
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
        // parent 关系在保存后设置
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
        user.setDeptId(deptId);
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

    /**
     * 确保中间表存在，不存在则创建
     */
    /**
     * 初始化字典数据
     */
    private void initializeDicts() {
        if (dictRepository.count() > 0) {
            log.info("字典数据已存在，跳过初始化");
            return;
        }

        // 1. 用户状态字典
        DictEntity userStatusDict = createDict("用户状态", "user_status", "系统用户状态配置");
        userStatusDict = dictRepository.save(userStatusDict);
        List<DictItemEntity> userStatusItems = Arrays.asList(
                createDictItem(userStatusDict.getId(), null, "正常", "1", "启用状态", 1),
                createDictItem(userStatusDict.getId(), null, "禁用", "0", "禁用状态", 2)
        );
        dictItemRepository.saveAll(userStatusItems);
        log.info("初始化用户状态字典完成");

        // 2. 性别字典
        DictEntity genderDict = createDict("性别", "gender", "用户性别配置");
        genderDict = dictRepository.save(genderDict);
        List<DictItemEntity> genderItems = Arrays.asList(
                createDictItem(genderDict.getId(), null, "男", "1", "男性", 1),
                createDictItem(genderDict.getId(), null, "女", "0", "女性", 2),
                createDictItem(genderDict.getId(), null, "未知", "2", "未知性别", 3)
        );
        dictItemRepository.saveAll(genderItems);
        log.info("初始化性别字典完成");

        // 3. 菜单类型字典
        DictEntity menuTypeDict = createDict("菜单类型", "menu_type", "系统菜单类型配置");
        menuTypeDict = dictRepository.save(menuTypeDict);
        List<DictItemEntity> menuTypeItems = Arrays.asList(
                createDictItem(menuTypeDict.getId(), null, "目录", "0", "菜单目录", 1),
                createDictItem(menuTypeDict.getId(), null, "菜单", "1", "菜单项", 2),
                createDictItem(menuTypeDict.getId(), null, "按钮", "2", "按钮权限", 3)
        );
        dictItemRepository.saveAll(menuTypeItems);
        log.info("初始化菜单类型字典完成");

        // 4. 操作状态字典
        DictEntity operationStatusDict = createDict("操作状态", "operation_status", "操作结果状态配置");
        operationStatusDict = dictRepository.save(operationStatusDict);
        List<DictItemEntity> operationStatusItems = Arrays.asList(
                createDictItem(operationStatusDict.getId(), null, "成功", "1", "操作成功", 1),
                createDictItem(operationStatusDict.getId(), null, "失败", "0", "操作失败", 2)
        );
        dictItemRepository.saveAll(operationStatusItems);
        log.info("初始化操作状态字典完成");

        // 5. 是否字典
        DictEntity yesNoDict = createDict("是否", "yes_no", "布尔值配置");
        yesNoDict = dictRepository.save(yesNoDict);
        List<DictItemEntity> yesNoItems = Arrays.asList(
                createDictItem(yesNoDict.getId(), null, "是", "1", "是/启用", 1),
                createDictItem(yesNoDict.getId(), null, "否", "0", "否/禁用", 2)
        );
        dictItemRepository.saveAll(yesNoItems);
        log.info("初始化是否字典完成");

        // 6. 系统环境字典
        DictEntity envDict = createDict("系统环境", "system_env", "系统运行环境配置");
        envDict = dictRepository.save(envDict);
        List<DictItemEntity> envItems = Arrays.asList(
                createDictItem(envDict.getId(), null, "开发环境", "dev", "开发环境", 1),
                createDictItem(envDict.getId(), null, "测试环境", "test", "测试环境", 2),
                createDictItem(envDict.getId(), null, "生产环境", "prod", "生产环境", 3)
        );
        dictItemRepository.saveAll(envItems);
        log.info("初始化系统环境字典完成");

        log.info("初始化字典数据完成，共 {} 个字典", 6);
    }

    /**
     * 创建字典实体
     */
    private DictEntity createDict(String dictName, String dictType, String remark) {
        DictEntity dict = new DictEntity();
        dict.setDictName(dictName);
        dict.setDictType(dictType);
        dict.setStatus(1);
        dict.setRemark(remark);
        dict.setCreateUser("system");
        dict.setUpdateUser("system");
        return dict;
    }

    /**
     * 创建字典项实体
     */
    private DictItemEntity createDictItem(String dictId, String parentId, String label, String value, String remark, Integer sortOrder) {
        DictItemEntity item = new DictItemEntity();
        item.setDictId(dictId);
        item.setLabel(label);
        item.setValue(value);
        item.setStatus(1);
        item.setRemark(remark);
        item.setSortOrder(sortOrder);
        item.setCreateUser("system");
        item.setUpdateUser("system");

        // 处理父子关系
        if (parentId != null && !parentId.isEmpty()) {
            DictItemEntity parent = new DictItemEntity();
            parent.setId(parentId);
            item.setParent(parent);
            // 需要先保存父节点获取完整信息
        }

        return item;
    }
}