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
     */
    LoginResponse login(UserLoginRequest request);

    /**
     * 获取当前用户信息
     */
    UserResponse getCurrentUser(String username);

    /**
     * 获取当前用户的权限列表
     */
    List<String> getCurrentUserPermissions(String username);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 刷新 Access Token
     */
    String refreshAccessToken(String refreshToken);
}