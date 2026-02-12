package com.adminplus.service;

import com.adminplus.pojo.dto.req.RoleCreateReq;
import com.adminplus.pojo.dto.req.RoleUpdateReq;
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
     * 查询角色列表
     */
    List<RoleResp> getRoleList();

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
}