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
     *
     * @return 菜单树形结构列表
     */
    List<MenuResponse> getMenuTree();

    /**
     * 根据ID查询菜单
     *
     * @param id 菜单ID
     * @return 菜单信息
     * @throws BizException 当菜单不存在时抛出
     */
    MenuResponse getMenuById(String id);

    /**
     * 创建菜单
     *
     * @param request 菜单创建请求
     * @return 创建的菜单信息
     * @throws BizException 当父菜单不存在或菜单编码重复时抛出
     */
    MenuResponse createMenu(MenuCreateRequest request);

    /**
     * 更新菜单
     *
     * @param id      菜单ID
     * @param request 菜单更新请求
     * @return 更新后的菜单信息
     * @throws BizException 当菜单不存在时抛出
     */
    MenuResponse updateMenu(String id, MenuUpdateRequest request);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @throws BizException 当菜单不存在或存在子菜单时抛出
     */
    void deleteMenu(String id);

    /**
     * 批量更新菜单状态
     *
     * @param request 批量状态更新请求（包含菜单ID列表和目标状态）
     * @throws BizException 当菜单不存在时抛出
     */
    void batchUpdateStatus(MenuBatchStatusRequest request);

    /**
     * 批量删除菜单
     *
     * @param request 批量删除请求（包含菜单ID列表）
     * @throws BizException 当菜单不存在或存在子菜单时抛出
     */
    void batchDelete(MenuBatchDeleteRequest request);

    /**
     * 获取用户的菜单树（根据用户权限过滤）
     *
     * @param userId 用户ID
     * @return 用户有权访问的菜单树
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