package com.adminplus.pojo.dto.resp;

import lombok.Data;

import java.util.List;

/**
 * 用户登录响应DTO
 *
 * @author AdminPlus
 * @since 2026-02-10
 */
@Data
public class UserLoginResp {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * JWT令牌
     */
    private String token;

    /**
     * 令牌过期时间（毫秒）
     */
    private Long expiresIn;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 用户权限列表
     */
    private List<String> permissions;
}