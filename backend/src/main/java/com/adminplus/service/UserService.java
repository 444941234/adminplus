package com.adminplus.service;

import com.adminplus.pojo.dto.req.UserCreateReq;
import com.adminplus.pojo.dto.req.UserUpdateReq;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.pojo.dto.resp.UserResp;
import com.adminplus.pojo.entity.UserEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 用户服务接口
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public interface UserService {

    /**
     * 分页查询用户列表
     */
    PageResultResp<UserResp> getUserList(Integer page, Integer size, String keyword, String deptId);

    /**
     * 异步分页查询用户列表（使用虚拟线程）
     */
    CompletableFuture<PageResultResp<UserResp>> getUserListAsync(Integer page, Integer size, String keyword, String deptId);

    /**
     * 根据ID查询用户
     */
    UserResp getUserById(String id);

    /**
     * 异步根据ID查询用户（使用虚拟线程）
     */
    CompletableFuture<UserResp> getUserByIdAsync(String id);

    /**
     * 根据用户名查询用户
     */
    UserEntity getUserByUsername(String username);

    /**
     * 创建用户
     */
    UserResp createUser(UserCreateReq req);

    /**
     * 更新用户
     */
    UserResp updateUser(String id, UserUpdateReq req);

    /**
     * 删除用户
     */
    void deleteUser(String id);

    /**
     * 启用/禁用用户
     */
    void updateUserStatus(String id, Integer status);

    /**
     * 重置密码
     */
    void resetPassword(String id, String newPassword);

    /**
     * 为用户分配角色
     */
    void assignRoles(String userId, List<String> roleIds);

    /**
     * 查询用户的角色ID列表
     */
    List<String> getUserRoleIds(String userId);
}