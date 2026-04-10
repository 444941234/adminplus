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
     *
     * @param id 用户ID
     * @return 用户信息
     */
    UserResponse getUserById(String id);

    /**
     * 根据用户名查询用户实体
     *
     * @param username 用户名
     * @return 用户实体，不存在时返回null
     */
    UserEntity getUserByUsername(String username);

    /**
     * 创建用户
     *
     * @param req 用户创建请求
     * @return 创建的用户信息
     * @throws BizException 当用户名已存在时抛出
     */
    UserResponse createUser(UserCreateRequest req);

    /**
     * 更新用户
     *
     * @param id  用户ID
     * @param req 用户更新请求
     * @return 更新后的用户信息
     * @throws BizException 当用户不存在时抛出
     */
    UserResponse updateUser(String id, UserUpdateRequest req);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @throws BizException 当用户不存在时抛出
     */
    void deleteUser(String id);

    /**
     * 启用/禁用用户
     *
     * @param id     用户ID
     * @param status 状态值（0:禁用, 1:启用）
     * @throws BizException 当用户不存在时抛出
     */
    void updateUserStatus(String id, Integer status);

    /**
     * 重置用户密码
     *
     * @param id          用户ID
     * @param newPassword 新密码（明文，方法内部会加密）
     * @throws BizException 当用户不存在时抛出
     */
    void resetPassword(String id, String newPassword);

    /**
     * 为用户分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @throws BizException 当用户不存在或角色不存在时抛出
     */
    void assignRoles(String userId, List<String> roleIds);

    /**
     * 查询用户的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
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