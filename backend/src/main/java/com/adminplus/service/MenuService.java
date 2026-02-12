package com.adminplus.service;

import com.adminplus.pojo.dto.req.MenuBatchDeleteReq;
import com.adminplus.pojo.dto.req.MenuBatchStatusReq;
import com.adminplus.pojo.dto.req.MenuCreateReq;
import com.adminplus.pojo.dto.req.MenuUpdateReq;
import com.adminplus.pojo.dto.resp.MenuResp;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public interface MenuService {

    /**
     * 查询菜单树形列表
     */
    List<MenuResp> getMenuTree();

    /**
     * 根据ID查询菜单
     */
    MenuResp getMenuById(String id);

    /**
     * 创建菜单
     */
    MenuResp createMenu(MenuCreateReq req);

    /**
     * 更新菜单
     */
    MenuResp updateMenu(String id, MenuUpdateReq req);

    /**
     * 删除菜单
     */
    void deleteMenu(String id);

    /**
     * 批量更新菜单状态
     */
    void batchUpdateStatus(MenuBatchStatusReq req);

    /**
     * 批量删除菜单
     */
    void batchDelete(MenuBatchDeleteReq req);

    /**
     * 获取用户的菜单树（根据用户权限过滤）
     */
    List<MenuResp> getUserMenuTree(String userId);
}