package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.MenuEntity;
import com.adminplus.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
        if (menuRepository.count() > 0) {
            log.info("菜单数据已存在，跳过初始化");
            return;
        }

        // 菜单数据：[临时ID, 父临时ID, 类型, 名称, 路径, 组件, 权限, 图标, 排序, 可见, 状态]
        List<Object[]> menuData = buildMenuData();

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

        // 数据分析子菜单
        data.add(new Object[]{"M31", "M3", 1, "数据统计", "/analysis/statistics", "analysis/Statistics", "analysis:statistics:view", "TrendCharts", 1, 1, 1});
        data.add(new Object[]{"M32", "M3", 1, "报表管理", "/analysis/report", "analysis/Report", "analysis:report:view", "DataAnalysis", 2, 1, 1});

        // 工作流管理子菜单
        data.add(new Object[]{"M41", "M4", 1, "流程模板", "/workflow/definitions", "workflow/WorkflowCenter", "workflow:definition:list", "GitBranch", 1, 1, 1});
        data.add(new Object[]{"M42", "M4", 1, "我的流程", "/workflow/my", "workflow/MyWorkflow", "workflow:instance:list", "Workflow", 2, 1, 1});
        data.add(new Object[]{"M43", "M4", 1, "待我审批", "/workflow/pending", "workflow/PendingApproval", "workflow:pending:list", "Clock3", 3, 1, 1});
        data.add(new Object[]{"M44", "M4", 1, "流程设计", "/workflow/designer", "workflow/WorkflowDesigner", "workflow:design", "Settings", 4, 1, 1});

        return data;
    }

    private void addButtonPermissions(List<Object[]> data, String parentId, String resource, List<String> actions) {
        int order = 1;
        for (String action : actions) {
            data.add(new Object[]{parentId + action.charAt(0), parentId, 2,
                    action.equals("add") ? "新增" : action.equals("edit") ? "编辑" : action.equals("delete") ? "删除" :
                    action.equals("assign") ? "分配" : action.equals("reset") ? "重置" : action.equals("query") ? "查询" :
                    action.equals("list") ? "列表" : action.equals("export") ? "导出" : action.equals("upload") ? "上传" : action,
                    null, null, resource + ":" + action, null, order++, 0, 1});
        }
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