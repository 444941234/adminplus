package com.adminplus.service;

import com.adminplus.dto.RoleCreateReq;
import com.adminplus.dto.RoleUpdateReq;
import com.adminplus.vo.RoleVO;

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
    List<RoleVO> getRoleList();

    /**
     * 根据ID查询角色
     */
    RoleVO getRoleById(String id);

    /**
     * 创建角色
     */
    RoleVO createRole(RoleCreateReq req);

    /**
     * 更新角色
     */
    RoleVO updateRole(String id, RoleUpdateReq req);

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