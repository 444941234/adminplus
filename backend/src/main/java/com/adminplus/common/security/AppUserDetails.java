package com.adminplus.common.security;

import com.adminplus.enums.CommonStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static com.adminplus.enums.CommonStatus.ENABLED;

/**
 * 自定义用户详情实现
 * <p>
 * 同时支持 Session 认证和 JWT 认证两种模式：
 * <ul>
 *   <li>Session 模式：从数据库加载完整用户信息</li>
 *   <li>JWT 模式：从 JWT Token 中提取用户信息</li>
 * </ul>
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Getter
public class AppUserDetails implements UserDetails {

    /**
     * 默认用户角色，当没有分配角色时使用
     */
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final String id;
    private final String username;
    private final String password;
    private final String nickname;
    private final String email;
    private final String phone;
    private final String avatar;
    private final String deptId;
    private final Integer status;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * 构造用户详情（完整模式）
     *
     * @param id          用户ID
     * @param username    用户名
     * @param password    密码（加密后）
     * @param nickname    昵称
     * @param email       邮箱
     * @param phone       手机号
     * @param avatar      头像URL
     * @param deptId      部门ID
     * @param status      状态（1=启用，0=禁用）
     * @param roles       角色列表（可选）
     * @param permissions 权限列表（可选）
     */
    public AppUserDetails(String id, String username, String password, String nickname,
                          String email, String phone, String avatar, String deptId, Integer status,
                          List<String> roles, List<String> permissions) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.deptId = deptId;
        this.status = status;
        this.authorities = buildAuthorities(roles, permissions);
    }

    /**
     * 构建权限集合
     * <p>
     * 权限来源：
     * <ol>
     *   <li>角色：自动添加 ROLE_ 前缀（如果不存在）</li>
     *   <li>权限：直接使用</li>
     *   <li>默认：如果都没有，添加 ROLE_USER</li>
     * </ol>
     *
     * @param roles       角色列表（可能为 null）
     * @param permissions 权限列表（可能为 null）
     * @return 权限集合
     */
    private static Collection<GrantedAuthority> buildAuthorities(
            List<String> roles, List<String> permissions) {
        List<GrantedAuthority> authorityList = new ArrayList<>();

        // 处理角色
        if (roles != null && !roles.isEmpty()) {
            roles.stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorityList::add);
        }

        // 处理权限
        if (permissions != null && !permissions.isEmpty()) {
            permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorityList::add);
        }

        // 默认角色
        if (authorityList.isEmpty()) {
            authorityList.add(new SimpleGrantedAuthority(DEFAULT_ROLE));
        }

        return authorityList;
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
        return CommonStatus.isEnabled(status);
    }

    /**
     * 从 JWT Token 创建用户详情
     * <p>
     * 用于 JWT 认证模式，从 Token 中提取用户信息。注意：此方法创建的对象
     * 不包含敏感信息（如密码），且基本信息有限。
     *
     * @param jwt JWT Token
     * @return 用户详情
     * @throws IllegalArgumentException 如果 JWT 缺少必要字段
     */
    public static AppUserDetails fromJwt(Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        String username = jwt.getSubject();

        if (userId == null) {
            throw new IllegalArgumentException("JWT 缺少 userId 字段");
        }
        if (username == null) {
            throw new IllegalArgumentException("JWT 缺少 subject (username) 字段");
        }

        // 从 scope 提取角色
        Collection<GrantedAuthority> authorities = extractAuthoritiesFromScope(jwt);

        // 从JWT中获取部门ID
        String deptId = jwt.getClaimAsString("deptId");

        return new AppUserDetails(
                userId,
                username,
                null,     // password - JWT 认证不需要密码
                null,     // nickname
                null,     // email
                null,     // phone
                null,     // avatar
                deptId,
                ENABLED.getCode(),  // status - 默认启用
                extractRolesFromAuthorities(authorities),
                extractPermissionsFromAuthorities(authorities)
        );
    }

    /**
     * 从 JWT 的 scope claim 提取权限
     *
     * @param jwt JWT Token
     * @return 权限集合
     */
    private static Collection<GrantedAuthority> extractAuthoritiesFromScope(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Object scopeClaim = jwt.getClaim("scope");

        if (scopeClaim instanceof String scope) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + scope));
        } else if (scopeClaim instanceof Collection<?> scopes) {
            scopes.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(scope -> new SimpleGrantedAuthority("ROLE_" + scope))
                    .forEach(authorities::add);
        }

        return authorities;
    }

    /**
     * 从权限集合中提取角色（ROLE_ 开头的）
     *
     * @param authorities 权限集合
     * @return 角色列表
     */
    private static List<String> extractRolesFromAuthorities(Collection<GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // 移除 ROLE_ 前缀
                .collect(Collectors.toList());
    }

    /**
     * 从权限集合中提取权限（非 ROLE_ 开头的）
     *
     * @param authorities 权限集合
     * @return 权限列表
     */
    private static List<String> extractPermissionsFromAuthorities(Collection<GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList());
    }
}
