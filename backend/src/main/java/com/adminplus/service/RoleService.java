package com.adminplus.service;

import com.adminplus.pojo.dto.query.RoleQuery;
import com.adminplus.pojo.dto.request.RoleCreateRequest;
import com.adminplus.pojo.dto.request.RoleUpdateRequest;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.dto.response.RoleResponse;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public interface RoleService {

    /**
     * 分页查询角色列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResultResponse<RoleResponse> getRoleList(RoleQuery query);

    /**
     * 查询所有角色列表（用于下拉选择等场景）
     *
     * @return 角色列表
     */
    List<RoleResponse> getAllRoles();

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色信息
     * @throws BizException 当角色不存在时抛出
     */
    RoleResponse getRoleById(String id);

    /**
     * 创建角色
     *
     * @param request 角色创建请求
     * @return 创建的角色信息
     * @throws BizException 当角色编码已存在时抛出
     */
    RoleResponse createRole(RoleCreateRequest request);

    /**
     * 更新角色
     *
     * @param id      角色ID
     * @param request 角色更新请求
     * @return 更新后的角色信息
     * @throws BizException 当角色不存在时抛出
     */
    RoleResponse updateRole(String id, RoleUpdateRequest request);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @throws BizException 当角色不存在或已被分配给用户时抛出
     */
    void deleteRole(String id);

    /**
     * 为角色分配菜单权限
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     * @throws BizException 当角色不存在时抛出
     */
    void assignMenus(String roleId, List<String> menuIds);

    /**
     * 查询角色的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<String> getRoleMenuIds(String roleId);

    /**
     * 更新角色状态
     *
     * @param id     角色ID
     * @param status 状态值（0:禁用, 1:启用）
     * @throws BizException 当角色不存在时抛出
     */
    void updateRoleStatus(String id, Integer status);
}