package com.adminplus.service;

import com.adminplus.pojo.dto.query.UserQuery;
import com.adminplus.pojo.dto.request.UserCreateRequest;
import com.adminplus.pojo.dto.request.UserUpdateRequest;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.dto.response.UserResponse;
import com.adminplus.pojo.entity.UserEntity;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public interface UserService {

    /**
     * 分页查询用户列表
     *
     * @param req 查询条件
     * @return 分页结果
     */
    PageResultResponse<UserResponse> getUserList(UserQuery req);

    /**
     * 根据ID查询用户
     */
    UserResponse getUserById(String id);

    /**
     * 根据用户名查询用户
     */
    UserEntity getUserByUsername(String username);

    /**
     * 创建用户
     */
    UserResponse createUser(UserCreateRequest req);

    /**
     * 更新用户
     */
    UserResponse updateUser(String id, UserUpdateRequest req);

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

    /**
     * 获取用户的角色名称列表
     *
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> getUserRoleNames(String userId);
}