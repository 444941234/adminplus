package com.adminplus.service;

import com.adminplus.pojo.dto.req.UserLoginReq;
import com.adminplus.pojo.dto.resp.LoginResp;
import com.adminplus.pojo.dto.resp.UserResp;

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
    LoginResp login(UserLoginReq req);

    /**
     * 获取当前用户信息
     */
    UserResp getCurrentUser(String username);

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