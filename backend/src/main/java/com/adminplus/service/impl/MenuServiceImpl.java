package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.enums.OperationType;
import com.adminplus.pojo.dto.req.MenuBatchDeleteReq;
import com.adminplus.pojo.dto.req.MenuBatchStatusReq;
import com.adminplus.pojo.dto.req.MenuCreateReq;
import com.adminplus.pojo.dto.req.MenuUpdateReq;
import com.adminplus.pojo.dto.req.LogEntry;
import com.adminplus.pojo.dto.resp.MenuResp;
import com.adminplus.pojo.entity.MenuEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.MenuRepository;
import com.adminplus.repository.RoleMenuRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.LogService;
import com.adminplus.service.MenuService;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.HierarchyHelper;
import com.adminplus.utils.TreeUtils;
import com.adminplus.utils.XssUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final LogService logService;
    private final UserRoleRepository userRoleRepository;
    private final RoleMenuRepository roleMenuRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "menuTree", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<MenuResp> getMenuTree() {
        List<MenuEntity> allMenus = menuRepository.findAllByOrderBySortOrderAsc();

        // 转换为 VO（扁平结构，children 为 null）
        List<MenuResp> menuResps = allMenus.stream().map(this::toResp).toList();

        // 使用 TreeUtils.buildTreeForRecord 构建树形结构
        return TreeUtils.buildTreeForRecord(menuResps, this::createWithChildren);
    }

    /**
     * 创建包含子节点的新 MenuResp 实例（用于 record 类型）
     */
    private MenuResp createWithChildren(MenuResp original, List<MenuResp> children) {
        return new MenuResp(
                original.id(),
                original.parentId(),
                original.type(),
                original.name(),
                original.path(),
                original.component(),
                original.permKey(),
                original.icon(),
                original.sortOrder(),
                original.visible(),
                original.status(),
                children,
                original.createTime(),
                original.updateTime()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResp getMenuById(String id) {
        var menu = EntityHelper.findByIdOrThrow(menuRepository::findById, id, "菜单不存在");

        return toResp(menu);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userMenus", "menuTree", "allPermissions"}, allEntries = true)
    public MenuResp createMenu(MenuCreateReq req) {
        var menu = new MenuEntity();
        menu.setType(req.type());
        menu.setName(XssUtils.escape(req.name()));
        menu.setPath(XssUtils.escape(req.path()));
        menu.setComponent(XssUtils.escape(req.component()));
        menu.setPermKey(XssUtils.escape(req.permKey()));
        menu.setIcon(XssUtils.escape(req.icon()));
        menu.setSortOrder(req.sortOrder());
        menu.setVisible(req.visible());
        menu.setStatus(req.status());

        // 设置父菜单关系
        if (req.parentId() != null && !req.parentId().equals("0")) {
            MenuEntity parent = EntityHelper.findByIdOrThrow(menuRepository::findById, req.parentId(), "父菜单不存在");
            menu.setParent(parent);
            // 更新 ancestors
            String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
            menu.setAncestors(parentAncestors + parent.getId() + ",");
        } else {
            menu.setAncestors("0,");
        }

        menu = menuRepository.save(menu);

        // 记录审计日志
        logService.log(LogEntry.operation("菜单管理", OperationType.CREATE.getCode(), "创建菜单: " + menu.getName()));

        return toResp(menu);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userMenus", "menuTree", "allPermissions"}, allEntries = true)
    public MenuResp updateMenu(String id, MenuUpdateReq req) {
        var menu = EntityHelper.findByIdOrThrow(menuRepository::findById, id, "菜单不存在");

        req.parentId().ifPresent(parentId -> {
            // 不能将自己设置为父菜单
            if (id.equals(parentId)) {
                throw new BizException("不能将自己设置为父菜单");
            }

            // 检查是否将菜单设置为自己的子菜单（防止循环引用）
            if (!parentId.equals("0") && isChildMenu(id, parentId)) {
                throw new BizException("不能将菜单设置为自己的子菜单");
            }

            // 记录旧 ancestors 用于级联更新
            String oldAncestors = menu.getAncestors() != null ? menu.getAncestors() : "";

            if (!parentId.equals("0")) {
                MenuEntity parent = EntityHelper.findByIdOrThrow(menuRepository::findById, parentId, "父菜单不存在");
                menu.setParent(parent);
                // 更新 ancestors
                String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
                String newAncestors = parentAncestors + parent.getId() + ",";
                menu.setAncestors(newAncestors);
                // 级联更新所有子孙的 ancestors
                cascadeUpdateAncestors(oldAncestors, newAncestors, id);
            } else {
                menu.setParent(null);
                menu.setAncestors("0,");
                cascadeUpdateAncestors(oldAncestors, "0,", id);
            }
        });

        req.type().ifPresent(menu::setType);
        req.name().ifPresent(name -> menu.setName(XssUtils.escape(name)));
        req.path().ifPresent(path -> menu.setPath(XssUtils.escape(path)));
        req.component().ifPresent(component -> menu.setComponent(XssUtils.escape(component)));
        req.permKey().ifPresent(permKey -> menu.setPermKey(XssUtils.escape(permKey)));
        req.icon().ifPresent(icon -> menu.setIcon(XssUtils.escape(icon)));
        req.sortOrder().ifPresent(menu::setSortOrder);
        req.visible().ifPresent(menu::setVisible);
        req.status().ifPresent(menu::setStatus);

        var savedMenu = menuRepository.save(menu);

        // 记录审计日志
        logService.log(LogEntry.operation("菜单管理", OperationType.UPDATE.getCode(), "更新菜单: " + savedMenu.getName()));

        return toResp(savedMenu);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userMenus", "menuTree", "allPermissions"}, allEntries = true)
    public void deleteMenu(String id) {
        var menu = EntityHelper.findByIdOrThrow(menuRepository::findById, id, "菜单不存在");

        // 检查是否有子菜单
        if (!menu.getChildren().isEmpty()) {
            throw new BizException("该菜单下存在子菜单，无法删除");
        }

        menuRepository.delete(menu);

        // 记录审计日志
        logService.log(LogEntry.operation("菜单管理", OperationType.DELETE.getCode(), "删除菜单: " + menu.getName()));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userMenus", "menuTree", "allPermissions"}, allEntries = true)
    public void batchUpdateStatus(MenuBatchStatusReq req) {
        List<MenuEntity> menus = menuRepository.findAllById(req.ids());

        if (menus.size() != req.ids().size()) {
            throw new BizException("部分菜单不存在");
        }

        menus.forEach(menu -> {
            menu.setStatus(req.status());
        });

        menuRepository.saveAll(menus);

        // 记录审计日志
        logService.log(LogEntry.operation("菜单管理", OperationType.UPDATE.getCode(), "批量更新菜单状态，数量: " + req.ids().size()));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userMenus", "menuTree", "allPermissions"}, allEntries = true)
    public void batchDelete(MenuBatchDeleteReq req) {
        List<MenuEntity> menus = menuRepository.findAllById(req.ids());

        if (menus.size() != req.ids().size()) {
            throw new BizException("部分菜单不存在");
        }

        // 检查是否有子菜单
        for (MenuEntity menu : menus) {
            if (!menu.getChildren().isEmpty()) {
                throw new BizException("菜单 [" + menu.getName() + "] 下有子菜单，无法批量删除");
            }
        }

        menuRepository.deleteAll(menus);

        // 记录审计日志
        logService.log(LogEntry.operation("菜单管理", OperationType.DELETE.getCode(), "批量删除菜单，数量: " + req.ids().size()));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userMenus", key = "#userId")
    public List<MenuResp> getUserMenuTree(String userId) {
        // 1. 查询用户的角色ID列表
        List<String> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            return List.of();
        }

        // 2. 批量查询这些角色的菜单ID列表（去重）
        Set<String> menuIds = new HashSet<>(roleMenuRepository.findMenuIdsByRoleIds(roleIds));

        if (menuIds.isEmpty()) {
            return List.of();
        }

        // 3. 查询所有菜单（包括父菜单），因为需要构建树形结构
        List<MenuEntity> allMenus = menuRepository.findAllByOrderBySortOrderAsc();

        // 使用 Map 优化查找性能
        Map<String, MenuEntity> menuMap = allMenus.stream()
                .collect(Collectors.toMap(MenuEntity::getId, Function.identity()));

        // 4. 获取用户可访问的菜单ID集合（包括父菜单）
        Set<String> accessibleMenuIds = new HashSet<>(menuIds);
        // 递归添加所有父菜单
        for (String menuId : menuIds) {
            addParentMenus(menuMap, accessibleMenuIds, menuId);
        }

        // 5. 过滤出用户可访问的菜单
        List<MenuEntity> userMenus = allMenus.stream()
                .filter(menu -> accessibleMenuIds.contains(menu.getId()))
                .filter(menu -> menu.getVisible() == 1 && menu.getStatus() == 1) // 只返回可见且启用状态的菜单
                .toList();

        // 6. 转换为 VO 并构建树形结构
        List<MenuResp> menuResps = userMenus.stream().map(this::toResp).toList();

        // 7. 使用 TreeUtils.buildTreeForRecord 构建树形结构
        return TreeUtils.buildTreeForRecord(menuResps, this::createWithChildren);
    }

    /**
     * 递归添加父菜单到集合中（优化版，使用 Map 查找）
     */
    private void addParentMenus(Map<String, MenuEntity> menuMap, Set<String> menuIds, String menuId) {
        if (menuId == null || menuId.equals("0")) {
            return;
        }

        MenuEntity menu = menuMap.get(menuId);

        if (menu != null && menu.getParent() != null) {
            String parentId = menu.getParent().getId();
            if (parentId != null && !parentId.equals("0") && !menuIds.contains(parentId)) {
                menuIds.add(parentId);
                addParentMenus(menuMap, menuIds, parentId);
            }
        }
    }

    /**
     * 检查目标菜单是否是指定菜单的子孙（防止循环引用）
     */
    private boolean isChildMenu(String parentId, String targetId) {
        return HierarchyHelper.isDescendant(parentId, targetId,
                id -> menuRepository.findById(id).map(MenuEntity::getAncestors));
    }

    /**
     * 级联更新子孙节点的 ancestors 字段
     * <p>
     * 当父菜单变更时，所有子孙的 ancestors 前缀需要从 oldPrefix 替换为 newPrefix
     * </p>
     */
    private void cascadeUpdateAncestors(String oldAncestors, String newAncestors, String menuId) {
        HierarchyHelper.cascadeUpdateAncestors(
                oldAncestors, newAncestors, menuId,
                menuRepository::findByAncestorsStartingWith,
                MenuEntity::getAncestors,
                MenuEntity::setAncestors,
                menuRepository::saveAll
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userMenus", "menuTree", "allPermissions"}, allEntries = true)
    public MenuResp copyMenu(String id, String targetParentId) {
        // 获取原菜单
        MenuEntity sourceMenu = EntityHelper.findByIdOrThrow(menuRepository::findById, id, "菜单不存在");

        // 不能复制到自己的子孙节点（防止循环引用）
        if (!targetParentId.equals("0") && isChildMenu(id, targetParentId)) {
            throw new BizException("不能将菜单复制到自己的子菜单下");
        }

        // 创建副本
        MenuEntity copiedMenu = new MenuEntity();
        copiedMenu.setType(sourceMenu.getType());
        copiedMenu.setName(sourceMenu.getName() + " (副本)");
        copiedMenu.setPath(sourceMenu.getPath());
        copiedMenu.setComponent(sourceMenu.getComponent());
        copiedMenu.setPermKey(sourceMenu.getPermKey());
        copiedMenu.setIcon(sourceMenu.getIcon());
        copiedMenu.setVisible(sourceMenu.getVisible());
        copiedMenu.setStatus(sourceMenu.getStatus());

        // 计算排序值：目标父级下最大值 + 10
        int maxSortOrder = 0;
        if (targetParentId.equals("0")) {
            // 顶级菜单最大排序值
            List<MenuEntity> topMenus = menuRepository.findAllByOrderBySortOrderAsc();
            maxSortOrder = topMenus.stream()
                    .filter(m -> m.getParent() == null)
                    .mapToInt(MenuEntity::getSortOrder)
                    .max()
                    .orElse(0);
        } else {
            // 指定父级下最大排序值
            MenuEntity parent = EntityHelper.findByIdOrThrow(menuRepository::findById, targetParentId, "父菜单不存在");
            maxSortOrder = parent.getChildren().stream()
                    .mapToInt(MenuEntity::getSortOrder)
                    .max()
                    .orElse(0);
        }
        copiedMenu.setSortOrder(maxSortOrder + 10);

        // 设置父菜单关系
        if (!targetParentId.equals("0")) {
            MenuEntity parent = EntityHelper.findByIdOrThrow(menuRepository::findById, targetParentId, "父菜单不存在");
            copiedMenu.setParent(parent);
            String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
            copiedMenu.setAncestors(parentAncestors + parent.getId() + ",");
        } else {
            copiedMenu.setAncestors("0,");
        }

        copiedMenu = menuRepository.save(copiedMenu);

        // 记录审计日志
        logService.log(LogEntry.operation("菜单管理", OperationType.CREATE.getCode(),
                "复制菜单: " + sourceMenu.getName() + " -> " + copiedMenu.getName()));

        return toResp(copiedMenu);
    }

    /**
     * 转换为响应 VO
     */
    private MenuResp toResp(MenuEntity menu) {
        String parentId = menu.getParent() != null ? menu.getParent().getId() : "0";
        return new MenuResp(
                menu.getId(),
                parentId,
                menu.getType(),
                menu.getName(),
                menu.getPath(),
                menu.getComponent(),
                menu.getPermKey(),
                menu.getIcon(),
                menu.getSortOrder(),
                menu.getVisible(),
                menu.getStatus(),
                null, // children 在构建树时填充
                menu.getCreateTime(),
                menu.getUpdateTime()
        );
    }
}
