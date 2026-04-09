package com.adminplus.service;

import com.adminplus.pojo.dto.request.MenuBatchDeleteRequest;
import com.adminplus.pojo.dto.request.MenuBatchStatusRequest;
import com.adminplus.pojo.dto.request.MenuCreateRequest;
import com.adminplus.pojo.dto.request.MenuUpdateRequest;
import com.adminplus.pojo.dto.response.MenuResponse;

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
    List<MenuResponse> getMenuTree();

    /**
     * 根据ID查询菜单
     */
    MenuResponse getMenuById(String id);

    /**
     * 创建菜单
     */
    MenuResponse createMenu(MenuCreateRequest request);

    /**
     * 更新菜单
     */
    MenuResponse updateMenu(String id, MenuUpdateRequest request);

    /**
     * 删除菜单
     */
    void deleteMenu(String id);

    /**
     * 批量更新菜单状态
     */
    void batchUpdateStatus(MenuBatchStatusRequest request);

    /**
     * 批量删除菜单
     */
    void batchDelete(MenuBatchDeleteRequest request);

    /**
     * 获取用户的菜单树（根据用户权限过滤）
     */
    List<MenuResponse> getUserMenuTree(String userId);

    /**
     * 复制菜单到指定父级
     *
     * @param id 原菜单ID
     * @param targetParentId 目标父级ID，"0"表示顶级
     * @return 复制后的菜单
     */
    MenuResponse copyMenu(String id, String targetParentId);
}