package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.constants.OperationType;
import com.adminplus.pojo.dto.req.MenuBatchDeleteReq;
import com.adminplus.pojo.dto.req.MenuBatchStatusReq;
import com.adminplus.pojo.dto.req.MenuCreateReq;
import com.adminplus.pojo.dto.req.MenuUpdateReq;
import com.adminplus.pojo.dto.resp.MenuResp;
import com.adminplus.pojo.entity.MenuEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.MenuRepository;
import com.adminplus.repository.RoleMenuRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.LogService;
import com.adminplus.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
        var menu = menuRepository.findById(id)
                .orElseThrow(() -> new BizException("菜单不存在"));

        return toResp(menu);
    }

    @Override
    @Transactional
    public MenuResp createMenu(MenuCreateReq req) {
        var menu = new MenuEntity();
        menu.setType(req.type());
        menu.setName(req.name());
        menu.setPath(req.path());
        menu.setComponent(req.component());
        menu.setPermKey(req.permKey());
        menu.setIcon(req.icon());
        menu.setSortOrder(req.sortOrder());
        menu.setVisible(req.visible());
        menu.setStatus(req.status());

        // 设置父菜单关系
        if (req.parentId() != null && !req.parentId().equals("0")) {
            MenuEntity parent = menuRepository.findById(req.parentId())
                    .orElseThrow(() -> new BizException("父菜单不存在"));
            menu.setParent(parent);
            // 更新 ancestors
            String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
            menu.setAncestors(parentAncestors + parent.getId() + ",");
        } else {
            menu.setAncestors("0,");
        }

        menu = menuRepository.save(menu);

        // 记录审计日志
        logService.log("菜单管理", OperationType.CREATE, "创建菜单: " + menu.getName());

        return toResp(menu);
    }

    @Override
    @Transactional
    public MenuResp updateMenu(String id, MenuUpdateReq req) {
        var menu = menuRepository.findById(id)
                .orElseThrow(() -> new BizException("菜单不存在"));

        req.parentId().ifPresent(parentId -> {
            if (parentId != null && !parentId.equals("0")) {
                MenuEntity parent = menuRepository.findById(parentId)
                        .orElseThrow(() -> new BizException("父菜单不存在"));
                menu.setParent(parent);
                // 更新 ancestors
                String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
                menu.setAncestors(parentAncestors + parent.getId() + ",");
            } else {
                menu.setParent(null);
                menu.setAncestors("0,");
            }
        });

        req.type().ifPresent(menu::setType);
        req.name().ifPresent(menu::setName);
        req.path().ifPresent(menu::setPath);
        req.component().ifPresent(menu::setComponent);
        req.permKey().ifPresent(menu::setPermKey);
        req.icon().ifPresent(menu::setIcon);
        req.sortOrder().ifPresent(menu::setSortOrder);
        req.visible().ifPresent(menu::setVisible);
        req.status().ifPresent(menu::setStatus);

        var savedMenu = menuRepository.save(menu);

        return toResp(savedMenu);
    }

    @Override
    @Transactional
    public void deleteMenu(String id) {
        var menu = menuRepository.findById(id)
                .orElseThrow(() -> new BizException("菜单不存在"));

        // 检查是否有子菜单
        if (!menu.getChildren().isEmpty()) {
            throw new BizException("该菜单下存在子菜单，无法删除");
        }

        menuRepository.delete(menu);

        // 记录审计日志
        logService.log("菜单管理", OperationType.DELETE, "删除菜单: " + menu.getName());
    }

    @Override
    @Transactional
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
        logService.log("菜单管理", OperationType.UPDATE, "批量更新菜单状态，数量: " + req.ids().size());
    }

    @Override
    @Transactional
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
        logService.log("菜单管理", OperationType.DELETE, "批量删除菜单，数量: " + req.ids().size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResp> getUserMenuTree(String userId) {
        // 1. 查询用户的角色ID列表
        List<String> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            return List.of();
        }

        // 2. 查询这些角色的菜单ID列表（去重）
        Set<String> menuIds = roleIds.stream()
                .flatMap(roleId -> roleMenuRepository.findMenuIdByRoleId(roleId).stream())
                .collect(Collectors.toSet());

        if (menuIds.isEmpty()) {
            return List.of();
        }

        // 3. 查询所有菜单（包括父菜单），因为需要构建树形结构
        List<MenuEntity> allMenus = menuRepository.findAllByOrderBySortOrderAsc();

        // 4. 获取用户可访问的菜单ID集合（包括父菜单）
        Set<String> accessibleMenuIds = new HashSet<>(menuIds);
        // 递归添加所有父菜单
        for (String menuId : menuIds) {
            addParentMenus(allMenus, accessibleMenuIds, menuId);
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
     * 递归添加父菜单到集合中
     */
    private void addParentMenus(List<MenuEntity> allMenus, Set<String> menuIds, String menuId) {
        if (menuId == null || menuId.equals("0")) {
            return;
        }

        MenuEntity menu = allMenus.stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElse(null);

        if (menu != null && menu.getParent() != null) {
            String parentId = menu.getParent().getId();
            if (parentId != null && !parentId.equals("0") && !menuIds.contains(parentId)) {
                menuIds.add(parentId);
                addParentMenus(allMenus, menuIds, parentId);
            }
        }
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
