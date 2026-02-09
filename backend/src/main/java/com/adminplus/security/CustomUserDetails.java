package com.adminplus.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义用户详情
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final String id;
    private final String username;
    private final String password;
    private final String nickname;
    private final String email;
    private final String phone;
    private final String avatar;
    private final Integer status;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String id, String username, String password, String nickname,
                              String email, String phone, String avatar, Integer status,
                              List<String> roles, List<String> permissions) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.status = status;

        // 构建权限列表：角色（ROLE_前缀）+ 权限标识符
        List<GrantedAuthority> authorityList = new java.util.ArrayList<>();

        // 添加角色权限（ROLE_前缀）
        if (roles != null && !roles.isEmpty()) {
            authorityList.addAll(roles.stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }

        // 添加菜单权限
        if (permissions != null && !permissions.isEmpty()) {
            authorityList.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }

        // 如果没有任何权限，默认给予 ROLE_USER
        if (authorityList.isEmpty()) {
            authorityList.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        this.authorities = authorityList;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 从 JWT 对象创建 CustomUserDetails
     * 用于 JWT 认证场景
     */
    public static CustomUserDetails fromJwt(org.springframework.security.oauth2.jwt.Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        String username = jwt.getSubject();

        // 从 scope claim 中提取权限
        Collection<GrantedAuthority> authorities = new java.util.ArrayList<>();
        Object scopeClaim = jwt.getClaim("scope");
        if (scopeClaim instanceof String) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + scopeClaim));
        } else if (scopeClaim instanceof Collection) {
            ((Collection<?>) scopeClaim).forEach(scope ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + scope))
            );
        }

        return new CustomUserDetails(
            userId,
            username,
            null,  // JWT 不包含密码
            null,  // nickname
            null,  // email
            null,  // phone
            null,  // avatar
            1,     // status - 假设有效
            null,  // roles
            null   // permissions
        );
    }
}