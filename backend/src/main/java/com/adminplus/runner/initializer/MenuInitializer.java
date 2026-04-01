package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.MenuEntity;
import com.adminplus.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单数据初始化器
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuInitializer implements DataInitializer {

    private final MenuRepository menuRepository;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getName() {
        return "菜单数据初始化";
    }

    @Override
    @Transactional
    public void initialize() {
        // 获取现有菜单
        List<MenuEntity> existingMenus = menuRepository.findAll();
        Set<String> existingPaths = existingMenus.stream()
                .map(MenuEntity::getPath)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> existingPermKeys = existingMenus.stream()
                .map(MenuEntity::getPermKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 如果菜单数据为空，执行完整初始化
        if (existingMenus.isEmpty()) {
            log.info("菜单数据为空，执行完整初始化");
            performFullInitialization();
        } else {
            // 增量添加缺失的菜单（包括按钮权限）
            log.info("菜单数据已存在，检查并添加缺失的菜单");
            performIncrementalInitialization(existingPaths, existingPermKeys);
        }
    }

    private void performFullInitialization() {
        List<Object[]> menuData = buildMenuData();
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

    private void performIncrementalInitialization(Set<String> existingPaths, Set<String> existingPermKeys) {
        List<Object[]> menuData = buildMenuData();
        List<MenuEntity> newMenus = new ArrayList<>();

        for (var d : menuData) {
            String path = (String) d[4];
            String permKey = (String) d[6];

            // 检查是否需要添加：path不存在 或 permKey不存在（用于按钮权限）
            boolean shouldAdd = false;
            if (path != null && !existingPaths.contains(path)) {
                shouldAdd = true;
            } else if (permKey != null && !existingPermKeys.contains(permKey)) {
                shouldAdd = true;
            }

            if (shouldAdd) {
                MenuEntity menu = createMenu(
                        (String) d[0], (String) d[1], (Integer) d[2], (String) d[3], (String) d[4],
                        (String) d[5], (String) d[6], (String) d[7], (Integer) d[8], (Integer) d[9], (Integer) d[10]
                );
                newMenus.add(menu);
                if (path != null) existingPaths.add(path);
                if (permKey != null) existingPermKeys.add(permKey);
            }
        }

        if (!newMenus.isEmpty()) {
            menuRepository.saveAll(newMenus);
            log.info("增量添加菜单数据完成，新增 {} 个菜单", newMenus.size());
        } else {
            log.info("没有需要添加的新菜单");
        }
    }

    private List<Object[]> buildMenuData() {
        List<Object[]> data = new ArrayList<>();

        // 顶级菜单
        data.add(new Object[]{"M1", null, 1, "首页", "/dashboard", "Dashboard", "dashboard:view", "HomeFilled", 0, 1, 1});
        data.add(new Object[]{"M2", null, 0, "系统管理", "/system", null, null, "Setting", 1, 1, 1});
        data.add(new Object[]{"M3", null, 0, "数据分析", "/analysis", null, null, "DataLine", 2, 1, 1});
        data.add(new Object[]{"M4", null, 0, "工作流管理", "/workflow", null, null, "Workflow", 3, 1, 1});

        // 系统管理子菜单
        data.add(new Object[]{"M21", "M2", 1, "用户管理", "/system/user", "system/User", "system:user:list", "User", 1, 1, 1});
        data.add(new Object[]{"M22", "M2", 1, "角色管理", "/system/role", "system/Role", "system:role:list", "UserFilled", 2, 1, 1});
        data.add(new Object[]{"M23", "M2", 1, "菜单管理", "/system/menu", "system/Menu", "system:menu:list", "Menu", 3, 1, 1});
        data.add(new Object[]{"M24", "M2", 1, "字典管理", "/system/dict", "system/Dict", "system:dict:list", "Document", 4, 1, 1});
        data.add(new Object[]{"M25", "M2", 1, "部门管理", "/system/dept", "system/Dept", "system:dept:list", "OfficeBuilding", 5, 1, 1});
        data.add(new Object[]{"M26", "M2", 1, "日志管理", "/system/log", "system/Log", "system:log:list", "DocumentCopy", 6, 1, 1});
        data.add(new Object[]{"M27", "M2", 1, "参数配置", "/system/config", "system/Config", "system:config:list", "Settings", 7, 1, 1});
        data.add(new Object[]{"M28", "M2", 1, "文件管理", "/system/file", "system/File", "file:list", "FolderOpen", 8, 1, 1});

        // 用户管理按钮权限
        addButtonPermissions(data, "M21", "user", Arrays.asList("add", "edit", "delete", "assign", "reset"));

        // 角色管理按钮权限
        addButtonPermissions(data, "M22", "role", Arrays.asList("add", "edit", "delete", "assign"));

        // 菜单管理按钮权限
        addButtonPermissions(data, "M23", "menu", Arrays.asList("add", "edit", "delete"));

        // 字典管理按钮权限
        addButtonPermissions(data, "M24", "dict", Arrays.asList("add", "edit", "delete"));

        // 部门管理按钮权限
        addButtonPermissions(data, "M25", "dept", Arrays.asList("add", "edit", "delete", "query", "list"));

        // 日志管理按钮权限
        addButtonPermissions(data, "M26", "log", Arrays.asList("query", "delete", "export"));

        // 文件管理按钮权限
        addButtonPermissions(data, "M28", "file", Arrays.asList("upload", "delete"));

        // 参数配置按钮权限
        addButtonPermissions(data, "M27", "config", Arrays.asList("add", "edit", "delete", "export", "import", "refresh"));

        // 配置组管理按钮权限（config:group:* 系列权限）
        addButtonPermissions(data, "M27", "config:group", Arrays.asList("query", "list", "add", "edit", "delete"));

        // 配置项管理按钮权限（config:item/* 系列权限，与config:*对应）
        addButtonPermissions(data, "M27", "config:item", Arrays.asList("query", "list", "add", "edit", "delete"));

        // 数据分析子菜单
        data.add(new Object[]{"M31", "M3", 1, "数据统计", "/analysis/statistics", "analysis/Statistics", "analysis:statistics:view", "TrendCharts", 1, 1, 1});
        data.add(new Object[]{"M32", "M3", 1, "报表管理", "/analysis/report", "analysis/Report", "analysis:report:view", "DataAnalysis", 2, 1, 1});

        // 工作流管理子菜单
        data.add(new Object[]{"M41", "M4", 1, "流程模板", "/workflow/definitions", "workflow/WorkflowCenter", "workflow:definition:list", "GitBranch", 1, 1, 1});
        data.add(new Object[]{"M42", "M4", 1, "我的流程", "/workflow/my", "workflow/MyWorkflow", "workflow:instance:list", "Workflow", 2, 1, 1});
        data.add(new Object[]{"M43", "M4", 1, "待我审批", "/workflow/pending", "workflow/PendingApproval", "workflow:pending:list", "Clock3", 3, 1, 1});
        data.add(new Object[]{"M44", "M4", 1, "流程设计", "/workflow/designer", "workflow/WorkflowDesigner", "workflow:design", "Settings", 4, 1, 1});
        data.add(new Object[]{"M45", "M4", 1, "抄送我的", "/workflow/cc", "workflow/CopyToMe", "workflow:cc:list", "CopyDocument", 5, 1, 1});
        data.add(new Object[]{"M46", "M4", 1, "催办中心", "/workflow/urge", "workflow/UrgeCenter", "workflow:urge:list", "Bell", 6, 1, 1});

        // 流程模板按钮权限（具体权限 + 通用权限）
        addButtonPermissions(data, "M41", "workflow:definition", Arrays.asList("create", "update", "delete"));
        addButtonPermissions(data, "M41", "workflow", Arrays.asList("create", "update", "delete"));

        // 流程实例按钮权限（我的流程、待我审批）
        addButtonPermissions(data, "M42", "workflow", Arrays.asList("draft", "start", "create", "cancel", "withdraw"));
        addButtonPermissions(data, "M43", "workflow", Arrays.asList("approve", "reject", "rollback", "add-sign"));

        // 流程设计按钮权限
        addButtonPermissions(data, "M44", "workflow:design", Arrays.asList("save", "publish", "delete"));

        // 抄送按钮权限
        addButtonPermissions(data, "M45", "workflow:cc", Arrays.asList("read"));

        // 催办按钮权限（具体权限 + 通用权限）
        addButtonPermissions(data, "M46", "workflow:urge", Arrays.asList("create", "read"));
        addButtonPermissions(data, "M46", "workflow", Arrays.asList("urge"));

        return data;
    }

    private void addButtonPermissions(List<Object[]> data, String parentId, String resource, List<String> actions) {
        int order = 1;
        for (String action : actions) {
            String actionName = getActionDisplayName(action);
            data.add(new Object[]{parentId + action.charAt(0), parentId, 2,
                    actionName,
                    null, null, resource + ":" + action, null, order++, 0, 1});
        }
    }

    private String getActionDisplayName(String action) {
        return switch (action) {
            case "add" -> "新增";
            case "edit" -> "编辑";
            case "delete" -> "删除";
            case "assign" -> "分配";
            case "reset" -> "重置";
            case "query" -> "查询";
            case "list" -> "列表";
            case "export" -> "导出";
            case "import" -> "导入";
            case "upload" -> "上传";
            case "refresh" -> "刷新";
            // 工作流相关操作
            case "draft" -> "保存草稿";
            case "start" -> "发起流程";
            case "create" -> "创建";
            case "update" -> "更新";
            case "cancel" -> "取消流程";
            case "withdraw" -> "撤回流程";
            case "approve" -> "审批通过";
            case "reject" -> "审批驳回";
            case "rollback" -> "回退流程";
            case "add-sign" -> "加签转办";
            case "save" -> "保存";
            case "publish" -> "发布";
            case "read" -> "查看";
            case "urge" -> "催办";
            default -> action;
        };
    }

    private MenuEntity createMenu(String tempId, String parentTempId, Integer type, String name, String path,
                                  String component, String permission, String icon, Integer sort, Integer visible, Integer status) {
        MenuEntity menu = new MenuEntity();
        menu.setType(type);
        menu.setName(name);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setPermKey(permission);
        menu.setIcon(icon);
        menu.setSortOrder(sort);
        menu.setVisible(visible);
        menu.setStatus(status);
        return menu;
    }
}