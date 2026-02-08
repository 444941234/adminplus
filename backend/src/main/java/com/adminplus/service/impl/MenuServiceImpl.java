package com.adminplus.service.impl;

import com.adminplus.constants.OperationType;
import com.adminplus.dto.MenuCreateReq;
import com.adminplus.dto.MenuUpdateReq;
import com.adminplus.dto.MenuBatchStatusReq;
import com.adminplus.dto.MenuBatchDeleteReq;
import com.adminplus.entity.MenuEntity;
import com.adminplus.exception.BizException;
import com.adminplus.repository.MenuRepository;
import com.adminplus.service.LogService;
import com.adminplus.service.MenuService;
import com.adminplus.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Override
    @Transactional(readOnly = true)
    public List<MenuVO> getMenuTree() {
        List<MenuEntity> allMenus = menuRepository.findAllByOrderBySortOrderAsc();

        // 转换为 VO
        List<MenuVO> menuVOs = allMenus.stream().map(menu -> new MenuVO(
                menu.getId(),
                menu.getParentId(),
                menu.getType(),
                menu.getName(),
                menu.getPath(),
                menu.getComponent(),
                menu.getPermKey(),
                menu.getIcon(),
                menu.getSortOrder(),
                menu.getVisible(),
                menu.getStatus(),
                null, // children 稍后填充
                menu.getCreateTime(),
                menu.getUpdateTime()
        )).toList();

        // 构建树形结构
        return buildTreeWithChildren(menuVOs, null);
    }

    /**
     * 构建树形结构（带 children）
     */
    private List<MenuVO> buildTreeWithChildren(List<MenuVO> menus, Long parentId) {
        Map<Long, List<MenuVO>> childrenMap = menus.stream()
                .filter(menu -> menu.parentId() != null && menu.parentId() != 0)
                .collect(Collectors.groupingBy(MenuVO::parentId));

        return menus.stream()
                .filter(menu -> {
                    if (parentId == null) {
                        return menu.parentId() == null || menu.parentId() == 0;
                    }
                    return parentId.equals(menu.parentId());
                })
                .map(menu -> {
                    List<MenuVO> children = childrenMap.getOrDefault(menu.id(), new ArrayList<>());
                    // 递归构建子节点
                    List<MenuVO> childTree = buildTreeWithChildren(menus, menu.id());
                    if (!childTree.isEmpty()) {
                        children = childTree;
                    }
                    return new MenuVO(
                            menu.id(),
                            menu.parentId(),
                            menu.type(),
                            menu.name(),
                            menu.path(),
                            menu.component(),
                            menu.permKey(),
                            menu.icon(),
                            menu.sortOrder(),
                            menu.visible(),
                            menu.status(),
                            children,
                            menu.createTime(),
                            menu.updateTime()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MenuVO getMenuById(Long id) {
        var menu = menuRepository.findById(id)
                .orElseThrow(() -> new BizException("菜单不存在"));

        return new MenuVO(
                menu.getId(),
                menu.getParentId(),
                menu.getType(),
                menu.getName(),
                menu.getPath(),
                menu.getComponent(),
                menu.getPermKey(),
                menu.getIcon(),
                menu.getSortOrder(),
                menu.getVisible(),
                menu.getStatus(),
                null,
                menu.getCreateTime(),
                menu.getUpdateTime()
        );
    }

    @Override
    @Transactional
    public MenuVO createMenu(MenuCreateReq req) {
        // 如果有父菜单，检查父菜单是否存在
        if (req.parentId() != null && req.parentId() != 0) {
            if (!menuRepository.existsById(req.parentId())) {
                throw new BizException("父菜单不存在");
            }
        }

        var menu = new MenuEntity();
        menu.setParentId(req.parentId());
        menu.setType(req.type());
        menu.setName(req.name());
        menu.setPath(req.path());
        menu.setComponent(req.component());
        menu.setPermKey(req.permKey());
        menu.setIcon(req.icon());
        menu.setSortOrder(req.sortOrder());
        menu.setVisible(req.visible());
        menu.setStatus(req.status());

        menu = menuRepository.save(menu);

        // 记录审计日志
        logService.log("菜单管理", OperationType.CREATE, "创建菜单: " + menu.getName());

        return new MenuVO(
                menu.getId(),
                menu.getParentId(),
                menu.getType(),
                menu.getName(),
                menu.getPath(),
                menu.getComponent(),
                menu.getPermKey(),
                menu.getIcon(),
                menu.getSortOrder(),
                menu.getVisible(),
                menu.getStatus(),
                null,
                menu.getCreateTime(),
                menu.getUpdateTime()
        );
    }

    @Override
    @Transactional
    public MenuVO updateMenu(Long id, MenuUpdateReq req) {
        var menu = menuRepository.findById(id)
                .orElseThrow(() -> new BizException("菜单不存在"));

        req.parentId().ifPresent(parentId -> {
            if (parentId != null && parentId != 0) {
                if (!menuRepository.existsById(parentId)) {
                    throw new BizException("父菜单不存在");
                }
            }
            menu.setParentId(parentId);
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

        return new MenuVO(
                savedMenu.getId(),
                savedMenu.getParentId(),
                savedMenu.getType(),
                savedMenu.getName(),
                savedMenu.getPath(),
                menu.getComponent(),
                menu.getPermKey(),
                menu.getIcon(),
                menu.getSortOrder(),
                menu.getVisible(),
                menu.getStatus(),
                null,
                menu.getCreateTime(),
                menu.getUpdateTime()
        );
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        var menu = menuRepository.findById(id)
                .orElseThrow(() -> new BizException("菜单不存在"));

        // 检查是否有子菜单
        List<MenuEntity> children = menuRepository.findAllByOrderBySortOrderAsc().stream()
                .filter(m -> id.equals(m.getParentId()))
                .toList();

        if (!children.isEmpty()) {
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
        Set<Long> idSet = req.ids();
        List<MenuEntity> allMenus = menuRepository.findAllByOrderBySortOrderAsc();

        // 找出所有要删除的菜单的子菜单
        List<MenuEntity> children = allMenus.stream()
                .filter(m -> idSet.contains(m.getParentId()))
                .toList();

        if (!children.isEmpty()) {
            throw new BizException("存在菜单下有子菜单，无法批量删除");
        }

        menuRepository.deleteAll(menus);

        // 记录审计日志
        logService.log("菜单管理", OperationType.DELETE, "批量删除菜单，数量: " + req.ids().size());
    }
}