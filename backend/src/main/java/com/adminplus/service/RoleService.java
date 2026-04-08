package com.adminplus.service;

import com.adminplus.pojo.dto.query.RoleQuery;
import com.adminplus.pojo.dto.req.RoleCreateReq;
import com.adminplus.pojo.dto.req.RoleUpdateReq;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.pojo.dto.resp.RoleResp;

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
     * @param req 查询条件
     * @return 分页结果
     */
    PageResultResp<RoleResp> getRoleList(RoleQuery req);

    /**
     * 查询所有角色列表（用于下拉选择等场景）
     *
     * @return 角色列表
     */
    List<RoleResp> getAllRoles();

    /**
     * 根据ID查询角色
     */
    RoleResp getRoleById(String id);

    /**
     * 创建角色
     */
    RoleResp createRole(RoleCreateReq req);

    /**
     * 更新角色
     */
    RoleResp updateRole(String id, RoleUpdateReq req);

    /**
     * 删除角色
     */
    void deleteRole(String id);

    /**
     * 为角色分配菜单权限
     */
    void assignMenus(String roleId, List<String> menuIds);

    /**
     * 查询角色的菜单ID列表
     */
    List<String> getRoleMenuIds(String roleId);

    /**
     * 更新角色状态
     */
    void updateRoleStatus(String id, Integer status);
}