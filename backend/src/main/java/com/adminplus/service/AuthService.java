package com.adminplus.service;

import com.adminplus.pojo.dto.request.UserLoginRequest;
import com.adminplus.pojo.dto.response.LoginResponse;
import com.adminplus.pojo.dto.response.UserResponse;

import java.util.List;

/**
 * 认证服务接口
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求（用户名、密码、验证码）
     * @return 登录响应（包含访问令牌和刷新令牌）
     * @throws BizException 当用户名或密码错误、验证码错误、用户被禁用时抛出
     */
    LoginResponse login(UserLoginRequest request);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     * @throws BizException 当用户不存在时抛出
     */
    UserResponse getUserById(String userId);

    /**
     * 根据用户ID获取权限列表
     *
     * @param userId 用户ID
     * @return 权限标识列表（如 user:add, role:edit）
     */
    List<String> getUserPermissions(String userId);

    /**
     * 用户登出
     * 将当前用户的访问令牌加入黑名单
     */
    void logout();

    /**
     * 刷新 Access Token
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     * @throws BizException 当刷新令牌无效或已过期时抛出
     */
    String refreshAccessToken(String refreshToken);
}